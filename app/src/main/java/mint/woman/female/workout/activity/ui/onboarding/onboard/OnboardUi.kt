package mint.woman.female.workout.activity.ui.onboarding.onboard

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import mint.woman.female.workout.activity.ui.onboarding.RulerSliderView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ---------------------------------------------------------------- ảnh thiết kế

/**
 * Nạp ảnh của onboard từ `assets/onboards/v2/<name>.(webp|png|jpg)`.
 *
 * Bộ ảnh chụp trong `task/` là ảnh chụp màn hình nên không kèm file gốc; khi nào có ảnh
 * thật chỉ cần thả vào thư mục trên đúng tên là màn hình tự dùng, không phải sửa code.
 */
@Composable
fun rememberObArt(name: String): ImageBitmap? {
    if (name.isEmpty()) return null
    val context = LocalContext.current
    return remember(name) {
        listOf("webp", "png", "jpg").firstNotNullOfOrNull { ext ->
            runCatching {
                context.assets.open("onboards/v2/$name.$ext").use {
                    BitmapFactory.decodeStream(it)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
}

/**
 * Khung ảnh: dùng ảnh thật nếu có, nếu chưa có thì vẽ ô giữ chỗ (gradient + emoji) để
 * bố cục vẫn đúng tỉ lệ.
 */
@Composable
fun ObArt(
    name: String,
    modifier: Modifier = Modifier,
    placeholderEmoji: String = "",
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = rememberObArt(name)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(listOf(Color(0xFFFDE7EF), Color(0xFFEDE7F6)))
            ),
            contentAlignment = Alignment.Center
        ) {
            if (placeholderEmoji.isNotEmpty()) {
                Text(placeholderEmoji, fontSize = 44.sp)
            }
        }
    }
}

// ---------------------------------------------------------------- thanh trên

/**
 * Thanh trên cùng: nút back, 4 đoạn tiến trình (đoạn đã xong có dấu tick), nút Skip.
 *
 * @param section phần hiện tại (1..4)
 * @param fraction tiến độ trong phần hiện tại, 0f..1f
 */
@Composable
fun ObTopBar(
    section: Int,
    fraction: Float,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    light: Boolean = false
) {
    val foreground = if (light) Color.White else Color(0xFF8A8A8A)
    val track = if (light) Color.White.copy(alpha = 0.35f) else ObColor.Track
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            ObBackArrow(color = if (light) Color.White else Color(0xFF8A8A8A))
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 1..SECTION_COUNT) {
                val segmentFraction = when {
                    index < section -> 1f
                    index > section -> 0f
                    else -> fraction.coerceIn(0f, 1f)
                }
                val animated by animateFloatAsState(segmentFraction, tween(260), label = "seg$index")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(track)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animated)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(ObColor.Pink)
                    )
                }
                if (index < SECTION_COUNT) {
                    ObSegmentDot(done = index < section, light = light)
                }
            }
        }

        Text(
            text = "Skip",
            color = foreground,
            fontSize = 17.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSkip)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ObSegmentDot(done: Boolean, light: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(14.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> ObColor.Pink
                    light -> Color.White.copy(alpha = 0.55f)
                    else -> Color(0xFFCFCFCF)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Canvas(Modifier.size(8.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.1f, size.height * 0.55f)
                    lineTo(size.width * 0.4f, size.height * 0.85f)
                    lineTo(size.width * 0.92f, size.height * 0.2f)
                }
                drawPath(path, Color.White, style = Stroke(width = size.width * 0.22f, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun ObBackArrow(color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.08f)
            lineTo(size.width * 0.28f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.92f)
        }
        drawPath(path, color, style = Stroke(width = size.width * 0.13f, cap = StrokeCap.Round))
    }
}

// ---------------------------------------------------------------- chữ & nút

/** Tiêu đề lớn, tô hồng phần [accent] nếu có. */
@Composable
fun ObTitle(
    text: String,
    accent: String = "",
    modifier: Modifier = Modifier,
    fontSize: Int = 30,
    color: Color = ObColor.TextPrimary,
    textAlign: TextAlign = TextAlign.Center
) {
    val annotated = buildAnnotatedString {
        val start = if (accent.isEmpty()) -1 else text.indexOf(accent)
        if (start < 0) {
            append(text)
        } else {
            append(text.substring(0, start))
            withStyle(SpanStyle(color = ObColor.PinkText)) { append(accent) }
            append(text.substring(start + accent.length))
        }
    }
    Text(
        text = annotated,
        modifier = modifier.fillMaxWidth(),
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.25f).sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        textAlign = textAlign
    )
}

