package mint.woman.female.workout.utils;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gửi thông tin các ANR gần đây lên Crashlytics (Android 11+).
 *
 * <p>Play Console chỉ báo "android.os.MessageQueue.nativePollOnce – Root cause for this ANR is
 * unknown": lúc chụp stack main thread đang rảnh, nên không biết cái gì đã lỡ hạn. Hệ thống vẫn
 * lưu lại lý do (ApplicationExitInfo): phần mô tả và đầu file trace có dòng "Subject:" cho biết
 * chính xác loại ANR - ví dụ "Broadcast of Intent { act=android.intent.action.BOOT_COMPLETED }",
 * "executing service …SystemJobService", "Input dispatching timed out (… no focused window)".</p>
 *
 * <p>Chạy ở luồng nền lúc mở app; mỗi ANR chỉ gửi một lần.</p>
 */
public final class AnrReporter {

    private static final String TAG = "AnrReporter";
    private static final String KEY_LAST_REPORTED = "anr_last_reported_timestamp";
    /** Đầu file trace đủ để có Subject + stack main thread + vài luồng khác. */
    private static final int MAX_TRACE_CHARS = 12_000;

    private AnrReporter() {
    }

    public static void reportRecentAnrs(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return;
        try {
            report(context.getApplicationContext());
        } catch (Throwable t) {
            Log.w(TAG, "Unable to read exit reasons", t);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static void report(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastReported = prefs.getLong(KEY_LAST_REPORTED, 0L);
        long newest = lastReported;

        List<ApplicationExitInfo> exits = am.getHistoricalProcessExitReasons(context.getPackageName(), 0, 10);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        for (ApplicationExitInfo info : exits) {
            if (info.getReason() != ApplicationExitInfo.REASON_ANR) continue;
            if (info.getTimestamp() <= lastReported) continue;
            newest = Math.max(newest, info.getTimestamp());

            String description = String.valueOf(info.getDescription());
            String trace = readTraceHead(info);
            crashlytics.setCustomKey("anr_description", description);
            crashlytics.setCustomKey("anr_importance", info.getImportance());
            crashlytics.setCustomKey("anr_timestamp", info.getTimestamp());
            crashlytics.setCustomKey("anr_process", String.valueOf(info.getProcessName()));
            for (String line : trace.split("\n")) {
                crashlytics.log(line);
            }
            crashlytics.recordException(new AnrReport(description, subjectOf(trace)));
        }
        if (newest > lastReported) {
            prefs.edit().putLong(KEY_LAST_REPORTED, newest).apply();
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static String readTraceHead(ApplicationExitInfo info) {
        try (InputStream in = info.getTraceInputStream()) {
            if (in == null) return "";
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && sb.length() < MAX_TRACE_CHARS) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Dòng "Subject: ..." trong file trace (loại ANR), để nhóm lỗi trên Crashlytics. */
    private static String subjectOf(String trace) {
        for (String line : trace.split("\n")) {
            if (line.startsWith("Subject:")) return line.substring("Subject:".length()).trim();
        }
        return "unknown";
    }

    /** Non-fatal để ANR hiện trong Crashlytics, tiêu đề là loại ANR. */
    static final class AnrReport extends Exception {
        AnrReport(String description, String subject) {
            super("ANR: " + subject + " | " + description);
        }
    }
}
