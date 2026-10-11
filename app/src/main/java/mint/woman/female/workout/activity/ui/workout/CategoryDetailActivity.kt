package mint.woman.female.workout.activity.ui.workout

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.model.CategoryObject
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Locale

/**
 * Chi tiết một nhóm bài tập (vd "Bụng phẳng") ở Discover, Jetpack Compose: ảnh hero tràn viền kèm
 * tên + mô tả, bên dưới là các cấp độ (Người mới / Trung bình / Nâng cao) để mở Preview.
 * Nhóm được truyền qua StorageCommon.category như bản XML cũ.
 */
class CategoryDetailActivity : AppCompatActivity(), EdgeToEdgeHelper.HandlesInsets {

    private var category by mutableStateOf<CategoryObject?>(null)
    private var isPremium by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FitnessApplication.getApplication().storageCommon.category.observe(this) { category = it }
        setContent {
            MinTheme {
                CategoryDetailScreen(category, isPremium, onBack = ::finish, onOpen = ::openProgram)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isPremium = MyPreference(this).isPremiumMember
    }

    private fun openProgram(program: ProgramObject) {
        startActivity(
            Intent(this, PreviewActivity::class.java).putExtras(
                Bundle().apply {
                    putInt("PROGRAM_ID", program.id)
                    putInt("DAY_NUMBER", 0)
                },
            ),
        )
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
}

/** Tên + biểu tượng cấp độ theo thứ tự chương trình trong nhóm. */
private val LEVELS = listOf(
    R.string.beginner to R.drawable.ic_level_up_1,
    R.string.intermediate to R.drawable.ic_level_up_2,
    R.string.advanced to R.drawable.ic_level_up,
)

@Composable
private fun CategoryDetailScreen(
    category: CategoryObject?,
    isPremium: Boolean,
    onBack: () -> Unit,
    onOpen: (ProgramObject) -> Unit,
) {
    val programs = category?.programObjectList.orEmpty()
    val first = category?.firstProgramObject
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Hero(category?.name.orEmpty(), first, onBack) }
        first?.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                MinCard(Modifier.padding(horizontal = 16.dp)) {
                    Text(description, fontSize = 15.sp, lineHeight = 22.sp, color = MinColor.TextSecondary)
                }
            }
        }
        itemsIndexed(programs, key = { _, p -> p.id }) { index, program ->
            LevelRow(index, program, locked = program.premium == 1 && !isPremium) { onOpen(program) }
        }
        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun Hero(title: String, program: ProgramObject?, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .clipToBounds(),
    ) {
        if (program != null) {
            GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
            GlideImage(
                program.modelUrl(),
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.62f)
                    .align(Alignment.BottomEnd)
                    .padding(top = 40.dp),
                scaleType = ImageView.ScaleType.FIT_END,
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.25f),
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.6f),
                    ),
                ),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .clickable(onClick = onBack),
        ) {
            Text("←", fontSize = 22.sp, color = MinColor.TextPrimary, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .fillMaxWidth(0.7f),
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LevelRow(index: Int, program: ProgramObject, locked: Boolean, onClick: () -> Unit) {
    val (nameRes, iconRes) = LEVELS.getOrElse(index) { LEVELS.last() }
    MinCard(Modifier.padding(horizontal = 16.dp), onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinColor.PinkSoft),
            ) {
                Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(nameRes), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MinColor.TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("⏱ " + stringResource(R.string.x_minutes, program.total), fontSize = 14.sp, color = MinColor.TextSecondary)
            }
            if (locked) {
                Text(
                    "👑",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .background(MinColor.PinkSoft)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
            }
            Text("›", fontSize = 28.sp, color = MinColor.Pink, fontWeight = FontWeight.Bold)
        }
    }
}
