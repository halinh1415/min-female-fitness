package mint.woman.female.workout.activity.ui.premium;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

/**
 * Play Billing wrapper shared by the paywall and the main screen.
 * <p>
 * Public fields {@link #isPremium}, {@link #isRemoveAd}, {@link #productSkuDetailsMap},
 * {@link #productDetailInApps} and {@link #selected} are kept for backward compatibility.
 */
public class PremiumViewModel extends AndroidViewModel implements PurchasesUpdatedListener,
        BillingClientStateListener {

    private static final String TAG = "PremiumViewModel";

    public static final int STATE_CONNECTING = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_UNAVAILABLE = 2;

    public static final int EVENT_NONE = 0;
    public static final int EVENT_PURCHASED = 1;
    public static final int EVENT_RESTORED = 2;
    public static final int EVENT_RESTORE_NONE = 3;
    public static final int EVENT_PENDING = 4;
    public static final int EVENT_RESTORE_ERROR = 5;
    public static final int EVENT_PURCHASE_CANCELED = 6;
    public static final int EVENT_PURCHASE_ERROR = 7;

    /** Plan indexes used by {@link #selected} (kept from the old paywall). */
    public static final int PLAN_LIFETIME = 0;
    public static final int PLAN_YEARLY = 1;
    public static final int PLAN_MONTHLY = 2;

    private final BillingClient billingClient;
    private final MyPreference myDB;

    public final MutableLiveData<Map<String, ProductDetails>> productSkuDetailsMap = new MutableLiveData<>();
    public final MutableLiveData<Map<String, ProductDetails>> productDetailInApps = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isPremium = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isRemoveAd = new MutableLiveData<>();
    public final MutableLiveData<Integer> selected = new MutableLiveData<>();
    public final MutableLiveData<Integer> billingState = new MutableLiveData<>(STATE_CONNECTING);
    public final MutableLiveData<Integer> events = new MutableLiveData<>(EVENT_NONE);

    // Result of the two purchase queries (SUBS + INAPP) of one refresh round.
    private final Object queryLock = new Object();
    private int pendingQueries = 0;
    private boolean queryFailed = false;
    private boolean foundPremium = false;
    private boolean foundRemoveAd = false;
    private boolean restoreRequested = false;

    public PremiumViewModel(@NonNull Application application) {
        super(application);
        myDB = new MyPreference(application);
        selected.setValue(PLAN_YEARLY);
        isPremium.setValue(myDB.isPremiumMember());
        billingClient = BillingClient.newBuilder(application)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .enableAutoServiceReconnection()
                .build();
        billingClient.startConnection(this);
    }

    // ------------------------------------------------------------------ connection

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected");
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        Log.d(TAG, "onBillingSetupFinished: " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        if (billingResult.getResponseCode() != OK) {
            // Keep the cached premium state - never revoke access because the store is unreachable.
            billingState.postValue(STATE_UNAVAILABLE);
            if (restoreRequested) {
                restoreRequested = false;
                events.postValue(EVENT_RESTORE_ERROR);
            }
            return;
        }
        billingState.postValue(STATE_READY);
        queryPurchases();
        querySkuDetails();
    }

    public boolean isReady() {
        return billingClient.isReady();
    }

    // ------------------------------------------------------------------ purchases

    /** Re-reads owned purchases. Premium is only updated when both queries succeed. */
    public void queryPurchases() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready");
            return;
        }
        synchronized (queryLock) {
            pendingQueries = 2;
            queryFailed = false;
            foundPremium = false;
            foundRemoveAd = false;
        }
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS).build(), this::onPurchasesQueried);
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP).build(), this::onPurchasesQueried);
    }

    /** Gọi ở onResume: thanh toán đang chờ được kích hoạt ngay khi Google Play xác nhận. */
    public void syncPremiumStatus() {
        if (billingClient.isReady()) {
            queryPurchases();
        }
        // Chưa sẵn sàng thì enableAutoServiceReconnection() tự kết nối lại và
        // onBillingSetupFinished sẽ truy vấn.
    }

    /** "Restore purchases" button. */
    public void restorePurchases() {
        restoreRequested = true;
        if (billingClient.isReady()) {
            queryPurchases();
        } else if (Integer.valueOf(STATE_UNAVAILABLE).equals(billingState.getValue())) {
            billingState.setValue(STATE_CONNECTING);
            billingClient.startConnection(this);
        }
    }

    private void onPurchasesQueried(@NonNull BillingResult result, @NonNull List<Purchase> purchases) {
        boolean premium = false;
        boolean removeAd = false;
        if (result.getResponseCode() == OK) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                if (!purchase.isAcknowledged()) {
                    acknowledge(purchase);
                }
                if (grantsPremium(purchase)) premium = true;
                if (grantsRemoveAd(purchase)) removeAd = true;
            }
        }
        boolean failed;
        synchronized (queryLock) {
            if (result.getResponseCode() != OK) queryFailed = true;
            foundPremium |= premium;
            foundRemoveAd |= removeAd;
            pendingQueries--;
            if (pendingQueries > 0) return;
            premium = foundPremium;
            removeAd = foundRemoveAd;
            failed = queryFailed;
        }
        if (failed) {
            // Một trong hai truy vấn lỗi: danh sách chưa đầy đủ nên chỉ được CẤP quyền, không thu
            // hồi. Trước đây vẫn post removeAd = false khi truy vấn INAPP lỗi mà SUBS có premium.
            Log.w(TAG, "purchase query failed - keeping cached state, grant only");
            if (premium) isPremium.postValue(true);
            if (removeAd) isRemoveAd.postValue(true);
            if (restoreRequested) {
                restoreRequested = false;
                events.postValue(premium ? EVENT_RESTORED : EVENT_RESTORE_ERROR);
            }
            return;
        }
        isPremium.postValue(premium);
        isRemoveAd.postValue(removeAd);
        if (restoreRequested) {
            restoreRequested = false;
            events.postValue(premium ? EVENT_RESTORED : EVENT_RESTORE_NONE);
        }
    }

    private static boolean grantsPremium(Purchase purchase) {
        if (purchase.isSuspended()) return false;
        for (String product : purchase.getProducts()) {
            if (product.contains("premium")) return true;
        }
        return false;
    }

    private static boolean grantsRemoveAd(Purchase purchase) {
        return purchase.getProducts().contains(InAppKey.REMOVE_ADS);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        switch (responseCode) {
            case OK:
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                }
                break;
            case USER_CANCELED:
                Log.d(TAG, "purchase canceled");
                events.postValue(EVENT_PURCHASE_CANCELED);
                break;
            case ITEM_ALREADY_OWNED:
                Toast.makeText(getApplication(), new PremiumException("Update", responseCode).getMessage(), Toast.LENGTH_SHORT).show();
                queryPurchases();
                break;
            default:
                Toast.makeText(getApplication(), new PremiumException("Update", responseCode).getMessage(), Toast.LENGTH_SHORT).show();
                events.postValue(EVENT_PURCHASE_ERROR);
        }
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.APP_OPEN_RESUME)) {
            AppOpenManager.getInstance().enableAppResume();
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            events.postValue(EVENT_PENDING);
            return;
        }
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;
        if (grantsPremium(purchase)) {
            isPremium.postValue(true);
            myDB.setPremiumMember(true);
            events.postValue(EVENT_PURCHASED);
        }
        if (grantsRemoveAd(purchase)) {
            isRemoveAd.postValue(true);
        }
        if (!purchase.isAcknowledged()) {
            acknowledge(purchase);
        }
    }

    private void acknowledge(Purchase purchase) {
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(params, result -> {
            if (result.getResponseCode() != OK) {
                Log.e(TAG, "acknowledgePurchase failed " + result.getResponseCode());
            }
        });
    }

    // ------------------------------------------------------------------ products

    private void querySkuDetails() {
        List<QueryProductDetailsParams.Product> subs = new ArrayList<>();
        subs.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS).build());
        subs.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS).build());
        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(subs).build(),
                (billingResult, result) -> {
                    Map<String, ProductDetails> map = new HashMap<>();
                    if (billingResult.getResponseCode() == OK) {
                        for (ProductDetails details : result.getProductDetailsList()) {
                            map.put(details.getProductId(), details);
                        }
                    } else {
                        Log.e(TAG, "query subs failed " + billingResult.getResponseCode());
                    }
                    productSkuDetailsMap.postValue(map);
                });

        List<QueryProductDetailsParams.Product> inApps = new ArrayList<>();
        inApps.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP).build());
        inApps.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP).build());
        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(inApps).build(),
                (billingResult, result) -> {
                    Map<String, ProductDetails> map = new HashMap<>();
                    if (billingResult.getResponseCode() == OK) {
                        for (ProductDetails details : result.getProductDetailsList()) {
                            map.put(details.getProductId(), details);
                        }
                    }
                    productDetailInApps.postValue(map);
                });
    }

    @Nullable
    public ProductDetails getSubscription(String productId) {
        Map<String, ProductDetails> map = productSkuDetailsMap.getValue();
        return map == null ? null : map.get(productId);
    }

    @Nullable
    public ProductDetails getInApp(String productId) {
        Map<String, ProductDetails> map = productDetailInApps.getValue();
        return map == null ? null : map.get(productId);
    }

    /**
     * The offer that will be purchased for a subscription: the free-trial offer when trials are
     * enabled (Remote Config {@code enable_trial}) and the user is eligible, otherwise the base plan.
     */
    @Nullable
    public ProductDetails.SubscriptionOfferDetails getDefaultOffer(String productId) {
        ProductDetails details = getSubscription(productId);
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_TRIAL)) {
            ProductDetails.SubscriptionOfferDetails trial = PaywallOffers.trialOffer(details);
            if (trial != null) return trial;
        }
        return PaywallOffers.baseOffer(details);
    }

    // ------------------------------------------------------------------ billing flows

    /** Launches a subscription purchase with a specific offer (null = default offer). */
    public int launchSubscription(Activity activity, String productId,
                                  @Nullable ProductDetails.SubscriptionOfferDetails offer) {
        ProductDetails details = getSubscription(productId);
        if (!billingClient.isReady() || details == null) return BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE;
        if (offer == null) offer = getDefaultOffer(productId);
        if (offer == null) return BillingClient.BillingResponseCode.ITEM_UNAVAILABLE;
        AppOpenManager.getInstance().disableAppResume();
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .setOfferToken(offer.getOfferToken())
                                .build()))
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, params).getResponseCode();
        if (responseCode != OK
                && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.APP_OPEN_RESUME)) {
            AppOpenManager.getInstance().enableAppResume();
        }
        return responseCode;
    }

    public int launchBillingFlow(Activity activity, String sku) {
        return launchSubscription(activity, sku, null);
    }

    public int launchBillingFlowYearly(Activity activity, String sku) {
        return launchSubscription(activity, sku, null);
    }

    public int launchBillingFlowInApp(Activity activity, String sku) {
        ProductDetails details = getInApp(sku);
        if (!billingClient.isReady() || details == null) return BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE;
        AppOpenManager.getInstance().disableAppResume();
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build()))
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, params).getResponseCode();
        if (responseCode != OK
                && FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.APP_OPEN_RESUME)) {
            AppOpenManager.getInstance().enableAppResume();
        }
        return responseCode;
    }

    // ------------------------------------------------------------------ misc

    public void setSelected(int position) {
        selected.setValue(position);
    }

    public LiveData<Integer> getSelected() {
        return selected;
    }

    public void consumeEvent() {
        events.setValue(EVENT_NONE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        billingClient.endConnection();
    }
}
