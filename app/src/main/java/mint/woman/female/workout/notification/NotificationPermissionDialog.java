package mint.woman.female.workout.notification;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.MyPreference;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class NotificationPermissionDialog {

    public static final int REQ_POST_NOTIFICATIONS = 1001;

    private NotificationPermissionDialog() {
    }

    public static void showPracticeReminderDialog(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationScheduler.schedule(activity);
            return;
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            NotificationScheduler.schedule(activity);
            return;
        }

        // Use custom dialog layout for a branded reminder prompt.
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_request_notification, null, false);

        final androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
       MaterialButton btnEnableReminder =
                dialogView.findViewById(R.id.btnEnableReminder);
        MaterialButton btnLater =
                dialogView.findViewById(R.id.btnLater);

        btnLater.setOnClickListener(v -> dialog.dismiss());
        btnEnableReminder.setOnClickListener(v -> {
            dialog.dismiss();
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIFICATIONS
            );
        });

        dialog.show();
    }

    public static void handlePermissionResult(
            @NonNull Activity activity,
            int requestCode,
            @NonNull int[] grantResults
    ) {
        if (requestCode != REQ_POST_NOTIFICATIONS) return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            NotificationScheduler.schedule(activity);
            MyPreference.newInstance(activity).setReminder(true);
            showSuccessDialog(activity);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                );
                if (!showRationale) {
                    showGoToSettingsDialog(activity);
                }
            }
        }
    }

    private static void showSuccessDialog(@NonNull Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.notifications_enabled))
                .setMessage(activity.getString(R.string.notifications_enabled_message))
                .setPositiveButton("OK", null)
                .show();
    }

    private static void showGoToSettingsDialog(@NonNull Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.notifications_blocked))
                .setMessage(activity.getString(R.string.notifications_blocked_message))
                .setNegativeButton(activity.getString(R.string.later), null)
                .setPositiveButton(activity.getString(R.string.open_settings), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                    activity.startActivity(intent);
                })
                .show();
    }
}