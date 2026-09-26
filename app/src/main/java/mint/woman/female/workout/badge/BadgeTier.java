package mint.woman.female.workout.badge;

import androidx.annotation.ColorRes;

import mint.woman.female.workout.R;

import java.util.Locale;

/**
 * Bac cua huy hieu - chi anh huong toi mau sac hien thi.
 * Gia tri doc tu truong {@code tier} trong assets/badges/badges.json.
 */
public enum BadgeTier {
    BRONZE(R.color.badge_bronze, R.color.badge_bronze_light, R.string.badge_tier_bronze),
    SILVER(R.color.badge_silver, R.color.badge_silver_light, R.string.badge_tier_silver),
    GOLD(R.color.badge_gold, R.color.badge_gold_light, R.string.badge_tier_gold),
    JADE(R.color.badge_jade, R.color.badge_jade_light, R.string.badge_tier_jade);

    @ColorRes
    public final int colorRes;
    @ColorRes
    public final int lightColorRes;
    public final int labelRes;

    BadgeTier(@ColorRes int colorRes, @ColorRes int lightColorRes, int labelRes) {
        this.colorRes = colorRes;
        this.lightColorRes = lightColorRes;
        this.labelRes = labelRes;
    }

    /** Gia tri la nay trong JSON se roi ve {@link #BRONZE} thay vi lam hong ca danh sach. */
    public static BadgeTier from(String value) {
        if (value != null) {
            for (BadgeTier tier : values()) {
                if (tier.name().equalsIgnoreCase(value.trim())) {
                    return tier;
                }
            }
        }
        return BRONZE;
    }

    public String jsonValue() {
        return name().toLowerCase(Locale.US);
    }
}
