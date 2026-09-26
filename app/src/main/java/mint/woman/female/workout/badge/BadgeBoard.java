package mint.woman.female.workout.badge;

import java.util.Collections;
import java.util.List;

/**
 * Ket qua mot lan tinh huy hieu: danh sach theo nhom + tong ket + nhung huy hieu vua mo khoa.
 */
public class BadgeBoard {

    private final List<BadgeSection> sections;
    private final List<BadgeProgress> newlyUnlocked;
    private final int unlockedCount;
    private final int totalCount;
    private final BadgeMetrics metrics;

    BadgeBoard(List<BadgeSection> sections,
               List<BadgeProgress> newlyUnlocked,
               int unlockedCount,
               int totalCount,
               BadgeMetrics metrics) {
        this.sections = sections;
        this.newlyUnlocked = newlyUnlocked;
        this.unlockedCount = unlockedCount;
        this.totalCount = totalCount;
        this.metrics = metrics;
    }

    static BadgeBoard empty() {
        return new BadgeBoard(Collections.emptyList(), Collections.emptyList(), 0, 0, new BadgeMetrics());
    }

    public List<BadgeSection> getSections() {
        return sections;
    }

    /** Huy hieu vua dat duoc trong lan tinh nay (lan dau tien vuot nguong). */
    public List<BadgeProgress> getNewlyUnlocked() {
        return newlyUnlocked;
    }

    public int getUnlockedCount() {
        return unlockedCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public BadgeMetrics getMetrics() {
        return metrics;
    }

    public int getPercent() {
        if (totalCount <= 0) return 0;
        return (int) Math.round(unlockedCount * 100.0 / totalCount);
    }

    /** Huy hieu gan dat duoc nhat va chua mo khoa - dung lam goi y tren man Report. */
    public BadgeProgress getClosestLockedBadge() {
        BadgeProgress best = null;
        for (BadgeSection section : sections) {
            for (BadgeProgress badge : section.getBadges()) {
                if (badge.isUnlocked()) continue;
                if (best == null || badge.getPercent() > best.getPercent()) {
                    best = badge;
                }
            }
        }
        return best;
    }
}
