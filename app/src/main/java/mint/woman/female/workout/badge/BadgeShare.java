package mint.woman.female.workout.badge;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import mint.woman.female.workout.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chia se mot huy hieu da dat duoc ra mang xa hoi: mot dong text kem tam anh huy hieu.
 *
 * <p>Anh khong phai la icon goc ma la mot tam the ve tu {@code view_badge_share_card.xml} -
 * co ten huy hieu, hang, ngay dat duoc va ten app - de nguoi xem hieu duoc noi dung ma khong
 * can chu thich them.</p>
 */
public final class BadgeShare {

    private static final String TAG = "BadgeShare";

    /** Trung voi cache-path khai bao trong res/xml/provider_paths.xml. */
    private static final String SHARE_DIR = "shared";
    private static final String AUTHORITY_SUFFIX = ".fileprovider";

    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";

    /** Be rong khai bao trong layout the chia se. */
    private static final int CARD_WIDTH_DP = 360;

    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private BadgeShare() {
    }

    /**
     * Mo bang chon ung dung de chia se huy hieu. Khong lam gi neu huy hieu chua dat duoc.
     *
     * <p>Ve bitmap phai o main thread (View khong dung duoc o thread khac), con nen PNG va ghi
     * file thi day sang thread nen de khong giu main thread.</p>
     */
    public static void share(@NonNull Activity activity, @NonNull BadgeProgress badge) {
        if (!badge.isUnlocked() || activity.isFinishing() || activity.isDestroyed()) return;

        final String message = buildMessage(activity, badge);
        final Bitmap card = renderCard(activity, badge);
        final Context appContext = activity.getApplicationContext();
        final String fileName = fileNameFor(badge);

        IO_EXECUTOR.execute(() -> {
            final Uri uri = card == null ? null : writeToCache(appContext, card, fileName);
            MAIN.post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                startChooser(activity, uri, message);
            });
        });
    }

    // ------------------------------------------------------------------ text

    /** Ten huy hieu + ten app + link tai app, de nguoi doc con biet duong tim den. */
    static String buildMessage(Context context, BadgeProgress badge) {
        StringBuilder text = new StringBuilder(context.getString(
                R.string.badge_share_message, badge.getTitle(), context.getString(R.string.app_name)));

        String description = badge.getDescription();
        if (!TextUtils.isEmpty(description)) {
            text.append('\n').append(description);
        }

        String earnedOn = BadgeFormatter.earnedOn(context, badge);
        if (!TextUtils.isEmpty(earnedOn)) {
            text.append('\n').append(earnedOn);
        }

        return text.append("\n\n")
                .append(PLAY_STORE_URL)
                .append(context.getPackageName())
                .toString();
    }

    // ------------------------------------------------------------------ anh

    /** Do va ve the chia se ra bitmap. Tra ve null neu khong dung duoc anh. */
    @Nullable
    private static Bitmap renderCard(Context context, BadgeProgress badge) {
        try {
            View card = LayoutInflater.from(context).inflate(R.layout.view_badge_share_card, null);
            bindCard(context, card, badge);

            int widthPx = Math.round(
                    CARD_WIDTH_DP * context.getResources().getDisplayMetrics().density);
            card.measure(
                    View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            card.layout(0, 0, card.getMeasuredWidth(), card.getMeasuredHeight());

            if (card.getMeasuredWidth() <= 0 || card.getMeasuredHeight() <= 0) return null;

            Bitmap bitmap = Bitmap.createBitmap(
                    card.getMeasuredWidth(), card.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            // Mot so app chat dat anh trong suot len nen toi; to nen trang cho chac.
            canvas.drawColor(Color.WHITE);
            card.draw(canvas);
            return bitmap;
        } catch (Throwable t) {
            Log.w(TAG, "Khong ve duoc the chia se: " + t.getMessage());
            return null;
        }
    }

    private static void bindCard(Context context, View card, BadgeProgress badge) {
        ImageView medallion = card.findViewById(R.id.img_badge);
        BadgeIconBinder.bind(badge, medallion, null);

        TextView tier = card.findViewById(R.id.tv_badge_tier);
        tier.setText(badge.getTier().labelRes);
        tier.setTextColor(ContextCompat.getColor(context, badge.getTier().colorRes));

        ((TextView) card.findViewById(R.id.tv_badge_title)).setText(badge.getTitle());
        setOrHide(card.findViewById(R.id.tv_badge_description), badge.getDescription());
        setOrHide(card.findViewById(R.id.tv_badge_status), BadgeFormatter.earnedOn(context, badge));
    }

    private static void setOrHide(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }

    /** Ghi anh vao cache roi doi thanh content:// - file:// khong gui qua app khac duoc. */
    @Nullable
    private static Uri writeToCache(Context context, Bitmap bitmap, String fileName) {
        File dir = new File(context.getCacheDir(), SHARE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "Khong tao duoc thu muc chia se");
            return null;
        }

        File file = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            Log.w(TAG, "Khong ghi duoc anh chia se: " + e.getMessage());
            return null;
        }

        try {
            return FileProvider.getUriForFile(
                    context, context.getPackageName() + AUTHORITY_SUFFIX, file);
        } catch (Exception e) {
            Log.w(TAG, "Khong lay duoc uri chia se: " + e.getMessage());
            return null;
        }
    }

    private static String fileNameFor(BadgeProgress badge) {
        String id = badge.getId();
        String safe = TextUtils.isEmpty(id) ? "badge" : id.replaceAll("[^A-Za-z0-9_-]", "_");
        return "badge_" + safe + ".png";
    }

    // ------------------------------------------------------------------ intent

    /** Khong co anh thi van chia se phan text - mat anh con hon khong chia se duoc gi. */
    private static void startChooser(Activity activity, @Nullable Uri uri, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.badge_share_chooser));

        if (uri == null) {
            intent.setType("text/plain");
        } else {
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            // ClipData de cac app doc quyen tu uri thay vi chi doc EXTRA_STREAM.
            intent.setClipData(ClipData.newRawUri("", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            activity.startActivity(Intent.createChooser(
                    intent, activity.getString(R.string.badge_share_chooser)));
        } catch (Exception e) {
            Log.w(TAG, "Khong mo duoc bang chon chia se: " + e.getMessage());
        }
    }
}
