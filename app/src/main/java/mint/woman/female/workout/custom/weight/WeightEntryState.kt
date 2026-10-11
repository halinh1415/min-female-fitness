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
    /**
     * Số cm gốc khi kéo thước hoặc đổi kg/lb: cm → ft/in → cm không bị lệch do làm tròn
     * (170 cm → 5 ft 6.9 in → 169.9 cm).
     */
    val heightCmExact: Float? = null,
    val waistCmExact: Float? = null,
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
        get() = heightCmExact ?: measurementCm(heightCmText, heightFeetText, heightInchesText)

    val waistCm: Float?
        get() = waistCmExact ?: measurementCm(waistCmText, waistFeetText, waistInchesText)

    /** Giá trị cho thước kéo: cm, hoặc tổng số inch khi đang dùng lb. */
    val heightDisplay: Float? get() = heightCm?.let(::toDisplay)
    val waistDisplay: Float? get() = waistCm?.let(::toDisplay)

    private fun toDisplay(cm: Float): Float = if (isKg) cm else cm / WeightEntryMath.CM_PER_INCH

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
