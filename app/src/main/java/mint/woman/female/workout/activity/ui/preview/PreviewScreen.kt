package mint.woman.female.workout.activity.ui.preview

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.bumptech.glide.Glide
import com.minfitness.decode.Decoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.GlideImage
import mint.woman.female.workout.activity.ui.compose.MinColor
import mint.woman.female.workout.activity.ui.compose.backgroundUrl
import mint.woman.female.workout.activity.ui.compose.fallbackBackgroundUrl
import mint.woman.female.workout.activity.ui.compose.modelUrl
import mint.woman.female.workout.model.ExerciseObject
import mint.woman.female.workout.model.PlanObject
import mint.woman.female.workout.model.ProgramObject

// ---------------------------------------------------------------- state

/** Kiểu nút chính ở đáy màn. */
enum class PreviewButtonMode { GO, CONTINUE, NEED_DOWNLOAD, DOWNLOADING, RETRY }

/**
 * Trạng thái màn Preview. PreviewActivity (Java) giữ toàn bộ logic và chỉ cập nhật các trường này.
 */
class PreviewUiState {
    var program by mutableStateOf<ProgramObject?>(null)
    var levelText by mutableStateOf("")
    var minutes by mutableIntStateOf(0)
    var calories by mutableIntStateOf(0)

    /** Danh sách bài; null = chưa có dữ liệu (đang chờ tải gói bài tập). */
    var items by mutableStateOf<List<PlanObject.ActionObject>?>(null)

    /** Tăng mỗi khi một bài đổi thời lượng tại chỗ để danh sách vẽ lại. */
    var revision by mutableIntStateOf(0)
    var editMode by mutableStateOf(false)
    var canEdit by mutableStateOf(true)

    var buttonMode by mutableStateOf(PreviewButtonMode.GO)
    var buttonText by mutableStateOf("")
    var buttonEnabled by mutableStateOf(true)

    /** Phần đã tô của nút (tiến độ tải hoặc phần đã tập dở), 0..1. */
    var buttonProgress by mutableFloatStateOf(1f)
    var showRestart by mutableStateOf(false)

    /** Banner quảng cáo (AdView) do Activity tạo; null = không hiện. */
    var banner by mutableStateOf<View?>(null)

    fun setItemsFrom(list: List<PlanObject.ActionObject>?) {
        items = list?.let { ArrayList(it) }
        revision++
    }
}

interface PreviewActions {
    fun onBack()
    fun onGo()
    fun onStartOver()
    fun onAudio()
    fun onEdit()
    fun onSave()
    fun onResetPlan()
    fun onItemClick(index: Int)
    fun onReplace(index: Int)
    fun onMove(from: Int, to: Int)
    fun exercise(actionId: Int): ExerciseObject?
    fun discover(): Int
    fun version(): Int
}

// ---------------------------------------------------------------- screen

private const val HEADER_ITEMS = 1

@Composable
fun PreviewScreen(state: PreviewUiState, actions: PreviewActions) {
    val program = state.program ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragState = remember(listState) {
        DragDropState(listState, HEADER_ITEMS, scope) { from, to -> actions.onMove(from, to) }
    }
    val items = state.items

    Column(
        Modifier
            .fillMaxSize()
            .background(MinColor.Background)
    ) {
        LazyColumn(
            state = listState,
            // Đang kéo một bài thì tắt cuộn tay để cử chỉ kéo không bị LazyColumn giành mất.
            userScrollEnabled = dragState.draggingIndex == null,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (state.editMode && state.canEdit) Modifier.pointerInput(dragState) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragState.onDragStart(it) },
                            onDrag = { change, offset ->
                                change.consume()
                                dragState.onDrag(offset)
                            },
                            onDragEnd = { dragState.onDragEnd() },
                            onDragCancel = { dragState.onDragEnd() }
                        )
                    } else Modifier
                ),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(key = "header") {
                if (state.editMode) EditBar(program, actions) else Header(program, state, actions)
            }
            if (items == null) {
                item(key = "download") { DownloadPlaceholder() }
            } else {
                itemsIndexed(items, key = { _, item -> System.identityHashCode(item) }) { index, item ->
                    val dragging = dragState.draggingIndex == index + HEADER_ITEMS
                    ExerciseRow(
                        index = index,
                        item = item,
                        revision = state.revision,
                        editMode = state.editMode,
                        canReorder = state.canEdit,
                        dragging = dragging,
                        actions = actions,
                        modifier = Modifier
                            .zIndex(if (dragging) 1f else 0f)
                            .graphicsLayer { translationY = if (dragging) dragState.draggingItemOffset else 0f }
                    )
                }
            }
        }
        BottomBar(state, actions)
    }
}

// ---------------------------------------------------------------- header

