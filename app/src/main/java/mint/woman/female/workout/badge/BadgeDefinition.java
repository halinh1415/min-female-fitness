package mint.woman.female.workout.badge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mot huy hieu, doc nguyen van tu assets/badges/badges.json.
 */
public class BadgeDefinition {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("category")
    @Expose
    private String category;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("description")
    @Expose
    private String description;

    /** Cau goi y hien khi huy hieu con khoa. Co the bo trong. */
    @SerializedName("hint")
    @Expose
    private String hint;

    @SerializedName("metric")
    @Expose
    private String metric;

    @SerializedName("target")
    @Expose
    private int target;

    @SerializedName("tier")
    @Expose
    private String tier;

    @SerializedName("glyph")
    @Expose
    private String glyph;

    /** Ten file trong assets/badges/icons/. Bo trong -> ve huy hieu mac dinh theo tier + glyph. */
    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("order")
    @Expose
    private int order;

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Doi chu sang ban dich. Chi ghi de nhung truong co noi dung, de mot huy hieu chua
     * dich xong van giu nguyen tieng Anh thay vi hien ra o trong.
     */
    void applyTranslation(BadgeDefinition translation) {
        if (translation == null) return;
        if (notBlank(translation.title)) this.title = translation.title;
        if (notBlank(translation.description)) this.description = translation.description;
        if (notBlank(translation.hint)) this.hint = translation.hint;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
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

    public String getMetric() {
        return metric;
    }

    public int getTarget() {
        return target;
    }

    public String getIcon() {
        return icon;
    }

    public int getOrder() {
        return order;
    }

    public BadgeMetric getMetricType() {
        return BadgeMetric.from(metric);
    }

    public BadgeTier getTierType() {
        return BadgeTier.from(tier);
    }

    public BadgeGlyph getGlyphType() {
        return BadgeGlyph.from(glyph);
    }

    /**
     * Huy hieu chi dung duoc khi co id, ten va mot chi so app hieu duoc.
     * JSON sai o mot huy hieu se khong lam hong nhung huy hieu con lai.
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty()
                && title != null && !title.trim().isEmpty()
                && getMetricType() != null
                && target > 0;
    }
}
