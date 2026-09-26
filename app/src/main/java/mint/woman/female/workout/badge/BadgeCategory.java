package mint.woman.female.workout.badge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Nhom huy hieu, doc tu assets/badges/badges.json.
 */
public class BadgeCategory {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("subtitle")
    @Expose
    private String subtitle;

    @SerializedName("order")
    @Expose
    private int order;

    public BadgeCategory() {
    }

    BadgeCategory(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getOrder() {
        return order;
    }

    /** Xem {@code BadgeDefinition#applyTranslation}: chi ghi de truong co noi dung. */
    void applyTranslation(BadgeCategory translation) {
        if (translation == null) return;
        if (notBlank(translation.title)) this.title = translation.title;
        if (notBlank(translation.subtitle)) this.subtitle = translation.subtitle;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