/** Nút CTA đen bo tròn ở đáy màn hình. */
@Composable
fun ObPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val background = if (enabled) ObColor.Cta else ObColor.CtaDisabled
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(31.dp))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Color(0xFF00BCD4), Color(0xFF03413B)))
                else Brush.horizontalGradient(listOf(background, background))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** Thẻ gợi ý nền hồng nhạt kèm emoji (step9, step17). */
@Composable
fun ObHintCard(emoji: String, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFBEEF2))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji.isNotEmpty()) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(14.dp))
        }
        Text(text, fontSize = 17.sp, color = ObColor.TextPrimary, lineHeight = 23.sp)
    }
}

/** Thẻ phản hồi của trợ lý sau khi chọn đáp án (step20, 21, 22). */
@Composable
fun ObAssistantCard(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3EBEE))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🤖", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
        }
        Spacer(Modifier.height(8.dp))
        Text(body, fontSize = 16.sp, color = ObColor.TextSecondary, lineHeight = 22.sp)
    }
}

// ---------------------------------------------------------------- thẻ lựa chọn

/** Dòng lựa chọn: emoji + nhãn (step3, 19, 27, 29, 39, 40). */
@Composable
fun ObOptionRow(
    option: ObOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        if (selected) ObColor.PinkSoft else ObColor.Surface, tween(180), label = "optionBg"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .then(
                if (selected) Modifier.border(2.dp, ObColor.Pink, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (option.icon.isNotEmpty()) {
            Text(option.icon, fontSize = 24.sp, modifier = Modifier.width(44.dp))
        }
        Text(
            option.label,
            fontSize = 20.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) ObColor.PinkText else ObColor.TextPrimary
        )
    }
}

/** Thẻ lựa chọn có ảnh minh hoạ bên phải (step4, 7, 11, 12, 13). */
@Composable
fun ObImageOptionCard(
    option: ObOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        if (selected) ObColor.PinkSoft else ObColor.Surface, tween(180), label = "imgOptionBg"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .then(
                if (selected) Modifier.border(2.dp, ObColor.Pink, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            option.label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 22.dp, end = 8.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) ObColor.PinkText else ObColor.TextPrimary
        )
        ObArt(
            name = option.art,
            modifier = Modifier
                .width(140.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)),
            placeholderEmoji = "🧍‍♀️"
        )
    }
}

/** Ô tròn tick/untick ở cạnh phải (step6). */
@Composable
fun ObCheckCircle(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (selected) ObColor.Pink else Color.Transparent)
            .border(2.dp, if (selected) ObColor.Pink else Color(0xFFD5D5D5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Canvas(Modifier.size(14.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.12f, size.height * 0.55f)
                    lineTo(size.width * 0.4f, size.height * 0.82f)
                    lineTo(size.width * 0.9f, size.height * 0.22f)
                }
                drawPath(path, Color.White, style = Stroke(width = size.width * 0.18f, cap = StrokeCap.Round))
            }
        }
    }
}

// ---------------------------------------------------------------- thanh trượt

