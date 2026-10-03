package mint.woman.female.workout.weight

import mint.woman.female.workout.custom.weight.WeightEntryMath
import mint.woman.female.workout.custom.weight.WeightEntryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightEntryMathTest {
    @Test
    fun decimalInputAcceptsCommaAndDot() {
        assertEquals(70.5f, WeightEntryMath.parseDecimal("70,5")!!, 0.001f)
        assertEquals(70.5f, WeightEntryMath.parseDecimal("70.5")!!, 0.001f)
        assertNull(WeightEntryMath.parseDecimal("7.0.5"))
    }

    @Test
    fun conversionsRoundTrip() {
        assertEquals(70f, WeightEntryMath.lbToKg(WeightEntryMath.kgToLb(70f)), 0.001f)
        val (feet, inches) = WeightEntryMath.cmToFtIn(170f)
        assertEquals(5, feet)
        assertEquals(6.9f, inches, 0.001f)
        assertEquals(170.18f, WeightEntryMath.ftInToCm(5f, 7f), 0.01f)
    }

    @Test
    fun stateValidatesAllMeasurements() {
        val metric = WeightEntryState(
            weightText = "65,5",
            heightCmText = "165",
            waistCmText = "72",
        )
        assertTrue(metric.canSave)
        assertEquals(65.5f, metric.weightKg!!, 0.001f)

        val imperial = WeightEntryState(
            weightText = "144.4",
            isKg = false,
            heightFeetText = "5",
            heightInchesText = "5",
            waistFeetText = "2",
            waistInchesText = "4.5",
        )
        assertTrue(imperial.canSave)
        assertFalse(imperial.heightError)

        assertTrue(imperial.copy(heightInchesText = "12").heightError)
        assertFalse(metric.copy(weightText = "").canSave)
    }
}
