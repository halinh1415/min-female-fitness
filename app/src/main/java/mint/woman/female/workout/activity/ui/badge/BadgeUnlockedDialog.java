package mint.woman.female.workout.activity.ui.badge;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import mint.woman.female.workout.R;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.badge.BadgeIconBinder;
import mint.woman.female.workout.badge.BadgeProgress;
import mint.woman.female.workout.badge.BadgeShare;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Man an mung khi user vua dat duoc huy hieu moi.
 *
 * <p>Neu mot luc dat nhieu huy hieu thi hien huy hieu dau tien va ghi chu so con lai,
 * tranh dap lien tiep nhieu dialog vao mat user.</p>
 */
public final class BadgeUnlockedDialog {

    private BadgeUnlockedDialog() {
    }

    public interface OnViewAllListener {
        void onViewAll();
    }

    public static void show(@NonNull Activity activity,
                            @NonNull List<BadgeProgress> unlocked,
                            OnViewAllListener viewAllListener) {
        if (unlocked.isEmpty() || activity.isFinishing()) return;

        BadgeProgress primary = unlocked.get(0);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_badge_unlocked);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        BadgeIconBinder.bind(primary, dialog.findViewById(R.id.img_badge), null);
        ((TextView) dialog.findViewById(R.id.tv_badge_title)).setText(primary.getTitle());

        TextView description = dialog.findViewById(R.id.tv_badge_description);
        if (TextUtils.isEmpty(primary.getDescription())) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(primary.getDescription());
        }

        TextView more = dialog.findViewById(R.id.tv_badge_more);
        if (unlocked.size() > 1) {
            more.setVisibility(View.VISIBLE);
            more.setText(activity.getString(
                    R.string.badge_unlocked_celebration_more, unlocked.size() - 1));
        }

        MaterialButton viewAll = dialog.findViewById(R.id.btn_badge_view_all);
        if (viewAllListener == null) {
            viewAll.setVisibility(View.GONE);
        } else {
            viewAll.setOnClickListener(v -> {
                dialog.dismiss();
                viewAllListener.onViewAll();
            });
        }

        bindShare(activity, dialog.findViewById(R.id.btn_badge_share), primary);

        MaterialButton continueButton = dialog.findViewById(R.id.btn_badge_continue);
        continueButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Chia se huy hieu vua dat duoc. Khong dong dialog sau khi chia se: user quay ve tu bang
     * chon van con man an mung de bam "Tiep tuc" hay "Xem tat ca".
     */
    private static void bindShare(Activity activity, MaterialButton share, BadgeProgress badge) {
        if (!badge.isUnlocked()) {
            share.setVisibility(View.GONE);
            return;
        }

        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(v -> {
            // Roi app sang bang chon chia se khong phai la vao nen, dung hien quang cao khi quay lai.
            AppOpenManager.getInstance().disableAdResumeByClickAction();
            BadgeShare.share(activity, badge);
        });
    }
}
