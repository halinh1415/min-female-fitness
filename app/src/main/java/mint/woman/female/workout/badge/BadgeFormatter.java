package mint.woman.female.workout.badge;

import android.content.Context;

import mint.woman.female.workout.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Chuyen tien do huy hieu thanh chuoi hien thi, co xu ly so it / so nhieu theo ngon ngu.
 */
public final class BadgeFormatter {

    private BadgeFormatter() {
    }

    /** Vi du: "12 / 30 days". */
    public static String progress(Context context, BadgeProgress badge) {
        return context.getString(
                R.string.badge_progress_format,
                badge.getCurrent(),
                badge.getTarget(),
                unit(context, badge, badge.getTarget())
        );
    }

    /** Vi du: "18 days to go". Tra ve null khi huy hieu da dat duoc. */
    public static String remaining(Context context, BadgeProgress badge) {
        if (badge.isUnlocked()) return null;
        int remaining = badge.getRemaining();
        return context.getString(R.string.badge_remaining, remaining, unit(context, badge, remaining));
    }

    public static String earnedOn(Context context, BadgeProgress badge) {
        if (!badge.isUnlocked()) return null;
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(new Date(badge.getUnlockedAt()));
        return context.getString(R.string.badge_earned_on, date);
    }

    private static String unit(Context context, BadgeProgress badge, int quantity) {
        BadgeMetric metric = badge.getMetric();
        if (metric == null) return "";
        return context.getResources().getQuantityString(metric.unitPluralRes, quantity);
    }
}
