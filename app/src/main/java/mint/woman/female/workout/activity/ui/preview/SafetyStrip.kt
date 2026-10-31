package mint.woman.female.workout.activity.ui.preview

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.plan.ExerciseTags
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore
import mint.woman.female.workout.activity.ui.plan.PlanInjury
import mint.woman.female.workout.model.PlanObject
import mint.woman.female.workout.activity.ui.compose.MinColor

/**
 * Nhãn an toàn ở màn Preview (port từ app Tai Chi, 31/10/2026): ✓ / ⚠ theo nhãn gắn tay [ExerciseTags]
 * (gối, lưng dưới, tay-vai, cổ tay, bật nhảy). Plan đang tránh chấn thương mà buổi có bài hại vùng đó → khung cảnh báo
 * + nút đổi sang bài an toàn ([swap]). Xét cả khởi động / giãn cơ thêm vào; bài chưa gắn nhãn thì không khẳng định ✓.
 */
object SafetyStrip {
    private class Note(val text: Int, val warn: Boolean)

    /** Mọi bài đã được gắn nhãn (có trong một danh sách nào đó của [ExerciseTags]). */
    private val KNOWN: Set<Int> by lazy {
        ExerciseTags.FOCUS.values.flatten().toSet() + ExerciseTags.CARDIO + ExerciseTags.WARM_UP + ExerciseTags.COOL_DOWN +
            ExerciseTags.JUMP + ExerciseTags.KNEE + ExerciseTags.BACK + ExerciseTags.ARM_SHOULDER + ExerciseTags.WRIST +
            ExerciseTags.PAIRS.keys
    }

    /** Chấn thương đang tránh: theo plan cá nhân, chưa có plan thì theo câu hỏi đau khớp ở onboarding. */
    @JvmStatic
    fun injuries(context: Context): Set<PlanInjury> =
        (PersonalPlanStore.settings(context) ?: PersonalPlanStore.defaultsFromOnboarding(context)).injuries

    /** Bài hại ít nhất một chấn thương đang tránh. */
    private fun harmful(id: Int, injuries: Set<PlanInjury>) = injuries.any { id in ExerciseTags.blockedBy(it) }

    /** Nội dung thẻ (null = không có gì để hiện). */
    private class Model(val notes: List<Note>, val alert: List<PlanInjury>, val swappable: Boolean)

    private fun model(context: Context, actions: List<PlanObject.ActionObject>): Model? {
        val ids = actions.map { it.actionId }
        val injuries = injuries(context)
        val allKnown = ids.isNotEmpty() && ids.all { it in KNOWN }
        val notes = ArrayList<Note>()
        fun area(set: Set<Int>, ok: Int?, warn: Int) {
            if (ids.any { it in set }) notes += Note(warn, true) else if (allKnown && ok != null) notes += Note(ok, false)
        }
        area(ExerciseTags.KNEE, R.string.sf_easy_knees, R.string.sf_knee_load)
        area(ExerciseTags.BACK, R.string.sf_easy_back, R.string.sf_back_load)
        area(ExerciseTags.JUMP, R.string.sf_no_jump, R.string.sf_jump)
        area(ExerciseTags.WRIST, null, R.string.sf_wrist)
        val alert = injuries.filter { inj -> ids.any { it in ExerciseTags.blockedBy(inj) } }.sortedBy { it.ordinal }
        val swappable = alert.isNotEmpty() && ids.any { id -> harmful(id, injuries) && replacement(id, injuries, ids.toSet()) != null }
        return if (notes.isEmpty() && alert.isEmpty()) null else Model(notes, alert, swappable)
    }

    /** Thẻ nhãn an toàn dưới hàng thẻ tuỳ chọn của màn Preview; [revision] đổi thì tính lại. */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun SafetyCard(actions: List<PlanObject.ActionObject>, revision: Int, onSwap: () -> Unit) {
        val context = LocalContext.current
        val m = remember(actions, revision) { model(context, actions) } ?: return
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(MinColor.Surface).padding(12.dp),
        ) {
            if (m.alert.isNotEmpty()) {
                Column(
                    Modifier.padding(bottom = 10.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MinColor.Danger.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "⚠  " + stringResource(
                            if (m.swappable) R.string.sf_avoid_warn else R.string.sf_avoid_left,
                            m.alert.map { stringResource(PersonalPlanStore.injuryLabel(it)) }.joinToString(", "),
                        ),
                        color = MinColor.Danger, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
                    )
                    if (m.swappable) {
                        Text(
                            "⇄  " + stringResource(R.string.sf_swap), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp).clip(RoundedCornerShape(50)).background(MinColor.Danger)
                                .clickable(onClick = onSwap).padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                m.notes.forEach { n ->
                    val tint = if (n.warn) MinColor.Warning else MinColor.Ok
                    Text(
                        (if (n.warn) "⚠ " else "✓ ") + stringResource(n.text), color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(tint.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
        }
    }

    /** Các nhóm bài (vùng tập, cardio, khởi động, giãn cơ) chứa [id]: bài thay thế lấy trong cùng nhóm. */
    private fun groupsOf(id: Int): List<List<Int>> =
        (ExerciseTags.FOCUS.values + listOf(ExerciseTags.CARDIO, ExerciseTags.WARM_UP, ExerciseTags.COOL_DOWN)).filter { id in it }

    private fun replacement(id: Int, injuries: Set<PlanInjury>, used: Set<Int>, exists: (Int) -> Boolean = { true }): Int? {
        val jumpOk = id in ExerciseTags.JUMP
        // Không đổi chỗ đau này lấy rủi ro khác: ưu tiên bài ít nhãn tải (gối, lưng, tay-vai, cổ tay, nhảy) nhất.
        val hazards = { c: Int ->
            listOf(ExerciseTags.KNEE, ExerciseTags.BACK, ExerciseTags.ARM_SHOULDER, ExerciseTags.WRIST, ExerciseTags.JUMP).count { c in it }
        }
        return groupsOf(id).flatten().distinct().filter { c ->
            c !in used && c !in ExerciseTags.PAIRS && !harmful(c, injuries) && (jumpOk || c !in ExerciseTags.JUMP) && exists(c)
        }.minByOrNull(hazards)
    }

    /**
     * Đổi bài hại chấn thương đang tránh sang bài an toàn cùng nhóm (cùng kiểu đếm lần / tính giờ thì giữ số lần cũ).
     * [isTimed] (id) cho biết bài thay thế tính giờ hay không; [exists] = bài có trong dữ liệu. Trả về số bài đã đổi.
     */
    @JvmStatic
    fun swap(
        context: Context, actions: MutableList<PlanObject.ActionObject>,
        isTimed: (Int) -> Boolean?, replace: (Int, PlanObject.ActionObject) -> Unit,
    ): Int {
        val injuries = injuries(context)
        val used = actions.map { it.actionId }.toMutableSet()
        var count = 0
        actions.toList().forEachIndexed { index, a ->
            if (!harmful(a.actionId, injuries)) return@forEachIndexed
            val c = replacement(a.actionId, injuries, used) { isTimed(it) != null } ?: return@forEachIndexed
            val timed = isTimed(c) == true
            val sameKind = timed == a.unit.orEmpty().contains("s")
            val time = if (sameKind) a.time else if (timed) 30 else 12
            // Giữ phần của buổi (khởi động / giãn cơ thêm vào) để bản đồ cơ và tiến độ tính như cũ.
            replace(index, PlanObject.ActionObject(c, time, if (timed) "s" else "", 0, 10).also {
                it.section = a.section
                it.added = a.added
            })
            used += c
            count++
        }
        return count
    }
}
