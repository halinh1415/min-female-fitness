package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import mint.woman.female.workout.R

/**
 * Định nghĩa toàn bộ luồng Onboard mới (theo thứ tự ảnh thiết kế `task/step1..step48`).
 *
 * Mỗi màn hình là một [OnboardStep]; kiểu màn hình ([StepKind]) quyết định composable nào
 * được dùng để vẽ, còn nội dung nằm trong chính step đó. Nhờ vậy muốn thêm/bớt/đổi thứ tự
 * Nội dung gốc nằm trong [ONBOARD_STEPS]; [OnboardingStepFactory] chọn tập con phù hợp
 * cho từng variant mà không nhân bản component UI.
 */

// ---------------------------------------------------------------- palette

/**
 * Bộ font chung của app (giống res/font/fonts.xml): Inter cho chữ thường đến semibold,
 * Barlow Semi Condensed (g1_black) cho chữ đậm - tiêu đề, con số, đồng hồ.
 */
val ObFontFamily = androidx.compose.ui.text.font.FontFamily(
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.inter_regular, androidx.compose.ui.text.font.FontWeight.Light),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.inter_regular, androidx.compose.ui.text.font.FontWeight.Normal),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.inter_medium, androidx.compose.ui.text.font.FontWeight.Medium),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.inter_semibold, androidx.compose.ui.text.font.FontWeight.SemiBold),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.g1_black, androidx.compose.ui.text.font.FontWeight.Bold),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.g1_black, androidx.compose.ui.text.font.FontWeight.ExtraBold),
    androidx.compose.ui.text.font.Font(mint.woman.female.workout.R.font.g1_black, androidx.compose.ui.text.font.FontWeight.Black)
)

object ObColor {
    // Bảng màu "Rose" – đồng bộ với colorPrimary #E91E63 của app.
    val Pink = Color(0xFFE91E63)
    val PinkLight = Color(0xFFFF5C8A)
    val PinkSoft = Color(0xFFFDE7EF)
    val PinkText = Color(0xFFE91E63)
    val Background = Color(0xFFFFF7FA)
    val Surface = Color.White
    val TextPrimary = Color(0xFF1F1A1D)
    val TextSecondary = Color(0xFF6B5E66)
    val TextMuted = Color(0xFFA0939B)
    val Track = Color(0xFFEFE3E8)
    val Cta = Color(0xFFE91E63)
    val CtaDisabled = Color(0xFFF3C1D2)
    val Warning = Color(0xFFFF9F43)
    val Ok = Color(0xFF2EAD6B)
    val Purple = Color(0xFF8E5CF7)
    /** Điểm giữa của gradient biểu đồ hồng → tím. */
    val Plum = Color(0xFFC2459F)
}

// ---------------------------------------------------------------- model

/** Kiểu màn hình – mỗi kiểu ứng với một composable trong `OnboardScreens.kt`. */
enum class StepKind {
    /** step1 – màn chào, số liệu, "Start Now". */
    WELCOME,

    /** step2 – huấn luyện viên tự giới thiệu, nút bắt đầu. */
    COACH,

    /** step3, 29, 39, 40 – danh sách lựa chọn có icon, chọn xong tự sang màn kế. */
    CHOICE,

    /** step4, 7, 11, 12, 13 – lựa chọn kèm ảnh minh hoạ bên phải. */
    CHOICE_IMAGE,

    /** step19/20, 21, 22/24 – lựa chọn kèm thẻ phản hồi của trợ lý + nút Next. */
    CHOICE_FEEDBACK,

    /** step27 – chọn nhiều đáp án + nút Next. */
    MULTI_CHOICE,

    /** step5, 25, 38, 41 – màn thông tin: ảnh + tiêu đề + mô tả + Next. */
    INFO,

    /** step18 – thông tin kèm biểu đồ "Fitness Results". */
    INFO_POTENTIAL,

    /** step32 – chứng thực người dùng (carousel before/after). */
    TESTIMONIAL,

    /** step6 – chọn vùng tập kèm hình cơ thể. */
    FOCUS_BODY,

    /** step8 – chọn dáng mong muốn bằng thanh trượt + thẻ tỉ lệ mỡ. */
    DESIRED_BODY,

