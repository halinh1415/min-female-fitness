package mint.woman.female.workout.utils;

import mint.woman.female.workout.model.ProgramObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProgramRecommenderV3 {

    public static final String TIME_MORNING = "morning";
    public static final String TIME_AFTERNOON = "afternoon";
    public static final String TIME_EVENING = "evening";
    public static final String TIME_ANY = "any";

    // =========================
    // PUBLIC API
    // =========================

    /**
     * Gợi ý bài tập cá nhân hóa cho người dùng (Daily Workout)
     *
     * @param userTargetFocus Mục tiêu của user (VD: "Relaxation", "Martial Arts", "Energy"). Truyền null/rỗng nếu không có.
     * @param requiresSeated  True nếu user có thể trạng yếu/người già/làm việc văn phòng cần bài tập ngồi.
     */
    public ProgramObject suggest(
            List<ProgramObject> programs,
            int streak,
            boolean isPremiumUser,
            String timeOfDay,
            Set<Integer> recentProgramIds,
            int dayOff,
            int avgDuration,
            String userTargetFocus,
            boolean requiresSeated
    ) {
        if (programs == null || programs.isEmpty()) return null;

        List<ScoredProgram> ranked = rankPrograms(
                programs,
                streak,
                isPremiumUser,
                normalizeTimeOfDay(timeOfDay),
                recentProgramIds,
                dayOff,
                avgDuration,
                userTargetFocus,
                requiresSeated
        );

        return ranked.isEmpty() ? null : ranked.get(0).program;
    }

    // =========================
    // CORE RANKING
    // =========================

    private List<ScoredProgram> rankPrograms(
            List<ProgramObject> programs,
            int streak,
            boolean isPremiumUser,
            String timeOfDay,
            Set<Integer> recentProgramIds,
            int dayOff,
            int avgDuration,
            String userTargetFocus,
            boolean requiresSeated
    ) {
        List<ScoredProgram> scored = new ArrayList<>();
        Set<Integer> recentIds = recentProgramIds != null ? recentProgramIds : new HashSet<>();

        for (ProgramObject item : programs) {
            // 1. FAIL-SAFE: Bỏ qua data lỗi hoặc null
            if (item == null || item.getLevel() <= 0 || item.getTotal() <= 0) continue;

            // 2. HARD FILTERS (LỌC CỨNG)

            // 🔒 Lọc Premium
            if (!isPremiumUser && item.getPremium() == 1) continue;

            // 🛑 Bỏ qua các Khóa học dài ngày (Type = 1). Chỉ gợi ý Bài tập lẻ (Type = 2)
            if (item.getType() == 1) continue;

            // 🔥 Lọc Level dựa theo độ kiên trì (Streak)
            if (!isLevelAllowedByStreak(item.getLevel(), streak)) continue;

            // 3. SCORING (CHẤM ĐIỂM)
            int score = score(item, streak, timeOfDay, recentIds, dayOff, avgDuration, userTargetFocus, requiresSeated);
            scored.add(new ScoredProgram(item, score));
        }

        // 🎲 ANTI-STALENESS: Trộn ngẫu nhiên danh sách trước khi sort.
        // Giúp các bài tập có ĐIỂM BẰNG NHAU sẽ xuất hiện luân phiên, không bị nhàm chán.
        Collections.shuffle(scored);

        // 🏆 Sắp xếp giảm dần theo điểm
        Collections.sort(scored, (a, b) -> Integer.compare(b.score, a.score));

        return scored;
    }

    // =========================
    // HARD RULE – LEVEL GATE
    // =========================

    private boolean isLevelAllowedByStreak(int level, int streak) {
        // Tuần đầu tiên: Chỉ cho phép Level 1 (Beginner)
        if (streak <= 7) {
            return level == 1;
        }
        // Dưới 1 tháng: Cho phép Level 1 và Level 2
        if (streak <= 30) {
            return level <= 2;
        }
        // Trên 30 ngày: Mở khóa Level 3 (Advanced)
        return true;
    }

    // =========================
    // SCORE CALCULATION
    // =========================

    private int score(
            ProgramObject item,
            int streak,
            String timeOfDay,
            Set<Integer> recentProgramIds,
            int dayOff,
            int avgDuration,
            String userTargetFocus,
            boolean requiresSeated
    ) {
        int score = 0;

        int duration = item.getTotal();
        int level = item.getLevel();
        int progress = clamp(item.progress, 0, 100);

        // Tối ưu hóa memory: Gộp chuỗi để tìm kiếm keyword
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(text(item.getName())).append(" ")
                .append(text(item.getTags())).append(" ")
                .append(text(item.getFocus())).append(" ")
                .append(text(item.getBody())).append(" ")
                .append(text(item.getDescription()));
        String allText = textBuilder.toString();

        // --- A. CÁC TIÊU CHÍ CỐT LÕI ---

        // 1. Phù hợp độ khó (Level + Streak)
        score += scoreByLevelAndStreak(level, streak);

        // 2. Phù hợp thời lượng trung bình (Avg Duration)
        score += scoreByAvgDuration(duration, avgDuration);

        // 3. Phục hồi sau ngày nghỉ (Day Off Recovery)
        score += scoreByDayOff(duration, level, dayOff);

        // 4. Logic Tiến độ học (Progress)
        if (progress == 0) {
            score += 20; // Khuyến khích bài mới
        } else if (progress > 0 && progress < 90) {
            score += 50; // Nhắc nhở hoàn thành bài đang tập dở!
        } else if (progress >= 90) {
            score -= 30; // Đã tập xong thì giảm ưu tiên
        }

        // 5. Tránh lặp lại bài vừa tập hôm qua/hôm kia
        if (recentProgramIds.contains(item.getId())) {
            score -= 50;
        }

        // 6. Thời gian trong ngày (Morning/Afternoon/Evening)
        score += timeOfDayScore(allText, timeOfDay);

        // --- B. CÁC TIÊU CHÍ NÂNG CAO (SMART LOGIC) ---

        // 7. Giải quyết "Ngày đầu tiên" (Cold Start)
        if (streak <= 2) {
            if (contains(allText, "basic", "foundation", "warmup", "intro")) {
                score += 50; // Trợ giúp newbie không bị ngợp
            }
        }

        // 8. Cá nhân hóa: Nhu cầu tập ngồi (Người già / Dân văn phòng)
        if (requiresSeated) {
            if (contains(allText, "seated", "chair", "office")) {
                score += 100; // Thưởng cực mạnh
            } else if (level > 1 || contains(allText, "legs", "balance", "agility")) {
                score -= 200; // Phạt nặng bài đứng/nhảy nhót/giữ thăng bằng
            }
        }

        // 9. Cá nhân hóa: Mục tiêu của người dùng
        if (userTargetFocus != null && !userTargetFocus.trim().isEmpty()) {
            if (contains(text(item.getFocus()), userTargetFocus.trim().toLowerCase(Locale.US))) {
                score += 40; // Gãi đúng chỗ ngứa của user
            }
        }

        return score;
    }

    // =========================
    // LEVEL & STREAK SCORE
    // =========================

    private int scoreByLevelAndStreak(int level, int streak) {
        int score = 0;
        if (streak <= 14) {
            if (level == 1) score += 30;
        } else if (streak <= 45) {
            if (level == 2) score += 30;
            if (level == 1) score += 10;
        } else {
            if (level == 3) score += 30;
            if (level == 2) score += 15;
        }
        return score;
    }

    // =========================
    // AVG DURATION SCORE
    // =========================

    private int scoreByAvgDuration(int duration, int avgDuration) {
        if (avgDuration <= 0) return 0;

        int gap = Math.abs(duration - avgDuration);

        if (gap <= 3) return 30;
        if (gap <= 5) return 20;
        if (gap <= 10) return 5;

        // 🚨 PHẠT NẶNG: Tránh tình trạng user chỉ có 10p rảnh nhưng bị ép tập bài 45p
        if (gap >= 20) return -200;
        if (gap >= 15) return -50;

        return 0;
    }

    // =========================
    // DAY OFF RECOVERY
    // =========================

    private int scoreByDayOff(int duration, int level, int dayOff) {
        int score = 0;
        if (dayOff >= 4) {
            // Vừa nghỉ dài xong, cần bài khởi động nhẹ nhàng
            if (duration <= 15) score += 30;
            else if (duration <= 25) score += 10;
            else score -= 30;

            // Ngăn chặn lao vào bài khó gây chấn thương/nản
            if (level == 3) score -= 50;
        }
        return score;
    }

    // =========================
    // TIME OF DAY
    // =========================

    private int timeOfDayScore(String text, String timeOfDay) {
        if (TIME_ANY.equals(timeOfDay)) return 0;

        int score = 0;

        if (TIME_MORNING.equals(timeOfDay)) {
            if (contains(text, "morning", "wake", "energy", "warmup")) score += 20;
        } else if (TIME_AFTERNOON.equals(timeOfDay)) {
            if (contains(text, "focus", "office", "mobility", "tension")) score += 20;
        } else if (TIME_EVENING.equals(timeOfDay)) {
            if (contains(text, "relax", "sleep", "calm", "meditation", "wind-down")) score += 20;
        }

        return score;
    }

    // =========================
    // UTILS
    // =========================

    private boolean contains(String text, String... keywords) {
        if (text == null || text.isEmpty() || keywords == null) return false;

        for (String keyword : keywords) {
            if (keyword != null && text.contains(keyword.toLowerCase(Locale.US))) {
                return true;
            }
        }
        return false;
    }

    private String text(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalizeTimeOfDay(String timeOfDay) {
        if (timeOfDay == null) return TIME_ANY;

        String value = timeOfDay.toLowerCase(Locale.US).trim();
        switch (value) {
            case TIME_MORNING: return TIME_MORNING;
            case TIME_AFTERNOON: return TIME_AFTERNOON;
            case TIME_EVENING: return TIME_EVENING;
            default: return TIME_ANY;
        }
    }

    private static class ScoredProgram {
        final ProgramObject program;
        final int score;

        ScoredProgram(ProgramObject program, int score) {
            this.program = program;
            this.score = score;
        }
    }
}