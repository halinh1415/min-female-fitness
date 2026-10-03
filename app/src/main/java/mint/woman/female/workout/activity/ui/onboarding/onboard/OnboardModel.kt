package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.ui.graphics.Color

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

    /** step2 – huấn luyện viên tự giới thiệu, "I'M READY!". */
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
    val cta: String = "Next",
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
val ONBOARD_STEPS: List<OnboardStep> = listOf(

    OnboardStep(
        design = "step1", kind = StepKind.WELCOME,
        title = "Your fitness journey starts at home",
        cta = "Start Now", art = "welcome_hero"
    ),

    OnboardStep(
        design = "step2", kind = StepKind.COACH,
        title = "Hi, I'm Mia!",
        body = "I'll be your coach along the way.\nAnswer a few quick questions and I'll build a personalized plan just for you.",
        titleAccent = "personalized plan",
        cta = "I'M READY!", art = "coach_kelly"
    ),

    // ---------------------------------------------------------- phần 1: mục tiêu
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = "Your Goals", subtitle = "1"
    ),
    OnboardStep(
        design = "step3", kind = StepKind.CHOICE, section = 1, id = ObId.MOTIVATION,
        title = "What motivates you the most?",
        options = listOf(
            ObOption("confident", "Feel confident", "👍"),
            ObOption("stress", "Release stress", "🙂"),
            ObOption("health", "Improve health", "❤️"),
            ObOption("energy", "Boost energy", "🔋"),
            ObOption("shape", "Get shaped", "💪")
        )
    ),

    OnboardStep(
        design = "step4", kind = StepKind.CHOICE_IMAGE, section = 1, id = ObId.GOAL,
        title = "What's your main goal?",
        options = listOf(
            ObOption("lose_weight", "Lose weight", art = "goal_lose_weight"),
            ObOption("build_muscle", "Build muscle", art = "goal_build_muscle"),
            ObOption("keep_fit", "Keep fit", art = "goal_keep_fit")
        )
    ),

    OnboardStep(
        design = "step5", kind = StepKind.INFO, section = 1,
        title = "Lose weight at your own pace",
        body = "Regular home workouts and balanced eating help you lose weight steadily and keep it off.",
        art = "info_scale", hintEmoji = "⚖️"
    ),

    OnboardStep(
        design = "step6", kind = StepKind.FOCUS_BODY, section = 1, id = ObId.FOCUS,
        title = "Which area would you like to focus on?",
        options = listOf(
            ObOption("arm", "Arm"),
            ObOption("belly", "Belly"),
            ObOption("butt", "Butt"),
            ObOption("leg", "Leg"),
            ObOption("full_body", "Full body")
        )
    ),

    OnboardStep(
        design = "step7", kind = StepKind.CHOICE_IMAGE, section = 1, id = ObId.BODY_SHAPE,
        title = "What's your current body shape?", titleAccent = "current",
        options = listOf(
            ObOption("medium", "Medium", art = "shape_medium"),
            ObOption("flabby", "Flabby", art = "shape_flabby"),
            ObOption("skinny", "Skinny", art = "shape_skinny"),
            ObOption("toned", "Toned", art = "shape_toned")
        )
    ),

    OnboardStep(
        design = "step8", kind = StepKind.DESIRED_BODY, section = 1, id = ObId.DESIRED_SHAPE,
        title = "What's your desired body shape?", titleAccent = "desired",
        defaultStop = 2
    ),

    // ---------------------------------------------------------- phần 2: số đo
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = "Body Data", subtitle = "2"
    ),
    OnboardStep(
        design = "step9", kind = StepKind.HEIGHT, section = 2, id = ObId.HEIGHT,
        title = "What's your height?",
        hint = "This will help us make adjustment to your personal plan.", hintEmoji = "✨"
    ),

    OnboardStep(
        design = "step10", kind = StepKind.WEIGHT, section = 2, id = ObId.WEIGHT,
        title = "What's your current weight?", titleAccent = "current"
    ),

    OnboardStep(
        design = "step11", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.BELLY,
        title = "How would you describe your belly area?", titleAccent = "belly",
        options = listOf(
            ObOption("normal", "Mostly flat", art = "belly_normal"),
            ObOption("mommy", "Soft after pregnancy", art = "belly_mommy"),
            ObOption("hormonal", "Lower belly pooch", art = "belly_hormonal"),
            ObOption("alcohol", "Round upper belly", art = "belly_alcohol"),
            ObOption("stressed", "Often bloated", art = "belly_stressed")
        )
    ),

    OnboardStep(
        design = "step12", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.HIP,
        title = "How would you describe your hips and glutes?", titleAccent = "hips",
        options = listOf(
            ObOption("normal", "Balanced", art = "hip_normal"),
            ObOption("flat", "Flat", art = "hip_flat"),
            ObOption("saggy", "Need lifting", art = "hip_saggy"),
            ObOption("double", "Dips on the sides", art = "hip_double"),
            ObOption("bubble", "Round", art = "hip_bubble")
        )
    ),

    OnboardStep(
        design = "step13", kind = StepKind.CHOICE_IMAGE, section = 2, id = ObId.LEG,
        title = "How do your legs line up when you stand?", titleAccent = "legs",
        options = listOf(
            ObOption("normal", "Straight", art = "leg_normal"),
            ObOption("x", "Knees touch (X)", art = "leg_x"),
            ObOption("o", "Knees apart (O)", art = "leg_o"),
            ObOption("xo", "Mixed (XO)", art = "leg_xo")
        )
    ),

    OnboardStep(
        design = "step14", kind = StepKind.PREDICTION, section = 2,
        subtitle = "Step by step, you can get there!",
        body = "This estimate assumes a safe, steady pace with regular workouts. Results vary from person to person."
    ),

    // ---------------------------------------------------------- phần 3: về bạn

    OnboardStep(
        design = "step16", kind = StepKind.SECTION,
        title = "About you", subtitle = "3"
    ),

    OnboardStep(
        design = "step17", kind = StepKind.AGE, section = 3, id = ObId.AGE,
        title = "What's your age?",
        hint = "This will help us to adjust the workouts for your age.", hintEmoji = "🎂"
    ),

    OnboardStep(
        design = "step18", kind = StepKind.INFO_POTENTIAL, section = 3,
        title = "You've got what it takes!"
    ),

    OnboardStep(
        design = "step19", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.PLACE,
        title = "Where do you prefer to work out?",
        options = listOf(
            ObOption(
                "any", "No Preference", "✨",
                feedbackTitle = "OK!",
                feedbackBody = "We'll pick the most suitable workouts for you."
            ),
            ObOption(
                "bed", "Bed", "🛏️",
                feedbackTitle = "OK!",
                feedbackBody = "It's suitable for specific workouts, and we've got just what you need."
            ),
            ObOption(
                "mat", "Yoga mat", "🧘",
                feedbackTitle = "Great!",
                feedbackBody = "A mat makes floor workouts much more comfortable."
            )
        )
    ),

    OnboardStep(
        design = "step21", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.WORKOUT_TYPE,
        title = "What's your preferred workout type?",
        options = listOf(
            ObOption(
                "no_equipment", "No equipment", "🏋️",
                feedbackTitle = "Got it!",
                feedbackBody = "We'll provide you with equipment-free and easy-to-follow workouts."
            ),
            ObOption(
                "no_jumping", "No Jumping", "🤸",
                feedbackTitle = "Got it!",
                feedbackBody = "We'll keep your plan jump-free and joint-friendly."
            ),
            ObOption(
                "lying_down", "More lying down", "🛌",
                feedbackTitle = "Got it!",
                feedbackBody = "We'll add more floor and lying-down exercises for you."
            )
        )
    ),

    OnboardStep(
        design = "step22", kind = StepKind.CHOICE_FEEDBACK, section = 3, id = ObId.WORKOUT_LEVEL,
        title = "What's your preferred workout level?",
        options = listOf(
            ObOption(
                "easy", "Easy Enough", "🙂",
                feedbackTitle = "Understood!",
                feedbackBody = "We'll provide beginner-friendly exercises to help you start smoothly."
            ),
            ObOption(
                "light_sweat", "Break a light sweat", "💧",
                feedbackTitle = "Understood!",
                feedbackBody = "We'll keep the intensity moderate so you sweat but stay comfortable."
            ),
            ObOption(
                "challenging", "A bit challenging", "🏆",
                feedbackTitle = "Understood!",
                feedbackBody = "We'll add a bit more intensity to keep you challenged."
            )
        )
    ),

    OnboardStep(
        design = "step25", kind = StepKind.INFO, section = 3,
        title = "Busy day?\nTry a 7-minute session",
        body = "Even a few minutes counts. Quick sessions make it easy to keep your streak going.",
        art = "info_7min", hintEmoji = "⏱️"
    ),

    OnboardStep(
        design = "step26", kind = StepKind.SLIDER, section = 3, id = ObId.FREQUENCY,
        title = "How often would you like to work out?",
        figure = ObFigure.CALENDAR, defaultStop = 2,
        subtitle = "Less", body = "More",
        stops = listOf(
            ObSliderStop("1 time / week", "I want to start really slowly"),
            ObSliderStop("2 times / week", "I can manage a couple of sessions"),
            ObSliderStop("3 times / week", "I enjoy workout as a part of my lifestyle"),
            ObSliderStop("5 times / week", "I want to train almost every day")
        )
    ),

    OnboardStep(
        design = "step27", kind = StepKind.MULTI_CHOICE, section = 3, id = ObId.DISCOMFORT,
        title = "Any injuries or health conditions?",
        options = listOf(
            ObOption("none", "None", "👌"),
            ObOption("back", "Back injury", "🦴"),
            ObOption("arm", "Arm injury", "💪"),
            ObOption("knee", "Knee injury", "🦵"),
            ObOption("heart", "Heart condition", "🫀"),
            ObOption("other", "Other", "➕")
        )
    ),

    OnboardStep(
        design = "step28", kind = StepKind.PREDICTION, section = 3,
        subtitle = "We predict that you'll reach"
    ),

    // ---------------------------------------------------------- phần 4: lối sống
    OnboardStep(
        design = "step0", kind = StepKind.SECTION,
        title = "Life Style", subtitle = "4"
    ),
    OnboardStep(
        design = "step29", kind = StepKind.CHOICE, section = 4, id = ObId.TYPICAL_DAY,
        title = "What does your typical day look like?",
        options = listOf(
            ObOption("seated", "Mostly seated at work", "🖥️"),
            ObOption("home", "Mostly sedentary at home", "🏠"),
            ObOption("walking", "Walking daily", "👣"),
            ObOption("feet", "On my feet all day", "🚶")
        )
    ),

    OnboardStep(
        design = "step30", kind = StepKind.SLIDER, section = 4, id = ObId.ACTIVITY,
        title = "What's your activity level?",
        figure = ObFigure.PLANK, defaultStop = 2,
        subtitle = "Not active", body = "Very active",
        stops = listOf(
            ObSliderStop("Not active", "I barely exercise at the moment"),
            ObSliderStop("Lightly active", "I move a little during the day"),
            ObSliderStop("Moderately active", "I do moderate exercises several times a week for overall health"),
            ObSliderStop("Very active", "I train hard almost every day")
        )
    ),

    OnboardStep(
        design = "step31", kind = StepKind.SLIDER, section = 4, id = ObId.FITNESS,
        title = "What's your fitness level?",
        figure = ObFigure.GAUGE, defaultStop = 0,
        subtitle = "Beginner", body = "Advanced",
        stops = listOf(
            ObSliderStop("Beginner", "I'm new to fitness and want a solid foundation"),
            ObSliderStop("Intermediate", "I work out from time to time"),
            ObSliderStop("Advanced", "I train regularly and know my limits")
        )
    ),

    OnboardStep(
        design = "step32", kind = StepKind.TESTIMONIAL, section = 4,
        title = "Real progress comes from small daily steps",
        titleAccent = "small daily steps"
    ),

    OnboardStep(
        design = "step33", kind = StepKind.SLIDER, section = 4, id = ObId.FLEXIBILITY,
        title = "Your flexibility", subtitle = "Far", body = "Touch",
        hint = "How far are your hands from toes?",
        figure = ObFigure.REACH, defaultStop = 0,
        stops = listOf(
            ObSliderStop("It's OK!", "Lots of people start here. Daily stretching will help you improve."),
            ObSliderStop("Almost there!", "A bit more stretching and you'll touch your toes."),
            ObSliderStop("Nice!", "You already have good flexibility - let's keep it.")
        )
    ),

    OnboardStep(
        design = "step34", kind = StepKind.SLIDER, section = 4, id = ObId.AEROBIC,
        title = "Your aerobic level", subtitle = "Too hard", body = "Easy",
        hint = "How do you feel after climbing 5 floors?",
        figure = ObFigure.STAIRS, defaultStop = 0,
        stops = listOf(
            ObSliderStop("Out of breath", "Got it! Our tailored workouts will gradually boost your endurance."),
            ObSliderStop("A little tired", "Good! We'll build your endurance step by step."),
            ObSliderStop("Totally fine", "Great! We can start with a higher intensity.")
        )
    ),

    OnboardStep(
        design = "step35", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_BODY,
        title = "Does this sound like you?",
        body = "I want to feel more comfortable and confident in my body.",
        art = "statement_mirror"
    ),

    OnboardStep(
        design = "step36", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_CHOICE,
        title = "Is this true for you?",
        body = "I'm not sure which workouts are right for me.",
        art = "statement_unsure"
    ),

    OnboardStep(
        design = "step37", kind = StepKind.STATEMENT, section = 4, id = ObId.STATEMENT_GIVE_UP,
        title = "How about this one?",
        body = "I lose motivation when workouts feel too hard or repetitive.",
        art = "statement_give_up"
    ),

    OnboardStep(
        design = "step38", kind = StepKind.INFO, section = 4,
        title = "Made for busy women like you",
        subtitle = "Short & simple",
        body = "Follow-along videos, clear timers and a plan that fits into your day.",
        art = "info_community", hintEmoji = "👭"
    ),

    OnboardStep(
        design = "step39", kind = StepKind.CHOICE, section = 4, id = ObId.CELEBRATE,
        title = "How will you treat yourself when you hit your goal?",
        options = listOf(
            ObOption("clothes", "Buy new clothes", "👕"),
            ObOption("vacation", "Go on vacation", "🏝️"),
            ObOption("photos", "Take photos", "📷"),
            ObOption("concert", "Attend a concert", "🎸"),
            ObOption("other", "Other", "💬")
        )
    ),

    OnboardStep(
        design = "step40", kind = StepKind.CHOICE, section = 4, id = ObId.FEELING,
        title = "How do you want to feel when you reach your goal?",
        options = listOf(
            ObOption("proud", "Be proud of myself", "👍"),
            ObOption("energy", "Full of energy", "🔋"),
            ObOption("healthy", "Healthy & vibrant", "🙂"),
            ObOption("happy", "Self-assured and happy", "👗"),
            ObOption("less_worry", "Worry less about body image", "⚖️")
        )
    ),

    OnboardStep(
        design = "step41", kind = StepKind.INFO, section = 4,
        title = "What regular workouts can do for you",
        body = "Exercise improves posture, muscle tone and energy, so you look and feel more confident.",
        art = "info_before_after"
    ),

    OnboardStep(
        design = "step42", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_LOSE_WEIGHT,
        title = "Want to lose weight?", art = "full_lose_weight"
    ),

    OnboardStep(
        design = "step43", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_ATTRACTIVE,
        title = "Want to stay active and feel great?", art = "full_active"
    ),

    OnboardStep(
        design = "step44", kind = StepKind.FULL_YES_NO, section = 4, id = ObId.WANT_HEALTHY,
        title = "Want to live a longer and healthier life?", art = "full_healthy"
    ),

    // ---------------------------------------------------------- kết thúc

    OnboardStep(
        design = "step45", kind = StepKind.GENERATING,
        title = "Creating your plan...",
        subtitle = "Built around your answers"
    ),

    OnboardStep(
        design = "step46", kind = StepKind.PLAN_RESULT,
        title = "Based on your answers, you will be",
        cta = "Get my plan"
    ),

    OnboardStep(
        design = "step47", kind = StepKind.TRIAL_INTRO,
        title = "Start 7-day free trial\nNo payment now!",
        titleAccent = "free"
    )
)