    /** step9 – chiều cao (thước dọc, ft/cm). */
    HEIGHT,

    /** step10 – cân nặng hiện tại (thước ngang, lbs/kg) + thẻ BMI. */
    WEIGHT,

    /** step10b – cân nặng mục tiêu do người dùng chọn (thước ngang) + thẻ nhận xét mục tiêu. */
    TARGET_WEIGHT,

    /** step17 – tuổi (bánh xe chọn số). */
    AGE,

    /** step16 – thẻ chuyển phần ("About you" + số thứ tự phần). */
    SECTION,

    /** step14/15, 28 – biểu đồ dự đoán cân nặng. */
    PREDICTION,

    /** step26, 30, 31, 33, 34 – thanh trượt rời rạc + hình minh hoạ. */
    SLIDER,

    /** step35, 36, 37 – phát biểu trong bong bóng thoại, trả lời No/Yes. */
    STATEMENT,

    /** step42, 43, 44 – ảnh tràn màn hình, trả lời No/Yes. */
    FULL_YES_NO,

    /** step45 – đang tạo kế hoạch. */
    GENERATING,

    /** step46 – kết quả kế hoạch (cuộn dọc). */
    PLAN_RESULT,

    /** step47 – mời dùng thử 7 ngày, dẫn sang paywall. */
    TRIAL_INTRO
}

/** Một lựa chọn trong các màn hỏi. */
data class ObOption(
    val key: String,
    val label: String,
    /** Emoji dùng khi không có icon vector/ảnh. */
    val icon: String = "",
    /** Tên file ảnh trong `assets/onboards/v2/` (không kèm đuôi), rỗng nếu không có. */
    val art: String = "",
    /** Tiêu đề thẻ phản hồi hiện ra sau khi chọn (chỉ dùng cho CHOICE_FEEDBACK). */
    val feedbackTitle: String = "",
    val feedbackBody: String = ""
)

/** Một nấc của thanh trượt rời rạc. */
data class ObSliderStop(
    val title: String,
    val description: String = ""
)

/** Hình minh hoạ kiểu "stick figure" vẽ bằng Canvas cho các màn SLIDER. */
enum class ObFigure { NONE, PLANK, REACH, STAIRS, GAUGE, CALENDAR }

data class OnboardStep(
    /** Tên file ảnh thiết kế gốc, tiện đối chiếu khi chỉnh sửa. */
    val design: String,
    val kind: StepKind,
    /** Phần (1..4) dùng để vẽ thanh tiến trình; 0 = không hiện thanh tiến trình. */
    val section: Int = 0,
    /** Khoá lưu đáp án; rỗng nghĩa là màn không thu thập dữ liệu. */
    val id: String = "",
    val title: String = "",
    /** Đoạn được tô hồng trong tiêu đề (khớp chính xác chuỗi con của [title]). */
    val titleAccent: String = "",
    val subtitle: String = "",
    val body: String = "",
    /** Rỗng = "Next" theo ngôn ngữ (R.string.obf_next), lấy lúc vẽ. */
    val cta: String = "",
    val options: List<ObOption> = emptyList(),
    val stops: List<ObSliderStop> = emptyList(),
    val defaultStop: Int = 0,
    val figure: ObFigure = ObFigure.NONE,
    val art: String = "",
    val hint: String = "",
    val hintEmoji: String = ""
) {
    val hasProgress: Boolean get() = section in 1..SECTION_COUNT
}

const val SECTION_COUNT = 4

// ---------------------------------------------------------------- answer ids