/** Thanh trượt rời rạc có các chấm nấc (step8, 26, 30, 31, 33, 34). */
@Composable
fun ObDiscreteSlider(
    stopCount: Int,
    value: Int,
    leftLabel: String,
    rightLabel: String,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    var trackWidth by remember { mutableStateOf(0f) }
    val thumbRadiusPx = with(density) { 17.dp.toPx() }

    fun valueFor(x: Float): Int {
        if (trackWidth <= 0f || stopCount <= 1) return 0
        val usable = trackWidth - thumbRadiusPx * 2
        val clamped = ((x - thumbRadiusPx) / usable).coerceIn(0f, 1f)
        return (clamped * (stopCount - 1)).roundToInt()
    }

    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(stopCount, trackWidth) {
                    detectTapGestures { offset -> onValueChange(valueFor(offset.x)) }
                }
                .pointerInput(stopCount, trackWidth) {
                    detectHorizontalDragGestures { change, _ ->
                        onValueChange(valueFor(change.position.x))
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                trackWidth = size.width
                val centerY = size.height / 2f
                val usable = size.width - thumbRadiusPx * 2
                drawRoundRectTrack(centerY)
                // chấm của từng nấc
                for (index in 0 until stopCount) {
                    val cx = thumbRadiusPx + usable * (index.toFloat() / max(1, stopCount - 1))
                    drawCircle(Color(0xFFBDBDBD), radius = 5f, center = Offset(cx, centerY))
                }
                val thumbX = thumbRadiusPx + usable * (value.toFloat() / max(1, stopCount - 1))
                drawCircle(Color.White, radius = thumbRadiusPx, center = Offset(thumbX, centerY))
                drawCircle(
                    ObColor.Pink, radius = thumbRadiusPx, center = Offset(thumbX, centerY),
                    style = Stroke(width = with(density) { 5.dp.toPx() })
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leftLabel, fontSize = 17.sp, color = ObColor.TextSecondary)
            Text(rightLabel, fontSize = 17.sp, color = ObColor.TextSecondary)
        }
    }
}

private fun DrawScope.drawRoundRectTrack(centerY: Float) {
    val height = 34f
    drawRoundRect(
        color = ObColor.Track,
        topLeft = Offset(0f, centerY - height / 2f),
        size = Size(size.width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f)
    )
}

// ---------------------------------------------------------------- thước đo

/** Thước ngang tái sử dụng [RulerSliderView] (step10). */
@Composable
fun ObHorizontalRuler(
    min: Int,
    max: Int,
    value: Int,
    majorStep: Int,
    labelStep: Int,
    pixelsPerUnitDp: Int,
    feetAndInches: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RulerSliderView(context).apply {
                setOnValueChangeListener { newValue, _ -> onValueChange(newValue) }
            }
        },
        update = { view ->
            view.setRange(min, max, majorStep)
            view.setFeetAndInches(feetAndInches)
            view.setLabelStep(labelStep)
            view.setPixelsPerUnit(pixelsPerUnitDp * view.resources.displayMetrics.density)
            if (view.value != value) view.setValue(value)
        }
    )
}

