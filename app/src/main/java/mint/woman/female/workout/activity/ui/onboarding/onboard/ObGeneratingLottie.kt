package mint.woman.female.workout.activity.ui.onboarding.onboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView

/**
 * Màn "Building your plan" dùng lại bộ Lottie của Tai Chi Qigong (assets/lt/part1..3 + vòng
 * guide_plan_generate.json). Ảnh trong lt/partN/images hiện vẫn là ảnh Tai Chi (tạm) — thay bằng
 * file cùng tên là xong, không cần sửa code.
 */
internal const val GENERATING_RING_ANIMATION = "guide_plan_generate.json"

internal enum class GeneratingStage(val folder: String, val speed: Float) {
    COMMUNITY("lt/part1", 1f),
    EXERCISES("lt/part2", 0.6f),
    RATING("lt/part3", 1f)
}

/** Vòng loading lặp vô hạn phía sau con số %. */
@Composable
internal fun GeneratingRingLottie(modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            LottieAnimationView(ctx).apply {
                setAnimation(GENERATING_RING_ANIMATION)
                repeatCount = -1
                playAnimation()
            }
        }
    )
}

/** Một pha của màn tạo kế hoạch, chạy một lần. */
@Composable
internal fun GeneratingStageLottie(stage: GeneratingStage, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        // Các stage kèm bitmap, chỉ chạy một lần, không giữ trong cache tĩnh của Lottie.
        factory = { ctx ->
            LottieAnimationView(ctx).apply {
                setCacheComposition(false)
                // Ảnh thay sau này có độ phân giải khác vẫn vẽ đúng khung của asset trong data.json.
                setMaintainOriginalImageBounds(true)
                imageAssetsFolder = "${stage.folder}/images"
                setAnimation("${stage.folder}/data.json")
                speed = stage.speed
                playAnimation()
            }
        }
    )
}
