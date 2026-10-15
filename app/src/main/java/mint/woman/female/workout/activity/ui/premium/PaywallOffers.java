package mint.woman.female.workout.activity.ui.premium;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers to pick the right subscription offer and read its prices.
 * Never rely on the order of {@link ProductDetails#getSubscriptionOfferDetails()}.
 */
public final class PaywallOffers {

    private static final Pattern ISO_PERIOD = Pattern.compile("P(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)W)?(?:(\\d+)D)?");

    private PaywallOffers() {
    }

    @Nullable
    public static List<ProductDetails.SubscriptionOfferDetails> offers(@Nullable ProductDetails details) {
        if (details == null) return null;
        List<ProductDetails.SubscriptionOfferDetails> list = details.getSubscriptionOfferDetails();
        return list == null || list.isEmpty() ? null : list;
    }

    /** The base plan (no offer id). Falls back to the first offer without a free phase. */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails baseOffer(@Nullable ProductDetails details) {
        List<ProductDetails.SubscriptionOfferDetails> list = offers(details);
        if (list == null) return null;
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (offer.getOfferId() == null) return offer;
        }
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (freePhase(offer) == null) return offer;
        }
        return list.get(0);
    }

    /**
     * Gói năm giá gốc (base plan {@link InAppKey#YEARLY_BASE}). premium_yearly còn các base plan giảm giá
     * (yearly-special-50-off, yearly-special-34-off) cũng có offer không offerId; {@link #baseOffer} lấy offer đầu
     * tiên Play trả về nên có thể hiện / bán nhầm giá giảm làm giá năm mặc định.
     */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails yearlyBaseOffer(@Nullable ProductDetails yearly) {
        ProductDetails.SubscriptionOfferDetails offer = offerByBasePlanId(yearly, InAppKey.YEARLY_BASE);
        return offer != null ? offer : baseOffer(yearly);
    }

    /** An offer that starts with a free phase (free trial), if the user is eligible. */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails trialOffer(@Nullable ProductDetails details) {
        List<ProductDetails.SubscriptionOfferDetails> list = offers(details);
        if (list == null) return null;
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (freePhase(offer) != null) return offer;
        }
        return null;
    }

    /**
     * An eligible free-trial offer belonging to the requested base plan.
     *
     * A subscription can contain several base plans. Picking the first trial from the product can
     * therefore display the price of one plan and purchase another. Keep the base-plan filter here
     * so the offer used by the paywall is also the offer passed to BillingClient.
     */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails trialOfferByBasePlanId(
            @Nullable ProductDetails details, @Nullable String basePlanId) {
        List<ProductDetails.SubscriptionOfferDetails> list = offers(details);
        if (list == null || basePlanId == null || basePlanId.isEmpty()) return null;
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (basePlanId.equals(offer.getBasePlanId()) && freePhase(offer) != null) {
                return offer;
            }
        }
        return null;
    }

    /** Offer by its Play Console offer id, or by offer tag with the same value. */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails offerById(@Nullable ProductDetails details, @Nullable String offerId) {
        List<ProductDetails.SubscriptionOfferDetails> list = offers(details);
        if (list == null || offerId == null || offerId.isEmpty()) return null;
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (offerId.equals(offer.getOfferId()) || offer.getOfferTags().contains(offerId)) return offer;
        }
        return null;
    }

    /**
     * Offer belonging to a given Play Console base plan id (a subscription can have several base
     * plans, e.g. one full-price and one discounted). Prefers the offer without an offer id
     * (the base plan itself), since that is the one whose price should be displayed.
     */
    @Nullable
    public static ProductDetails.SubscriptionOfferDetails offerByBasePlanId(@Nullable ProductDetails details, @Nullable String basePlanId) {
        List<ProductDetails.SubscriptionOfferDetails> list = offers(details);
        if (list == null || basePlanId == null || basePlanId.isEmpty()) return null;
        ProductDetails.SubscriptionOfferDetails fallback = null;
        for (ProductDetails.SubscriptionOfferDetails offer : list) {
            if (!basePlanId.equals(offer.getBasePlanId())) continue;
            if (offer.getOfferId() == null) return offer;
            if (fallback == null) fallback = offer;
        }
        return fallback;
    }

    @Nullable
    public static ProductDetails.PricingPhase freePhase(@NonNull ProductDetails.SubscriptionOfferDetails offer) {
        for (ProductDetails.PricingPhase phase : offer.getPricingPhases().getPricingPhaseList()) {
            if (phase.getPriceAmountMicros() == 0L) return phase;
        }
        return null;
    }

    /** The phase that repeats forever (the "real" price after trials / intro prices). */
    @Nullable
    public static ProductDetails.PricingPhase recurringPhase(@Nullable ProductDetails.SubscriptionOfferDetails offer) {
        if (offer == null) return null;
        List<ProductDetails.PricingPhase> phases = offer.getPricingPhases().getPricingPhaseList();
        for (ProductDetails.PricingPhase phase : phases) {
            if (phase.getRecurrenceMode() == ProductDetails.RecurrenceMode.INFINITE_RECURRING) return phase;
        }
        return phases.isEmpty() ? null : phases.get(phases.size() - 1);
    }

    /** First paid, non-infinite phase (introductory discount), if any. */
    @Nullable
    public static ProductDetails.PricingPhase introPhase(@Nullable ProductDetails.SubscriptionOfferDetails offer) {
        if (offer == null) return null;
        for (ProductDetails.PricingPhase phase : offer.getPricingPhases().getPricingPhaseList()) {
            if (phase.getPriceAmountMicros() > 0L
                    && phase.getRecurrenceMode() != ProductDetails.RecurrenceMode.INFINITE_RECURRING) {
                return phase;
            }
        }
        return null;
    }

    /** Length of the free trial in days, 0 when the offer has none. */
    public static int trialDays(@Nullable ProductDetails.SubscriptionOfferDetails offer) {
        if (offer == null) return 0;
        ProductDetails.PricingPhase free = freePhase(offer);
        if (free == null) return 0;
        return periodDays(free.getBillingPeriod()) * Math.max(1, free.getBillingCycleCount());
    }

    public static int periodDays(@Nullable String isoPeriod) {
        if (isoPeriod == null) return 0;
        Matcher m = ISO_PERIOD.matcher(isoPeriod.toUpperCase(Locale.US));
        if (!m.matches()) return 0;
        return parse(m.group(1)) * 365 + parse(m.group(2)) * 30 + parse(m.group(3)) * 7 + parse(m.group(4));
    }

    /** Number of months in a billing period (P1Y = 12, P1M = 1, P3M = 3). */
    public static int periodMonths(@Nullable String isoPeriod) {
        if (isoPeriod == null) return 1;
        Matcher m = ISO_PERIOD.matcher(isoPeriod.toUpperCase(Locale.US));
        if (!m.matches()) return 1;
        int months = parse(m.group(1)) * 12 + parse(m.group(2));
        return Math.max(1, months);
    }

    /** Formats {@code micros} with the currency of {@code reference} (e.g. "$2.49"). */
    @NonNull
    public static String formatMicros(long micros, @NonNull ProductDetails.PricingPhase reference) {
        try {
            java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance();
            format.setCurrency(java.util.Currency.getInstance(reference.getPriceCurrencyCode()));
            return format.format(micros / 1_000_000.0);
        } catch (Exception e) {
            return String.format(Locale.getDefault(), "%.2f %s", micros / 1_000_000.0, reference.getPriceCurrencyCode());
        }
    }

    private static int parse(@Nullable String value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
