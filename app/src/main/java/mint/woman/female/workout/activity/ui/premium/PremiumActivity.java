package mint.woman.female.workout.activity.ui.premium;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.PolicyActivity;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.databinding.ActivityPremiumBinding;
import mint.woman.female.workout.databinding.DialogPwExitOfferBinding;
import mint.woman.female.workout.databinding.ItemPwPlanBinding;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.RemoteConfigKeys;

/**
 * Paywall v2: benefits, plan picker (yearly / monthly / lifetime), free-trial timeline,
 * clear price disclosure, restore, delayed close button and an optional exit offer.
 * Everything tunable is driven by Remote Config (see {@link RemoteConfigKeys}).
 */
public class PremiumActivity extends AppCompatActivity implements EdgeToEdgeHelper.HandlesInsets {

    public static final String EXTRA_IS_FIRST = "IS_FIRST";
    public static final String EXTRA_SOURCE = "SOURCE";

    private ActivityPremiumBinding binding;
    private PremiumViewModel vm;
    private MyPreference myPreference;
    private FirebaseRemoteConfig rc;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isFirstSetup = false;
    private String source = "unknown";
    private boolean exitOfferShown = false;
    private boolean closing = false;
    private boolean purchaseStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rc = FirebaseRemoteConfig.getInstance();
        myPreference = new MyPreference(this);
        isFirstSetup = getIntent().getBooleanExtra(EXTRA_IS_FIRST, false);
        String src = getIntent().getStringExtra(EXTRA_SOURCE);
        if (src != null) source = src;
        if (savedInstanceState != null) {
            exitOfferShown = savedInstanceState.getBoolean("exit_offer_shown", false);
        }
        vm = new ViewModelProvider(this).get(PremiumViewModel.class);

        applyInsets();
        setupStaticContent();
        setupPlans(savedInstanceState == null);
        setupActions();
        observeViewModel();
        scheduleCloseButton(savedInstanceState != null);

        if (savedInstanceState == null) {
            Bundle params = new Bundle();
            params.putString("source", source);
            FirebaseAnalytics.getInstance(this).logEvent("paywall_view", params);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("exit_offer_shown", exitOfferShown);
    }

    // ------------------------------------------------------------------ setup

