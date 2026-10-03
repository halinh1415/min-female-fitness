package mint.woman.female.workout.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Locale

/** "Xem thêm" của một mục ở Home / Discover: danh sách đầy đủ các bài của mục đó (Compose). */
class ExploreDetailActivity : AppCompatActivity() {

    private var isPremium by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val all = FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap
        val programs = (intent.getIntArrayExtra(EXTRA_PROGRAM_IDS) ?: IntArray(0)).toList().mapNotNull { all[it] }
        setContent {
            MinTheme { ExploreDetailScreen(title, programs, isPremium, ::finish, ::openProgram) }
        }
    }

    override fun onResume() {
        super.onResume()
        isPremium = MyPreference(this).isPremiumMember
    }

    private fun openProgram(program: ProgramObject) {
        val intent = when (program.type) {
            1 -> Intent(this, ChallengeActivity::class.java)
                .putExtras(Bundle().apply { putInt("PROGRAM_ID", program.id) })
            2 -> Intent(this, PreviewActivity::class.java)
                .putExtras(Bundle().apply {
                    putInt("PROGRAM_ID", program.id)
                    putInt("DAY", 0)
                })
            else -> null
        }
        intent?.let(::startActivity)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language)
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

    companion object {
        const val EXTRA_TITLE = "PLAN_LIST_NAME"
        const val EXTRA_PROGRAM_IDS = "PROGRAM_IDS"

        @JvmStatic
        fun intent(context: Context, title: String, programs: List<ProgramObject>): Intent =
            Intent(context, ExploreDetailActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_PROGRAM_IDS, programs.map { it.id }.toIntArray())
    }
}

@Composable
private fun ExploreDetailScreen(
    title: String,
    programs: List<ProgramObject>,
    isPremium: Boolean,
    onBack: () -> Unit,
    onProgramClick: (ProgramObject) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MinColor.Pink)
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("←", fontSize = 24.sp, color = Color.White) }
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp)
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(programs, key = { it.id }) { program ->
                ProgramRow(program, isPremium) { onProgramClick(program) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProgramRow(program: ProgramObject, isPremium: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .clipToBounds()
            .clickable(onClick = onClick)
    ) {
        GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
        GlideImage(
            program.modelUrl(),
            Modifier
                .fillMaxHeight()
                .width(170.dp)
                .align(Alignment.BottomEnd),
            scaleType = ImageView.ScaleType.FIT_END
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        0.6f to Color.Black.copy(alpha = 0.12f),
                        1f to Color.Transparent
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.CenterStart)
                .padding(18.dp)
                .fillMaxWidth(0.6f)
        ) {
            Text(
                listOfNotNull(program.name, program.subtitle?.takeIf { it.isNotBlank() }).joinToString(" "),
                color = Color.White,
                fontSize = 20.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (program.type == 1) "🏆 " + stringResource(R.string.txt_challenge_7x4)
                else "⏱ " + stringResource(R.string.x_minutes, program.total),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
        }
        if (program.premium == 1 && !isPremium) {
            Text(
                "👑",
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}