@Composable
private fun Header(program: ProgramObject, state: PreviewUiState, actions: PreviewActions) {
    var menu by remember { mutableStateOf(false) }
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .clipToBounds()
        ) {
            GlideImage(program.backgroundUrl(), Modifier.fillMaxSize(), errorUrl = program.fallbackBackgroundUrl())
            GlideImage(
                program.modelUrl(),
                Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                scaleType = ImageView.ScaleType.FIT_END
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0x55000000),
                            0.35f to Color.Transparent,
                            1f to Color(0xE6880E4F)
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp)
            ) {
                IconCircle("←", actions::onBack)
                Spacer(Modifier.weight(1f))
                IconCircle("🔊", actions::onAudio)
                if (state.canEdit) {
                    Box {
                        IconCircle("⋮") { menu = true }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.txt_edit)) },
                                onClick = {
                                    menu = false
                                    actions.onEdit()
                                }
                            )
                        }
                    }
                }
            }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.72f)
                    .padding(18.dp)
            ) {
                if (state.levelText.isNotEmpty()) {
                    Text(
                        state.levelText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    program.name.orEmpty(),
                    color = Color.White,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // chỉ số buổi tập
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp)
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = MinColor.Pink.copy(alpha = 0.15f), spotColor = MinColor.Pink.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(20.dp))
                .background(MinColor.Surface)
                .padding(vertical = 14.dp)
        ) {
            StatCell("⏱", "${state.minutes}", stringResource(R.string.minutes), Modifier.weight(1f))
            StatCell("🏋️", "${state.items?.size ?: 0}", stringResource(R.string.txt_exercises), Modifier.weight(1f))
            StatCell("🔥", "${state.calories}", "kcal", Modifier.weight(1f))
        }
        program.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MinColor.TextSecondary,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)
            )
        }
        Text(
            stringResource(R.string.txt_exercises),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
        )
    }
}

@Composable
private fun EditBar(program: ProgramObject, actions: PreviewActions) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MinColor.Pink)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                program.name.orEmpty(),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            )
            BarTextButton(stringResource(R.string.txt_reset), actions::onResetPlan)
            BarTextButton(stringResource(R.string.save), actions::onSave)
        }
        Text(
            "≡  " + stringResource(R.string.txt_edit),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
        )
    }
}

@Composable
private fun BarTextButton(text: String, onClick: () -> Unit) {
    Text(
        text.uppercase(),
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun IconCircle(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
    ) { Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun StatCell(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MinColor.TextSecondary, maxLines = 1)
    }
}

@Composable
private fun DownloadPlaceholder() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("☁️", fontSize = 44.sp)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.download), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MinColor.TextSecondary)
    }
}

// ---------------------------------------------------------------- bài tập

@Composable
private fun ExerciseRow(
    index: Int,
    item: PlanObject.ActionObject,
    @Suppress("UNUSED_PARAMETER") revision: Int,
    editMode: Boolean,
    canReorder: Boolean,
    dragging: Boolean,
    actions: PreviewActions,
    modifier: Modifier
) {
    val exercise = actions.exercise(item.actionId)
    Row(
        modifier
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .fillMaxWidth()
            .shadow(if (dragging) 10.dp else 2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MinColor.Surface)
            .then(if (dragging) Modifier.border(2.dp, MinColor.Pink, RoundedCornerShape(18.dp)) else Modifier)
            .clickable { actions.onItemClick(index) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MinColor.PinkSoft.copy(alpha = 0.5f))
        ) {
            ExerciseGif(item.actionId, actions.discover(), actions.version(), Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                exercise?.name ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            val value = if (item.unit.contains("s")) "%02d:%02d".format(item.time / 60, item.time % 60) else "x ${item.time}"
            Text(
                (if (item.unit.contains("s")) "⏱ " else "🔁 ") + value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MinColor.Pink
            )
        }
        if (editMode) {
            Text(
                "⇄",
                fontSize = 20.sp,
                color = MinColor.Pink,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { actions.onReplace(index) }
                    .padding(10.dp)
            )
            if (canReorder) {
                Text("≡", fontSize = 24.sp, color = MinColor.TextMuted, modifier = Modifier.padding(horizontal = 6.dp))
            }
        } else {
            Text("›", fontSize = 26.sp, color = MinColor.TextMuted, modifier = Modifier.padding(horizontal = 6.dp))
        }
    }
}

/** Ảnh động minh hoạ bài tập (GIF được mã hoá trong assets hoặc trong gói discover đã tải). */
@Composable
private fun ExerciseGif(actionId: Int, discover: Int, version: Int, modifier: Modifier) {
    val context = LocalContext.current
    val bytes by produceState<ByteArray?>(null, actionId, discover, version) {
        value = withContext(Dispatchers.IO) { loadGif(context, actionId, discover, version) }
    }
    AndroidView(
        factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        update = { view ->
            val data = bytes
            if (data != null && view.tag !== data) {
                view.tag = data
                Glide.with(view).asGif().load(data).into(view)
            }
        },
        modifier = modifier
    )
}

