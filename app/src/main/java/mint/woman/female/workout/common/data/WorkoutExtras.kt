package mint.woman.female.workout.common.data

import mint.woman.female.workout.model.ExerciseObject
import mint.woman.female.workout.model.PlanObject
import mint.woman.female.workout.model.ProgramObject
import java.util.Locale

/**
 * Phần thêm quanh danh sách động tác của một buổi tập (giống Leap Fitness), chỉ dùng động tác có sẵn:
 * - Khởi động (3 động tác) ở đầu và Giãn cơ (3 động tác theo vùng tập) ở cuối, mỗi động tác 30 giây.
 * - "Không nhảy" (người đau gối / lưng chọn ở onboarding hoặc Cài đặt): đổi động tác bật nhảy sang
 *   động tác cùng vùng không nhảy, giữ nguyên thời gian / số lần của bài.
 */
object WorkoutExtras {

    const val SECTION_MAIN = 0
    const val SECTION_WARM_UP = 1
    const val SECTION_COOL_DOWN = 2

    private const val EXTRA_SECONDS = 30
    private const val EXTRA_REST = 10

    /** classid 8 = giãn cơ: bài mà phần lớn là giãn cơ thì không thêm khởi động / giãn cơ. */
    private const val CLASS_STRETCH = 8

    private const val JUMPING_JACKS = 15
    /** Đấm tay tại chỗ: cardio nhẹ không nhảy (dữ liệu bài tập không có "High knee with twist"). */
    private const val LOW_IMPACT_CARDIO = 62
    private const val ARM_CIRCLES_CW = 258
    private const val ARM_CIRCLES_CCW = 136

    /** Động tác bật nhảy -> động tác không nhảy cùng vùng (id theo dữ liệu bài tập của app nữ). */
    private val NO_JUMP = mapOf(
        JUMPING_JACKS to LOW_IMPACT_CARDIO,
        33 to LOW_IMPACT_CARDIO, // Bước cao
        275 to LOW_IMPACT_CARDIO, // Nhảy dây không dây
        140 to LOW_IMPACT_CARDIO, // Đá mông
        26 to 21, // Burpees -> Gánh đùi
        282 to 21, // Burpee điều chỉnh -> Gánh đùi
        40 to 21, // Gánh tạ nhảy -> Gánh đùi
        87 to 22, // Nhảy sang bên -> Chùng chân ngang
    )

    private enum class Area { BELLY, ARMS, BUTT, LEGS }

    /** Vùng tập của chương trình theo trường focus (null = toàn thân / chung). */
    private fun areaOf(p: ProgramObject): Area? = when (p.focus.orEmpty().lowercase(Locale.US)) {
        "abs" -> Area.BELLY
        "arm" -> Area.ARMS
        "butt" -> Area.BUTT
        "thigh", "leg" -> Area.LEGS
        else -> null
    }

    private fun coolDownFor(area: Area?): List<Int> = when (area) {
        Area.ARMS -> listOf(155, 203, 204) // vai, tay sau trái / phải
        Area.BELLY -> listOf(198, 196, 197) // rắn hổ mang, vặn cột sống phải / trái
        Area.BUTT -> listOf(330, 331, 144) // duỗi mông trái / phải, tư thế em bé
        Area.LEGS -> listOf(218, 219, 144) // đùi trước trái / phải, tư thế em bé
        null -> listOf(198, 144, 155)
    }

    @JvmStatic
    fun apply(
        list: MutableList<PlanObject.ActionObject>,
        exercises: Map<Int, ExerciseObject>,
        program: ProgramObject,
        warmUpCoolDown: Boolean,
        noJump: Boolean,
    ) {
        if (noJump) {
            list.forEach { action ->
                val swap = NO_JUMP[action.actionId]
                if (swap != null && exercises.containsKey(swap)) action.actionId = swap
            }
        }
        if (!warmUpCoolDown || !supportsWarmUp(list, exercises, program)) return

        val head = list.take(3).map { it.actionId }.toSet()
        val tail = list.takeLast(3).map { it.actionId }.toSet()
        val warmUp = listOf(if (noJump) LOW_IMPACT_CARDIO else JUMPING_JACKS, ARM_CIRCLES_CW, ARM_CIRCLES_CCW)
            .filter { it !in head && exercises.containsKey(it) }
            .map { extra(it, SECTION_WARM_UP) }
        val coolDown = coolDownFor(areaOf(program))
            .filter { it !in tail && exercises.containsKey(it) }
            .map { extra(it, SECTION_COOL_DOWN) }
        list.addAll(0, warmUp)
        list.addAll(coolDown)
    }

    /** Có nên thêm khởi động / giãn cơ: bài có sẵn của app (không phải bài tự tạo / Discover tải về) và không phải bài giãn cơ. */
    @JvmStatic
    fun supportsWarmUp(list: List<PlanObject.ActionObject>, exercises: Map<Int, ExerciseObject>, program: ProgramObject): Boolean {
        if (program.discover != 0 || program.id >= 2000 || list.isEmpty()) return false
        val stretches = list.count { exercises[it.actionId]?.classid == CLASS_STRETCH }
        return stretches * 2 < list.size
    }

    private fun extra(actionId: Int, section: Int) =
        PlanObject.ActionObject(actionId, EXTRA_SECONDS, "s", 0, EXTRA_REST).also { it.section = section }
}
