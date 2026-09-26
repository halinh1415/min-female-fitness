package mint.woman.female.workout.badge;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import mint.woman.female.workout.utils.HistoryDao;
import mint.woman.female.workout.utils.HistoryDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ghep noi dung huy hieu trong assets voi lich su tap cua user.
 *
 * <p>Toan bo viec doc file va truy van database chay tren thread nen; ket qua tra ve
 * tren main thread.</p>
 */
public final class BadgeRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private BadgeRepository() {
    }

    public interface Callback {
        void onResult(BadgeBoard board);
    }

    /**
     * Tinh toan bo bang huy hieu. Nhung huy hieu vua dat nguong lan dau se duoc ghi nhan
     * la da mo khoa va tra ve trong {@link BadgeBoard#getNewlyUnlocked()}.
     */
    public static void load(Context context, Callback callback) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            BadgeBoard board = loadBlocking(appContext);
            MAIN.post(() -> callback.onResult(board));
        });
    }

    /** Phai goi tu thread nen. */
    static BadgeBoard loadBlocking(Context context) {
        BadgeCatalog catalog = BadgeCatalogLoader.load(context);
        if (catalog.isEmpty()) {
            return BadgeBoard.empty();
        }

        HistoryDao dao = HistoryDatabase.getDatabase(context).historyDao();
        BadgeMetrics metrics = BadgeMetricsCalculator.calculate(dao);

        Map<String, BadgeCategory> categories = new LinkedHashMap<>();
        for (BadgeCategory category : catalog.getCategories()) {
            categories.put(category.getId(), category);
        }

        Map<String, List<BadgeProgress>> badgesByCategory = new LinkedHashMap<>();
        for (String categoryId : categories.keySet()) {
            badgesByCategory.put(categoryId, new ArrayList<>());
        }

        List<BadgeProgress> newlyUnlocked = new ArrayList<>();
        long now = System.currentTimeMillis();
        int unlockedCount = 0;

        for (BadgeDefinition definition : catalog.getBadges()) {
            int current = metrics.valueOf(definition.getMetricType());
            boolean reached = current >= definition.getTarget();

            long unlockedAt = BadgeUnlockStore.getUnlockedAt(context, definition.getId());
            boolean isNew = false;
            if (reached && unlockedAt <= 0L) {
                isNew = BadgeUnlockStore.markUnlocked(context, definition.getId(), now);
                unlockedAt = now;
            }

            BadgeProgress progress = new BadgeProgress(
                    definition,
                    current,
                    unlockedAt,
                    BadgeIconAssets.resolve(context, definition.getId(), definition.getIcon())
            );

            if (progress.isUnlocked()) unlockedCount++;
            if (isNew) newlyUnlocked.add(progress);

            List<BadgeProgress> bucket = badgesByCategory.get(definition.getCategory());
            if (bucket == null) {
                bucket = new ArrayList<>();
                badgesByCategory.put(definition.getCategory(), bucket);
            }
            bucket.add(progress);
        }

        List<BadgeSection> sections = new ArrayList<>();
        int totalCount = 0;
        for (Map.Entry<String, List<BadgeProgress>> entry : badgesByCategory.entrySet()) {
            List<BadgeProgress> badges = entry.getValue();
            if (badges.isEmpty()) continue;

            BadgeCategory category = categories.get(entry.getKey());
            sections.add(new BadgeSection(
                    entry.getKey(),
                    category != null ? category.getTitle() : entry.getKey(),
                    category != null ? category.getSubtitle() : null,
                    badges
            ));
            totalCount += badges.size();
        }

        return new BadgeBoard(sections, newlyUnlocked, unlockedCount, totalCount, metrics);
    }
}
