package mint.woman.female.workout.badge;

import androidx.annotation.DrawableRes;

import mint.woman.female.workout.R;

/**
 * Hinh ve mac dinh ben trong huy hieu khi JSON khong chi dinh file anh rieng.
 * Gia tri doc tu truong {@code glyph} trong assets/badges/badges.json.
 */
public enum BadgeGlyph {
    ROOT(R.drawable.ic_badge_root),
    BAMBOO(R.drawable.ic_badge_bamboo),
    SUN(R.drawable.ic_badge_sun),
    MOON(R.drawable.ic_badge_moon),
    BAGUA(R.drawable.ic_badge_bagua),
    PATH(R.drawable.ic_badge_path);

    @DrawableRes
    public final int drawableRes;

    BadgeGlyph(@DrawableRes int drawableRes) {
        this.drawableRes = drawableRes;
    }

    public static BadgeGlyph from(String value) {
        if (value != null) {
            for (BadgeGlyph glyph : values()) {
                if (glyph.name().equalsIgnoreCase(value.trim())) {
                    return glyph;
                }
            }
        }
        return BAGUA;
    }
}