object ObId {
    const val MOTIVATION = "motivation"
    const val GOAL = "goal"
    const val FOCUS = "focus"
    const val BODY_SHAPE = "body_shape"
    const val DESIRED_SHAPE = "desired_shape"
    const val HEIGHT = "height"
    const val WEIGHT = "weight"
    const val TARGET_WEIGHT = "target_weight"
    const val BELLY = "belly"
    const val HIP = "hip"
    const val LEG = "leg"
    const val AGE = "age"
    const val PLACE = "place"
    const val WORKOUT_TYPE = "workout_type"
    const val WORKOUT_LEVEL = "workout_level"
    const val FREQUENCY = "frequency"
    const val DISCOMFORT = "discomfort"
    const val TYPICAL_DAY = "typical_day"
    const val ACTIVITY = "activity"
    const val FITNESS = "fitness"
    const val FLEXIBILITY = "flexibility"
    const val AEROBIC = "aerobic"
    const val STATEMENT_BODY = "statement_body"
    const val STATEMENT_CHOICE = "statement_choice"
    const val STATEMENT_GIVE_UP = "statement_give_up"
    const val CELEBRATE = "celebrate"
    const val FEELING = "feeling"
    const val WANT_LOSE_WEIGHT = "want_lose_weight"
    const val WANT_ATTRACTIVE = "want_attractive"
    const val WANT_HEALTHY = "want_healthy"
}

// ---------------------------------------------------------------- flow

/**
 * Thứ tự màn hình đúng như thư mục `task/`.
 *
 * `step15` là khung animation của `step14`, `step24` của `step22` nên gộp làm một màn.
 * `step23` không có trong thư mục. `step48` là màn "Mine" của app (đã tồn tại), không thuộc onboard.
 */
/**
 * Danh sách màn onboard, chữ lấy từ strings_onboard_flow.xml (values, values-xx) theo ngôn ngữ của [res]
 * (Activity đã áp ngôn ngữ app trong attachBaseContext).
 */
