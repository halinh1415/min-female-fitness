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

    var variant by mutableStateOf(OnboardingVariant.CONTROL_46)
        private set

    var steps by mutableStateOf(ONBOARD_STEPS)
        private set

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

    // ------------------------------------------------------------ dẫn xuất

    val weightKg: Float
        get() = if (weightMetric) weightValue.toFloat() else weightValue / LB_PER_KG

    val heightMeters: Float get() = heightCm / 100f

    val bmi: Float
        get() {
            val meters = heightMeters
            return if (meters <= 0f) 0f else weightKg / (meters * meters)
        }

    /** Cân nặng mục tiêu (kg) – phụ thuộc mục tiêu chính người dùng chọn. */
    val targetWeightKg: Float
        get() {
            val current = weightKg
            return when (answers[ObId.GOAL]) {
                "build_muscle" -> current + max(1.5f, current * 0.02f)
                "keep_fit" -> current - max(1f, current * 0.015f)
                else -> current - max(2f, current * 0.035f)
            }.round1()
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

    val fasterDays: Int get() = (baseDays - earlyDays).coerceAtLeast(14)

    fun targetDate(days: Int): Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, days)
    }.time

    // ------------------------------------------------------------ điều hướng

    val step: OnboardStep get() = steps[index.coerceIn(0, steps.lastIndex)]

    fun configure(assignedVariant: OnboardingVariant) {
        if (configured) return
        variant = assignedVariant
        steps = OnboardingStepFactory.create(variant, answers)
        index = index.coerceIn(0, steps.lastIndex)
        configured = true
    }

    /**
     * Làm mới nhánh adaptive sau khi câu trả lời hiện tại đã được ghi, rồi đi tiếp.
     * @return true khi đang ở màn cuối và flow cần được hoàn thành.
     */
    fun advance(): Boolean {
        refreshStepsKeepingCurrent()
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
        refreshStepsKeepingCurrent()
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
            ?: ONBOARD_STEPS.firstOrNull { it.id == id }
                ?.options
                ?.firstOrNull { it.key == key }
                ?.label
            ?: fallback

    private fun refreshStepsKeepingCurrent() {
        if (!configured) return
        val currentKey = step.stableKey
        val updated = OnboardingStepFactory.create(variant, answers)
        if (updated.map(OnboardStep::stableKey) == steps.map(OnboardStep::stableKey)) return

        steps = updated
        index = updated.indexOfFirst { it.stableKey == currentKey }
            .takeIf { it >= 0 }
            ?: index.coerceIn(0, updated.lastIndex)
    }

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
        answers[ObId.FOCUS]?.let { preference.setUserFocusAreas(it) }
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
        private const val KG_PER_DAY = 0.085f
        val FREQUENCY_PER_WEEK = intArrayOf(1, 2, 3, 5)

        private val DATE_FORMAT = SimpleDateFormat("MMM dd", Locale.ENGLISH)
        private val DATE_FORMAT_SHORT = SimpleDateFormat("MMM d", Locale.ENGLISH)

        fun formatDate(date: Date): String = DATE_FORMAT.format(date)
        fun formatDateShort(date: Date): String = DATE_FORMAT_SHORT.format(date)
    }
}

fun Float.round1(): Float = (this * 10f).roundToInt() / 10f

fun Float.format1(): String = String.format(Locale.US, "%.1f", this)
