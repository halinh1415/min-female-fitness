package mint.woman.female.workout.custom

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import kotlin.random.Random

/**
 * Hộp thoát giữa buổi tập, 2 bước: (1) nhắc tiến độ + câu động viên, mặc định "Tiếp tục tập";
 * (2) bấm "Dừng buổi tập" thì hỏi lý do, chạm một lý do là thoát luôn.
 *
 * Analytics: quit_dialog_show, quit_dialog_continue (step = bước người dùng quay lại tập),
 * workout_quit (reason, program_id, day, done, total, progress_pct, elapsed_sec).
 */
class DialogQuit : DialogFragment() {

    private var listener: OnQuitListener? = null
    private var quitting = false
    private var step = 0

    fun setQuitListener(listener: OnQuitListener?) {
        this.listener = listener
    }

    // Sau khi xoay màn hình listener cũ mất: dùng activity (DoWorkoutActivity implement OnQuitListener).
    private fun quitListener(): OnQuitListener? = listener ?: activity as? OnQuitListener

    private val args get() = requireArguments()
    private val programId get() = args.getInt(ARG_PROGRAM_ID)
    private val done get() = args.getInt(ARG_DONE)
    private val total get() = args.getInt(ARG_TOTAL)
    private val progressPct get() = if (total > 0) done * 100 / total else 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            log("quit_dialog_show", bundleOf("program_id" to programId, "progress_pct" to progressPct))
        } else {
            step = savedInstanceState.getInt(STATE_STEP)
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MinTheme {
                    QuitContent(
                        done = done,
                        total = total,
                        onStep = { step = it },
                        onContinue = ::keepGoing,
                        onQuit = ::quit,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_STEP, step)
    }

    // Chạm ra ngoài / nút Back: tập tiếp.
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        log("quit_dialog_continue", bundleOf("step" to step, "via" to "cancel"))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!quitting && activity?.isChangingConfigurations != true) quitListener()?.onNoQuitWorkout()
    }

    private fun keepGoing() {
        log("quit_dialog_continue", bundleOf("step" to step, "via" to "button"))
        dismissAllowingStateLoss()
    }

    private fun quit(reason: QuitReason?) {
        if (quitting) return // chạm hai lần: không lưu lịch sử / đóng màn hai lần
        quitting = true
        val key = reason?.key ?: "skip"
        log(
            "workout_quit",
            bundleOf(
                "reason" to key,
                "program_id" to programId,
                "day" to args.getInt(ARG_DAY),
                "done" to done,
                "total" to total,
                "progress_pct" to progressPct,
                "elapsed_sec" to args.getLong(ARG_ELAPSED_SEC),
            ),
        )
        val target = quitListener()
        dismissAllowingStateLoss()
        target?.onQuitWorkout()
    }

    private fun log(event: String, params: Bundle) {
        val context = context ?: return
        FirebaseAnalytics.getInstance(context).logEvent(event, params)
    }

    interface OnQuitListener {
        fun onQuitWorkout()

        fun onNoQuitWorkout()
    }

    companion object {
        private const val ARG_PROGRAM_ID = "program_id"
        private const val ARG_DAY = "day"
        private const val ARG_DONE = "done"
        private const val ARG_TOTAL = "total"
        private const val ARG_ELAPSED_SEC = "elapsed_sec"
        private const val STATE_STEP = "step"

        @JvmStatic
        fun newInstance(programId: Int, day: Int, done: Int, total: Int, elapsedSec: Long) = DialogQuit().apply {
            arguments = bundleOf(
                ARG_PROGRAM_ID to programId,
                ARG_DAY to day,
                ARG_DONE to done,
                ARG_TOTAL to total,
                ARG_ELAPSED_SEC to elapsedSec,
            )
        }
    }
}

/** Lý do thoát; [key] là giá trị gửi Firebase. */
private enum class QuitReason(val key: String, val emoji: String, @StringRes val label: Int) {
    TooHard("too_hard", "😣", R.string.quit_reason_too_hard),
    TooEasy("too_easy", "🥱", R.string.quit_reason_too_easy),
    NoTime("no_time", "⏰", R.string.quit_reason_no_time),
    Tired("tired", "🤕", R.string.quit_reason_tired),
    Dislike("dislike", "👎", R.string.quit_reason_dislike),
    JustLooking("just_looking", "👀", R.string.quit_reason_just_looking),
    Other("other", "💬", R.string.quit_reason_other),
}

@Composable
private fun QuitContent(
    done: Int,
    total: Int,
    onStep: (Int) -> Unit,
    onContinue: () -> Unit,
    onQuit: (QuitReason?) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MinColor.Surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        if (step == 0) {
            ConfirmStep(done, total, onContinue) {
                step = 1
                onStep(1)
            }
        } else {
            ReasonStep(onContinue, onQuit)
        }
    }
}

@Composable
private fun ConfirmStep(done: Int, total: Int, onContinue: () -> Unit, onExit: () -> Unit) {
    val quotes = stringArrayResource(R.array.arr_quit)
    val quoteIndex = rememberSaveable { Random.nextInt(quotes.size) }
    Box(Modifier.size(64.dp).clip(CircleShape).background(MinColor.PinkSoft), contentAlignment = Alignment.Center) {
        Text("💪", fontSize = 30.sp)
    }
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.txt_do_you_quit),
        color = MinColor.TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    if (total > 0) {
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.quit_progress, done, total), color = MinColor.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (done.toFloat() / total).coerceIn(0f, 1f) },
            color = MinColor.Pink,
            trackColor = MinColor.Track,
            drawStopIndicator = {},
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
        )
    }
    Spacer(Modifier.height(16.dp))
    Text(
        "“" + quotes[quoteIndex.coerceIn(0, quotes.lastIndex)] + "”",
        color = MinColor.Pink,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MinColor.PinkSoft)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.txt_press_tired), color = MinColor.TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = onContinue,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = MinColor.Pink, contentColor = androidx.compose.ui.graphics.Color.White),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) { Text("▶  " + stringResource(R.string.quit_keep_going), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.quit_exit), color = MinColor.TextMuted, fontSize = 15.sp)
    }
}

@Composable
private fun ReasonStep(onContinue: () -> Unit, onQuit: (QuitReason?) -> Unit) {
    Text(
        stringResource(R.string.quit_reason_title),
        color = MinColor.TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.quit_reason_sub), color = MinColor.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        QuitReason.entries.forEach { reason ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MinColor.Background)
                    .border(1.dp, MinColor.Track, RoundedCornerShape(14.dp))
                    .clickable { onQuit(reason) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(reason.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(reason.label), color = MinColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onContinue,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.5.dp, MinColor.Pink),
            contentPadding = PaddingValues(vertical = 12.dp),
            modifier = Modifier.weight(1.4f),
        ) { Text("▶  " + stringResource(R.string.quit_keep_going), color = MinColor.Pink, fontWeight = FontWeight.Bold) }
        TextButton(onClick = { onQuit(null) }, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.quit_skip), color = MinColor.TextMuted, fontSize = 15.sp)
        }
    }
}
