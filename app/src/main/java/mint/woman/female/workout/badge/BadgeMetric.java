package mint.woman.female.workout.badge;

import mint.woman.female.workout.R;

import java.util.Locale;

/**
 * Cac chi so ma huy hieu co the do. Them chi so moi = them mot hang o day
 * va tinh gia tri tuong ung trong {@link BadgeMetricsCalculator}.
 *
 * <p>Ten trong JSON la dang chu thuong co gach duoi, vi du {@code "total_sessions"}.</p>
 */
public enum BadgeMetric {
    TOTAL_SESSIONS(R.plurals.badge_unit_session),
    TOTAL_DAYS(R.plurals.badge_unit_day),
    TOTAL_MINUTES(R.plurals.badge_unit_minute),
    TOTAL_CALORIES(R.plurals.badge_unit_kcal),
    CURRENT_STREAK(R.plurals.badge_unit_day),
    BEST_STREAK(R.plurals.badge_unit_day),
    DAWN_SESSIONS(R.plurals.badge_unit_session),
    EVENING_SESSIONS(R.plurals.badge_unit_session),
    WEEKEND_SESSIONS(R.plurals.badge_unit_session),
    LONG_SESSIONS(R.plurals.badge_unit_session),
    SINGLE_DAY_MINUTES(R.plurals.badge_unit_minute),
    DISTINCT_PROGRAMS(R.plurals.badge_unit_program),
    ACTIVE_WEEKS(R.plurals.badge_unit_week),
    ACTIVE_MONTHS(R.plurals.badge_unit_month);

    /** Don vi hien thi kem tien do, vi du "12 / 30 days". */
    public final int unitPluralRes;

    BadgeMetric(int unitPluralRes) {
        this.unitPluralRes = unitPluralRes;
    }

    /** @return null neu JSON ghi mot chi so app khong ho tro - huy hieu do se bi bo qua. */
    public static BadgeMetric from(String value) {
        if (value == null) return null;
        String key = value.trim().replace('-', '_');
        for (BadgeMetric metric : values()) {
            if (metric.name().equalsIgnoreCase(key)) {
                return metric;
            }
        }
        return null;
    }

    public String jsonValue() {
        return name().toLowerCase(Locale.US);
    }
}
