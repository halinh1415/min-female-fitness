package mint.woman.female.workout.activity.ui.workout

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.model.ProgramObject
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Calendar
import java.util.Locale

/**
 * Ngày nghỉ của thử thách (ngày không có động tác). Mở từ ChallengeActivity / ProgramActivity / MyWorkoutActivity với
 * PROGRAM_ID + DAY_NUMBER (0-based). "Hoàn thành ngày nghỉ" tăng tiến độ như tập xong một ngày; có gợi ý giãn cơ nhẹ
 * (không bắt buộc, không tính vào thử thách).
 */
class RestTimeActivity : AppCompatActivity(), EdgeToEdgeHelper.HandlesInsets {

    private lateinit var myPreference: MyPreference
    private var program: ProgramObject? = null
    private var indexDay = 0
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Header màu hồng: icon status bar màu trắng.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        myPreference = MyPreference(this)
        val extras = intent.extras
        val programId = extras?.getInt("PROGRAM_ID", 0) ?: 0
        indexDay = extras?.getInt("DAY_NUMBER", 0) ?: 0
        program = runCatching { FitnessApplication.getApplication().getProgram(programId) }.getOrNull()
        // Gợi ý giãn cơ: tối giãn cơ trước ngủ, ban ngày giảm đau lưng (đều nhẹ, không nhảy).
        val stretchId = if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 18) STRETCH_EVENING else STRETCH_DAY
        val stretch = runCatching { FitnessApplication.getApplication().getProgram(stretchId) }.getOrNull()
        setContent {
            MinTheme {
                RestDayScreen(
                    program = program,
                    day = indexDay,
                    stretch = stretch,
                    onBack = ::finish,
                    onFinish = ::finishRestDay,
                    onStretch = { openStretch(stretchId) },
                    banner = { AdBanner() },
                )
            }
        }
    }

    private fun finishRestDay() {
        program?.let {
            if (indexDay + 1 > myPreference.getPlanProgress(it.id)) myPreference.setPlanProgress(it.id, indexDay + 1)
        }
        finish()
    }

    private fun openStretch(id: Int) {
        startActivity(
            Intent(this, PreviewActivity::class.java).putExtras(
                Bundle().apply {
                    putInt("PROGRAM_ID", id)
                    putInt("DAY_NUMBER", 0)
                },
            ),
        )
    }

    @Composable
    private fun AdBanner() {
        if (!(myPreference.isShowAds && myPreference.ok)) return
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = getString(R.string.ad_banner_unit_id)
                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language),
        )
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private companion object {
        /** SLEEPY TIME STRETCH */
        const val STRETCH_EVENING = 19
        /** Lower back pain relief */
        const val STRETCH_DAY = 201
    }
}

private val Teal get() = MinColor.Pink
private val TealLight get() = MinColor.PinkLight

@Composable
private fun RestDayScreen(
    program: ProgramObject?,
    day: Int,
    stretch: ProgramObject?,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onStretch: () -> Unit,
    banner: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MinColor.Background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // Header: nền teal dịu, ly cà phê lớn, "Ngày nghỉ" + tiến độ thử thách.
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Teal, TealLight)))
                    .statusBarsPadding()
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    "←",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack)
                        .padding(top = 6.dp),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 44.dp, start = 24.dp, end = 24.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(112.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                        ) { Text("☕", fontSize = 44.sp) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.rest_day_title),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.rest_day_sub),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                    )
                    if (program != null && program.total > 1) {
                        Spacer(Modifier.height(18.dp))
                        val total = program.total
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                program.name.orEmpty(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                stringResource(R.string.rest_day_progress, day + 1, total),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.3f))) {
                            Box(Modifier.fillMaxWidth(((day + 1f) / total).coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(Color.White))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Tip("💧", stringResource(R.string.rest_tip_water))
                Tip("🚶‍♀️", stringResource(R.string.rest_tip_walk))
                Tip("😴", stringResource(R.string.rest_tip_sleep))
            }

            if (stretch != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.rest_stretch_title),
                    color = MinColor.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onStretch),
                ) {
                    GlideImage(stretch.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = stretch.fallbackBackgroundUrl())
                    GlideImage(stretch.modelUrl(), Modifier.fillMaxSize().padding(top = 8.dp), scaleType = ImageView.ScaleType.FIT_END)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0f to Color.White.copy(alpha = 0.92f),
                                0.55f to Color.White.copy(alpha = 0.55f),
                                0.8f to Color.Transparent,
                            ),
                        ),
                    )
                    Column(Modifier.align(Alignment.CenterStart).padding(start = 18.dp).width(200.dp)) {
                        Text(stretch.name.orEmpty(), color = Color(0xFF1F2A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text("${stretch.total} " + stringResource(R.string.minutes).lowercase(), color = Color(0xFF55636A), fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(Modifier.fillMaxWidth().background(MinColor.Surface).navigationBarsPadding()) {
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(56.dp),
            ) {
                Text("✓  " + stringResource(R.string.rest_day_done), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
            banner()
        }
    }
}

@Composable
private fun Tip(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MinColor.Surface)
            .border(1.dp, MinColor.Track, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(MinColor.PinkSoft),
        ) { Text(icon, fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Text(text, color = MinColor.TextSecondary, fontSize = 15.sp, lineHeight = 20.sp)
    }
}
