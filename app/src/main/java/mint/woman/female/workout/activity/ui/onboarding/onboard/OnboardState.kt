package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mint.woman.female.workout.utils.MyPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Trạng thái dùng chung cho toàn bộ luồng onboard: vị trí màn hình, đáp án và số đo.
 * Đặt trong ViewModel nên xoay màn hình / tạo lại Activity không mất dữ liệu.
 */
class OnboardState : ViewModel() {

    var index by mutableIntStateOf(0)

    var variant by mutableStateOf(OnboardingVariant.FULL)
        private set

    var steps by mutableStateOf<List<OnboardStep>>(emptyList())
        private set

    /** Toàn bộ màn (đã dịch theo ngôn ngữ app), để tra nhãn lựa chọn của màn không có trong variant. */
    private var allSteps: List<OnboardStep> = emptyList()

    private var configured = false

    /** Đáp án dạng chữ của từng câu hỏi (khoá là [ObId]). */
    val answers = mutableStateMapOf<String, String>()

    /** Vị trí nấc đang chọn của các màn thanh trượt. */
    val sliderStops = mutableStateMapOf<String, Int>()

    var heightCm by mutableIntStateOf(165)
    var heightMetric by mutableStateOf(true)

    /** Cân nặng nhập vào, tính theo đơn vị đang chọn. */
    var weightValue by mutableIntStateOf(70)
    var weightMetric by mutableStateOf(true)

    var age by mutableIntStateOf(25)

    /**
     * Cân nặng mục tiêu người dùng chọn ở màn TARGET_WEIGHT, cùng đơn vị với [weightValue].
     * 0 = chưa chọn (nhánh rút gọn không có màn này) → dùng [suggestedTargetKg].
     */
    var targetValue by mutableIntStateOf(0)

    // ------------------------------------------------------------ dẫn xuất

    val weightKg: Float
        get() = if (weightMetric) weightValue.toFloat() else weightValue / LB_PER_KG

    val heightMeters: Float get() = heightCm / 100f

    val bmi: Float
        get() {
            val meters = heightMeters
            return if (meters <= 0f) 0f else weightKg / (meters * meters)
        }

    /** Gợi ý cân mục tiêu (kg) theo mục tiêu chính – giá trị mặc định của màn TARGET_WEIGHT. */
    val suggestedTargetKg: Float
        get() {
            val current = weightKg
            return when (answers[ObId.GOAL]) {
                "build_muscle" -> current + max(1.5f, current * 0.02f)
                "keep_fit" -> current - max(1f, current * 0.015f)
                else -> current - max(2f, current * 0.035f)
            }.round1()
        }

    /** Cân nặng mục tiêu (kg): ưu tiên giá trị người dùng chọn, không thì lấy gợi ý. */
    val targetWeightKg: Float
        get() = if (targetValue > 0) {
            (if (weightMetric) targetValue.toFloat() else targetValue / LB_PER_KG).round1()
        } else suggestedTargetKg

    /** Gợi ý theo đơn vị đang hiển thị (số nguyên để khớp nấc thước). */
    fun suggestedTargetValue(): Int =
        (if (weightMetric) suggestedTargetKg else suggestedTargetKg * LB_PER_KG).roundToInt()

    /** Số buổi/tuần theo màn tần suất (mặc định 3 khi nhánh không hỏi). */
    val trainingDaysPerWeek: Int
        get() = FREQUENCY_PER_WEEK[(sliderStops[ObId.FREQUENCY] ?: 2).coerceIn(0, FREQUENCY_PER_WEEK.lastIndex)]

    /** BMI ứng với cân mục tiêu, để cảnh báo mục tiêu quá thấp. */
    val targetBmi: Float
        get() {
            val meters = heightMeters
            return if (meters <= 0f) 0f else targetWeightKg / (meters * meters)
        }

    /** Đổi đơn vị cân nặng: quy đổi cả cân hiện tại lẫn cân mục tiêu đã chọn. */
    fun switchWeightUnit(metric: Boolean) {
        if (metric == weightMetric) return
        fun convert(v: Int) = if (metric) (v / LB_PER_KG).roundToInt() else (v * LB_PER_KG).roundToInt()
        weightValue = convert(weightValue)
        if (targetValue > 0) targetValue = convert(targetValue)
        weightMetric = metric
    }

    val weightDeltaKg: Float get() = kotlin.math.abs(targetWeightKg - weightKg).round1()

    /** Số ngày dự kiến đạt mục tiêu ở lần dự đoán đầu (step14). */
    val baseDays: Int
        get() = (weightDeltaKg / KG_PER_DAY).roundToInt().coerceIn(21, 120)

    /** Số ngày rút ngắn được sau khi trả lời thêm về lối sống (step28). */
    val earlyDays: Int
        get() {
            val frequency = sliderStops[ObId.FREQUENCY] ?: 2
            val activity = sliderStops[ObId.ACTIVITY] ?: 2
            return (4 + frequency * 2 + activity).coerceIn(4, baseDays / 2)
        }

    /** Không bao giờ nhanh hơn mức an toàn [MAX_KG_PER_DAY] (~0.7 kg/tuần). */
    val fasterDays: Int
        get() {
            val safest = kotlin.math.ceil(weightDeltaKg / MAX_KG_PER_DAY).toInt()
            return maxOf(baseDays - earlyDays, safest, 14).coerceAtMost(baseDays)
        }