fun onboardSteps(res: Resources): List<OnboardStep> {
    fun s(@StringRes id: Int): String = res.getString(id)
    return listOf(

    OnboardStep(
        design = "step1", kind = StepKind.WELCOME,
        title = s(R.string.obf_step1_title),
        cta = s(R.string.obf_step1_cta), art = "welcome_hero"
    ),

    OnboardStep(
        design = "step2", kind = StepKind.COACH,
        title = s(R.string.obf_step2_title),
        body = s(R.string.obf_step2_body),
        titleAccent = s(R.string.obf_step2_accent),
        cta = s(R.string.obf_step2_cta), art = "coach_mia"
    ),

    // ---------------------------------------------------------- phần 1: mục tiêu
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = s(R.string.obf_step0_title), subtitle = "1"
    ),
    OnboardStep(
        design = "step3", kind = StepKind.CHOICE, section = 1, id = ObId.MOTIVATION,
        title = s(R.string.obf_step3_title),
        options = listOf(
            ObOption("confident", s(R.string.obf_step3_opt_confident), "👍"),
            ObOption("stress", s(R.string.obf_step3_opt_stress), "🙂"),
            ObOption("health", s(R.string.obf_step3_opt_health), "❤️"),
            ObOption("energy", s(R.string.obf_step3_opt_energy), "🔋"),
            ObOption("shape", s(R.string.obf_step3_opt_shape), "💪")
        )
    ),

    OnboardStep(
        design = "step4", kind = StepKind.CHOICE_IMAGE, section = 1, id = ObId.GOAL,
        title = s(R.string.obf_step4_title),
        options = listOf(
            ObOption("lose_weight", s(R.string.obf_step4_opt_lose_weight), art = "goal_lose_weight"),
            ObOption("build_muscle", s(R.string.obf_step4_opt_build_muscle), art = "goal_build_muscle"),
            ObOption("keep_fit", s(R.string.obf_step4_opt_keep_fit), art = "goal_keep_fit")
        )
    ),

    OnboardStep(
        design = "step5", kind = StepKind.INFO, section = 1,
        title = s(R.string.obf_step5_title),
        body = s(R.string.obf_step5_body),
        art = "info_scale", hintEmoji = "⚖️"
    ),

    OnboardStep(
        design = "step6", kind = StepKind.FOCUS_BODY, section = 1, id = ObId.FOCUS,
        title = s(R.string.obf_step6_title),
        options = listOf(
            ObOption("arm", s(R.string.obf_step6_opt_arm)),
            ObOption("belly", s(R.string.obf_step6_opt_belly)),
            ObOption("butt", s(R.string.obf_step6_opt_butt)),
            ObOption("leg", s(R.string.obf_step6_opt_leg)),
            ObOption("full_body", s(R.string.obf_step6_opt_full_body))
        )
    ),

    OnboardStep(
        design = "step7", kind = StepKind.CHOICE_IMAGE, section = 1, id = ObId.BODY_SHAPE,
        title = s(R.string.obf_step7_title), titleAccent = s(R.string.obf_step7_accent),
        options = listOf(
            ObOption("medium", s(R.string.obf_step7_opt_medium), art = "shape_medium"),
            ObOption("flabby", s(R.string.obf_step7_opt_flabby), art = "shape_flabby"),
            ObOption("skinny", s(R.string.obf_step7_opt_skinny), art = "shape_skinny"),
            ObOption("toned", s(R.string.obf_step7_opt_toned), art = "shape_toned")
        )
    ),

    OnboardStep(
        design = "step8", kind = StepKind.DESIRED_BODY, section = 1, id = ObId.DESIRED_SHAPE,
        title = s(R.string.obf_step8_title), titleAccent = s(R.string.obf_step8_accent),
        defaultStop = 2
    ),

    // ---------------------------------------------------------- phần 2: số đo
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = s(R.string.obf_step0_title_2), subtitle = "2"
    ),
    OnboardStep(
        design = "step9", kind = StepKind.HEIGHT, section = 2, id = ObId.HEIGHT,
        title = s(R.string.obf_step9_title),
        hint = s(R.string.obf_step9_hint), hintEmoji = "✨"
    ),

    OnboardStep(
        design = "step10", kind = StepKind.WEIGHT, section = 2, id = ObId.WEIGHT,
        title = s(R.string.obf_step10_title), titleAccent = s(R.string.obf_step10_accent)
    ),

    OnboardStep(
        design = "step10b", kind = StepKind.TARGET_WEIGHT, section = 2, id = ObId.TARGET_WEIGHT,
        title = s(R.string.obf_step10b_title), titleAccent = s(R.string.obf_step10b_accent)
    ),

    OnboardStep(
        design = "step11", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.BELLY,
        title = s(R.string.obf_step11_title), titleAccent = s(R.string.obf_step11_accent),
        options = listOf(
            ObOption("normal", s(R.string.obf_step11_opt_normal), art = "belly_normal"),
            ObOption("mommy", s(R.string.obf_step11_opt_mommy), art = "belly_mommy"),
            ObOption("hormonal", s(R.string.obf_step11_opt_hormonal), art = "belly_hormonal"),
            ObOption("alcohol", s(R.string.obf_step11_opt_alcohol), art = "belly_alcohol"),
            ObOption("stressed", s(R.string.obf_step11_opt_stressed), art = "belly_stressed")
        )
    ),

    OnboardStep(
        design = "step12", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.HIP,
        title = s(R.string.obf_step12_title), titleAccent = s(R.string.obf_step12_accent),
        options = listOf(
            ObOption("normal", s(R.string.obf_step12_opt_normal), art = "hip_normal"),
            ObOption("flat", s(R.string.obf_step12_opt_flat), art = "hip_flat"),
            ObOption("saggy", s(R.string.obf_step12_opt_saggy), art = "hip_saggy"),
            ObOption("double", s(R.string.obf_step12_opt_double), art = "hip_double"),
            ObOption("bubble", s(R.string.obf_step12_opt_bubble), art = "hip_bubble")
        )
    ),

    OnboardStep(
        design = "step13", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.LEG,
        title = s(R.string.obf_step13_title), titleAccent = s(R.string.obf_step13_accent),
        options = listOf(
            ObOption("normal", s(R.string.obf_step13_opt_normal), art = "leg_normal"),
            ObOption("x", s(R.string.obf_step13_opt_x), art = "leg_x"),
            ObOption("o", s(R.string.obf_step13_opt_o), art = "leg_o"),
            ObOption("xo", s(R.string.obf_step13_opt_xo), art = "leg_xo")
        )
    ),

    OnboardStep(
        design = "step14", kind = StepKind.PREDICTION, section = 2,
        subtitle = s(R.string.obf_step14_subtitle),
        body = s(R.string.obf_step14_body)
    ),

    // ---------------------------------------------------------- phần 3: về bạn

    OnboardStep(
        design = "step16", kind = StepKind.SECTION,
        title = s(R.string.obf_step16_title), subtitle = "3"
    ),

    OnboardStep(
        design = "step17", kind = StepKind.AGE, section = 3, id = ObId.AGE,
        title = s(R.string.obf_step17_title),
        hint = s(R.string.obf_step17_hint), hintEmoji = "🎂"
    ),

    OnboardStep(
        design = "step18", kind = StepKind.INFO_POTENTIAL, section = 3,
        title = s(R.string.obf_step18_title)
    ),

    OnboardStep(
        design = "step19", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.PLACE,
        title = s(R.string.obf_step19_title),
        options = listOf(
            ObOption(
                "any", s(R.string.obf_step19_opt_any), "✨",
                feedbackTitle = s(R.string.obf_step19_fb_title),
                feedbackBody = s(R.string.obf_step19_fb_body)
            ),
            ObOption(
                "bed", s(R.string.obf_step19_opt_bed), "🛏️",
                feedbackTitle = s(R.string.obf_step19_fb_title),
                feedbackBody = s(R.string.obf_step19_fb_body_2)
            ),
            ObOption(
                "mat", s(R.string.obf_step19_opt_mat), "🧘",
                feedbackTitle = s(R.string.obf_step19_fb_title_2),
                feedbackBody = s(R.string.obf_step19_fb_body_3)
            )
        )
    ),

    OnboardStep(
        design = "step21", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.WORKOUT_TYPE,
        title = s(R.string.obf_step21_title),
        options = listOf(
            ObOption(
                "no_equipment", s(R.string.obf_step21_opt_no_equipment), "🏋️",
                feedbackTitle = s(R.string.obf_step21_fb_title),
                feedbackBody = s(R.string.obf_step21_fb_body)
            ),
            ObOption(
                "no_jumping", s(R.string.obf_step21_opt_no_jumping), "🤸",
                feedbackTitle = s(R.string.obf_step21_fb_title),
                feedbackBody = s(R.string.obf_step21_fb_body_2)
            ),
            ObOption(
                "lying_down", s(R.string.obf_step21_opt_lying_down), "🛌",
                feedbackTitle = s(R.string.obf_step21_fb_title),
                feedbackBody = s(R.string.obf_step21_fb_body_3)
            )
        )
    ),

    OnboardStep(
        design = "step22", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.WORKOUT_LEVEL,
        title = s(R.string.obf_step22_title),
        options = listOf(
            ObOption(
                "easy", s(R.string.obf_step22_opt_easy), "🙂",
                feedbackTitle = s(R.string.obf_step22_fb_title),
                feedbackBody = s(R.string.obf_step22_fb_body)
            ),
            ObOption(
                "light_sweat", s(R.string.obf_step22_opt_light_sweat), "💧",
                feedbackTitle = s(R.string.obf_step22_fb_title),
                feedbackBody = s(R.string.obf_step22_fb_body_2)
            ),
            ObOption(
                "challenging", s(R.string.obf_step22_opt_challenging), "🏆",
                feedbackTitle = s(R.string.obf_step22_fb_title),
                feedbackBody = s(R.string.obf_step22_fb_body_3)
            )
        )
    ),

    OnboardStep(
        design = "step25", kind = StepKind.INFO, section = 3,
        title = s(R.string.obf_step25_title),
        body = s(R.string.obf_step25_body),
        art = "info_7min", hintEmoji = "⏱️"
    ),

    OnboardStep(
        design = "step26", kind = StepKind.SLIDER, section = 3, id = ObId.FREQUENCY,
        title = s(R.string.obf_step26_title),
        figure = ObFigure.CALENDAR, defaultStop = 2,
        subtitle = s(R.string.obf_step26_subtitle), body = s(R.string.obf_step26_body),
        stops = listOf(
            ObSliderStop(s(R.string.obf_step26_stop0), s(R.string.obf_step26_stop0_desc)),
            ObSliderStop(s(R.string.obf_step26_stop1), s(R.string.obf_step26_stop1_desc)),
            ObSliderStop(s(R.string.obf_step26_stop2), s(R.string.obf_step26_stop2_desc)),
            ObSliderStop(s(R.string.obf_step26_stop3), s(R.string.obf_step26_stop3_desc))
        )
    ),

    OnboardStep(
        design = "step27", kind = StepKind.MULTI_CHOICE, section = 3, id = ObId.DISCOMFORT,
        title = s(R.string.obf_step27_title),
        options = listOf(
            ObOption("none", s(R.string.obf_step27_opt_none), "👌"),
            ObOption("back", s(R.string.obf_step27_opt_back), "🦴"),
            ObOption("arm", s(R.string.obf_step27_opt_arm), "💪"),
            ObOption("knee", s(R.string.obf_step27_opt_knee), "🦵"),
            ObOption("heart", s(R.string.obf_step27_opt_heart), "🫀"),
            ObOption("other", s(R.string.obf_step27_opt_other), "➕")
        )
    ),

    OnboardStep(
        design = "step28", kind = StepKind.PREDICTION, section = 3,
        subtitle = s(R.string.obf_step28_subtitle)
    ),

    // ---------------------------------------------------------- phần 4: lối sống
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = s(R.string.obf_step0_title_3), subtitle = "4"
    ),
    OnboardStep(
        design = "step29", kind = StepKind.CHOICE, section = 4, id = ObId.TYPICAL_DAY,
        title = s(R.string.obf_step29_title),
        options = listOf(
            ObOption("seated", s(R.string.obf_step29_opt_seated), "🖥️"),
            ObOption("home", s(R.string.obf_step29_opt_home), "🏠"),
            ObOption("walking", s(R.string.obf_step29_opt_walking), "👣"),
            ObOption("feet", s(R.string.obf_step29_opt_feet), "🚶")
        )
    ),

    OnboardStep(
        design = "step30", kind = StepKind.SLIDER, section = 4, id = ObId.ACTIVITY,
        title = s(R.string.obf_step30_title),
        figure = ObFigure.PLANK, defaultStop = 2,
        subtitle = s(R.string.obf_step30_subtitle), body = s(R.string.obf_step30_body),
        stops = listOf(
            ObSliderStop(s(R.string.obf_step30_subtitle), s(R.string.obf_step30_stop0_desc)),
            ObSliderStop(s(R.string.obf_step30_stop1), s(R.string.obf_step30_stop1_desc)),
            ObSliderStop(s(R.string.obf_step30_stop2), s(R.string.obf_step30_stop2_desc)),
            ObSliderStop(s(R.string.obf_step30_body), s(R.string.obf_step30_stop3_desc))
        )
    ),

    OnboardStep(
        design = "step31", kind = StepKind.SLIDER, section = 4, id = ObId.FITNESS,
        title = s(R.string.obf_step31_title),
        figure = ObFigure.GAUGE, defaultStop = 0,
        subtitle = s(R.string.obf_step31_subtitle), body = s(R.string.obf_step31_body),
        stops = listOf(
            ObSliderStop(s(R.string.obf_step31_subtitle), s(R.string.obf_step31_stop0_desc)),
            ObSliderStop(s(R.string.obf_step31_stop1), s(R.string.obf_step31_stop1_desc)),
            ObSliderStop(s(R.string.obf_step31_body), s(R.string.obf_step31_stop2_desc))
        )
    ),

    OnboardStep(
        design = "step32", kind = StepKind.TESTIMONIAL, section = 4,
        title = s(R.string.obf_step32_title),
        titleAccent = s(R.string.obf_step32_accent)
    ),

    OnboardStep(
        design = "step33", kind = StepKind.SLIDER, section = 4, id = ObId.FLEXIBILITY,
        title = s(R.string.obf_step33_title), subtitle = s(R.string.obf_step33_subtitle), body = s(R.string.obf_step33_body),
        hint = s(R.string.obf_step33_hint),
        figure = ObFigure.REACH, defaultStop = 0,
        stops = listOf(
            ObSliderStop(s(R.string.obf_step33_stop0), s(R.string.obf_step33_stop0_desc)),
            ObSliderStop(s(R.string.obf_step33_stop1), s(R.string.obf_step33_stop1_desc)),
            ObSliderStop(s(R.string.obf_step33_stop2), s(R.string.obf_step33_stop2_desc))
        )
    ),

    OnboardStep(
        design = "step34", kind = StepKind.SLIDER, section = 4, id = ObId.AEROBIC,
        title = s(R.string.obf_step34_title), subtitle = s(R.string.obf_step34_subtitle), body = s(R.string.obf_step34_body),
        hint = s(R.string.obf_step34_hint),
        figure = ObFigure.STAIRS, defaultStop = 0,
        stops = listOf(
            ObSliderStop(s(R.string.obf_step34_stop0), s(R.string.obf_step34_stop0_desc)),
            ObSliderStop(s(R.string.obf_step34_stop1), s(R.string.obf_step34_stop1_desc)),
            ObSliderStop(s(R.string.obf_step34_stop2), s(R.string.obf_step34_stop2_desc))
        )
    ),

    OnboardStep(
        design = "step35", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_BODY,
        title = s(R.string.obf_step35_title),
        body = s(R.string.obf_step35_body),
        art = "statement_mirror"
    ),

    OnboardStep(
        design = "step36", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_CHOICE,
        title = s(R.string.obf_step36_title),
        body = s(R.string.obf_step36_body),
        art = "statement_unsure"
    ),

    OnboardStep(
        design = "step37", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_GIVE_UP,
        title = s(R.string.obf_step37_title),
        body = s(R.string.obf_step37_body),
        art = "statement_give_up"
    ),

    OnboardStep(
        design = "step38", kind = StepKind.INFO, section = 4,
        title = s(R.string.obf_step38_title),
        subtitle = s(R.string.obf_step38_subtitle),
        body = s(R.string.obf_step38_body),
        art = "info_community", hintEmoji = "👭"
    ),

    OnboardStep(
        design = "step39", kind = StepKind.CHOICE, section = 4, id = ObId.CELEBRATE,
        title = s(R.string.obf_step39_title),
        options = listOf(
            ObOption("clothes", s(R.string.obf_step39_opt_clothes), "👕"),
            ObOption("vacation", s(R.string.obf_step39_opt_vacation), "🏝️"),
            ObOption("photos", s(R.string.obf_step39_opt_photos), "📷"),
            ObOption("concert", s(R.string.obf_step39_opt_concert), "🎸"),
            ObOption("other", s(R.string.obf_step39_opt_other), "💬")
        )
    ),

    OnboardStep(
        design = "step40", kind = StepKind.CHOICE, section = 4, id = ObId.FEELING,
        title = s(R.string.obf_step40_title),
        options = listOf(
            ObOption("proud", s(R.string.obf_step40_opt_proud), "👍"),
            ObOption("energy", s(R.string.obf_step40_opt_energy), "🔋"),
            ObOption("healthy", s(R.string.obf_step40_opt_healthy), "🙂"),
            ObOption("happy", s(R.string.obf_step40_opt_happy), "👗"),
            ObOption("less_worry", s(R.string.obf_step40_opt_less_worry), "⚖️")
        )
    ),

    OnboardStep(
        design = "step41", kind = StepKind.INFO, section = 4,
        title = s(R.string.obf_step41_title),
        body = s(R.string.obf_step41_body),
        art = "info_before_after"
    ),

    OnboardStep(
        design = "step42", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_LOSE_WEIGHT,
        title = s(R.string.obf_step42_title), art = "full_lose_weight"
    ),

    OnboardStep(
        design = "step43", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_ATTRACTIVE,
        title = s(R.string.obf_step43_title), art = "full_active"
    ),

    OnboardStep(
        design = "step44", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_HEALTHY,
        title = s(R.string.obf_step44_title), art = "full_healthy"
    ),

    // ---------------------------------------------------------- kết thúc

    OnboardStep(
        design = "step45", kind = StepKind.GENERATING,
        title = s(R.string.obf_step45_title),
        subtitle = s(R.string.obf_step45_subtitle)
    ),

    OnboardStep(
        design = "step46", kind = StepKind.PLAN_RESULT,
        title = s(R.string.obf_step46_title),
        cta = s(R.string.obf_step46_cta)
    ),

    OnboardStep(
        design = "step47", kind = StepKind.TRIAL_INTRO,
        title = s(R.string.obf_step47_title),
        titleAccent = s(R.string.obf_step47_accent)
    )
)
}
