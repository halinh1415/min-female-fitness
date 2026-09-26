package mint.woman.female.workout.activity.ui.badge;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import mint.woman.female.workout.R;
import mint.woman.female.workout.admodule.AppOpenManager;
import mint.woman.female.workout.badge.BadgeFormatter;
import mint.woman.female.workout.badge.BadgeIconBinder;
import mint.woman.female.workout.badge.BadgeProgress;
import mint.woman.female.workout.badge.BadgeShare;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

/**
 * Chi tiet mot huy hieu: mo ta, goi y va tien do hien tai.
 */
public final class BadgeDetailDialog {

    private BadgeDetailDialog() {
    }

    public static void show(@NonNull Context context, @NonNull BadgeProgress badge) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = View.inflate(context, R.layout.dialog_badge_detail, null);
        dialog.setContentView(view);

        ImageView medallion = view.findViewById(R.id.img_badge);
        ImageView lock = view.findViewById(R.id.img_badge_lock);
        BadgeIconBinder.bind(badge, medallion, lock);

        TextView tier = view.findViewById(R.id.tv_badge_tier);
        tier.setText(badge.getTier().labelRes);
        tier.setTextColor(ContextCompat.getColor(context, badge.getTier().colorRes));

        ((TextView) view.findViewById(R.id.tv_badge_title)).setText(badge.getTitle());
        setOrHide(view.findViewById(R.id.tv_badge_description), badge.getDescription());
        setOrHide(view.findViewById(R.id.tv_badge_hint), badge.isUnlocked() ? null : badge.getHint());

        ProgressBar progressBar = view.findViewById(R.id.progress_badge);
        progressBar.setProgress(badge.getPercent());
        progressBar.setProgressTintList(
                ContextCompat.getColorStateList(context, badge.getTier().colorRes));

        ((TextView) view.findViewById(R.id.tv_badge_progress))
                .setText(BadgeFormatter.progress(context, badge));

        TextView status = view.findViewById(R.id.tv_badge_status);
        String statusText = badge.isUnlocked()
                ? BadgeFormatter.earnedOn(context, badge)
                : BadgeFormatter.remaining(context, badge);
        setOrHide(status, statusText);

        bindShare(context, view.findViewById(R.id.btn_badge_share), badge, dialog);

        dialog.show();
    }

    /**
     * Chi huy hieu da dat duoc moi chia se duoc - khoe mot huy hieu chua mo thi vo nghia.
     * Can Activity de mo bang chon, nen dialog goi tu Context khac se an nut di.
     */
    private static void bindShare(Context context, MaterialButton share,
                                  BadgeProgress badge, BottomSheetDialog dialog) {
        Activity activity = activityOf(context);
        if (!badge.isUnlocked() || activity == null) {
            share.setVisibility(View.GONE);
            return;
        }

        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(v -> {
            // Roi app sang bang chon chia se khong phai la vao nen, dung hien quang cao khi quay lai.
            AppOpenManager.getInstance().disableAdResumeByClickAction();
            BadgeShare.share(activity, badge);
            dialog.dismiss();
        });
    }

    @Nullable
    private static Activity activityOf(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static void setOrHide(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }
}
