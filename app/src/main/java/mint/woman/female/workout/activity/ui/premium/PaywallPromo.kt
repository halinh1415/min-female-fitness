package mint.woman.female.workout.activity.ui.premium

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.utils.RemoteConfigKeys
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Ưu đãi của paywall theo "bậc thang" (dùng chung logic với WomenWorkout / Home / Karate / Taekwondo):
 *
 * 1. Sau onboarding: LuckyDraw 50% đúng một lần ([startNow]).
 * 2. [paywall_promo_min_days] ngày đầu (tính từ lúc cài / LuckyDraw): chỉ giá gốc.
 * 3. Mở paywall từ lần thứ [paywall_offer_rotate_open_count]: chào 34% hoặc Trọn đời ([stickyPick], giữ 7 ngày).
 * 4. Đợt 50% (2 tiếng) theo hành vi: mở paywall lần thứ [paywall_promo_open_count] trở đi, cách đợt
 *    trước ≥ [paywall_promo_cooldown_days] ngày, tối đa [paywall_promo_max_per_month] đợt / 30 ngày.
 * 5. Quay lại sau ≥ [paywall_promo_winback_days] ngày không mở app: lần vào paywall kế tiếp được một đợt
 *    (bỏ qua thời gian chờ, vẫn tính vào giới hạn tháng).
 *
 * Nơi gọi chỉ mở đợt 50% khi Google Play thật sự có base plan giảm 50%.
 */
object PaywallPromo {

    private const val KEY_END_TIME = "PROMO_END_TIME"
    private const val KEY_LAST_START = "PROMO_LAST_START"
    private const val KEY_STARTS = "PROMO_STARTS"
    private const val KEY_OPEN_COUNT = "PAYWALL_OPEN_COUNT"
    private const val KEY_LUCKY_DRAW_AT = "LUCKY_DRAW_AT"
    private const val KEY_LAST_ACTIVE = "APP_LAST_ACTIVE"
    private const val KEY_WINBACK = "PROMO_WINBACK_PENDING"
    private const val KEY_ROTATE_PICK = "PAYWALL_ROTATE_PICK"
    private const val KEY_ROTATE_PICK_AT = "PAYWALL_ROTATE_PICK_AT"
    private val DURATION = TimeUnit.HOURS.toMillis(2)
    private val DAY = TimeUnit.DAYS.toMillis(1)
    private val MONTH = TimeUnit.DAYS.toMillis(30)
    private val PICK_TTL = TimeUnit.DAYS.toMillis(7)

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
    private fun rc(key: String) = FirebaseRemoteConfig.getInstance().getLong(key)

    /** Thời điểm kết thúc đợt 50% đang chạy, null nếu không có. */
    @JvmStatic
    fun activeEndTime(context: Context, now: Long = System.currentTimeMillis()): Long? =
        prefs(context).getLong(KEY_END_TIME, 0L).takeIf { it > now }

    @JvmStatic
    fun clear(context: Context) {
        prefs(context).edit().putLong(KEY_END_TIME, 0L).apply()
    }

    /** Mỗi lần mở paywall (không tính lần sau onboarding, người đã mua). @return số lần đã mở. */
    @JvmStatic
    fun recordOpen(context: Context): Int {
        val prefs = prefs(context)
        val count = prefs.getInt(KEY_OPEN_COUNT, 0) + 1
        prefs.edit().putInt(KEY_OPEN_COUNT, count).apply()
        return count
    }

    /** LuckyDraw trúng thưởng: mở đợt 2 tiếng ngay. @return thời điểm kết thúc. */
    @JvmStatic
    fun startNow(context: Context, now: Long = System.currentTimeMillis()): Long {
        activeEndTime(context, now)?.let { return it }
        prefs(context).edit().putLong(KEY_LUCKY_DRAW_AT, now).apply()
        return start(context, now)
    }

