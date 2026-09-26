package mint.woman.female.workout.activity.ui.premium;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.ProductDetails;

import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.LayoutDialogDiscountBinding;
import mint.woman.female.workout.utils.MyPreference;

/** Full-screen discount campaign dialog shown after a Lucky Draw win. */
public class DiscountDialogFragment extends DialogFragment {
    private static final String TAG = "DiscountDialogFragment";
    private static final long DEFAULT_DURATION_MILLIS = 60 * 60 * 1000L;

    /** Host activity nào cần biết người dùng đã đóng ưu đãi thì implement interface này. */
    public interface DismissListener {
        void onDiscountDialogDismissed();
    }

    private LayoutDialogDiscountBinding binding;
    private CountDownTimer countDownTimer;
    private PremiumViewModel premiumViewModel;
    private long durationMillis = DEFAULT_DURATION_MILLIS;

    public static DiscountDialogFragment newInstance() {
        return new DiscountDialogFragment();
    }

    public static DiscountDialogFragment newInstance(long durationMillis) {
        DiscountDialogFragment dialog = new DiscountDialogFragment();
        Bundle args = new Bundle();
        args.putLong("duration_millis", Math.max(0L, durationMillis));
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);

        Bundle args = getArguments();
        if (args != null && args.containsKey("duration_millis")) {
            durationMillis = Math.max(0L, args.getLong("duration_millis"));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = LayoutDialogDiscountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        premiumViewModel = new ViewModelProvider(requireActivity()).get(PremiumViewModel.class);
        premiumViewModel.productSkuDetailsMap.observe(getViewLifecycleOwner(), this::updateDiscountPrice);

        binding.ivCloseDiscount.setOnClickListener(v -> dismiss());
        binding.btnClaimDiscount.setOnClickListener(v -> claimDiscount());
        updateDiscountPrice(premiumViewModel.productSkuDetailsMap.getValue());
        startCountdown();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void startCountdown() {
        stopCountdown();
        if (binding == null) {
            return;
        }

        Context context = getContext();
        if (context != null) {
            MyPreference preference = new MyPreference(context);
            long now = System.currentTimeMillis();
            long savedEndTime = preference.getPromoEndTime();
            if (savedEndTime > 0L) {
                // Reopening the dialog (or rotating the device) must not restart the offer clock.
                durationMillis = Math.max(0L, savedEndTime - now);
            } else if (durationMillis > 0L) {
                preference.setPromoEndTime(now + durationMillis);
            }
        }

        if (durationMillis <= 0L) {
            updateCountdown(0L);
            setClaimEnabled(false);
            return;
        }

        countDownTimer = new CountDownTimer(durationMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateCountdown(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateCountdown(0L);
                setClaimEnabled(false);
            }
        }.start();
    }

    private void updateCountdown(long millis) {
        if (binding == null) {
            return;
        }

        long totalSeconds = Math.max(0L, millis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        binding.tvDiscountHours.setText(String.format(Locale.US, "%02d", hours));
        binding.tvDiscountMinutes.setText(String.format(Locale.US, "%02d", minutes));
        binding.tvDiscountSeconds.setText(String.format(Locale.US, "%02d", seconds));
    }

    private void setClaimEnabled(boolean enabled) {
        if (binding == null) {
            return;
        }
        binding.btnClaimDiscount.setEnabled(enabled);
        binding.btnClaimDiscount.setAlpha(enabled ? 1f : 0.5f);
    }

    /** Giá ưu đãi lấy từ base plan YEARLY_SPECIAL_50 (phần thưởng thắng Lucky Draw), giống bản Tai Chi. */
    private void updateDiscountPrice(java.util.Map<String, ProductDetails> products) {
        if (binding == null || products == null) {
            return;
        }

        ProductDetails yearly = products.get(InAppKey.PREMIUM_YEARLY);
        ProductDetails.SubscriptionOfferDetails offer =
                PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_SPECIAL_50);
        ProductDetails.SubscriptionOfferDetails baseOffer =
                PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_BASE);
        ProductDetails.PricingPhase salePhase = PaywallOffers.recurringPhase(offer);
        ProductDetails.PricingPhase originalPhase = PaywallOffers.recurringPhase(baseOffer);
        if (offer == null || salePhase == null) {
            Log.w(TAG, "No discounted yearly offer configured for the Lucky Draw dialog");
            return;
        }

        String salePrice = salePhase.getFormattedPrice();
        String originalPrice = originalPhase == null ? "" : originalPhase.getFormattedPrice();
        binding.tvDiscountTotal.setText(getString(
                R.string.discount_total_price_format,
                salePrice
        ));
        setStrikethroughPrice(binding.tvOriginalPrice, originalPrice);
        binding.tvDiscountMonthly.setText(getString(
                R.string.discount_monthly_price_format,
                getMonthlyPrice(salePhase)
        ));
    }

    /** Giá gốc nằm trong ngoặc; chỉ con số bị gạch ngang, hai dấu ngoặc để nguyên. */
    private void setStrikethroughPrice(TextView textView, String price) {
        if (textView == null || TextUtils.isEmpty(price)) {
            return;
        }
        try {
            String text = getString(R.string.discount_original_price_format, price);
            int start = text.lastIndexOf(price);
            SpannableString spannableString = new SpannableString(text);
            if (start >= 0) {
                spannableString.setSpan(new StrikethroughSpan(), start, start + price.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(spannableString);
            textView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.w(TAG, "setStrikethroughPrice failed", e);
            textView.setText(price);
        }
    }

    private String getMonthlyPrice(ProductDetails.PricingPhase yearlyPhase) {
        try {
            long monthlyMicros = yearlyPhase.getPriceAmountMicros() / 12L;
            return String.format(Locale.getDefault(), "%s%.2f",
                    yearlyPhase.getFormattedPrice().replaceAll("[0-9.,\\s]", ""),
                    monthlyMicros / 1_000_000.0);
        } catch (Exception ignored) {
            return yearlyPhase.getFormattedPrice();
        }
    }

    private void claimDiscount() {
        if (!isAdded() || premiumViewModel == null) {
            return;
        }

        ProductDetails yearly = premiumViewModel.getSubscription(InAppKey.PREMIUM_YEARLY);
        ProductDetails.SubscriptionOfferDetails offer =
                PaywallOffers.offerByBasePlanId(yearly, InAppKey.YEARLY_SPECIAL_50);
        premiumViewModel.launchSubscription(requireActivity(), InAppKey.PREMIUM_YEARLY, offer);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // DialogFragment cung goi onDismiss khi view bi huy luc xoay man hinh, luc do khong
        // phai nguoi dung dong uu dai nen bo qua.
        androidx.fragment.app.FragmentActivity activity = getActivity();
        if (activity == null || activity.isChangingConfigurations() || activity.isFinishing()) {
            return;
        }
        if (activity instanceof DismissListener) {
            ((DismissListener) activity).onDiscountDialogDismissed();
        }
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopCountdown();
        binding = null;
        super.onDestroyView();
    }
}
