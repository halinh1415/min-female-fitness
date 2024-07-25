package mint.woman.female.workout.activity.premium;

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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
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
import mint.woman.female.workout.utils.RemoteConfigKeys;

public class PremiumViewModel extends AndroidViewModel implements PurchasesUpdatedListener,
        BillingClientStateListener, PurchasesResponseListener {
    private static final String TAG = "PremiumViewModel";
    private static final List<String> LIST_OF_SKUS = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add(InAppKey.REMOVE_ADS);
                add(InAppKey.PREMIUM_LIFETIME);
                add(InAppKey.PREMIUM_MONTHLY);
                add(InAppKey.PREMIUM_YEARLY);
            }});
    private BillingClient billingClient;
    public MutableLiveData<Map<String, ProductDetails>> productSkuDetailsMap = new MutableLiveData<>();

    public MutableLiveData<Map<String, ProductDetails>> productDetailInApps= new MutableLiveData<>();
    public MutableLiveData<List<Purchase>> purchases = new MutableLiveData<>();
    public MutableLiveData<Boolean> isPremium = new MutableLiveData<>();
    public MutableLiveData<Boolean> isRemoveAd = new MutableLiveData<>();
    public MutableLiveData<Integer> selected = new MutableLiveData<>();


    public PremiumViewModel(Application application) {
        super(application);
        billingClient = BillingClient.newBuilder(application)
                .setListener(this)
                .enablePendingPurchases() // Not used for subscriptions.
                .build();
        if (!billingClient.isReady()) {

            billingClient.startConnection(this);
        }
        selected.setValue(1);
        isPremium.setValue(false);
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected");
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult == null) {
            Log.d(TAG, "billingResult is null");
            return;
        }
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "onBillingSetupFinished: " + responseCode + " " + debugMessage);

        queryPurchases();
        querySkuDetails();
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                         @NonNull List<Purchase> list) {
        if (billingResult.getResponseCode() == OK) {
            processPurchases(list);
        }
    }

    private void processPurchases(List<Purchase> purchasesList) {
        if (purchasesList != null) {
            for (Purchase purchase : purchasesList) {
                if (purchase.getSkus().toString().contains("premium")) {
                    if (purchase.isAcknowledged()) {
                        isPremium.postValue(true);
                        Log.d(TAG, "premium is purchased");
                    } else {
                        handlePurchase(purchase);
                    }
                } else if (purchase.getSkus().toString().contains(InAppKey.REMOVE_ADS)) {
                    if (purchase.isAcknowledged()) {
                        Log.d(TAG, "remove ad is purchased");
                        isRemoveAd.postValue(true);
                    } else {
                        handlePurchase(purchase);
                    }
                }
            }
            logAcknowledgementStatus(purchasesList);
        }

    }

    private void logAcknowledgementStatus(List<Purchase> purchasesList) {
        int ack_yes = 0;
        int ack_no = 0;
        for (Purchase purchase : purchasesList) {
            if (purchase.isAcknowledged()) {
                ack_yes++;
            } else {
                ack_no++;
            }
        }
        Log.d(TAG, "logAcknowledgementStatus: acknowledged=" + ack_yes +
                " unacknowledged=" + ack_no);
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
                Log.e("HAHA", "Canceled");
                break;

            case ITEM_ALREADY_OWNED:
                Toast.makeText(getApplication(), new PremiumException("Update", responseCode).getMessage(), Toast.LENGTH_SHORT).show();
                processPurchases(purchases);
                break;

            default:
                Toast.makeText(getApplication(), new PremiumException("Update", responseCode).getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.APP_OPEN_RESUME)) {
            AppOpenManager.getInstance().enableAppResume();
        }
    }


    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            if (purchase.getSkus().toString().contains("premium")) {
                isPremium.setValue(true);
            }
            if (purchase.getSkus().toString().contains(InAppKey.REMOVE_ADS)) {
                isRemoveAd.setValue(true);
            }
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {

                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == OK) {
                        if (purchase.getSkus().toString().contains("premium")) {
                            Log.d(TAG, "premium is acknowledgePurchase");
                            isPremium.postValue(true);
                        }
                        if (purchase.getSkus().toString().contains(InAppKey.REMOVE_ADS)) {
                            Log.d(TAG, "remove ad is acknowledgePurchase");

                            isRemoveAd.postValue(true);
                        }
                    } else {
                        Log.e("HAHA", "acknowledgeNonConsumablePurchasesAsync " + billingResult.getResponseCode());
                    }
                });
            }
        } else {
        }
    }

    private void querySkuDetails() {
        Log.d(TAG, "BillingClient: querySkuDetails...");

        List<QueryProductDetailsParams.Product> listQuery = new ArrayList<>();
        listQuery.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_MONTHLY).
                setProductType(BillingClient.ProductType.SUBS).build());
        listQuery.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_YEARLY).
                setProductType(BillingClient.ProductType.SUBS).build());
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(listQuery)
                        .build();

        Map<String, ProductDetails> newProductDetails = new HashMap<>();
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                (billingResult, productDetailsList) -> {
                    if (billingResult.getResponseCode() == OK){
                        for (ProductDetails productDetails: productDetailsList){
                            Log.d(TAG,"product title:"+productDetails.getTitle());
                            newProductDetails.put(productDetails.getProductId(),productDetails);
                        }
                        productSkuDetailsMap.postValue(newProductDetails);
                    }
                }
        );
        List<QueryProductDetailsParams.Product> listQueryInApp = new ArrayList<>();
        listQueryInApp.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.PREMIUM_LIFETIME).
                setProductType(BillingClient.ProductType.INAPP).build());
        listQueryInApp.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(InAppKey.REMOVE_ADS).
                setProductType(BillingClient.ProductType.INAPP).build());
        QueryProductDetailsParams queryProductDetailsParamsLifeTime =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(listQueryInApp)
                        .build();
        Map<String, ProductDetails> newProductDetailsInApp = new HashMap<>();
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParamsLifeTime,
                (billingResult, productDetailsList) -> {
                    if (billingResult.getResponseCode() == OK){
                        for (ProductDetails productDetails: productDetailsList){
                            Log.d(TAG,"product title:"+productDetails.getTitle());
                            Log.d(TAG,"product inApp price:"+productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                            newProductDetailsInApp.put(productDetails.getProductId(),productDetails);
                        }
                        productDetailInApps.postValue(newProductDetailsInApp);
                    }
                }
        );



    }

    public void queryPurchases() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready");
        }
        isPremium.postValue(false);
        isRemoveAd.postValue(false);
        Log.d(TAG, "queryPurchases: SUBS");

        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(), this);
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(), this);
    }

    public boolean isReady() {
        return billingClient != null && billingClient.isReady();
    }

    public int launchBillingFlow(Activity activity, String sku) {
        if (!isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
        }
        if (productSkuDetailsMap.getValue() == null) return 1;
        AppOpenManager.getInstance().disableAppResume();
        ProductDetails productDetails = productSkuDetailsMap.getValue().get(sku);
        if (productDetails != null) {
            List<ProductDetails.SubscriptionOfferDetails> list = productDetails.getSubscriptionOfferDetails();
            String selectedOfferToken  = list.get(0).getOfferToken();
            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList1 = new ArrayList<>();
            productDetailsParamsList1.add(   BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(productDetails)
                    // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(selectedOfferToken)
                    .build());

            BillingFlowParams params = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList1)
                    .build();
            BillingResult billingResult = billingClient.launchBillingFlow(activity, params);
            int responseCode = billingResult.getResponseCode();
            return responseCode;
        }

        return 0;
    }
    public int launchBillingFlowYearly(Activity activity, String sku) {
        if (!isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
        }
        if (productSkuDetailsMap.getValue() == null) return 1;
        AppOpenManager.getInstance().disableAppResume();
        ProductDetails productDetails = productSkuDetailsMap.getValue().get(sku);
        if (productDetails != null) {
            List<ProductDetails.SubscriptionOfferDetails> list = productDetails.getSubscriptionOfferDetails();
            String selectedOfferToken;
            if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.ENABLE_TRIAL)) {
                selectedOfferToken = list.get(0).getOfferToken();
            }else{
                if (list.size() >1){
                    selectedOfferToken = list.get(1).getOfferToken();
                }else {
                    selectedOfferToken = list.get(0).getOfferToken();
                }
            }
            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList1 = new ArrayList<>();
            productDetailsParamsList1.add(   BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(productDetails)
                    // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(selectedOfferToken)
                    .build());

            BillingFlowParams params = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList1)
                    .build();
            BillingResult billingResult = billingClient.launchBillingFlow(activity, params);
            int responseCode = billingResult.getResponseCode();
            return responseCode;
        }

        return 0;
    }
    public int launchBillingFlowInApp(Activity activity,String sku) {
        if (!isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
        }

        AppOpenManager.getInstance().disableAppResume();
        if (productDetailInApps.getValue() == null) return 1;
        ProductDetails productDetails =null;
        productDetails = productDetailInApps.getValue().get(sku);
        if (productDetails != null) {
            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList1 = new ArrayList<>();
            productDetailsParamsList1.add( BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(productDetails)
                    // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .build());

            BillingFlowParams params = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList1)
                    .build();
            BillingResult billingResult = billingClient.launchBillingFlow(activity, params);
            int responseCode = billingResult.getResponseCode();
            return responseCode;
        }

        return 0;
    }

    public void setSelected(int position){
        selected.setValue(position);
    }
    public LiveData<Integer> getSelected(){
        return selected;
    }

}
