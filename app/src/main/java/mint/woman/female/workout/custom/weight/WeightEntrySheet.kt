package mint.woman.female.workout.custom.weight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt
import mint.woman.female.workout.R
import mint.woman.female.workout.custom.BMIView

@Composable
fun WeightEntrySheet(state: WeightEntryState, gender: Int, actions: WeightEntryActions) {
    val colors = WeightEntryThemeValues.colors
    val haptic = LocalHapticFeedback.current
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 12.dp),
    ) {
        Box(
            Modifier
                .padding(top = 10.dp, bottom = 14.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.border)
                .align(Alignment.CenterHorizontally),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.weight_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.title,
                modifier = Modifier.weight(1f),
            )
            DateChip(state) { showDatePicker = true }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = state.weightText,
                onValueChange = actions.onWeightText,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                    color = if (state.weightError) colors.bad else colors.title,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.width(150.dp),
            )
            Spacer(Modifier.width(10.dp))
            UnitToggle(
                options = listOf("kg", "lb"),
                selected = if (state.isKg) 0 else 1,
                onSelect = { actions.onUnitKg(it == 0) },
            )
        }

        val current = WeightEntryMath.parseDecimal(state.weightText)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            StepButton("−") { actions.onStep(-0.1f) }
            WeightRuler(
                value = current ?: if (state.isKg) 60f else 132f,
                onValueChange = actions.onWeightValue,
                modifier = Modifier.weight(1f).height(64.dp).padding(horizontal = 8.dp),
            )
            StepButton("+") { actions.onStep(0.1f) }
        }

        if (state.weightError) {
            ErrorText(
                stringResource(
                    R.string.weight_error_range,
                    weightRangeText(WeightEntryMath.MIN_WEIGHT_KG, state.isKg),
                    weightRangeText(WeightEntryMath.MAX_WEIGHT_KG, state.isKg),
                ),
            )
        } else {
            ComparisonText(state)
        }

        Spacer(Modifier.height(14.dp))
        BmiLine(state, gender)
        Spacer(Modifier.height(8.dp))

        MeasurementSection(
            label = stringResource(R.string.txt_height),
            isKg = state.isKg,
            cm = state.heightCm,
            cmText = state.heightCmText,
            feetText = state.heightFeetText,
            inchesText = state.heightInchesText,
            expanded = state.heightExpanded,
            error = state.heightError,
            minCm = WeightEntryMath.MIN_HEIGHT_CM,
            maxCm = WeightEntryMath.MAX_HEIGHT_CM,
            errorTextRes = R.string.height_error_range,
            onToggle = actions.onToggleHeight,
            onCm = actions.onHeightCm,
            onFeet = actions.onHeightFeet,
            onInches = actions.onHeightInches,
        )
        MeasurementSection(
            label = stringResource(R.string.txt_waist_circumference),
            isKg = state.isKg,
            cm = state.waistCm,
            cmText = state.waistCmText,
            feetText = state.waistFeetText,
            inchesText = state.waistInchesText,
            expanded = state.waistExpanded,
            error = state.waistError,
            minCm = WeightEntryMath.MIN_WAIST_CM,
            maxCm = WeightEntryMath.MAX_WAIST_CM,
            errorTextRes = R.string.waist_error_range,
            onToggle = actions.onToggleWaist,
            onCm = actions.onWaistCm,
            onFeet = actions.onWaistFeet,
            onInches = actions.onWaistInches,
        )

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = actions.onCancel,
                border = BorderStroke(1.dp, colors.border),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Text(stringResource(R.string.txt_cancel), color = colors.body, fontSize = 16.sp)
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    actions.onSave()
                },
                enabled = state.canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.weight(2f).height(52.dp),
            ) {
                Text(stringResource(R.string.txt_save), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showDatePicker) {
        WeightDatePicker(
            selectedMillis = state.dateMillis,
            onPick = {
                showDatePicker = false
                actions.onDate(it)
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

class WeightEntryActions(
    val onWeightText: (String) -> Unit,
    val onWeightValue: (Float) -> Unit,
    val onStep: (Float) -> Unit,
    val onUnitKg: (Boolean) -> Unit,
    val onToggleHeight: () -> Unit,
    val onHeightCm: (String) -> Unit,
    val onHeightFeet: (String) -> Unit,
    val onHeightInches: (String) -> Unit,
    val onToggleWaist: () -> Unit,
    val onWaistCm: (String) -> Unit,
    val onWaistFeet: (String) -> Unit,
    val onWaistInches: (String) -> Unit,
    val onDate: (Long) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
)

@Composable
private fun DateChip(state: WeightEntryState, onClick: () -> Unit) {
    val colors = WeightEntryThemeValues.colors
    val label = if (state.isToday) stringResource(R.string.weight_date_today)
    else DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(state.dateMillis))
    Text(
        "$label ▾",
        color = colors.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun UnitToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = WeightEntryThemeValues.colors
    Row(Modifier.clip(RoundedCornerShape(50)).background(colors.primaryContainer).padding(3.dp)) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Text(
                label,
                color = if (isSelected) colors.onPrimary else colors.body,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) colors.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    val colors = WeightEntryThemeValues.colors
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, colors.border),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(44.dp).semantics {
            contentDescription = if (label == "+") "+0.1" else "-0.1"
        },
    ) {
        Text(label, color = colors.primary, fontSize = 22.sp)
    }
}

/** Thước ngang: kéo sang trái tăng, sang phải giảm, mỗi vạch ứng với 0,1 đơn vị. */
@Composable
private fun WeightRuler(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    val colors = WeightEntryThemeValues.colors
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = colors.muted, fontSize = 10.sp)
    val currentValue by rememberUpdatedState(value)
    val onChange by rememberUpdatedState(onValueChange)

    Canvas(
        modifier.pointerInput(Unit) {
            val pxPerTenth = 9.dp.toPx()
            var startValue = 0f
            var dragged = 0f
            detectHorizontalDragGestures(
                onDragStart = {
                    startValue = currentValue
                    dragged = 0f
                },
            ) { change, dragAmount ->
                change.consume()
                dragged += dragAmount
                onChange(startValue - dragged / pxPerTenth * 0.1f)
            }
        },
    ) {
        val pxPerTenth = 9.dp.toPx()
        val centerX = size.width / 2
        val baseTenth = (value * 10).roundToInt()
        val offset = (value * 10 - baseTenth) * pxPerTenth
        val visible = (size.width / pxPerTenth / 2).toInt() + 2
        for (i in -visible..visible) {
            val tenth = baseTenth + i
            if (tenth < 0) continue
            val x = centerX + i * pxPerTenth - offset
            val major = tenth % 10 == 0
            val half = tenth % 5 == 0
            val tickHeight = when {
                major -> size.height * 0.5f
                half -> size.height * 0.35f
                else -> size.height * 0.22f
            }
            drawLine(
                colors.border,
                Offset(x, 0f),
                Offset(x, tickHeight),
                strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx(),
            )
            if (major) {
                val label = textMeasurer.measure((tenth / 10).toString(), labelStyle)
                drawText(label, topLeft = Offset(x - label.size.width / 2, tickHeight + 4.dp.toPx()))
            }
        }
        drawLine(
            colors.primary,
            Offset(centerX, 0f),
            Offset(centerX, size.height * 0.62f),
            strokeWidth = 3.dp.toPx(),
        )
    }
}

@Composable
private fun ComparisonText(state: WeightEntryState) {
    val colors = WeightEntryThemeValues.colors
    val kg = state.weightKg
    val previous = state.previousKg
    val text: String
    val color: Color
    if (previous == null || kg == null) {
        text = stringResource(R.string.weight_first_entry)
        color = colors.muted
    } else {
        val diffKg = kg - previous
        val diff = if (state.isKg) diffKg else WeightEntryMath.kgToLb(diffKg)
        val unit = if (state.isKg) "kg" else "lb"
        if (abs(diff) < 0.05f) {
            text = stringResource(R.string.weight_vs_last, "= 0 $unit")
            color = colors.muted
        } else {
            val arrow = if (diff < 0) "▼" else "▲"
            text = stringResource(R.string.weight_vs_last, "$arrow ${WeightEntryMath.format1(abs(diff))} $unit")
            color = if (diff < 0) colors.good else colors.warning
        }
    }
    Text(
        text,
        color = color,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )
}

@Composable
private fun BmiLine(state: WeightEntryState, gender: Int) {
    val colors = WeightEntryThemeValues.colors
    val context = LocalContext.current
    val kg = state.weightKg
    val cm = state.heightCm
    if (kg == null || cm == null || state.weightError || state.heightError) return
    val bmi = WeightEntryMath.bmi(kg, cm)
    val category = remember(kg, cm, gender) {
        BMIView(context).setGender(gender).setWeight(kg).setHeight(cm / 100f).bodyDescription
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(colors.primaryContainer).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("BMI ${WeightEntryMath.format1(bmi)}", color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(" · $category", color = colors.body, fontSize = 14.sp)
    }
}

@Composable
private fun MeasurementSection(
    label: String,
    isKg: Boolean,
    cm: Float?,
    cmText: String,
    feetText: String,
    inchesText: String,
    expanded: Boolean,
    error: Boolean,
    minCm: Float,
    maxCm: Float,
    errorTextRes: Int,
    onToggle: () -> Unit,
    onCm: (String) -> Unit,
    onFeet: (String) -> Unit,
    onInches: (String) -> Unit,
) {
    val colors = WeightEntryThemeValues.colors
    val summary = when {
        cm == null -> "–"
        isKg -> "${WeightEntryMath.format1(cm)} cm"
        else -> WeightEntryMath.cmToFtIn(cm).let { (ft, inch) -> "$ft ft ${WeightEntryMath.format1(inch)} in" }
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.body, modifier = Modifier.weight(1f))
        Text(summary, color = if (error) colors.bad else colors.title, fontWeight = FontWeight.SemiBold)
        Text(if (expanded) "  ˄" else "  ›", color = colors.muted)
    }
    if (!expanded && !error) return

    if (isKg) {
        NumberField(cmText, "cm", onCm, error, Modifier.fillMaxWidth())
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(feetText, "ft", onFeet, error, Modifier.weight(1f), decimal = false)
            NumberField(inchesText, "in", onInches, error, Modifier.weight(1f))
        }
    }
    if (error) {
        ErrorText(
            stringResource(
                errorTextRes,
                measurementRangeText(minCm, isKg),
                measurementRangeText(maxCm, isKg),
            ),
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    suffix: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier,
    decimal: Boolean = true,
) {
    val colors = WeightEntryThemeValues.colors
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        isError = isError,
        suffix = { Text(suffix, color = colors.muted) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.title,
            unfocusedTextColor = colors.title,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text,
        color = WeightEntryThemeValues.colors.bad,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )
}

private fun weightRangeText(kg: Float, isKg: Boolean): String =
    if (isKg) "${kg.roundToInt()} kg" else "${WeightEntryMath.kgToLb(kg).roundToInt()} lb"

private fun measurementRangeText(cm: Float, isKg: Boolean): String =
    if (isKg) "${cm.roundToInt()} cm"
    else WeightEntryMath.cmToFtIn(cm).let { (ft, inch) -> "$ft ft ${inch.roundToInt()} in" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightDatePicker(selectedMillis: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val colors = WeightEntryThemeValues.colors
    val todayUtc = remember { localToUtcDate(System.currentTimeMillis()) }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = localToUtcDate(selectedMillis),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtc
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { onPick(utcDateToLocalNoon(it)) } ?: onDismiss()
                },
            ) {
                Text(stringResource(R.string.OK), color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.txt_cancel), color = colors.muted)
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

private fun localToUtcDate(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun utcDateToLocalNoon(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0)
    }.timeInMillis
}
