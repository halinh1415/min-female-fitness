package mint.woman.female.workout.activity.ui.premium

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.PolicyActivity
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.admodule.MaticAd
import mint.woman.female.workout.admodule.MaticAdCallBack
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.RemoteConfigKeys
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Paywall trong app (Compose), giao diện giống paywall B của WomenWorkout: ảnh hero, thẻ ưu đãi trong
 * dải vàng, gói năm (kèm dùng thử nếu Play có), gói tháng, gói trọn đời. Billing giữ nguyên trong
 * [PremiumViewModel]; màn này chỉ hiển thị giá từ Google Play và mở luồng mua.
 *
 * Ưu đãi cho người mở nhiều lần mà chưa mua theo bậc thang [PaywallPromo]: đợt giảm 50% (có đồng hồ,
 * kèm popup vé ưu đãi lúc vừa mở đợt) hoặc gói 34% / trọn đời đã bốc cho người dùng.
 */
class PremiumActivity : AppCompatActivity(), EdgeToEdgeHelper.HandlesInsets {

    private lateinit var vm: PremiumViewModel
    private lateinit var myPreference: MyPreference
    private var isFirstSetup = false
    private var source = "unknown"
    private var leaving = false
    private var purchaseStarted = false

    private var prices by mutableStateOf(PaywallPrices())
    private var selected by mutableIntStateOf(PaywallPlan.YEARLY)
    private var closeVisible by mutableStateOf(true)
    private var pricesTimedOut by mutableStateOf(false)
    private var featureLifetime by mutableStateOf(false)
    private val handler = Handler(Looper.getMainLooper())
    private val priceTimeout = Runnable { if (!priceReady(selected)) pricesTimedOut = true }

    private var userPickedPlan = false
    /** Số lần mở tính cả lần này; 0 = không đếm (sau onboarding, đã mua). */
    private var openCount = 0
    /** Ưu đãi xoay vòng của người dùng (PaywallPlan.LIFETIME / SPECIAL); null = chưa tới ngưỡng. */
    private var rotatePick: Int? = null
    /** Chờ có giá từ Play để quyết định mở đợt 50% (chỉ khi Play có base plan 50%). */
    private var countPromoOpen = false
    private var offerShown = OFFER_DEFAULT
    private var offerLogged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        myPreference = MyPreference(this)
        isFirstSetup = intent.getBooleanExtra(EXTRA_IS_FIRST, false)
        intent.getStringExtra(EXTRA_SOURCE)?.let { source = it }
        vm = ViewModelProvider(this)[PremiumViewModel::class.java]

        if (savedInstanceState == null) {
            if (!isFirstSetup && !myPreference.isPremiumMember) {
                openCount = PaywallPromo.recordOpen(this)
                countPromoOpen = true
                if (PaywallPromo.shouldRotate(openCount)) rotatePick = PaywallPromo.stickyPick(this)
            }
            delayCloseButtonIfFirstView()
            FirebaseAnalytics.getInstance(this).logEvent("paywall_view", Bundle().apply { putString("source", source) })
        } else {
            openCount = savedInstanceState.getInt(STATE_OPEN_COUNT, 0)
            rotatePick = savedInstanceState.getInt(STATE_ROTATE_PICK, -1).takeIf { it >= 0 }
            userPickedPlan = savedInstanceState.getBoolean(STATE_USER_PICKED, false)
        }