/** Thước dọc bám cạnh phải cho màn chiều cao (step9). */
@Composable
fun ObVerticalRuler(
    min: Int,
    max: Int,
    value: Int,
    labelStep: Int,
    feetAndInches: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    // Chiều cao cũ 14dp nhỏ hơn font 18sp nên nhãn mốc bị parent cắt mất
    // phần trên/dưới. Giữ một khoảng đủ cho font metrics ở mọi font scale.
    val itemHeight = 20.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val listState = rememberLazyListState()
    var viewportHeight by remember { mutableStateOf(0) }
    var userScrolling by remember { mutableStateOf(false) }

    // giá trị lớn nhất nằm trên cùng nên danh sách chạy ngược
    val values = remember(min, max) { (max downTo min).toList() }

    fun indexOfValue(target: Int) = (max - target).coerceIn(0, values.lastIndex)

    LaunchedEffect(viewportHeight) {
        if (viewportHeight > 0) {
            val offset = (viewportHeight / 2f - itemHeightPx / 2f).roundToInt()
            listState.scrollToItem(indexOfValue(value), -offset)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { userScrolling = it }
    }

    LaunchedEffect(listState, viewportHeight) {
        if (viewportHeight <= 0) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (!listState.isScrollInProgress) return@collect
                val centerPx = index * itemHeightPx + offset + viewportHeight / 2f
                val centerIndex = (centerPx / itemHeightPx).toInt().coerceIn(0, values.lastIndex)
                val newValue = values[centerIndex]
                if (newValue != value) onValueChange(newValue)
            }
    }

    LaunchedEffect(value, userScrolling) {
        if (!userScrolling && viewportHeight > 0) {
            val offset = (viewportHeight / 2f - itemHeightPx / 2f).roundToInt()
            listState.animateScrollToItem(indexOfValue(value), -offset)
        }
    }

    Box(
        modifier = modifier.onSizeChangedCompat { viewportHeight = it },
        contentAlignment = Alignment.CenterEnd
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(values.size) { index ->
                val tickValue = values[index]
                val major = if (feetAndInches) tickValue % 12 == 0
                else tickValue % labelStep == 0
                val middle = feetAndInches && !major && tickValue % 6 == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (major) {
                        Text(
                            if (feetAndInches) "${tickValue / 12}'" else tickValue.toString(),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObColor.TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                    Box(
                        Modifier
                            .width(
                                when {
                                    major -> 34.dp
                                    middle -> 27.dp
                                    else -> 20.dp
                                }
                            )
                            .height(2.dp)
                            .background(
                                when {
                                    major -> Color(0xFF9E9E9E)
                                    middle -> Color(0xFFC7C7C7)
                                    else -> Color(0xFFE0E0E0)
                                }
                            )
                    )
                }
            }
        }
    }
}

/** Bọc `onSizeChanged` để chỉ quan tâm chiều cao vùng hiển thị. */
private fun Modifier.onSizeChangedCompat(onHeight: (Int) -> Unit): Modifier =
    this.onSizeChanged { onHeight(it.height) }

