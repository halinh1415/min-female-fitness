package mint.woman.female.workout.custom.weight

import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/** Tính toán thuần cho form số đo; tách riêng để kiểm thử không cần Android. */
object WeightEntryMath {
    const val KG_PER_LB = 0.45359237f
    const val CM_PER_INCH = 2.54f
    const val INCHES_PER_FOOT = 12

    const val MIN_WEIGHT_KG = 30f
    const val MAX_WEIGHT_KG = 150f
    const val MIN_HEIGHT_CM = 80f
    const val MAX_HEIGHT_CM = 250f
    const val MIN_WAIST_CM = 50f
    const val MAX_WAIST_CM = 200f

    fun parseDecimal(text: String): Float? {
        val normalized = text.trim().replace(',', '.')
        if (normalized.isEmpty() || normalized.count { it == '.' } > 1) return null
        return normalized.toFloatOrNull()?.takeIf { it.isFinite() }
    }

    fun format1(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)
    fun kgToLb(kg: Float): Float = kg / KG_PER_LB
    fun lbToKg(lb: Float): Float = lb * KG_PER_LB

    fun cmToFtIn(cm: Float): Pair<Int, Float> {
        val totalInches = cm / CM_PER_INCH
        var feet = floor(totalInches / INCHES_PER_FOOT).toInt()
        var inches = ((totalInches - feet * INCHES_PER_FOOT) * 10).roundToInt() / 10f
        if (inches >= INCHES_PER_FOOT) {
            feet++
            inches -= INCHES_PER_FOOT
        }
        return feet to inches
    }

    fun ftInToCm(feet: Float, inches: Float): Float =
        (feet * INCHES_PER_FOOT + inches) * CM_PER_INCH

    fun isValidWeightKg(value: Float?): Boolean = value != null && value in MIN_WEIGHT_KG..MAX_WEIGHT_KG
    fun isValidHeightCm(value: Float?): Boolean = value != null && value in MIN_HEIGHT_CM..MAX_HEIGHT_CM
    fun isValidWaistCm(value: Float?): Boolean = value != null && value in MIN_WAIST_CM..MAX_WAIST_CM
    fun roundTenth(value: Float): Float = (value * 10).roundToInt() / 10f

    fun bmi(kg: Float, cm: Float): Float {
        val meters = cm / 100f
        return if (meters <= 0f) 0f else kg / (meters * meters)
    }

    fun dayBounds(millis: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return start to calendar.timeInMillis - 1
    }
}