        vm.selected.observe(this) { selected = it ?: PaywallPlan.YEARLY }
        vm.productSkuDetailsMap.observe(this) { updatePrices() }
        vm.productDetailInApps.observe(this) { updatePrices() }
        vm.isPremium.observe(this) { premium -> if (premium == true) myPreference.setPremiumMember(true) }
        vm.events.observe(this) { event ->
            if (event == null || event == PremiumViewModel.EVENT_NONE) return@observe
            vm.consumeEvent()
            when (event) {
                PremiumViewModel.EVENT_PURCHASED -> onPurchased()
                PremiumViewModel.EVENT_RESTORED -> {
                    Toast.makeText(this, R.string.pw_restore_success, Toast.LENGTH_SHORT).show()
                    onPurchased()
                }
                PremiumViewModel.EVENT_RESTORE_NONE ->
                    Toast.makeText(this, R.string.pw_restore_none, Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            // Đang ẩn nút đóng thì back cũng không thoát.
            override fun handleOnBackPressed() {
                if (closeVisible) close()
            }
        })

        handler.postDelayed(priceTimeout, PRICE_TIMEOUT_MILLIS)
        setContent {
            MinTheme {
                PaywallScreenB(
                    prices = prices,
                    selected = selected,
                    closeVisible = closeVisible,
                    onSelect = ::selectPlan,
                    onPurchase = ::purchase,
                    onRestore = ::restore,
                    onClose = ::close,
                    onPromoExpired = ::onPromoExpired,
                    footer = PaywallFooter(
                        ctaEnabled = priceReady(selected),
                        showPriceError = pricesTimedOut && !priceReady(selected),
                        onRetry = ::retryPrices,
                        termsUrl = "",
                        privacyUrl = PRIVACY_IN_APP,
                        onOpenUrl = ::openUrl,
                    ),
                    featureLifetime = featureLifetime,
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_OPEN_COUNT, openCount)
        outState.putInt(STATE_ROTATE_PICK, rotatePick ?: -1)
        outState.putBoolean(STATE_USER_PICKED, userPickedPlan)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun priceReady(plan: Int): Boolean = when (plan) {
        PaywallPlan.LIFETIME -> prices.lifetime != null
        PaywallPlan.MONTHLY -> prices.monthly != null
        PaywallPlan.SPECIAL -> prices.special != null
        else -> prices.yearly != null
    }

    private fun retryPrices() {
        pricesTimedOut = false
        vm.reloadProducts()
        handler.removeCallbacks(priceTimeout)
        handler.postDelayed(priceTimeout, PRICE_TIMEOUT_MILLIS)
    }

    private fun selectPlan(plan: Int) {
        userPickedPlan = true
        vm.setSelected(plan)
    }

    /** Hết giờ khuyến mãi: bỏ giá 50%, quay về ưu đãi đã bốc (nếu có). */
    private fun onPromoExpired() {
        PaywallPromo.clear(this)
        updatePrices()
    }

    private fun updatePrices() {
        val yearly = vm.getSubscription(InAppKey.PREMIUM_YEARLY)
        val monthly = vm.getSubscription(InAppKey.PREMIUM_MONTHLY)
        val lifetime = vm.getInApp(InAppKey.PREMIUM_LIFETIME)?.oneTimePurchaseOfferDetails
        val year = PaywallOffers.recurringPhase(PaywallOffers.yearlyBaseOffer(yearly))
        val month = PaywallOffers.recurringPhase(PaywallOffers.baseOffer(monthly))
        val trialDays = PaywallOffers.trialDays(vm.getDefaultOffer(InAppKey.PREMIUM_YEARLY))

        val savePercent = if (year != null && month != null && month.priceAmountMicros > 0 &&
            month.priceCurrencyCode == year.priceCurrencyCode
        ) {
            val twelve = 12 * month.priceAmountMicros
            ((twelve - year.priceAmountMicros) * 100 / twelve).toInt().takeIf { it > 0 }
        } else {
            null
        }

        // Đợt giảm 50% luôn được ưu tiên; không có đợt nào thì chào gói đã bốc cho người mở nhiều lần
        // (bốc trúng 34% mà Play không có base plan 34% thì chào Trọn đời).
        val offer50 = PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_SPECIAL_50)
        val offer34 = PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_SPECIAL_34)
        if (countPromoOpen && yearly != null) {
            countPromoOpen = false
            if (offer50 != null && PaywallPromo.maybeStart(this, openCount)) showPromoPopup()
        }
        val promoEnd = if (offer50 != null) PaywallPromo.activeEndTime(this) else null
        featureLifetime = rotatePick != null && promoEnd == null && lifetime != null &&
            (rotatePick == PaywallPlan.LIFETIME || offer34 == null)
        val specialOffer = when {
            promoEnd != null -> offer50
            rotatePick == PaywallPlan.SPECIAL && !featureLifetime -> offer34
            else -> null
        }

        prices = PaywallPrices(
            yearly = year?.formattedPrice,
            monthly = month?.formattedPrice,
            lifetime = lifetime?.formattedPrice,
            savePercent = savePercent,
            trialAvailable = trialDays > 0,
            trialDays = if (trialDays > 0) trialDays else 7,
            yearlyWeekly = year?.let { formatMoney(it.priceAmountMicros / 1_000_000.0 / WEEKS_PER_YEAR, it.priceCurrencyCode) },
            monthlyWeekly = month?.let { formatMoney(it.priceAmountMicros / 1_000_000.0 * 12 / WEEKS_PER_YEAR, it.priceCurrencyCode) },
            special = special(specialOffer, year, if (specialOffer == offer50) promoEnd else null),
        )
        if (year != null || month != null || lifetime != null) {
            handler.removeCallbacks(priceTimeout)
            pricesTimedOut = false
        }

        offerShown = when {
            prices.special?.endsAt != null -> OFFER_PROMO_50
            featureLifetime -> OFFER_ROTATE_LIFETIME
            prices.special != null -> OFFER_ROTATE_SPECIAL_34
            else -> OFFER_DEFAULT
        }
        // Gói ưu đãi biến mất (hết giờ / Play gỡ base plan) mà đang chọn thì về gói năm; người dùng
        // chưa tự chọn thì chọn sẵn gói đang chào.
        when {
            selected == PaywallPlan.SPECIAL && prices.special == null -> vm.setSelected(PaywallPlan.YEARLY)
            !userPickedPlan && prices.special != null -> vm.setSelected(PaywallPlan.SPECIAL)
            !userPickedPlan && featureLifetime -> vm.setSelected(PaywallPlan.LIFETIME)
        }
        if (!offerLogged && yearly != null) {
            offerLogged = true
            FirebaseAnalytics.getInstance(this).logEvent(
                "paywall_offer",
                Bundle().apply {
                    putString("offer", offerShown)
                    putInt("open_count", openCount)
                    putString("source", source)
                },
            )
        }
    }

    private fun special(
        offer: ProductDetails.SubscriptionOfferDetails?,
        year: ProductDetails.PricingPhase?,
        endsAt: Long?,
    ): SpecialOffer? {
        val phase = PaywallOffers.recurringPhase(offer) ?: return null
        if (offer == null || year == null || phase.priceCurrencyCode != year.priceCurrencyCode ||
            phase.priceAmountMicros >= year.priceAmountMicros
        ) return null
        val percent = Math.round((year.priceAmountMicros - phase.priceAmountMicros) * 100.0 / year.priceAmountMicros).toInt()
        return SpecialOffer(
            price = phase.formattedPrice,
            original = year.formattedPrice,
            weekly = formatMoney(phase.priceAmountMicros / 1_000_000.0 / WEEKS_PER_YEAR, phase.priceCurrencyCode),
            percent = percent,
            basePlanId = offer.basePlanId,
            endsAt = endsAt,
        )
    }

    /** Vừa mở đợt giảm 50%: hiện popup vé ưu đãi đè lên paywall. */
    private fun showPromoPopup() {
        if (isFinishing || supportFragmentManager.isStateSaved || supportFragmentManager.findFragmentByTag(TAG_DISCOUNT) != null) return
        val end = PaywallPromo.activeEndTime(this) ?: return
        FirebaseAnalytics.getInstance(this).logEvent("paywall_promo50_popup", null)
        DiscountDialogFragment.newInstance(end - System.currentTimeMillis()).show(supportFragmentManager, TAG_DISCOUNT)
    }

    private fun purchase() {
        val plan = selected
        FirebaseAnalytics.getInstance(this).logEvent(
            "paywall_purchase_click",
            Bundle().apply {
                putString("source", source)
                putInt("plan", plan)
                putString("offer", offerShown)
            },
        )
        purchaseStarted = true
        val code = when (plan) {
            PaywallPlan.LIFETIME -> vm.launchBillingFlowInApp(this, InAppKey.PREMIUM_LIFETIME)
            PaywallPlan.MONTHLY -> vm.launchSubscription(this, InAppKey.PREMIUM_MONTHLY, null)
            PaywallPlan.SPECIAL -> vm.launchSubscription(
                this,
                InAppKey.PREMIUM_YEARLY,
                prices.special?.let { PaywallOffers.offerByBasePlanId(vm.getSubscription(InAppKey.PREMIUM_YEARLY), it.basePlanId) },
            )
            else -> vm.launchSubscription(this, InAppKey.PREMIUM_YEARLY, null)
        }
        if (code != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(this, R.string.pw_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun restore() {
        FirebaseAnalytics.getInstance(this).logEvent("paywall_restore", null)
        vm.restorePurchases()
    }

    private fun onPurchased() {
        if (leaving) return
        myPreference.setPremiumMember(true)
        if (purchaseStarted) {
            FirebaseAnalytics.getInstance(this).logEvent(
                "paywall_purchase_success",
                Bundle().apply {
                    putString("source", source)
                    putString("offer", offerShown)
                },
            )
            Toast.makeText(this, R.string.pw_purchase_success, Toast.LENGTH_LONG).show()
        }
        leaving = true
        finish()
    }

    /** Mọi cách đóng màn (X, back) đi qua đây; mở lần đầu sau hướng dẫn thì kèm quảng cáo inter. */
    private fun close() {
        if (leaving) return
        leaving = true
        FirebaseAnalytics.getInstance(this).logEvent("paywall_close", null)
        if (isFirstSetup && !myPreference.isPremiumMember &&
            FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigKeys.INTER_FIRST_OPEN) &&
            MaticAd.getInstance().isAvailableInter
        ) {
            MaticAd.getInstance().showPriorityInterstitialAd(this, object : MaticAdCallBack() {
                override fun onNextAction() {
                    super.onNextAction()
                    finish()
                }
            })
        } else {
            finish()
        }
    }

    /** Trả PREMIUM_MEMBER cho màn gọi (PreviewActivity mở khoá bài tập ngay sau khi mua). */
    override fun finish() {
        setResult(RESULT_OK, Intent().putExtra("PREMIUM_MEMBER", ::myPreference.isInitialized && myPreference.isPremiumMember))
        super.finish()
    }

    /** Lần đầu xem paywall (hoặc ngay sau hướng dẫn): ẩn nút đóng vài giây để người dùng xem ưu đãi. */
    private fun delayCloseButtonIfFirstView() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!isFirstSetup && prefs.getBoolean(PREF_SEEN, false)) return
        prefs.edit().putBoolean(PREF_SEEN, true).apply()
        val delaySec = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigKeys.PAYWALL_CLOSE_DELAY_SEC)
        if (delaySec <= 0) return
        closeVisible = false
        handler.postDelayed({ closeVisible = true }, delaySec * 1000L)
    }

