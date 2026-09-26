package mint.woman.female.workout.badge;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Luu thoi diem mo khoa cua tung huy hieu.
 *
 * <p>Trang thai mo khoa duoc luu rieng khoi noi dung trong assets: doi loi hay doi anh
 * cua mot huy hieu khong lam mat huy hieu user da dat. Doi {@code id} thi coi nhu huy
 * hieu moi.</p>
 */
public final class BadgeUnlockStore {

    private static final String PREF_NAME = "badge_pref";
    private static final String KEY_PREFIX_UNLOCKED_AT = "unlocked_at_";
    /** Danh dau huy hieu da duoc chuc mung, de khong hien lai man an mung nhieu lan. */
    private static final String KEY_PREFIX_CELEBRATED = "celebrated_";

    private BadgeUnlockStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static long getUnlockedAt(Context context, String badgeId) {
        return prefs(context).getLong(KEY_PREFIX_UNLOCKED_AT + badgeId, 0L);
    }

    public static boolean isUnlocked(Context context, String badgeId) {
        return getUnlockedAt(context, badgeId) > 0L;
    }

    /**
     * Ghi nhan huy hieu vua dat duoc.
     *
     * @return true neu day la lan dau tien - dung de quyet dinh co an mung hay khong.
     */
    public static boolean markUnlocked(Context context, String badgeId, long unlockedAt) {
        if (isUnlocked(context, badgeId)) return false;
        prefs(context).edit().putLong(KEY_PREFIX_UNLOCKED_AT + badgeId, unlockedAt).apply();
        return true;
    }

    public static boolean wasCelebrated(Context context, String badgeId) {
        return prefs(context).getBoolean(KEY_PREFIX_CELEBRATED + badgeId, false);
    }

    public static void markCelebrated(Context context, String badgeId) {
        prefs(context).edit().putBoolean(KEY_PREFIX_CELEBRATED + badgeId, true).apply();
    }

    /** Dung khi user xoa toan bo lich su tap. */
    public static void clearAll(Context context) {
        prefs(context).edit().clear().apply();
    }
}
