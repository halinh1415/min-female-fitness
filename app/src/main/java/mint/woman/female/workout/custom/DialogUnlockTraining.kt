package mint.woman.female.workout.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.model.ProgramObject

/**
 * Chặn bài premium ở Preview (Compose). Ưu tiên in-app: ảnh bài + lợi ích Premium + nút "Đi trả phí"
 * nổi bật (mở PremiumActivity, giá hiển thị ở đó); "Xem video" chỉ là lựa chọn phụ, mở khoá một lần,
 * ẩn khi bài chỉ dành cho Premium hoặc máy không được hiện quảng cáo.
 */
class DialogUnlockTraining : DialogFragment() {

    private var listener: OnUnlockTrainingListener? = null
    private var loadingAd by mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MinTheme {
                    UnlockSheet(
                        name = args.getString(ARG_NAME).orEmpty(),
                        background = args.getString(ARG_BACKGROUND).orEmpty(),
                        fallback = args.getString(ARG_FALLBACK),
                        model = args.getString(ARG_MODEL).orEmpty(),
                        showWatchAd = args.getBoolean(ARG_SHOW_AD),
                        loadingAd = loadingAd,
                        onClose = {
                            dismiss()
                            listener?.backActivity()
                        },
                        onPremium = { listener?.goPremium() },
                        onWatchAd = {
                            if (!loadingAd) {
                                loadingAd = true
                                listener?.watchVideo()
                            }
                        },
                    )
                }
            }
        }
    }

    /** Quảng cáo đã tải xong / lỗi: cho bấm "Xem video" lại. */
    fun loadDone() {
        loadingAd = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnUnlockTrainingListener
            ?: throw RuntimeException("$context must implement OnUnlockTrainingListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog = dialog ?: return
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            WindowCompat.setDecorFitsSystemWindows(this, false)
        }
        dialog.setOnCancelListener { listener?.onCancel() }
    }

    interface OnUnlockTrainingListener {
        fun watchVideo()
        fun goPremium()
        fun backActivity()
        fun onCancel()
    }

    companion object {
        private const val ARG_NAME = "NAME"
        private const val ARG_BACKGROUND = "BACKGROUND"
        private const val ARG_FALLBACK = "FALLBACK"
        private const val ARG_MODEL = "MODEL"
        private const val ARG_SHOW_AD = "SHOW_AD"

        /** @param allowWatchAd false khi bài chỉ dành cho Premium hoặc không hiện quảng cáo. */
        @JvmStatic
        fun newInstance(program: ProgramObject, allowWatchAd: Boolean) = DialogUnlockTraining().apply {
            arguments = Bundle().apply {
                putString(ARG_NAME, program.name)
                putString(ARG_BACKGROUND, program.backgroundUrl())
                putString(ARG_FALLBACK, program.fallbackBackgroundUrl())
                putString(ARG_MODEL, program.modelUrl())
                putBoolean(ARG_SHOW_AD, allowWatchAd)
            }
        }
    }
}

@Composable
private fun UnlockSheet(
    name: String,
    background: String,
    fallback: String?,
    model: String,
    showWatchAd: Boolean,
    loadingAd: Boolean,
    onClose: () -> Unit,
    onPremium: () -> Unit,
    onWatchAd: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MinColor.Background)
                // Chặn chạm xuyên xuống lớp nền.
                .clickable(remember { MutableInteractionSource() }, indication = null) {}
                .navigationBarsPadding(),
        ) {
            Hero(name, background, fallback, model, onClose)
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Benefit(stringResource(R.string.pwb_check_1))
                Benefit(stringResource(R.string.pwb_check_2))
                Benefit(stringResource(R.string.pwb_check_3))
                Spacer(Modifier.height(18.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MinColor.CtaBrush)
                        .clickable(onClick = onPremium),
                ) {
                    Text(
                        "👑  " + stringResource(R.string.txt_go_premium).uppercase(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    stringResource(R.string.pw_cancel_anytime),
                    color = MinColor.TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                if (showWatchAd) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(enabled = !loadingAd, onClick = onWatchAd)
                            .padding(vertical = 12.dp),
                    ) {
                        if (loadingAd) {
                            CircularProgressIndicator(
                                color = MinColor.TextSecondary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                            )
                        } else {
                            Text("▶", color = MinColor.TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.txt_watch_video),
                            color = MinColor.TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(name: String, background: String, fallback: String?, model: String, onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        GlideImage(background, Modifier.fillMaxSize(), errorUrl = fallback)
        GlideImage(
            model,
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.BottomEnd)
                .padding(top = 24.dp),
            scaleType = ImageView.ScaleType.FIT_END,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.65f))),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onClose),
        ) {
            Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .fillMaxWidth(0.7f),
        ) {
            Text(
                "PREMIUM",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinColor.CtaBrush)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                name,
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(stringResource(R.string.txt_unlock_training), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MinColor.Pink),
        ) {
            Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = MinColor.TextPrimary, fontSize = 15.sp)
    }
}
