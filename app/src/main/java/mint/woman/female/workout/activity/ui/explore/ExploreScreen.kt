package mint.woman.female.workout.activity.ui.explore

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinCard
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.MinLargeHeader
import mint.woman.female.workout.activity.ui.compose.MinSectionHeader
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.model.CategoryObject
import mint.woman.female.workout.model.ProgramObject

// ---------------------------------------------------------------- model

/** Kiểu hiển thị của một mục trong Discover. */
enum class DiscoverStyle {
    /** Banner lớn của một nhóm (có chọn level). */
    HERO,

    /** Lưới 2 cột các nhóm bài tập. */
    CATEGORY_GRID,

    /** Hàng thẻ cuộn ngang. */
    CAROUSEL,

    /** Danh sách gọn dạng dòng. */
    LIST
}

class DiscoverSection(
    val title: String,
    val description: String? = null,
    val style: DiscoverStyle,
    val programs: List<ProgramObject> = emptyList(),
    val categories: List<CategoryObject> = emptyList()
)

interface DiscoverActions {
    fun onSearch()
    fun onProgramClick(program: ProgramObject)
    fun onCategoryClick(category: CategoryObject)
    fun onSeeMore(section: DiscoverSection)
}

// ---------------------------------------------------------------- screen

@Composable
fun DiscoverScreen(sections: List<DiscoverSection>, isPremium: Boolean, actions: DiscoverActions) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item { SearchHeader(actions) }
        sections.forEach { section ->
            item(key = section.title + section.style) {
                Column(Modifier.padding(top = 20.dp)) {
                    MinSectionHeader(
                        section.title,
                        Modifier.padding(horizontal = 16.dp),
                        action = if (section.programs.isNotEmpty() && section.style != DiscoverStyle.HERO)
                            stringResource(R.string.explore_more) else null,
                        onAction = { actions.onSeeMore(section) }
                    )
                    section.description?.let {
                        Text(
                            it,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            color = MinColor.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    when (section.style) {
                        DiscoverStyle.HERO -> section.categories.firstOrNull()?.let {
                            HeroCategory(it, actions, Modifier.padding(horizontal = 16.dp))
                        }
                        DiscoverStyle.CATEGORY_GRID -> CategoryGrid(section.categories, actions)
                        DiscoverStyle.CAROUSEL -> LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(section.programs, key = { it.id }) { program ->
                                CarouselCard(program, isPremium) { actions.onProgramClick(program) }
                            }
                        }
                        DiscoverStyle.LIST -> Column(
                            Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            section.programs.forEach { program ->
                                ListRow(program, isPremium) { actions.onProgramClick(program) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(actions: DiscoverActions) {
    Column {
        MinLargeHeader(stringResource(R.string.training))
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(MinColor.Track)
                .clickable(onClick = actions::onSearch)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(androidx.appcompat.R.string.abc_search_hint),
                fontSize = 16.sp,
                color = MinColor.TextMuted
            )
        }
    }
}

// ---------------------------------------------------------------- nhóm

private fun levelLabelRes(level: Int): Int = when (level) {
    1 -> R.string.beginner
    2 -> R.string.intermediate
    3 -> R.string.advanced
    else -> R.string.any_level
}

@Composable
private fun HeroCategory(category: CategoryObject, actions: DiscoverActions, modifier: Modifier) {
    val first = category.firstProgramObject
    Box(
        modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .clipToBounds()
            .clickable { actions.onCategoryClick(category) }
    ) {
        GlideImage(first.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = first.fallbackBackgroundUrl())
        GlideImage(
            first.modelUrl(),
            Modifier
                .fillMaxHeight()
                .width(190.dp)
                .align(Alignment.BottomEnd),
            scaleType = ImageView.ScaleType.FIT_END
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.6f),
                        0.65f to Color.Black.copy(alpha = 0.1f),
                        1f to Color.Transparent
                    )
                )
        )
        Text(
            "🔥 " + stringResource(R.string.explore_tag_hot).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MinColor.Pink)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.68f)
        ) {
            Text(
                category.name,
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                category.programObjectList.take(3).forEach { program ->
                    Text(
                        stringResource(levelLabelRes(program.level)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { actions.onProgramClick(program) }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(categories: List<CategoryObject>, actions: DiscoverActions) {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    CategoryTile(category, Modifier.weight(1f)) { actions.onCategoryClick(category) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryTile(category: CategoryObject, modifier: Modifier, onClick: () -> Unit) {
    val first = category.firstProgramObject
    Box(
        modifier
            .height(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .clipToBounds()
            .clickable(onClick = onClick)
    ) {
        GlideImage(first.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = first.fallbackBackgroundUrl())
        GlideImage(first.modelUrl(), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.FIT_END)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.65f)
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                category.name,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${category.programObjectList.size} ${stringResource(R.string.txt_workout).lowercase()}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

// ---------------------------------------------------------------- thẻ chương trình

@Composable
private fun CarouselCard(program: ProgramObject, isPremium: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MinColor.Surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clipToBounds()
        ) {
            GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
            GlideImage(program.modelUrl(), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.FIT_END)
            PremiumMark(program, isPremium, Modifier.align(Alignment.TopEnd))
            Text(
                "⏱ ${program.total}'",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                listOfNotNull(program.name, program.subtitle?.takeIf { it.isNotBlank() }).joinToString(" "),
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(36.dp)
            )
            Text(
                stringResource(levelLabelRes(program.level)),
                fontSize = 12.sp,
                color = MinColor.Pink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ListRow(program: ProgramObject, isPremium: Boolean, onClick: () -> Unit) {
    MinCard(Modifier.fillMaxWidth(), onClick = onClick, padding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clipToBounds()
            ) {
                GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
                GlideImage(program.modelUrl(), Modifier.fillMaxSize(), scaleType = ImageView.ScaleType.FIT_CENTER)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    listOfNotNull(program.name, program.subtitle?.takeIf { it.isNotBlank() }).joinToString(" "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "⏱ " + stringResource(R.string.x_minutes, program.total) + "  •  " +
                        stringResource(levelLabelRes(program.level)),
                    fontSize = 13.sp,
                    color = MinColor.TextSecondary,
                    maxLines = 1
                )
            }
            PremiumMark(program, isPremium, Modifier)
            Text("›", fontSize = 26.sp, color = MinColor.TextMuted, modifier = Modifier.padding(horizontal = 6.dp))
        }
    }
}

@Composable
private fun PremiumMark(program: ProgramObject, isPremium: Boolean, modifier: Modifier) {
    if (program.premium == 1 && !isPremium) {
        Text(
            "👑",
            fontSize = 13.sp,
            modifier = modifier
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