    private fun openUrl(url: String) {
        if (url == PRIVACY_IN_APP) {
            startActivity(Intent(this, PolicyActivity::class.java))
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ignored: Exception) {
        }
    }

    /** Định dạng theo tiền tệ của Play (VND không có phần lẻ, USD có 2 số lẻ...). */
    private fun formatMoney(amount: Double, currencyCode: String): String = try {
        val format = NumberFormat.getCurrencyInstance(resources.configuration.locales[0])
        val currency = Currency.getInstance(currencyCode)
        format.currency = currency
        val digits = currency.defaultFractionDigits.coerceAtLeast(0)
        format.minimumFractionDigits = digits
        format.maximumFractionDigits = digits
        format.format(amount)
    } catch (e: IllegalArgumentException) {
        String.format(Locale.US, "%.2f %s", amount, currencyCode)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language),
        )
    }

    companion object {
        const val EXTRA_IS_FIRST = "IS_FIRST"
        const val EXTRA_SOURCE = "SOURCE"
        private const val PREF_SEEN = "PAYWALL_B_SEEN"
        private const val TAG_DISCOUNT = "discount"
        private const val PRICE_TIMEOUT_MILLIS = 8000L
        private const val WEEKS_PER_YEAR = 52.0
        /** Link "Chính sách" mở PolicyActivity trong app thay cho URL. */
        private const val PRIVACY_IN_APP = "app://privacy"
        private const val STATE_OPEN_COUNT = "open_count"
        private const val STATE_ROTATE_PICK = "rotate_pick"
        private const val STATE_USER_PICKED = "user_picked"
        private const val OFFER_DEFAULT = "default"
        private const val OFFER_PROMO_50 = "promo50"
        private const val OFFER_ROTATE_LIFETIME = "rotate_lifetime"
        private const val OFFER_ROTATE_SPECIAL_34 = "rotate_special34"
    }
}
