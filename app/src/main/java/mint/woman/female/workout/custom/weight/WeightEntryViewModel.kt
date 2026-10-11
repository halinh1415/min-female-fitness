package mint.woman.female.workout.custom.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import mint.woman.female.workout.model.UserObject
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.UserDatabase

class WeightEntryViewModel(application: Application) : AndroidViewModel(application) {
    data class SaveResult(val user: UserObject, val isKg: Boolean, val isNewest: Boolean)

    private val preference = MyPreference.newInstance(application)
    private val userDao = UserDatabase.getDatabase(application).userDao()
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<WeightEntryState> = _state.asStateFlow()

    init {
        loadPrevious()
    }

    private fun initialState(): WeightEntryState {
        val isKg = preference.isKg()
        val kg = preference.getCurrentWeightKG()
        val height = preference.getCurrentHeightCm()
        val waist = preference.getCurrentWaistCm()
        val (heightFt, heightIn) = WeightEntryMath.cmToFtIn(height)
        val (waistFt, waistIn) = WeightEntryMath.cmToFtIn(waist)
        return WeightEntryState(
            weightText = WeightEntryMath.format1(if (isKg) kg else WeightEntryMath.kgToLb(kg)),
            isKg = isKg,
            heightCmText = WeightEntryMath.format1(height),
            heightFeetText = heightFt.toString(),
            heightInchesText = WeightEntryMath.format1(heightIn),
            waistCmText = WeightEntryMath.format1(waist),
            waistFeetText = waistFt.toString(),
            waistInchesText = WeightEntryMath.format1(waistIn),
        )
    }

    fun setWeightText(text: String) {
        if (!isDecimalInput(text)) return
        _state.update { it.copy(weightText = text.take(6)) }
    }

    fun setWeightValue(value: Float) {
        val current = _state.value
        val min = if (current.isKg) WeightEntryMath.MIN_WEIGHT_KG else WeightEntryMath.kgToLb(WeightEntryMath.MIN_WEIGHT_KG)
        val max = if (current.isKg) WeightEntryMath.MAX_WEIGHT_KG else WeightEntryMath.kgToLb(WeightEntryMath.MAX_WEIGHT_KG)
        val clamped = WeightEntryMath.roundTenth(value.coerceIn(min, max))
        _state.update { it.copy(weightText = WeightEntryMath.format1(clamped)) }
    }

    fun stepWeight(delta: Float) {
        val value = WeightEntryMath.parseDecimal(_state.value.weightText) ?: return
        setWeightValue(value + delta)
    }

    fun setUnitKg(isKg: Boolean) {
        val current = _state.value
        if (current.isKg == isKg) return
        val kg = current.weightKg
        val height = current.heightCm
        val waist = current.waistCm
        _state.update {
            it.copy(
                isKg = isKg,
                weightText = kg?.let { value -> WeightEntryMath.format1(if (isKg) value else WeightEntryMath.kgToLb(value)) }
                    ?: it.weightText,
            ).withMeasurements(height, waist).copy(heightCmExact = height, waistCmExact = waist)
        }
    }

    fun toggleHeightExpanded() = _state.update { it.copy(heightExpanded = !it.heightExpanded) }
    fun toggleWaistExpanded() = _state.update { it.copy(waistExpanded = !it.waistExpanded) }

    /** Thước chiều cao: [value] là cm (kg) hoặc tổng inch (lb); bước 1 cm / 0.5 in. */
    fun setHeightValue(value: Float) {
        val cm = toCm(value, cmStep = 1f, min = WeightEntryMath.MIN_HEIGHT_CM, max = WeightEntryMath.MAX_HEIGHT_CM)
        _state.update { it.withMeasurements(cm, null).copy(heightCmExact = cm) }
    }

    fun stepHeight(delta: Float) {
        val current = _state.value.heightDisplay ?: return
        setHeightValue(current + delta)
    }

    /** Thước vòng eo: [value] là cm (kg) hoặc tổng inch (lb); bước 0.5. */
    fun setWaistValue(value: Float) {
        val cm = toCm(value, cmStep = 0.5f, min = WeightEntryMath.MIN_WAIST_CM, max = WeightEntryMath.MAX_WAIST_CM)
        _state.update { it.withMeasurements(null, cm).copy(waistCmExact = cm) }
    }

    fun stepWaist(delta: Float) {
        val current = _state.value.waistDisplay ?: return
        setWaistValue(current + delta)
    }

    private fun toCm(display: Float, cmStep: Float, min: Float, max: Float): Float {
        val isKg = _state.value.isKg
        val step = if (isKg) cmStep else 0.5f
        val stepped = (display / step).roundToInt() * step
        return (if (isKg) stepped else stepped * WeightEntryMath.CM_PER_INCH).coerceIn(min, max)
    }

    fun setDate(millis: Long) {
        val now = System.currentTimeMillis()
        val chosen = millis.coerceAtMost(now)
        val todayStart = WeightEntryMath.dayBounds(now).first
        _state.update {
            it.copy(dateMillis = chosen, isToday = WeightEntryMath.dayBounds(chosen).first == todayStart)
        }
        loadPrevious()
    }

    private fun loadPrevious() {
        val before = WeightEntryMath.dayBounds(_state.value.dateMillis).first
        viewModelScope.launch(Dispatchers.IO) {
            val previous = userDao.getLatestBefore(before)
            _state.update { it.copy(previousKg = previous?.getWeight()) }
        }
    }

    fun save(onSaved: (SaveResult) -> Unit) {
        val snapshot = _state.value
        val kg = snapshot.weightKg ?: return
        val height = snapshot.heightCm ?: return
        val waist = snapshot.waistCm ?: return
        if (!snapshot.canSave) return
        _state.update { it.copy(saving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val latest = userDao.getLatest()
            val dayEnd = WeightEntryMath.dayBounds(snapshot.dateMillis).second
            val isNewest = latest == null || latest.getDate() <= dayEnd
            val user = UserObject().apply {
                setDate(snapshot.dateMillis)
                setWeight(kg)
                setHeight(height)
                setWaist(waist)
            }
            userDao.insert(user)
            withContext(Dispatchers.Main) {
                preference.setUnitKg(snapshot.isKg)
                if (isNewest) {
                    preference.setCurrentWeight(kg)
                    preference.setCurrentHeight(height)
                    preference.setCurrentWaist(waist)
                }
                _state.update { it.copy(saving = false) }
                onSaved(SaveResult(user, snapshot.isKg, isNewest))
            }
        }
    }

    private fun WeightEntryState.withMeasurements(height: Float?, waist: Float?): WeightEntryState {
        val (heightFt, heightIn) = height?.let(WeightEntryMath::cmToFtIn) ?: (0 to 0f)
        val (waistFt, waistIn) = waist?.let(WeightEntryMath::cmToFtIn) ?: (0 to 0f)
        return copy(
            heightCmText = height?.let(WeightEntryMath::format1) ?: heightCmText,
            heightFeetText = height?.let { heightFt.toString() } ?: heightFeetText,
            heightInchesText = height?.let { WeightEntryMath.format1(heightIn) } ?: heightInchesText,
            waistCmText = waist?.let(WeightEntryMath::format1) ?: waistCmText,
            waistFeetText = waist?.let { waistFt.toString() } ?: waistFeetText,
            waistInchesText = waist?.let { WeightEntryMath.format1(waistIn) } ?: waistInchesText,
        )
    }



    private fun isDecimalInput(text: String): Boolean =
        text.all { it.isDigit() || it == '.' || it == ',' } &&
            text.count { it == '.' || it == ',' } <= 1
}