    /** Số ngày thực sự rút ngắn được so với dự đoán đầu. */
    val savedDays: Int get() = (baseDays - fasterDays).coerceAtLeast(0)

    fun targetDate(days: Int): Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, days)
    }.time

    // ------------------------------------------------------------ điều hướng

    val step: OnboardStep get() = steps[index.coerceIn(0, steps.lastIndex)]

    fun configure(assignedVariant: OnboardingVariant, all: List<OnboardStep>) {
        if (configured) return
        variant = assignedVariant
        allSteps = all
        steps = OnboardingStepFactory.create(variant, all)
        index = index.coerceIn(0, steps.lastIndex)
        configured = true
    }

    /** @return true khi đang ở màn cuối và flow cần được hoàn thành. */
    fun advance(): Boolean {
        if (index >= steps.lastIndex) return true
        index += 1
        return false
    }

    fun moveBack(): Boolean {
        if (index <= 0) return false
        index -= 1
        return true
    }

    /** Tiến độ 0f..1f bên trong phần hiện tại, để vẽ thanh trên cùng. */
    fun sectionFraction(): Float {
        val section = step.section
        if (section !in 1..SECTION_COUNT) return 0f
        val sectionIndexes = steps.indices.filter { steps[it].section == section }
        val first = sectionIndexes.firstOrNull() ?: return 0f
        val last = sectionIndexes.lastOrNull() ?: return 0f
        if (last == first) return 1f
        return (index - first + 1).toFloat() / (last - first + 1).toFloat()
    }

    /** Màn đầu tiên của phần kế tiếp, dùng cho nút Skip. */
    fun nextSectionIndex(): Int {
        val section = step.section
        if (section !in 1..SECTION_COUNT) return steps.lastIndex
        for (next in section + 1..SECTION_COUNT) {
            steps.indexOfFirst { it.section == next }
                .takeIf { it >= 0 }
                ?.let { return it }
        }
        // đã ở phần cuối: nhảy thẳng tới màn tạo kế hoạch
        return steps.indexOfFirst { it.kind == StepKind.GENERATING }
            .takeIf { it >= 0 } ?: steps.lastIndex
    }

    fun optionLabel(id: String, key: String?, fallback: String): String =
        steps.firstOrNull { it.id == id }
            ?.options
            ?.firstOrNull { it.key == key }
            ?.label
            ?: allSteps.firstOrNull { it.id == id }
                ?.options
                ?.firstOrNull { it.key == key }
                ?.label
            ?: fallback

    // ------------------------------------------------------------ lưu trữ

    /** Ghi các lựa chọn đã thu thập vào [MyPreference] khi kết thúc luồng. */
    fun persist(preference: MyPreference) {
        preference.setGender(0)
        preference.setAge(age)
        preference.setUnitKg(weightMetric)
        preference.setCurrentHeight(heightCm.toFloat())
        preference.setCurrentWeight(weightKg)
        preference.setTargetWeightKg(targetWeightKg)

        answers[ObId.GOAL]?.let { preference.setUserGoal(it) }
        answers[ObId.FOCUS]?.let {
            preference.setUserFocusAreas(it)
            // ProgramRecommenderV3 (gợi ý bài ở Home) đọc USER_TARGET_FOCUS theo khoá của màn setup cũ
            targetFocusFor(it)?.let(preference::setUserTargetFocus)
        }
        answers[ObId.WORKOUT_LEVEL]?.let {
            preference.setUserExperience(
                when (it) {
                    "easy" -> "beginner"
                    "light_sweat" -> "intermediate"
                    else -> "advanced"
                }
            )
        }
        preference.setOnboarded(true)
        preference.setFirstOpen(false)
    }

    companion object {
        const val LB_PER_KG = 2.20462f
        /** Tốc độ dự đoán mặc định ~0.6 kg/tuần. */
        const val KG_PER_DAY = 0.085f

        /** Tốc độ tối đa được phép hiển thị ~0.7 kg/tuần. */
        private const val MAX_KG_PER_DAY = 0.1f
        val FREQUENCY_PER_WEEK = intArrayOf(1, 2, 3, 5)

        /** Vùng tập mới → khoá cũ (abs / arm / butt / thigh / body); chọn nhiều thì ưu tiên vùng cụ thể đầu tiên. */
        fun targetFocusFor(areas: String): String? {
            val keys = areas.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val area = keys.firstOrNull { it != "full_body" } ?: keys.firstOrNull() ?: return null
            return when (area) {
                "belly" -> "abs"
                "arm" -> "arm"
                "butt" -> "butt"
                "leg" -> "thigh"
                "full_body" -> "body"
                else -> null
            }
        }


        // Ngày theo ngôn ngữ app (LanguageHelper đã Locale.setDefault): "Oct 31", "31 thg 10", "10月31日"…
        fun formatDate(date: Date): String = format(date, "MMMdd")
        fun formatDateShort(date: Date): String = format(date, "MMMd")

        private fun format(date: Date, skeleton: String): String {
            val locale = Locale.getDefault()
            return SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(date)
        }
    }
}

fun Float.round1(): Float = (this * 10f).roundToInt() / 10f

fun Float.format1(): String = String.format(Locale.US, "%.1f", this)
