package mint.woman.female.workout.badge;

import java.util.List;

/**
 * Mot nhom huy hieu cung {@code category} trong file assets.
 */
public class BadgeSection {

    private final String id;
    private final String title;
    private final String subtitle;
    private final List<BadgeProgress> badges;

    BadgeSection(String id, String title, String subtitle, List<BadgeProgress> badges) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.badges = badges;
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

    public List<BadgeProgress> getBadges() {
        return badges;
    }

    public int getUnlockedCount() {
        int count = 0;
        for (BadgeProgress badge : badges) {
            if (badge.isUnlocked()) count++;
        }
        return count;
    }
}