    private void applyInsets() {
        final int closeMarginTop = ((ViewGroup.MarginLayoutParams) binding.btnClose.getLayoutParams()).topMargin;
        final int panelPaddingBottom = binding.bottomPanel.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.pwRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) binding.btnClose.getLayoutParams();
            lp.topMargin = closeMarginTop + bars.top;
            binding.btnClose.setLayoutParams(lp);
            binding.bottomPanel.setPadding(
                    binding.bottomPanel.getPaddingLeft(), binding.bottomPanel.getPaddingTop(),
                    binding.bottomPanel.getPaddingRight(), panelPaddingBottom + bars.bottom);
            binding.pwRoot.setPadding(bars.left, 0, bars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
        // Keep the scroll content above the sticky purchase panel.
        binding.bottomPanel.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            int height = b - t;
            if (binding.scroll.getPaddingBottom() != height) {
                binding.scroll.setPadding(0, 0, 0, height);
            }
        });
    }

    private void setupStaticContent() {
        Glide.with(this)
                .load(Uri.parse("file:///android_asset/onboards/onboard_intro_1.png"))
                .centerCrop()
                .into(binding.ivHero);

        if (myPreference.isOnboarded()) {
            //int goalLabel = QuizQuestions.labelOf(OnboardingSteps.GOAL, myPreference.getUserGoal());
//            if (goalLabel != 0) {
//                binding.tvSubtitle.setText(getString(R.string.pw_subtitle_personal,
//                        getString(goalLabel).toLowerCase(Locale.getDefault())));
//            }
        }

        binding.planYearly.tvPlanName.setText(R.string.pw_plan_yearly);
        binding.planMonthly.tvPlanName.setText(R.string.pw_plan_monthly);
        binding.planLifetime.tvPlanName.setText(R.string.pw_plan_lifetime);
        binding.planLifetime.tvPlanSub.setText(R.string.pw_one_time);

        binding.planMonthly.getRoot().setVisibility(
                rc.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_MONTHLY) ? View.VISIBLE : View.GONE);
        binding.planLifetime.getRoot().setVisibility(
                rc.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_LIFETIME) ? View.VISIBLE : View.GONE);
    }

    private void setupPlans(boolean firstCreate) {
        if (firstCreate) {
            String plan = rc.getString(RemoteConfigKeys.PAYWALL_DEFAULT_PLAN);
            int selected = PremiumViewModel.PLAN_YEARLY;
            if ("monthly".equals(plan) && binding.planMonthly.getRoot().getVisibility() == View.VISIBLE) {
                selected = PremiumViewModel.PLAN_MONTHLY;
            } else if ("lifetime".equals(plan) && binding.planLifetime.getRoot().getVisibility() == View.VISIBLE) {
                selected = PremiumViewModel.PLAN_LIFETIME;
            }
            vm.setSelected(selected);
        }
        binding.planYearly.planCard.setOnClickListener(v -> vm.setSelected(PremiumViewModel.PLAN_YEARLY));
        binding.planMonthly.planCard.setOnClickListener(v -> vm.setSelected(PremiumViewModel.PLAN_MONTHLY));
        binding.planLifetime.planCard.setOnClickListener(v -> vm.setSelected(PremiumViewModel.PLAN_LIFETIME));
    }

    private void setupActions() {
        binding.btnClose.setOnClickListener(v -> onCloseRequested());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onCloseRequested();
            }
        });
        binding.btnPurchase.setOnClickListener(v -> purchaseSelected());
        binding.tvRestore.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(this).logEvent("paywall_restore", null);
            vm.restorePurchases();
        });
        binding.tvPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PolicyActivity.class)));
        binding.tvManage.setOnClickListener(v -> openUrl(
                "https://play.google.com/store/account/subscriptions?package=" + getPackageName()));
    }

    private void observeViewModel() {
        vm.getSelected().observe(this, s -> render());
        vm.productSkuDetailsMap.observe(this, map -> render());
        vm.productDetailInApps.observe(this, map -> render());
        vm.billingState.observe(this, state -> {
            if (state == PremiumViewModel.STATE_UNAVAILABLE) {
                showCloseButtonNow();
            }
            render();
        });
        vm.isPremium.observe(this, premium -> {
            if (premium != null && premium) myPreference.setPremiumMember(true);
        });
        vm.events.observe(this, event -> {
            if (event == null || event == PremiumViewModel.EVENT_NONE) return;
            vm.consumeEvent();
            switch (event) {
                case PremiumViewModel.EVENT_PURCHASED:
                    onPurchased();
                    break;
                case PremiumViewModel.EVENT_RESTORED:
                    Toast.makeText(this, R.string.pw_restore_success, Toast.LENGTH_SHORT).show();
                    onPurchased();
                    break;
                case PremiumViewModel.EVENT_RESTORE_NONE:
                    Toast.makeText(this, R.string.pw_restore_none, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        });
    }

    // ------------------------------------------------------------------ rendering

    private void render() {
        Integer sel = vm.getSelected().getValue();
        int selected = sel == null ? PremiumViewModel.PLAN_YEARLY : sel;

        ProductDetails yearly = vm.getSubscription(InAppKey.PREMIUM_YEARLY);
        ProductDetails monthly = vm.getSubscription(InAppKey.PREMIUM_MONTHLY);
        ProductDetails lifetime = vm.getInApp(InAppKey.PREMIUM_LIFETIME);

        ProductDetails.PricingPhase yearlyPrice = PaywallOffers.recurringPhase(PaywallOffers.baseOffer(yearly));
        ProductDetails.PricingPhase monthlyPrice = PaywallOffers.recurringPhase(PaywallOffers.baseOffer(monthly));
        ProductDetails.SubscriptionOfferDetails yearlyOffer = vm.getDefaultOffer(InAppKey.PREMIUM_YEARLY);
        ProductDetails.SubscriptionOfferDetails monthlyOffer = vm.getDefaultOffer(InAppKey.PREMIUM_MONTHLY);
        int yearlyTrial = PaywallOffers.trialDays(yearlyOffer);
        int monthlyTrial = PaywallOffers.trialDays(monthlyOffer);

        // Yearly card
        if (yearlyPrice != null) {
            int months = PaywallOffers.periodMonths(yearlyPrice.getBillingPeriod());
            binding.planYearly.tvPlanSub.setText(getString(R.string.pw_per_year, yearlyPrice.getFormattedPrice()));
            binding.planYearly.tvPlanPrice.setText(getString(R.string.pw_per_month,
                    PaywallOffers.formatMicros(yearlyPrice.getPriceAmountMicros() / months, yearlyPrice)));
            int save = 0;
            if (monthlyPrice != null && monthlyPrice.getPriceAmountMicros() > 0
                    && monthlyPrice.getPriceCurrencyCode().equals(yearlyPrice.getPriceCurrencyCode())) {
                double perMonth = yearlyPrice.getPriceAmountMicros() / (double) months;
                save = (int) Math.round((1 - perMonth / monthlyPrice.getPriceAmountMicros()) * 100);
            }
            if (yearlyTrial > 0) {
                showBadge(binding.planYearly, getString(R.string.pw_trial_badge, yearlyTrial));
            } else if (save >= 5) {
                showBadge(binding.planYearly, getString(R.string.pw_save_badge, save));
            } else {
                showBadge(binding.planYearly, getString(R.string.pw_best_value));
            }
        } else {
            binding.planYearly.tvPlanSub.setText("");
            binding.planYearly.tvPlanPrice.setText("—");
            binding.planYearly.tvBadge.setVisibility(View.GONE);
        }

        // Monthly card
        if (monthlyPrice != null) {
            binding.planMonthly.tvPlanSub.setText(monthlyTrial > 0
                    ? getString(R.string.pw_trial_badge, monthlyTrial) : "");
            binding.planMonthly.tvPlanPrice.setText(getString(R.string.pw_per_month, monthlyPrice.getFormattedPrice()));
        } else {
            binding.planMonthly.tvPlanPrice.setText("—");
        }

        // Lifetime card
        ProductDetails.OneTimePurchaseOfferDetails lifetimeOffer =
                lifetime == null ? null : lifetime.getOneTimePurchaseOfferDetails();
        binding.planLifetime.tvPlanPrice.setText(lifetimeOffer != null ? lifetimeOffer.getFormattedPrice() : "—");

        renderSelection(binding.planYearly, selected == PremiumViewModel.PLAN_YEARLY);
        renderSelection(binding.planMonthly, selected == PremiumViewModel.PLAN_MONTHLY);
        renderSelection(binding.planLifetime, selected == PremiumViewModel.PLAN_LIFETIME);

        // CTA, disclosure and timeline for the selected plan
        String disclosure = "";
        int trialDays = 0;
        String recurringText = null;
        boolean available;
        if (selected == PremiumViewModel.PLAN_LIFETIME) {
            available = lifetimeOffer != null;
            if (available) disclosure = getString(R.string.pw_disclosure_lifetime, lifetimeOffer.getFormattedPrice());
        } else {
            boolean isYearly = selected == PremiumViewModel.PLAN_YEARLY;
            ProductDetails.PricingPhase price = isYearly ? yearlyPrice : monthlyPrice;
            trialDays = isYearly ? yearlyTrial : monthlyTrial;
            available = price != null;
            if (available) {
                recurringText = getString(isYearly ? R.string.pw_per_year : R.string.pw_per_month,
                        price.getFormattedPrice());
                disclosure = trialDays > 0
                        ? getString(R.string.pw_disclosure_trial, trialDays, recurringText)
                        : getString(R.string.pw_disclosure_sub, recurringText);
            }
        }

        binding.btnPurchase.setEnabled(available);
        binding.btnPurchase.setText(trialDays > 0
                ? getString(R.string.pw_cta_trial, trialDays)
                : getString(R.string.pw_cta_continue));
        binding.tvDisclosure.setText(disclosure.isEmpty()
                ? getString(R.string.pw_cancel_anytime)
                : disclosure + " " + getString(R.string.pw_cancel_anytime) + ".");

        Integer state = vm.billingState.getValue();
        boolean loaded = vm.productSkuDetailsMap.getValue() != null;
        if (state != null && state == PremiumViewModel.STATE_UNAVAILABLE) {
            binding.tvStoreState.setVisibility(View.VISIBLE);
            binding.tvStoreState.setText(R.string.pw_unavailable);
        } else if (!loaded) {
            binding.tvStoreState.setVisibility(View.VISIBLE);
            binding.tvStoreState.setText(R.string.pw_loading);
        } else {
            binding.tvStoreState.setVisibility(View.GONE);
        }

        renderTimeline(trialDays, recurringText);
    }

    private void renderTimeline(int trialDays, @Nullable String recurringText) {
        boolean show = trialDays > 0 && recurringText != null
                && rc.getBoolean(RemoteConfigKeys.PAYWALL_SHOW_TIMELINE);
        binding.timelineCard.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;
        binding.tvTlToday.setText(R.string.pw_timeline_today);
        binding.tvTlTodayDesc.setText(R.string.pw_timeline_today_desc);
        boolean showReminder = trialDays >= 3;
        binding.tvTlReminderRow.setVisibility(showReminder ? View.VISIBLE : View.GONE);
        if (showReminder) {
            binding.tvTlReminder.setText(getString(R.string.pw_timeline_reminder, trialDays - 1));
            binding.tvTlReminderDesc.setText(R.string.pw_timeline_reminder_desc);
        }
        binding.tvTlCharge.setText(getString(R.string.pw_timeline_charge, trialDays + 1));
        binding.tvTlChargeDesc.setText(getString(R.string.pw_timeline_charge_desc, recurringText));
    }

    private void renderSelection(ItemPwPlanBinding plan, boolean isSelected) {
        plan.planCard.setSelected(isSelected);
        plan.ivRadio.setImageResource(isSelected ? R.drawable.ic_ob_check : R.drawable.pw_radio_off);
    }

    private void showBadge(ItemPwPlanBinding plan, String text) {
        plan.tvBadge.setText(text);
        plan.tvBadge.setVisibility(View.VISIBLE);
    }

    // ------------------------------------------------------------------ purchase

    private void purchaseSelected() {
        Integer sel = vm.getSelected().getValue();
        int selected = sel == null ? PremiumViewModel.PLAN_YEARLY : sel;
        Bundle params = new Bundle();
        params.putString("source", source);
        params.putInt("plan", selected);
        FirebaseAnalytics.getInstance(this).logEvent("paywall_purchase_click", params);

        purchaseStarted = true;
        int code;
        if (selected == PremiumViewModel.PLAN_LIFETIME) {
            code = vm.launchBillingFlowInApp(this, InAppKey.PREMIUM_LIFETIME);
        } else if (selected == PremiumViewModel.PLAN_MONTHLY) {
            code = vm.launchSubscription(this, InAppKey.PREMIUM_MONTHLY, null);
        } else {
            code = vm.launchSubscription(this, InAppKey.PREMIUM_YEARLY, null);
        }
        if (code != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(this, R.string.pw_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPurchased() {
        if (closing) return;
        closing = true;
        myPreference.setPremiumMember(true);
        if (purchaseStarted) {
            Bundle params = new Bundle();
            params.putString("source", source);
            FirebaseAnalytics.getInstance(this).logEvent("paywall_purchase_success", params);
            Toast.makeText(this, R.string.pw_purchase_success, Toast.LENGTH_LONG).show();
        }
        finishWithResult();
    }

    // ------------------------------------------------------------------ close / exit offer

    private void scheduleCloseButton(boolean immediately) {
        long delaySec = immediately ? 0 : rc.getLong(RemoteConfigKeys.PAYWALL_CLOSE_DELAY_SEC);
        delaySec = Math.max(0, Math.min(delaySec, 10)); // never trap the user
        handler.postDelayed(this::showCloseButtonNow, delaySec * 1000L);
    }

    private void showCloseButtonNow() {
        if (binding.btnClose.getVisibility() == View.VISIBLE) return;
        binding.btnClose.setVisibility(View.VISIBLE);
        binding.btnClose.animate().alpha(1f).setDuration(250).start();
    }

    private void onCloseRequested() {
        if (closing) return;
        if (!exitOfferShown && rc.getBoolean(RemoteConfigKeys.PAYWALL_EXIT_OFFER_ENABLED) && showExitOffer()) {
            return;
        }
        FirebaseAnalytics.getInstance(this).logEvent("paywall_close", null);
        closing = true;
        finishPremium();
    }

    /** @return true if the exit offer sheet was shown. */
    private boolean showExitOffer() {
        ProductDetails yearly = vm.getSubscription(InAppKey.PREMIUM_YEARLY);
        ProductDetails.SubscriptionOfferDetails offer =
                PaywallOffers.offerById(yearly, rc.getString(RemoteConfigKeys.PAYWALL_EXIT_OFFER_ID));
        ProductDetails.PricingPhase intro = PaywallOffers.introPhase(offer);
        ProductDetails.PricingPhase regular = PaywallOffers.recurringPhase(offer);
        if (offer == null || intro == null || regular == null
                || regular.getPriceAmountMicros() <= intro.getPriceAmountMicros()) {
            return false;
        }
        exitOfferShown = true;
        int discount = (int) Math.round((1 - intro.getPriceAmountMicros() / (double) regular.getPriceAmountMicros()) * 100);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogPwExitOfferBinding sheet = DialogPwExitOfferBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());
        View parent = (View) sheet.getRoot().getParent();
        if (parent != null) parent.setBackgroundResource(android.R.color.transparent);

        sheet.tvExitBadge.setText(getString(R.string.pw_exit_badge, discount));
        sheet.tvExitSubtitle.setText(getString(R.string.pw_exit_subtitle,
                intro.getFormattedPrice(), regular.getFormattedPrice()));
        sheet.tvExitDisclosure.setText(getString(R.string.pw_disclosure_sub,
                getString(R.string.pw_per_year, regular.getFormattedPrice())));
        sheet.btnClaim.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(this).logEvent("paywall_exit_offer_claim", null);
            dialog.dismiss();
            purchaseStarted = true;
            vm.launchSubscription(this, InAppKey.PREMIUM_YEARLY, offer);
        });
        sheet.btnDecline.setOnClickListener(v -> {
            dialog.dismiss();
            onCloseRequested();
        });
        FirebaseAnalytics.getInstance(this).logEvent("paywall_exit_offer_view", null);
        dialog.show();
        return true;
    }

    private void finishPremium() {
        if (isFirstSetup && rc.getBoolean(RemoteConfigKeys.INTER_FIRST_OPEN) && MaticAd.getInstance().isAvailableInter()) {
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
        Intent data = new Intent();
        data.putExtra("PREMIUM_MEMBER", myPreference.isPremiumMember());
        setResult(RESULT_OK, data);
        finish();
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
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty()) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));
    }
}
