package mint.woman.female.workout.activity.ui.premium;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.bumptech.glide.Glide;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.PolicyActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.databinding.ActivityPaywallBinding;
import mint.woman.female.workout.databinding.ItemPaywallPlanBinding;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

/**
 * Paywall shown after onboarding.
 *
 * The exact SubscriptionOfferDetails rendered on screen is retained and passed to BillingClient.
 * This prevents an ineligible trial or a different base plan from being advertised to the user.
 */
public class PaywallActivity extends AppCompatActivity implements EdgeToEdgeHelper.HandlesInsets {

    /** LuckyDraw (popup đè lên paywall) đóng thì paywall đóng theo, trả kết quả cho màn gọi. */
    private final androidx.activity.result.ActivityResultLauncher<Intent> luckyDrawLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> finishWithResult());

    public static final String EXTRA_IS_FIRST = "IS_FIRST";
    public static final String EXTRA_SOURCE = "SOURCE";

    private static final int PLAN_SPECIAL = 0;
    private static final int PLAN_YEARLY = 1;
    private static final int PLAN_MONTHLY = 2;

    private static final double WEEKS_PER_YEAR = 52d;
    private static final double WEEKS_PER_MONTH = WEEKS_PER_YEAR / 12d;
    private static final String STATE_SELECTED_PLAN = "selected_plan";
    private static final long MAX_CLOSE_DELAY_SECONDS = 10L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ActivityPaywallBinding binding;
    private PremiumViewModel premiumViewModel;
    private MyPreference myPreference;
    private FirebaseRemoteConfig remoteConfig;

    private int selectedPlan = PLAN_YEARLY;
    private boolean isFirstSetup;
    private boolean restoreInProgress;
    private boolean purchaseStarted;
    private boolean closing;
    private String source = "unknown";

    private boolean specialAvailable;
    private boolean yearlyAvailable;
    private boolean monthlyAvailable;

    private ProductDetails.SubscriptionOfferDetails specialOffer;
    private ProductDetails.SubscriptionOfferDetails yearlyOffer;
    private ProductDetails.SubscriptionOfferDetails monthlyOffer;
    private ProductDetails.PricingPhase specialRecurringPhase;
    private ProductDetails.PricingPhase yearlyRecurringPhase;
    private ProductDetails.PricingPhase monthlyRecurringPhase;

    private String specialPriceText = "";
    private String yearlyPriceText = "";
    private String monthlyPriceText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPaywallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Banner giống PaywallScreenB (màn Premium): ảnh ngang trong assets, iv_hero centerCrop chỉ cắt 2 bên.
        Glide.with(this)
                .load(Uri.parse("file:///android_asset/images/img_premium.webp"))
                .into(binding.ivHero);

        remoteConfig = FirebaseRemoteConfig.getInstance();
        myPreference = new MyPreference(this);
        premiumViewModel = new ViewModelProvider(this).get(PremiumViewModel.class);

        isFirstSetup = getIntent().getBooleanExtra(EXTRA_IS_FIRST, false);
        String requestedSource = getIntent().getStringExtra(EXTRA_SOURCE);
        source = requestedSource == null || requestedSource.isEmpty()
                ? (isFirstSetup ? "onboarding" : "unknown")
                : requestedSource;

        if (savedInstanceState != null) {
            selectedPlan = savedInstanceState.getInt(STATE_SELECTED_PLAN, PLAN_YEARLY);
        } else {
            selectRemoteDefaultPlan();
            logEvent("paywall_view", null);
            logEvent("premium_page_view", null);
        }

        applyInsets();
        initStaticContent();
        initListeners();
        observeBilling();
        render();
        scheduleCloseEmphasis();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_PLAN, selectedPlan);
    }

    private void selectRemoteDefaultPlan() {
        String configured = remoteConfig.getString(RemoteConfigKeys.PAYWALL_DEFAULT_PLAN);
        selectedPlan = "monthly".equals(configured)
                && remoteConfig.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_MONTHLY)
                ? PLAN_MONTHLY
                : PLAN_YEARLY;
    }

    private void initStaticContent() {
        binding.planSpecial.tvPlanBand.setText(R.string.paywall_band_special);
        binding.planSpecial.tvPlanBand.setVisibility(View.VISIBLE);
        binding.planTrial.tvPlanTitle.setText(R.string.pw_plan_yearly);
        binding.planMonthly.tvPlanTitle.setText(R.string.pw_plan_monthly);
        setPlanVisible(binding.planSpecial, false);
        setPlanVisible(binding.planTrial, false);
        setPlanVisible(binding.planMonthly, false);
        binding.btnContinue.setEnabled(false);
        binding.tvStoreState.setText(R.string.pw_loading);
        binding.tvStoreState.setVisibility(View.VISIBLE);
    }

    private void initListeners() {
        binding.ivClose.setOnClickListener(v -> finishPaywall());
        binding.tvRestore.setOnClickListener(v -> restorePurchases());
        binding.tvManage.setOnClickListener(v -> openUrl(
                "https://play.google.com/store/account/subscriptions?package=" + getPackageName()));
        binding.tvPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PolicyActivity.class)));
        binding.planSpecial.getRoot().setOnClickListener(v -> selectPlan(PLAN_SPECIAL));
        binding.planTrial.getRoot().setOnClickListener(v -> selectPlan(PLAN_YEARLY));
        binding.planMonthly.getRoot().setOnClickListener(v -> selectPlan(PLAN_MONTHLY));
        binding.btnContinue.setOnClickListener(v -> purchaseSelectedPlan());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishPaywall();
            }
        });
    }

    /** Keep controls outside status/navigation bars while allowing the hero to draw edge-to-edge. */
    private void applyInsets() {
        ViewGroup.MarginLayoutParams closeParams =
                (ViewGroup.MarginLayoutParams) binding.ivClose.getLayoutParams();
        int closeMarginTop = closeParams.topMargin;
        int bottomPadding = binding.bottomBarPaywall.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) binding.ivClose.getLayoutParams();
            params.topMargin = closeMarginTop + bars.top;
            binding.ivClose.setLayoutParams(params);
            binding.bottomBarPaywall.setPadding(
                    binding.bottomBarPaywall.getPaddingLeft(),
                    binding.bottomBarPaywall.getPaddingTop(),
                    binding.bottomBarPaywall.getPaddingRight(),
                    bottomPadding + bars.bottom);
            view.setPadding(bars.left, 0, bars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * The close control is always visible and clickable. Remote Config only delays its full visual
     * emphasis, so users are never trapped on the purchase screen.
     */
    private void scheduleCloseEmphasis() {
        long seconds = isFirstSetup
                ? remoteConfig.getLong(RemoteConfigKeys.PAYWALL_CLOSE_DELAY_SEC)
                : 0L;
        seconds = Math.max(0L, Math.min(seconds, MAX_CLOSE_DELAY_SECONDS));
        binding.ivClose.setVisibility(View.VISIBLE);
        binding.ivClose.setClickable(true);
        binding.ivClose.setAlpha(seconds > 0L ? 0.7f : 1f);
        if (seconds > 0L) {
            handler.postDelayed(() -> {
                if (binding != null && !isFinishing() && !isDestroyed()) {
                    binding.ivClose.animate().alpha(1f).setDuration(250L).start();
                }
            }, seconds * 1000L);
        }
    }

    private void observeBilling() {
        premiumViewModel.isPremium.observe(this, isPremium -> {
            if (isPremium == null) return;
            myPreference.setPremiumMember(isPremium);
            if (isPremium && !closing) {
                if (restoreInProgress) {
                    finishRestore("restored");
                    Toast.makeText(this, R.string.pw_restore_success, Toast.LENGTH_SHORT).show();
                }
                completePurchaseIfStarted();
                closing = true;
                finishWithResult();
            }
        });

        premiumViewModel.productSkuDetailsMap.observe(this, this::bindPlans);
        premiumViewModel.billingState.observe(this, state -> renderStoreState());
        premiumViewModel.events.observe(this, this::handleBillingEvent);
    }

    private void handleBillingEvent(@Nullable Integer event) {
        if (event == null || event == PremiumViewModel.EVENT_NONE) return;
        premiumViewModel.consumeEvent();

        switch (event) {
            case PremiumViewModel.EVENT_PURCHASED:
                completePurchaseIfStarted();
                break;
            case PremiumViewModel.EVENT_RESTORED:
                finishRestore("restored");
                Toast.makeText(this, R.string.pw_restore_success, Toast.LENGTH_SHORT).show();
                break;
            case PremiumViewModel.EVENT_RESTORE_NONE:
                finishRestore("none");
                Toast.makeText(this, R.string.pw_restore_none, Toast.LENGTH_SHORT).show();
                break;
            case PremiumViewModel.EVENT_RESTORE_ERROR:
                finishRestore("error");
                Toast.makeText(this, R.string.pw_restore_error, Toast.LENGTH_SHORT).show();
                break;
            case PremiumViewModel.EVENT_PENDING:
                purchaseStarted = false;
                logEvent("paywall_purchase_pending", purchaseParams());
                Toast.makeText(this, R.string.pw_purchase_pending, Toast.LENGTH_LONG).show();
                break;
            case PremiumViewModel.EVENT_PURCHASE_CANCELED:
                purchaseStarted = false;
                logEvent("paywall_purchase_cancel", purchaseParams());
                break;
            case PremiumViewModel.EVENT_PURCHASE_ERROR:
                purchaseStarted = false;
                logEvent("paywall_purchase_error", purchaseParams());
                break;
            default:
                break;
        }
    }

    private void finishRestore(String result) {
        if (!restoreInProgress) return;
        restoreInProgress = false;
        binding.tvRestore.setEnabled(true);
        binding.tvRestore.setAlpha(1f);
        Bundle params = new Bundle();
        params.putString("result", result);
        logEvent("paywall_restore_result", params);
    }

    private void completePurchaseIfStarted() {
        if (!purchaseStarted) return;
        purchaseStarted = false;
        logEvent("paywall_purchase_success", purchaseParams());
        Toast.makeText(this, R.string.pw_purchase_success, Toast.LENGTH_SHORT).show();
    }

    private void bindPlans(@Nullable Map<String, ProductDetails> productDetailsMap) {
        if (productDetailsMap == null) return;
        bindYearlyPlans(productDetailsMap.get(InAppKey.PREMIUM_YEARLY));
        bindMonthlyPlan(productDetailsMap.get(InAppKey.PREMIUM_MONTHLY));
        ensureSelectablePlan();
        render();
    }

    private void bindYearlyPlans(@Nullable ProductDetails yearly) {
        ProductDetails.SubscriptionOfferDetails baseOffer =
                PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_BASE);
        ProductDetails.SubscriptionOfferDetails eligibleTrial = isTrialEnabled()
                ? PaywallOffers.trialOfferByBasePlanId(yearly, InAppKey.YEARLY_BASE)
                : null;
        yearlyOffer = eligibleTrial != null ? eligibleTrial : baseOffer;
        yearlyRecurringPhase = PaywallOffers.recurringPhase(yearlyOffer);

        yearlyAvailable = yearlyOffer != null && yearlyRecurringPhase != null;
        if (yearlyAvailable) {
            yearlyPriceText = yearlyRecurringPhase.getFormattedPrice();
            binding.planTrial.tvPlanTitle.setText(R.string.pw_plan_yearly);
            binding.planTrial.tvPlanPrice.setText(
                    getString(R.string.pw_per_year, yearlyPriceText));
            binding.planTrial.tvPlanPerWeek.setText(
                    perWeek(yearlyRecurringPhase, WEEKS_PER_YEAR));
            int trialDays = PaywallOffers.trialDays(yearlyOffer);
            binding.planTrial.tvPlanCornerBadge.setText(trialDays > 0
                    ? getString(R.string.pw_trial_badge, trialDays)
                    : getString(R.string.pw_best_value));
            binding.planTrial.tvPlanCornerBadge.setVisibility(View.VISIBLE);
        }
        setPlanVisible(binding.planTrial, yearlyAvailable);

        specialOffer = isSpecialAllowed()
                ? PaywallOffers.offerByBasePlanId(yearly, specialBasePlanId())
                : null;
        specialRecurringPhase = PaywallOffers.recurringPhase(specialOffer);
        boolean comparable = yearlyRecurringPhase != null && specialRecurringPhase != null
                && yearlyRecurringPhase.getBillingPeriod().equals(
                        specialRecurringPhase.getBillingPeriod())
                && yearlyRecurringPhase.getPriceCurrencyCode().equals(
                        specialRecurringPhase.getPriceCurrencyCode());
        specialAvailable = specialOffer != null && comparable
                && specialRecurringPhase.getPriceAmountMicros()
                < yearlyRecurringPhase.getPriceAmountMicros();
        if (specialAvailable) {
            specialPriceText = specialRecurringPhase.getFormattedPrice();
            int discount = discountPercent(yearlyRecurringPhase, specialRecurringPhase);
            binding.planSpecial.tvPlanTitle.setText(
                    getString(R.string.paywall_special_offer, discount));
            binding.planSpecial.tvPlanPrice.setText(strikeOriginalPrice(
                    getString(R.string.paywall_special_price,
                            specialPriceText, yearlyPriceText),
                    yearlyPriceText));
            binding.planSpecial.tvPlanPerWeek.setText(
                    perWeek(specialRecurringPhase, WEEKS_PER_YEAR));
        }
        setPlanVisible(binding.planSpecial, specialAvailable);
    }

    private void bindMonthlyPlan(@Nullable ProductDetails monthly) {
        boolean showMonthly = remoteConfig.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_MONTHLY);
        ProductDetails.SubscriptionOfferDetails eligibleTrial = isTrialEnabled()
                ? PaywallOffers.trialOffer(monthly)
                : null;
        monthlyOffer = eligibleTrial != null ? eligibleTrial : PaywallOffers.baseOffer(monthly);
        monthlyRecurringPhase = PaywallOffers.recurringPhase(monthlyOffer);
        monthlyAvailable = showMonthly && monthlyOffer != null && monthlyRecurringPhase != null;
        if (monthlyAvailable) {
            monthlyPriceText = monthlyRecurringPhase.getFormattedPrice();
            binding.planMonthly.tvPlanPrice.setText(
                    getString(R.string.pw_per_month, monthlyPriceText));
            binding.planMonthly.tvPlanPerWeek.setText(
                    perWeek(monthlyRecurringPhase, WEEKS_PER_MONTH));
            int trialDays = PaywallOffers.trialDays(monthlyOffer);
            binding.planMonthly.tvPlanCornerBadge.setText(
                    getString(R.string.pw_trial_badge, trialDays));
            binding.planMonthly.tvPlanCornerBadge.setVisibility(
                    trialDays > 0 ? View.VISIBLE : View.GONE);
        }
        setPlanVisible(binding.planMonthly, monthlyAvailable);
    }

    private CharSequence strikeOriginalPrice(String text, String originalPrice) {
        int start = originalPrice.isEmpty() ? -1 : text.lastIndexOf(originalPrice);
        if (start < 0) return text;
        SpannableString styled = new SpannableString(text);
        styled.setSpan(new StrikethroughSpan(), start, start + originalPrice.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }

    private void setPlanVisible(ItemPaywallPlanBinding plan, boolean visible) {
        plan.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void ensureSelectablePlan() {
        if (isPlanAvailable(selectedPlan)) return;
        if (yearlyAvailable) selectedPlan = PLAN_YEARLY;
        else if (specialAvailable) selectedPlan = PLAN_SPECIAL;
        else if (monthlyAvailable) selectedPlan = PLAN_MONTHLY;
    }

    private boolean isPlanAvailable(int plan) {
        switch (plan) {
            case PLAN_SPECIAL:
                return specialAvailable;
            case PLAN_MONTHLY:
                return monthlyAvailable;
            default:
                return yearlyAvailable;
        }
    }

    private void selectPlan(int plan) {
        if (!isPlanAvailable(plan) || selectedPlan == plan) return;
        selectedPlan = plan;
        render();
        logEvent("paywall_plan_select", purchaseParams());
    }

    private void render() {
        highlight(binding.planSpecial, selectedPlan == PLAN_SPECIAL);
        highlight(binding.planTrial, selectedPlan == PLAN_YEARLY);
        highlight(binding.planMonthly, selectedPlan == PLAN_MONTHLY);
        highlightSpecialBand(selectedPlan == PLAN_SPECIAL);

        ProductDetails.SubscriptionOfferDetails offer = selectedOffer();
        int trialDays = PaywallOffers.trialDays(offer);
        binding.tvCtaTitle.setText(trialDays > 0
                ? getString(R.string.pw_cta_trial, trialDays)
                : getString(R.string.pw_cta_continue));
        binding.tvCtaSubtitle.setText(R.string.pw_no_payment_today);
        binding.tvCtaSubtitle.setVisibility(trialDays > 0 ? View.VISIBLE : View.GONE);
        binding.tvBillingNote.setText(billingDisclosure(trialDays));
        renderTimeline(trialDays);
        renderStoreState();
    }

    private void highlight(ItemPaywallPlanBinding plan, boolean selected) {
        plan.getRoot().setSelected(selected);
        ViewCompat.setStateDescription(plan.getRoot(), getString(
                selected ? R.string.pw_selected : R.string.pw_not_selected));
        plan.cardPlan.setBackgroundResource(selected
                ? R.drawable.bg_paywall_plan_selected
                : R.drawable.bg_paywall_plan_unselected);
        plan.tvPlanCornerBadge.setBackgroundResource(selected
                ? R.drawable.bg_paywall_corner_badge
                : R.drawable.bg_paywall_corner_badge_dim);
        plan.tvPlanCornerBadge.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.White : R.color.offer_ink_muted));
    }

    private void highlightSpecialBand(boolean selected) {
        binding.planSpecial.getRoot().setBackgroundResource(selected
                ? R.drawable.bg_paywall_special_band
                : R.drawable.bg_paywall_special_band_dim);
        binding.planSpecial.tvPlanBand.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.White : R.color.offer_ink_muted));
    }

    private String billingDisclosure(int trialDays) {
        String recurring = selectedRecurringText();
        if (recurring.isEmpty()) return getString(R.string.pw_cancel_anytime);
        String terms = trialDays > 0
                ? getString(R.string.pw_disclosure_trial, trialDays, recurring)
                : getString(R.string.pw_disclosure_sub, recurring);
        return terms + " " + getString(R.string.pw_cancel_anytime) + ".";
    }

    private String selectedRecurringText() {
        switch (selectedPlan) {
            case PLAN_SPECIAL:
                return specialPriceText.isEmpty() ? ""
                        : getString(R.string.pw_per_year, specialPriceText);
            case PLAN_MONTHLY:
                return monthlyPriceText.isEmpty() ? ""
                        : getString(R.string.pw_per_month, monthlyPriceText);
            default:
                return yearlyPriceText.isEmpty() ? ""
                        : getString(R.string.pw_per_year, yearlyPriceText);
        }
    }

    private void renderTimeline(int trialDays) {
        String recurring = selectedRecurringText();
        boolean visible = trialDays > 0
                && !recurring.isEmpty()
                && remoteConfig.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_TIMELINE);
        binding.timelineCard.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) return;

        binding.tvTimelineToday.setText(getString(R.string.pw_timeline_row,
                getString(R.string.pw_timeline_today),
                getString(R.string.pw_timeline_today_desc)));
        boolean showReminder = trialDays >= 3;
        binding.tvTimelineReminder.setVisibility(showReminder ? View.VISIBLE : View.GONE);
        if (showReminder) {
            binding.tvTimelineReminder.setText(getString(R.string.pw_timeline_row,
                    getString(R.string.pw_timeline_reminder, trialDays - 1),
                    getString(R.string.pw_timeline_reminder_desc)));
        }
        binding.tvTimelineCharge.setText(getString(R.string.pw_timeline_row,
                getString(R.string.pw_timeline_charge, trialDays + 1),
                getString(R.string.pw_timeline_charge_desc, recurring)));
    }

    private void renderStoreState() {
        if (binding == null) return;
        Integer state = premiumViewModel.billingState.getValue();
        boolean loaded = premiumViewModel.productSkuDetailsMap.getValue() != null;
        boolean available = isPlanAvailable(selectedPlan);

        if (state != null && state == PremiumViewModel.STATE_UNAVAILABLE) {
            binding.tvStoreState.setText(R.string.pw_unavailable);
            binding.tvStoreState.setVisibility(View.VISIBLE);
        } else if (!loaded) {
            binding.tvStoreState.setText(R.string.pw_loading);
            binding.tvStoreState.setVisibility(View.VISIBLE);
        } else if (!available) {
            binding.tvStoreState.setText(R.string.pw_unavailable);
            binding.tvStoreState.setVisibility(View.VISIBLE);
        } else {
            binding.tvStoreState.setVisibility(View.GONE);
        }
        binding.btnContinue.setEnabled(available
                && state != null
                && state == PremiumViewModel.STATE_READY);
    }

    private void purchaseSelectedPlan() {
        ProductDetails.SubscriptionOfferDetails offer = selectedOffer();
        if (!isPlanAvailable(selectedPlan) || offer == null) {
            Toast.makeText(this, R.string.pw_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle params = purchaseParams();
        logEvent("paywall_purchase_click", params);
        int response = premiumViewModel.launchSubscription(this, selectedProductId(), offer);
        if (response == BillingClient.BillingResponseCode.OK) {
            purchaseStarted = true;
        } else {
            purchaseStarted = false;
            params.putInt("response_code", response);
            logEvent("paywall_purchase_error", params);
            Toast.makeText(this, R.string.pw_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void restorePurchases() {
        if (restoreInProgress) return;
        restoreInProgress = true;
        binding.tvRestore.setEnabled(false);
        binding.tvRestore.setAlpha(0.55f);
        logEvent("paywall_restore", null);
        Toast.makeText(this, R.string.pw_restore_checking, Toast.LENGTH_SHORT).show();
        premiumViewModel.restorePurchases();
    }

    @Nullable
    private ProductDetails.SubscriptionOfferDetails selectedOffer() {
        switch (selectedPlan) {
            case PLAN_SPECIAL:
                return specialOffer;
            case PLAN_MONTHLY:
                return monthlyOffer;
            default:
                return yearlyOffer;
        }
    }

    private String selectedProductId() {
        return selectedPlan == PLAN_MONTHLY
                ? InAppKey.PREMIUM_MONTHLY
                : InAppKey.PREMIUM_YEARLY;
    }

    private Bundle purchaseParams() {
        Bundle params = new Bundle();
        params.putString("source", source);
        // Keep the legacy integer parameter so existing Firebase reports retain one value type.
        params.putInt("plan", selectedPlan);
        params.putString("plan_name", planName(selectedPlan));
        ProductDetails.SubscriptionOfferDetails offer = selectedOffer();
        if (offer != null) {
            params.putString("base_plan_id", offer.getBasePlanId());
            if (offer.getOfferId() != null) params.putString("offer_id", offer.getOfferId());
            params.putInt("trial_days", PaywallOffers.trialDays(offer));
        }
        return params;
    }

    private String planName(int plan) {
        switch (plan) {
            case PLAN_SPECIAL:
                return "special_yearly";
            case PLAN_MONTHLY:
                return "monthly";
            default:
                return "yearly";
        }
    }

    private boolean isTrialEnabled() {
        return remoteConfig.getBoolean(RemoteConfigKeys.ENABLE_TRIAL);
    }

    private boolean isSpecialAllowed() {
        return !isFirstSetup || hasActiveFlashOffer();
    }

    private String specialBasePlanId() {
        return hasActiveFlashOffer()
                ? InAppKey.YEARLY_SPECIAL_50
                : InAppKey.YEARLY_SPECIAL_34;
    }

    private boolean hasActiveFlashOffer() {
        return myPreference.getPromoEndTime() > System.currentTimeMillis();
    }

    private int discountPercent(ProductDetails.PricingPhase base,
                                ProductDetails.PricingPhase discounted) {
        long basePrice = base.getPriceAmountMicros();
        if (basePrice <= 0L) return 0;
        return (int) Math.round(
                (basePrice - discounted.getPriceAmountMicros()) * 100d / basePrice);
    }

    private String perWeek(ProductDetails.PricingPhase phase, double weeksInPeriod) {
        double amount = phase.getPriceAmountMicros() / 1_000_000d / weeksInPeriod;
        return getString(R.string.paywall_per_week,
                formatMoney(amount, phase.getPriceCurrencyCode()));
    }

    private String formatMoney(double amount, @Nullable String currencyCode) {
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance();
            if (currencyCode != null) {
                Currency currency = Currency.getInstance(currencyCode);
                format.setCurrency(currency);
                format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
                format.setMinimumFractionDigits(currency.getDefaultFractionDigits());
            }
            return format.format(amount);
        } catch (Exception exception) {
            return String.format(Locale.getDefault(), "%.2f", amount);
        }
    }

    private void finishPaywall() {
        if (closing) return;
        logEvent("paywall_close", null);

        if (isFirstSetup
                && !myPreference.isPremiumMember()
                && remoteConfig.getBoolean(RemoteConfigKeys.PAYWALL_EXIT_OFFER_ENABLED)) {
            closing = true;
            logEvent("paywall_exit_offer_view", null);
            // Vòng quay hiện dạng popup đè lên paywall; paywall đóng khi vòng quay / vé ưu đãi xong.
            luckyDrawLauncher.launch(new Intent(this, LuckyDrawActivity.class));
            return;
        }

        closing = true;
        if (isFirstSetup
                && !myPreference.isPremiumMember()
                && remoteConfig.getBoolean(RemoteConfigKeys.INTER_FIRST_OPEN)
                && MaticAd.getInstance().isAvailableInter()) {
            MaticAd.getInstance().showPriorityInterstitialAd(this, new MaticAdCallBack() {
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    finishWithResult();
                }
            });
        } else {
            finishWithResult();
        }
    }

    private void finishWithResult() {
        Intent result = new Intent();
        result.putExtra("PREMIUM_MEMBER", myPreference.isPremiumMember());
        setResult(RESULT_OK, result);
        finish();
    }

    private void logEvent(String name, @Nullable Bundle details) {
        Bundle params = details == null ? new Bundle() : new Bundle(details);
        params.putString("source", source);
        params.putString("variant", "paywall_new");
        FirebaseAnalytics.getInstance(this).logEvent(name, params);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        binding = null;
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (lang == null || lang.length() < 2) {
            lang = Locale.getDefault().toLanguageTag();
        } else if ("nlL".equals(lang)) {
            lang = "nl";
        }
        // Preserve region codes for Simplified/Traditional Chinese.
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));
    }
}
