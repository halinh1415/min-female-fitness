package mint.woman.female.workout.badge;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Mot huy hieu kem tien do hien tai cua user.
 */
public class BadgeProgress implements Parcelable {

    public static final Creator<BadgeProgress> CREATOR = new Creator<BadgeProgress>() {
        @Override
        public BadgeProgress createFromParcel(Parcel in) {
            return new BadgeProgress(in);
        }

        @Override
        public BadgeProgress[] newArray(int size) {
            return new BadgeProgress[size];
        }
    };

    private final String id;
    private final String title;
    private final String description;
    private final String hint;
    private final String iconAsset;
    private final BadgeTier tier;
    private final BadgeGlyph glyph;
    private final BadgeMetric metric;
    private final int target;
    private final int current;
    private final long unlockedAt;

    /**
     * @param iconAsset ten file anh da xac nhan co trong assets, hoac null de dung glyph
     */
    BadgeProgress(BadgeDefinition definition, int current, long unlockedAt, String iconAsset) {
        this.id = definition.getId();
        this.title = definition.getTitle();
        this.description = definition.getDescription();
        this.hint = definition.getHint();
        this.iconAsset = iconAsset;
        this.tier = definition.getTierType();
        this.glyph = definition.getGlyphType();
        this.metric = definition.getMetricType();
        this.target = definition.getTarget();
        this.current = current;
        this.unlockedAt = unlockedAt;
    }

    protected BadgeProgress(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        hint = in.readString();
        iconAsset = in.readString();
        tier = BadgeTier.values()[in.readInt()];
        glyph = BadgeGlyph.values()[in.readInt()];
        int metricOrdinal = in.readInt();
        metric = metricOrdinal < 0 ? null : BadgeMetric.values()[metricOrdinal];
        target = in.readInt();
        current = in.readInt();
        unlockedAt = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(hint);
        dest.writeString(iconAsset);
        dest.writeInt(tier.ordinal());
        dest.writeInt(glyph.ordinal());
        dest.writeInt(metric == null ? -1 : metric.ordinal());
        dest.writeInt(target);
        dest.writeInt(current);
        dest.writeLong(unlockedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getHint() {
        return hint;
    }

    /** Ten file trong assets/badges/icons/, hoac null neu dung hinh ve mac dinh. */
    public String getIconAsset() {
        return iconAsset;
    }

    public BadgeTier getTier() {
        return tier;
    }

    public BadgeGlyph getGlyph() {
        return glyph;
    }

    public BadgeMetric getMetric() {
        return metric;
    }

    public int getTarget() {
        return target;
    }

    /** Gia tri hien tai, da chan o muc target de hien thi "30 / 30" thay vi "41 / 30". */
    public int getCurrent() {
        return Math.min(current, target);
    }

    public int getRawCurrent() {
        return current;
    }

    public boolean isUnlocked() {
        return unlockedAt > 0L;
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }

    /** Tien do 0..100. */
    public int getPercent() {
        if (target <= 0) return 0;
        return Math.min(100, (int) Math.round(current * 100.0 / target));
    }

    public int getRemaining() {
        return Math.max(0, target - current);
    }
}