/** Bánh xe chọn số cho màn tuổi (step17). */
@Composable
fun ObWheelPicker(
    min: Int,
    max: Int,
    value: Int,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val itemHeight = 88.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var viewportHeight by remember { mutableStateOf(0) }
    var initialized by remember { mutableStateOf(false) }
    val currentValue by rememberUpdatedState(value)

    val values = remember(min, max) { (min..max).toList() }
    fun indexOfValue(target: Int) = (target - min).coerceIn(0, values.lastIndex)

    LaunchedEffect(viewportHeight) {
        if (viewportHeight > 0) {
            val offset = (viewportHeight / 2f - itemHeightPx / 2f).roundToInt()
            listState.scrollToItem(indexOfValue(value), -offset)
            initialized = true
        }
    }

    // Tuổi nằm gần tâm nhất luôn là tuổi được chọn, kể cả khi bánh xe đang trượt.
    LaunchedEffect(listState, viewportHeight, initialized) {
        if (viewportHeight <= 0 || !initialized) return@LaunchedEffect
        snapshotFlow {
            val center = viewportHeight / 2
            listState.layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - center)
            }?.index
        }.collect { centerIndex ->
            centerIndex ?: return@collect
            val selectedValue = values[centerIndex.coerceIn(0, values.lastIndex)]
            if (selectedValue != currentValue) onValueChange(selectedValue)
        }
    }

    // Khi người dùng thả tay, căn chính xác item được chọn vào khung accent cố định.
    LaunchedEffect(listState, viewportHeight, initialized) {
        if (viewportHeight <= 0 || !initialized) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) return@collect
            val center = viewportHeight / 2
            val centerIndex = listState.layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - center)
            }?.index ?: return@collect
            val offset = (viewportHeight / 2f - itemHeightPx / 2f).roundToInt()
            listState.animateScrollToItem(centerIndex, -offset)
        }
    }

    Box(modifier = modifier.onSizeChangedCompat { viewportHeight = it }) {
        // Khung chọn đứng yên tại tâm; các giá trị tuổi cuộn phía trên khung này.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight - 12.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ObColor.PinkSoft)
                .border(2.dp, ObColor.Pink, RoundedCornerShape(20.dp))
        )
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(values.size) { index ->
                val itemValue = values[index]
                val selected = itemValue == value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clickable {
                            onValueChange(itemValue)
                            scope.launch {
                                val offset = (viewportHeight / 2f - itemHeightPx / 2f).roundToInt()
                                listState.animateScrollToItem(index, -offset)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        itemValue.toString(),
                        fontSize = if (selected) 44.sp else 38.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) ObColor.PinkText else Color(0xFFAAAAAA)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- hình minh hoạ

/** Hình que vẽ bằng Canvas cho các màn thanh trượt. */
@Composable
fun ObFigureArt(figure: ObFigure, modifier: Modifier = Modifier, label: String = "") {
    Box(modifier, contentAlignment = Alignment.Center) {
        when (figure) {
            ObFigure.PLANK -> Canvas(Modifier.fillMaxSize()) { drawPlank() }
            ObFigure.REACH -> Canvas(Modifier.fillMaxSize()) { drawReach() }
            ObFigure.STAIRS -> Canvas(Modifier.fillMaxSize()) { drawStairs() }
            ObFigure.GAUGE -> ObGauge(label = label, modifier = Modifier.fillMaxSize())
            ObFigure.CALENDAR -> ObCalendarBadge(label = label, modifier = Modifier.fillMaxSize())
            ObFigure.NONE -> Unit
        }
    }
}

private fun DrawScope.drawPlank() {
    val pink = ObColor.Pink
    val w = size.width
    val h = size.height
    val stroke = h * 0.10f
    // đường sàn
    drawLine(
        pink.copy(alpha = 0.45f),
        Offset(w * 0.18f, h * 0.86f), Offset(w * 0.82f, h * 0.86f),
        strokeWidth = h * 0.035f, cap = StrokeCap.Round
    )
    // thân
    drawLine(pink, Offset(w * 0.24f, h * 0.68f), Offset(w * 0.62f, h * 0.52f), stroke * 1.6f, StrokeCap.Round)
    // tay chống
    drawLine(pink, Offset(w * 0.62f, h * 0.52f), Offset(w * 0.66f, h * 0.80f), stroke, StrokeCap.Round)
    // chân
    drawLine(pink, Offset(w * 0.24f, h * 0.68f), Offset(w * 0.30f, h * 0.74f), stroke, StrokeCap.Round)
    // đầu
    drawCircle(pink, radius = h * 0.11f, center = Offset(w * 0.70f, h * 0.42f))
}

private fun DrawScope.drawReach() {
    val pink = ObColor.Pink
    val w = size.width
    val h = size.height
    val stroke = h * 0.11f
    drawLine(
        pink.copy(alpha = 0.45f),
        Offset(w * 0.24f, h * 0.88f), Offset(w * 0.76f, h * 0.88f),
        strokeWidth = h * 0.035f, cap = StrokeCap.Round
    )
    // chân duỗi
    drawLine(pink, Offset(w * 0.36f, h * 0.78f), Offset(w * 0.68f, h * 0.78f), stroke, StrokeCap.Round)
    // thân
    drawLine(pink, Offset(w * 0.36f, h * 0.78f), Offset(w * 0.36f, h * 0.42f), stroke, StrokeCap.Round)
    // tay vươn
    drawLine(pink, Offset(w * 0.40f, h * 0.44f), Offset(w * 0.60f, h * 0.42f), stroke * 0.8f, StrokeCap.Round)
    // đầu
    drawCircle(pink, radius = h * 0.11f, center = Offset(w * 0.38f, h * 0.28f))
}

private fun DrawScope.drawStairs() {
    val pink = ObColor.Pink
    val w = size.width
    val h = size.height
    val stroke = h * 0.10f
    // bậc thang
    val steps = Path().apply {
        moveTo(w * 0.34f, h * 0.92f)
        lineTo(w * 0.46f, h * 0.92f)
        lineTo(w * 0.46f, h * 0.76f)
        lineTo(w * 0.58f, h * 0.76f)
        lineTo(w * 0.58f, h * 0.60f)
        lineTo(w * 0.70f, h * 0.60f)
        lineTo(w * 0.70f, h * 0.46f)
    }
    drawPath(steps, pink, style = Stroke(width = h * 0.035f, cap = StrokeCap.Round))
    // thân cúi
    drawLine(pink, Offset(w * 0.38f, h * 0.84f), Offset(w * 0.42f, h * 0.56f), stroke, StrokeCap.Round)
    drawLine(pink, Offset(w * 0.42f, h * 0.56f), Offset(w * 0.56f, h * 0.44f), stroke * 1.5f, StrokeCap.Round)
    drawCircle(pink, radius = h * 0.10f, center = Offset(w * 0.60f, h * 0.38f))
    // giọt mồ hôi
    drawCircle(pink.copy(alpha = 0.6f), radius = h * 0.028f, center = Offset(w * 0.68f, h * 0.24f))
    drawCircle(pink.copy(alpha = 0.6f), radius = h * 0.022f, center = Offset(w * 0.52f, h * 0.26f))
    drawCircle(pink.copy(alpha = 0.6f), radius = h * 0.02f, center = Offset(w * 0.56f, h * 0.30f))
}

/** Vòng cung thể hiện mức độ (step31). */
@Composable
fun ObGauge(label: String, modifier: Modifier = Modifier, fraction: Float = 0.33f) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(320), label = "gauge")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val diameter = min(size.width, size.height) * 0.82f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val stroke = diameter * 0.12f
            drawArc(
                color = Color(0xFFF3F0F1),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Color(0xFFFF9800), Color(0xFFFF2E63), Color(0xFFFF9800))),
                startAngle = -90f, sweepAngle = 360f * (0.25f + animated * 0.6f), useCenter = false,
                topLeft = topLeft, size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text("🔥", fontSize = 44.sp)
    }
}

