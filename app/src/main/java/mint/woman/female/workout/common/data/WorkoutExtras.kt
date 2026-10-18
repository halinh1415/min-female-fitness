package mint.woman.female.workout.common.data

import mint.woman.female.workout.model.ExerciseObject
import mint.woman.female.workout.model.PlanObject
import mint.woman.female.workout.model.ProgramObject
import java.util.Locale

/**
 * Phần thêm quanh danh sách động tác của một buổi tập (giống Leap Fitness), chỉ dùng động tác có sẵn:
 * - Khởi động (3 động tác) ở đầu và Giãn cơ (5–6 động tác theo vùng tập, đủ cặp trái / phải) ở cuối, mỗi động tác 30 giây.
 * - Động tác khởi động (classid 7) có sẵn ở đầu bài và động tác giãn cơ (classid 8) có sẵn ở cuối bài được gắn nhãn
 *   Khởi động / Giãn cơ; bài đã tự kết thúc bằng ≥ 3 động tác giãn cơ thì không thêm giãn cơ nữa.
 * - "Không nhảy" (người đau gối / lưng chọn ở onboarding hoặc Cài đặt): đổi động tác bật nhảy sang
 *   động tác cùng vùng không nhảy, giữ nguyên thời gian / số lần của bài.
 */
object WorkoutExtras {

    const val SECTION_MAIN = 0
    const val SECTION_WARM_UP = 1
    const val SECTION_COOL_DOWN = 2

    private const val EXTRA_SECONDS = 30
    private const val EXTRA_REST = 10

    /** classid 7 = khởi động / vận động khớp (xoay tay, cuộn vai...). */
    private const val CLASS_WARM_UP = 7
    /** classid 8 = giãn cơ: bài mà phần lớn là giãn cơ thì không thêm khởi động / giãn cơ. */
    private const val CLASS_STRETCH = 8
    /** Bài đã có sẵn từng này động tác giãn cơ ở cuối thì coi như đã có phần giãn cơ. */
    private const val OWN_COOL_DOWN_MIN = 3

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
        // căng vai, căng cơ tay trái / phải, cơ tay trước trái / phải, đan tay sau lưng
        Area.ARMS -> listOf(155, 203, 204, 268, 269, 300)
        // hổ mang, con bò con mèo, vặn sống lưng phải / trái, tư thế đứa trẻ
        Area.BELLY -> listOf(198, 141, 196, 197, 144)
        // duỗi mông trái / phải, ép gối lên ngực trái / phải, tư thế đứa trẻ
        Area.BUTT -> listOf(330, 331, 259, 260, 144)
        // đùi trước trái / phải (dựa tường), đùi sau trái / phải, bắp chân trái / phải
        Area.LEGS -> listOf(218, 219, 332, 333, 201, 202)
        // toàn thân: hổ mang, căng vai, vặn sống lưng phải / trái, tư thế đứa trẻ
        null -> listOf(198, 155, 196, 197, 144)
    }

    /** Gắn nhãn động tác khởi động có sẵn ở đầu bài và giãn cơ có sẵn ở cuối bài; trả về số động tác giãn cơ ở cuối. */
    private fun markOwnSections(list: List<PlanObject.ActionObject>, exercises: Map<Int, ExerciseObject>): Int {
        if (list.count { exercises[it.actionId]?.classid == CLASS_STRETCH } * 2 >= list.size) return 0
        for (action in list) {
            if (exercises[action.actionId]?.classid != CLASS_WARM_UP) break
            action.section = SECTION_WARM_UP
        }
        var coolDown = 0
        for (action in list.asReversed()) {
            if (exercises[action.actionId]?.classid != CLASS_STRETCH) break
            action.section = SECTION_COOL_DOWN
            coolDown++
        }
        return coolDown
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
        val ownCoolDown = markOwnSections(list, exercises)
        if (!warmUpCoolDown || !supportsWarmUp(list, exercises, program)) return

        val head = list.take(3).map { it.actionId }.toSet()
        val tail = list.map { it.actionId }.toSet()
        val warmUp = listOf(if (noJump) LOW_IMPACT_CARDIO else JUMPING_JACKS, ARM_CIRCLES_CW, ARM_CIRCLES_CCW)
            .filter { it !in head && exercises.containsKey(it) }
            .map { extra(it, SECTION_WARM_UP) }
        val coolDown = if (ownCoolDown >= OWN_COOL_DOWN_MIN) emptyList() else coolDownFor(areaOf(program))
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
