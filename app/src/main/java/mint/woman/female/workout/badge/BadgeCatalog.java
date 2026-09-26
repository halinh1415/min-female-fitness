package mint.woman.female.workout.badge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Toan bo noi dung huy hieu doc tu assets/badges/badges.json.
 */
public class BadgeCatalog {

    @SerializedName("version")
    @Expose
    private int version;

    @SerializedName("categories")
    @Expose
    private List<BadgeCategory> categories;

    @SerializedName("badges")
    @Expose
    private List<BadgeDefinition> badges;

    public int getVersion() {
        return version;
    }

    public List<BadgeCategory> getCategories() {
        return categories != null ? categories : Collections.emptyList();
    }

    public List<BadgeDefinition> getBadges() {
        return badges != null ? badges : Collections.emptyList();
    }

    public boolean isEmpty() {
        return getBadges().isEmpty();
    }

    /**
     * Bo cac huy hieu thieu du lieu hoac dung chi so app khong hieu, sap xep theo
     * {@code order} cua nhom roi den {@code order} cua tung huy hieu, va tu tao nhom
     * cho nhung huy hieu tro toi category chua khai bao.
     */
    BadgeCatalog normalized() {
        BadgeCatalog result = new BadgeCatalog();
        result.version = version;

        Map<String, BadgeCategory> categoryById = new LinkedHashMap<>();
        List<BadgeCategory> sortedCategories = new ArrayList<>(getCategories());
        Collections.sort(sortedCategories, (a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        for (BadgeCategory category : sortedCategories) {
            if (category != null && category.getId() != null) {
                categoryById.put(category.getId(), category);
            }
        }

        List<BadgeDefinition> validBadges = new ArrayList<>();
        for (BadgeDefinition badge : getBadges()) {
            if (badge == null || !badge.isValid()) continue;
            validBadges.add(badge);

            String categoryId = badge.getCategory();
            if (categoryId != null && !categoryById.containsKey(categoryId)) {
                // Category chua khai bao -> tu tao mot nhom de huy hieu van hien thi duoc.
                categoryById.put(categoryId, new BadgeCategory(categoryId, categoryId));
            }
        }

        List<String> categoryOrder = new ArrayList<>(categoryById.keySet());
        Collections.sort(validBadges, (a, b) -> {
            int categoryDiff = Integer.compare(
                    indexOfCategory(categoryOrder, a.getCategory()),
                    indexOfCategory(categoryOrder, b.getCategory())
            );
            if (categoryDiff != 0) return categoryDiff;
            return Integer.compare(a.getOrder(), b.getOrder());
        });

        result.categories = new ArrayList<>(categoryById.values());
        result.badges = validBadges;
        return result;
    }

    /**
     * Ghep ban dich vao catalog goc. File ban dich chi can chua {@code id} + phan chu;
     * cau truc (metric, target, tier, thu tu) luon lay tu badges.json de khong phai
     * dong bo so lieu o nhieu file.
     *
     * <p>Muc nao thieu trong ban dich thi giu nguyen tieng Anh.</p>
     */
    void applyTranslation(BadgeCatalog translation) {
        if (translation == null) return;

        Map<String, BadgeCategory> translatedCategories = new LinkedHashMap<>();
        for (BadgeCategory category : translation.getCategories()) {
            if (category != null && category.getId() != null) {
                translatedCategories.put(category.getId(), category);
            }
        }
        for (BadgeCategory category : getCategories()) {
            category.applyTranslation(translatedCategories.get(category.getId()));
        }

        Map<String, BadgeDefinition> translatedBadges = new LinkedHashMap<>();
        for (BadgeDefinition badge : translation.getBadges()) {
            if (badge != null && badge.getId() != null) {
                translatedBadges.put(badge.getId(), badge);
            }
        }
        for (BadgeDefinition badge : getBadges()) {
            badge.applyTranslation(translatedBadges.get(badge.getId()));
        }
    }

    private static int indexOfCategory(List<String> order, String categoryId) {
        int index = categoryId == null ? -1 : order.indexOf(categoryId);
        return index < 0 ? Integer.MAX_VALUE : index;
    }
}