/** Biểu tượng lịch có số buổi/tuần (step26). */
@Composable
fun ObCalendarBadge(label: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .width(132.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(ObColor.Pink)
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
            }
        }
    }
}

// ---------------------------------------------------------------- biểu đồ

/**
 * Biểu đồ dự đoán cân nặng (step14/15, 28, 46).
 *
 * @param progress 0f..1f – dùng để chạy hiệu ứng vẽ dần đường cong
 * @param ghostCurve vẽ thêm đường đứt nét của mốc cũ (step28)
 */
@Composable
fun ObWeightChart(
    startLabel: String,
    endLabel: String,
    startValue: String,
    endValue: String,
    progress: Float,
    modifier: Modifier = Modifier,
    ghostCurve: Boolean = false,
    ghostLabel: String = ""
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "chart")
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val top = h * 0.12f
                val bottom = h * 0.86f

                val curve = Path().apply {
                    moveTo(0f, top)
                    cubicTo(w * 0.22f, top, w * 0.30f, bottom * 0.62f, w * 0.55f, bottom * 0.72f)
                    cubicTo(w * 0.75f, bottom * 0.80f, w * 0.86f, bottom * 0.80f, w, bottom * 0.80f)
                }

                // vùng tô dưới đường cong
                val fill = Path().apply {
                    addPath(curve)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    fill,
                    Brush.verticalGradient(
                        listOf(Color(0x66FF2E63), Color(0x11673AB7), Color(0x00FFFFFF))
                    )
                )

                if (ghostCurve) {
                    val ghost = Path().apply {
                        moveTo(w * 0.14f, top * 1.2f)
                        cubicTo(w * 0.40f, top * 1.2f, w * 0.52f, bottom * 0.70f, w * 0.78f, bottom * 0.78f)
                        cubicTo(w * 0.88f, bottom * 0.80f, w * 0.94f, bottom * 0.80f, w, bottom * 0.80f)
                    }
                    drawPath(
                        ghost, Color(0xFFBDBDBD),
                        style = Stroke(
                            width = 6f, cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))
                        )
                    )
                }

                drawPath(
                    curve,
                    Brush.horizontalGradient(
                        listOf(ObColor.Pink, ObColor.Purple, ObColor.Blue)
                    ),
                    style = Stroke(width = 14f, cap = StrokeCap.Round)
                )

                // mốc bắt đầu
                val startX = w * 0.14f
                val startY = top * 1.05f
                drawLine(
                    ObColor.Pink.copy(alpha = 0.55f), Offset(startX, startY), Offset(startX, bottom),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                drawCircle(Color.White, radius = 22f, center = Offset(startX, startY))
                drawCircle(ObColor.Pink, radius = 22f, center = Offset(startX, startY), style = Stroke(width = 10f))

                // mốc kết thúc chỉ hiện khi hiệu ứng chạy xong
                if (animated > 0.95f) {
                    val endX = w * 0.78f
                    val endY = bottom * 0.795f
                    drawLine(
                        ObColor.Blue.copy(alpha = 0.55f), Offset(endX, endY), Offset(endX, bottom),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawCircle(Color.White, radius = 22f, center = Offset(endX, endY))
                    drawCircle(ObColor.Blue, radius = 22f, center = Offset(endX, endY), style = Stroke(width = 10f))
                }
            }

            Text(
                startValue,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp),
                fontSize = 20.sp, color = ObColor.TextPrimary
            )

            if (animated > 0.95f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp, bottom = 40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObColor.Pink)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(endValue, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(startLabel, fontSize = 17.sp, color = ObColor.TextMuted)
            if (ghostLabel.isNotEmpty()) {
                Text(ghostLabel, fontSize = 17.sp, color = ObColor.TextMuted)
            }
            Text(endLabel, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
        }
    }
}