    private fun start(context: Context, now: Long): Long {
        val prefs = prefs(context)
        val starts = (prefs.getString(KEY_STARTS, "").orEmpty().split(',').mapNotNull { it.toLongOrNull() } + now)
            .filter { now - it < MONTH }
        prefs.edit()
            .putLong(KEY_END_TIME, now + DURATION)
            .putLong(KEY_LAST_START, now)
            .putString(KEY_STARTS, starts.joinToString(","))
            .putBoolean(KEY_WINBACK, false)
            .apply()
        return now + DURATION
    }

    /**
     * Gọi ở màn chính mỗi lần app trở lại foreground: ghi lần hoạt động cuối, vắng mặt đủ lâu thì
     * đánh dấu lần vào paywall tới được tặng một đợt 50% (win-back).
     */
    @JvmStatic
    fun markAppActive(context: Context, now: Long = System.currentTimeMillis()) {
        val prefs = prefs(context)
        val last = prefs.getLong(KEY_LAST_ACTIVE, 0L)
        val winbackDays = rc(RemoteConfigKeys.PAYWALL_PROMO_WINBACK_DAYS)
        val editor = prefs.edit().putLong(KEY_LAST_ACTIVE, now)
        if (last > 0 && winbackDays > 0 && now - last >= winbackDays * DAY) editor.putBoolean(KEY_WINBACK, true)
        editor.apply()
    }

    /** Lần mở paywall thứ [openCount] (đã biết Play có giá 50%): đủ điều kiện thì mở đợt mới. */
    @JvmStatic
    fun maybeStart(context: Context, openCount: Int, now: Long = System.currentTimeMillis()): Boolean {
        val threshold = rc(RemoteConfigKeys.PAYWALL_PROMO_OPEN_COUNT)
        if (threshold <= 0 || activeEndTime(context, now) != null) return false
        val prefs = prefs(context)
        val starts = prefs.getString(KEY_STARTS, "").orEmpty().split(',').mapNotNull { it.toLongOrNull() }
        val maxPerMonth = rc(RemoteConfigKeys.PAYWALL_PROMO_MAX_PER_MONTH)
        if (maxPerMonth > 0 && starts.count { now - it < MONTH } >= maxPerMonth) return false
        if (prefs.getBoolean(KEY_WINBACK, false)) {
            start(context, now)
            return true
        }
        if (openCount < threshold) return false
        val installed = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrDefault(0L)
        val since = maxOf(installed, prefs.getLong(KEY_LUCKY_DRAW_AT, 0L))
        if (now - since < rc(RemoteConfigKeys.PAYWALL_PROMO_MIN_DAYS) * DAY) return false
        if (now - prefs.getLong(KEY_LAST_START, 0L) < rc(RemoteConfigKeys.PAYWALL_PROMO_COOLDOWN_DAYS) * DAY) return false
        start(context, now)
        return true
    }

    /**
     * Ưu đãi xoay vòng đã bốc cho người dùng này (PaywallPlan.LIFETIME hoặc SPECIAL): bốc một lần rồi
     * giữ cố định 7 ngày để giá không đổi qua lại giữa các lần mở và số liệu hai ưu đãi không lẫn.
     */
    @JvmStatic
    fun stickyPick(context: Context, now: Long = System.currentTimeMillis()): Int {
        val prefs = prefs(context)
        val saved = prefs.getInt(KEY_ROTATE_PICK, -1)
        if ((saved == PaywallPlan.LIFETIME || saved == PaywallPlan.SPECIAL) &&
            now - prefs.getLong(KEY_ROTATE_PICK_AT, 0L) < PICK_TTL
        ) return saved
        val pick = if (Random.nextBoolean()) PaywallPlan.LIFETIME else PaywallPlan.SPECIAL
        prefs.edit().putInt(KEY_ROTATE_PICK, pick).putLong(KEY_ROTATE_PICK_AT, now).apply()
        return pick
    }

    /** Đã mở đủ số lần mà chưa mua: chào ngẫu nhiên Trọn đời hoặc giảm 34%. */
    @JvmStatic
    fun shouldRotate(openCount: Int): Boolean {
        val threshold = rc(RemoteConfigKeys.PAYWALL_OFFER_ROTATE_OPEN_COUNT)
        return threshold > 0 && openCount >= threshold
    }
}