private fun loadGif(context: Context, actionId: Int, discover: Int, version: Int): ByteArray? = try {
    if (discover == 0) {
        Decoder.decryptFromAssets(context.assets, "i/g/video_$actionId")
    } else {
        Decoder.decryptInput(context.filesDir.path + "/discover/" + discover + "/" + version + "/g/video_" + actionId)
    }
} catch (e: Exception) {
    null
}

// ---------------------------------------------------------------- đáy màn

@Composable
private fun BottomBar(state: PreviewUiState, actions: PreviewActions) {
    val fill by animateFloatAsState(state.buttonProgress.coerceIn(0f, 1f), tween(300), label = "go")
    Column(
        Modifier
            .fillMaxWidth()
            .background(MinColor.Surface)
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.showRestart) {
                Text(
                    "↺ " + stringResource(R.string.ch_restart),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinColor.Pink,
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.5.dp, MinColor.Pink, RoundedCornerShape(26.dp))
                        .clickable(onClick = actions::onStartOver)
                        .padding(horizontal = 18.dp, vertical = 15.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(MinColor.CtaBrush)
                    .clickable(enabled = state.buttonEnabled, onClick = actions::onGo),
                contentAlignment = Alignment.Center
            ) {
                // Tiến độ (đang tải / đã tập dở): thanh mảnh trong nút để chữ luôn nằm trên nền hồng đậm.
                if (state.buttonMode == PreviewButtonMode.CONTINUE || state.buttonMode == PreviewButtonMode.DOWNLOADING) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 28.dp, end = 28.dp, bottom = 7.dp)
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fill)
                                .background(Color.White)
                        )
                    }
                }
                val icon = when (state.buttonMode) {
                    PreviewButtonMode.NEED_DOWNLOAD, PreviewButtonMode.DOWNLOADING -> "⬇ "
                    PreviewButtonMode.RETRY -> "↻ "
                    else -> ""
                }
                Text(
                    icon + state.buttonText.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
        state.banner?.let { adView ->
            AndroidView(factory = { (adView.parent as? android.view.ViewGroup)?.removeView(adView); adView }, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------- kéo thả

/** Kéo-thả sắp xếp bằng nhấn giữ trong LazyColumn (bỏ qua [headerCount] item đầu). */
internal class DragDropState(
    private val listState: LazyListState,
    private val headerCount: Int,
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val onMove: (Int, Int) -> Unit
) {
    var draggingIndex by mutableStateOf<Int?>(null)
        private set
    private var draggedDelta by mutableFloatStateOf(0f)
    private var initialOffset by mutableIntStateOf(0)

    private val draggingInfo: LazyListItemInfo?
        get() = draggingIndex?.let { index -> listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } }

    val draggingItemOffset: Float
        get() = draggingInfo?.let { initialOffset + draggedDelta - it.offset } ?: 0f

    fun onDragStart(offset: Offset) {
        listState.layoutInfo.visibleItemsInfo
            .firstOrNull { offset.y.toInt() in it.offset..(it.offset + it.size) && it.index >= headerCount }
            ?.let {
                draggingIndex = it.index
                initialOffset = it.offset
                draggedDelta = 0f
            }
    }

    fun onDrag(offset: Offset) {
        draggedDelta += offset.y
        val current = draggingInfo ?: return
        val start = current.offset + draggingItemOffset
        val middle = start + current.size / 2f
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.index >= headerCount && it.index != current.index &&
                middle.toInt() in it.offset..(it.offset + it.size)
        }
        if (target != null) {
            if (current.index == listState.firstVisibleItemIndex || target.index == listState.firstVisibleItemIndex) {
                scope.launch {
                    listState.scrollToItem(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
                }
            }
            onMove(current.index - headerCount, target.index - headerCount)
            draggingIndex = target.index
        } else {
            // Tự cuộn khi kéo sát mép trên/dưới.
            val viewportEnd = listState.layoutInfo.viewportEndOffset
            val end = start + current.size
            val overscroll = when {
                draggedDelta > 0 && end > viewportEnd -> end - viewportEnd
                draggedDelta < 0 && start < listState.layoutInfo.viewportStartOffset -> start - listState.layoutInfo.viewportStartOffset
                else -> 0f
            }
            if (overscroll != 0f) scope.launch { listState.scrollBy(overscroll * 0.5f) }
        }
    }

    fun onDragEnd() {
        draggingIndex = null
        draggedDelta = 0f
    }
}

/** Gắn màn Compose vào PreviewActivity (Java). */
object PreviewCompose {
    @JvmStatic
    fun attach(activity: androidx.activity.ComponentActivity, state: PreviewUiState, actions: PreviewActions) {
        activity.setContent {
            mint.woman.female.workout.activity.ui.compose.MinTheme { PreviewScreen(state, actions) }
        }
    }
}
