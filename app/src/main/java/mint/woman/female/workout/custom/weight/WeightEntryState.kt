package mint.woman.female.workout.custom.weight

data class WeightEntryState(
    val weightText: String = "",
    val isKg: Boolean = true,
    val heightCmText: String = "",
    val heightFeetText: String = "",
    val heightInchesText: String = "",
    val waistCmText: String = "",
    val waistFeetText: String = "",
    val waistInchesText: String = "",
    val heightExpanded: Boolean = false,
    val waistExpanded: Boolean = false,
    val dateMillis: Long = System.currentTimeMillis(),
    val isToday: Boolean = true,
    val previousKg: Float? = null,
    val saving: Boolean = false,
) {
    val weightKg: Float?
        get() = WeightEntryMath.parseDecimal(weightText)?.let {
            if (isKg) it else WeightEntryMath.lbToKg(it)
        }

    val heightCm: Float?
        get() = measurementCm(heightCmText, heightFeetText, heightInchesText)

    val waistCm: Float?
        get() = measurementCm(waistCmText, waistFeetText, waistInchesText)

    private fun measurementCm(cm: String, feet: String, inches: String): Float? {
        if (isKg) return WeightEntryMath.parseDecimal(cm)
        val ft = WeightEntryMath.parseDecimal(feet)
        val inch = if (inches.isBlank()) 0f else WeightEntryMath.parseDecimal(inches)
        return if (ft == null || inch == null || inch < 0f || inch >= WeightEntryMath.INCHES_PER_FOOT) null
        else WeightEntryMath.ftInToCm(ft, inch)
    }

    val weightError: Boolean
        get() = weightText.isNotBlank() && !WeightEntryMath.isValidWeightKg(weightKg)
    val heightError: Boolean get() = !WeightEntryMath.isValidHeightCm(heightCm)
    val waistError: Boolean get() = !WeightEntryMath.isValidWaistCm(waistCm)
    val canSave: Boolean
        get() = !saving && WeightEntryMath.isValidWeightKg(weightKg) &&
            WeightEntryMath.isValidHeightCm(heightCm) && WeightEntryMath.isValidWaistCm(waistCm)
}
