package mint.woman.female.workout.badge;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import mint.woman.female.workout.R;

/**
 * Ve mot huy hieu len giao dien.
 *
 * <p>Co anh rieng trong {@code assets/badges/icons/} thi dung anh do; khong co thi ve glyph
 * mac dinh theo tier. Huy hieu chua dat duoc van hien dung hinh cua no, nhung bi xam hoa va
 * lam mo, kem o khoa - de user thay truoc phan thuong dang cho.</p>
 */
public final class BadgeIconBinder {

    private static final float LOCKED_ALPHA = 0.55f;

    /** Anh rieng chiem gan het mat huy hieu; glyph la net ve nen can vien rong hon. */
    private static final float ICON_PADDING_RATIO = 0.10f;
    private static final float GLYPH_PADDING_RATIO = 0.23f;

    private static final ColorMatrixColorFilter GREYSCALE = createGreyscaleFilter();

    private BadgeIconBinder() {
    }

    /**
     * @param medallion ImageView tron dung lam nen + noi dat hinh huy hieu
     * @param lockIcon  o khoa chong len huy hieu, co the null
     */
    public static void bind(BadgeProgress badge, ImageView medallion, View lockIcon) {
        Context context = medallion.getContext();
        boolean unlocked = badge.isUnlocked();

        int backgroundColor = ContextCompat.getColor(context,
                unlocked ? badge.getTier().lightColorRes : R.color.badge_locked_light);
        int foregroundColor = ContextCompat.getColor(context,
                unlocked ? badge.getTier().colorRes : R.color.badge_locked);

        medallion.setBackgroundResource(R.drawable.bg_badge_medallion);
        medallion.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        int size = medallionSize(medallion);
        Bitmap custom = loadCustomIcon(context, badge, size);

        if (custom != null) {
            applyPadding(medallion, size, ICON_PADDING_RATIO);
            medallion.setImageTintList(null);
            // Chua mo khoa -> xam hoa anh that thay vi giau no di.
            medallion.setColorFilter(unlocked ? null : GREYSCALE);
            medallion.setImageBitmap(custom);
        } else {
            applyPadding(medallion, size, GLYPH_PADDING_RATIO);
            medallion.setColorFilter(null);
            medallion.setImageTintList(ColorStateList.valueOf(foregroundColor));
            medallion.setImageResource(badge.getGlyph().drawableRes);
        }

        medallion.setAlpha(unlocked ? 1f : LOCKED_ALPHA);

        if (lockIcon != null) {
            lockIcon.setVisibility(unlocked ? View.GONE : View.VISIBLE);
            if (lockIcon instanceof ImageView) {
                ((ImageView) lockIcon).setImageTintList(ColorStateList.valueOf(foregroundColor));
            }
        }
    }

    private static Bitmap loadCustomIcon(Context context, BadgeProgress badge, int size) {
        String iconAsset = badge.getIconAsset();
        if (iconAsset == null) return null;

        int target = size > 0 ? Math.round(size * (1f - 2 * ICON_PADDING_RATIO)) : 0;
        return BadgeIconAssets.load(context, iconAsset, target);
    }

    /**
     * Canh vien theo do lon that cua huy hieu, de cung mot ham dung duoc cho ca o 44dp tren
     * man Report lan o 104dp trong hop thoai chuc mung. Luon set lai vi RecyclerView dung lai
     * view cu giua huy hieu co anh va huy hieu dung glyph.
     */
    private static void applyPadding(ImageView medallion, int size, float ratio) {
        if (size <= 0) return;
        int padding = Math.round(size * ratio);
        medallion.setPadding(padding, padding, padding, padding);
    }

    /** Kich thuoc co dinh khai bao trong layout, doc duoc ngay ca khi view chua do xong. */
    private static int medallionSize(ImageView medallion) {
        ViewGroup.LayoutParams params = medallion.getLayoutParams();
        if (params != null && params.width > 0) {
            return params.width;
        }
        return medallion.getWidth();
    }

    private static ColorMatrixColorFilter createGreyscaleFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        return new ColorMatrixColorFilter(matrix);
    }
}