/**
 * Biểu đồ kết quả kế hoạch với ba mốc cân nặng thật.
 *
 * Đường được vẽ dần từ cân nặng hiện tại qua mốc sau một tuần rồi mới tới mục tiêu.
 * Trục Y dựa vào giá trị nên tự đi xuống khi giảm cân và đi lên khi tăng cân.
 */
@Composable
fun ObPlanWeightChart(
    currentWeight: Float,
    weekOneWeight: Float,
    targetWeight: Float,
    unitLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember(currentWeight, weekOneWeight, targetWeight) {
        mutableStateOf(false)
    }
    LaunchedEffect(currentWeight, weekOneWeight, targetWeight) {
        startAnimation = false
        kotlinx.coroutines.delay(120)
        startAnimation = true
    }
    val animated by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "planWeightChart"
    )

    fun valueLabel(value: Float): String {
        val rounded = value.roundToInt()
        val number = if (kotlin.math.abs(value - rounded) < 0.05f) {
            rounded.toString()
        } else {
            value.format1()
        }
        return "$number $unitLabel"
    }

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val chartLeft = size.width * 0.10f
                val chartMiddle = size.width * 0.50f
                val chartRight = size.width * 0.90f
                val chartTop = size.height * 0.28f
                val chartBottom = size.height * 0.82f

                val dataMin = min(currentWeight, min(weekOneWeight, targetWeight))
                val dataMax = max(currentWeight, max(weekOneWeight, targetWeight))
                val rawRange = (dataMax - dataMin).coerceAtLeast(
                    if (unitLabel == "kg") 2f else 2f * OnboardState.LB_PER_KG
                )
                val visualMin = dataMin - rawRange * 0.20f
                val visualMax = dataMax + rawRange * 0.20f

                fun yFor(value: Float): Float {
                    val fraction = ((visualMax - value) / (visualMax - visualMin))
                        .coerceIn(0f, 1f)
                    return chartTop + (chartBottom - chartTop) * fraction
                }

                val start = Offset(chartLeft, yFor(currentWeight))
                val week = Offset(chartMiddle, yFor(weekOneWeight))
                val end = Offset(chartRight, yFor(targetWeight))
                val curve = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        start.x + (week.x - start.x) * 0.45f, start.y,
                        week.x - (week.x - start.x) * 0.35f, week.y,
                        week.x, week.y
                    )
                    cubicTo(
                        week.x + (end.x - week.x) * 0.35f, week.y,
                        end.x - (end.x - week.x) * 0.45f, end.y,
                        end.x, end.y
                    )
                }

                drawLine(
                    color = ObColor.Track,
                    start = Offset(chartLeft, chartBottom + 12.dp.toPx()),
                    end = Offset(chartRight, chartBottom + 12.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawPath(
                    path = curve,
                    color = ObColor.Track.copy(alpha = 0.7f),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                val pathMeasure = PathMeasure().apply { setPath(curve, false) }
                val visiblePath = Path()
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = pathMeasure.length * animated,
                    destination = visiblePath,
                    startWithMoveTo = true
                )
                drawPath(
                    path = visiblePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(ObColor.Pink, ObColor.Purple, ObColor.Blue),
                        startX = chartLeft,
                        endX = chartRight
                    ),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                fun drawMilestone(point: Offset, color: Color, visible: Boolean) {
                    if (!visible) return
                    drawLine(
                        color = color.copy(alpha = 0.45f),
                        start = point,
                        end = Offset(point.x, chartBottom + 12.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 7.dp.toPx()))
                    )
                    drawCircle(Color.White, radius = 8.dp.toPx(), center = point)
                    drawCircle(
                        color = color,
                        radius = 8.dp.toPx(),
                        center = point,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                drawMilestone(start, ObColor.Pink, true)
                drawMilestone(week, ObColor.Purple, animated >= 0.48f)
                drawMilestone(end, ObColor.Blue, animated >= 0.98f)
            }

            Text(
                valueLabel(currentWeight),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ObColor.Pink
            )
            if (animated >= 0.48f) {
                Text(
                    valueLabel(weekOneWeight),
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObColor.Purple
                )
            }
            if (animated >= 0.98f) {
                Text(
                    valueLabel(targetWeight),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 2.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObColor.Blue
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Today", fontSize = 15.sp, color = ObColor.TextMuted)
            Text("1 week", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ObColor.TextSecondary)
            Text(endLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ObColor.TextPrimary)
        }
    }
}

/** Biểu đồ "Fitness Results" đi lên của step18. */
@Composable
fun ObPotentialChart(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val left = w * 0.10f
            val bottom = h * 0.84f

            drawLine(Color(0xFFBDBDBD), Offset(left, h * 0.05f), Offset(left, bottom), strokeWidth = 3f)
            drawLine(Color(0xFFBDBDBD), Offset(left, bottom), Offset(w * 0.97f, bottom), strokeWidth = 3f)

            val curve = Path().apply {
                moveTo(left, bottom * 0.94f)
                cubicTo(w * 0.42f, bottom * 0.90f, w * 0.60f, bottom * 0.62f, w * 0.88f, h * 0.12f)
            }
            val fill = Path().apply {
                addPath(curve)
                lineTo(w * 0.88f, bottom)
                lineTo(left, bottom)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(Color(0x33FF2E63), Color(0x00FFFFFF))))
            drawPath(
                curve,
                Brush.linearGradient(listOf(ObColor.Blue, ObColor.Purple, ObColor.Pink)),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            listOf(0.18f to 0.90f, 0.5f to 0.66f).forEach { (fx, fy) ->
                val cx = left + (w * 0.88f - left) * fx
                val cy = bottom * fy
                drawLine(
                    Color(0xFFBDBDBD), Offset(cx, cy), Offset(cx, bottom),
                    strokeWidth = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
                drawCircle(if (fx < 0.3f) ObColor.Blue else ObColor.Purple, radius = 14f, center = Offset(cx, cy))
            }
        }
    }
}
