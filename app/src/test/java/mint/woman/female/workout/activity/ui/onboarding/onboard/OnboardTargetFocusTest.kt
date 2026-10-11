package mint.woman.female.workout.activity.ui.onboarding.onboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardTargetFocusTest {

    @Test
    fun mapsNewAreasToLegacyKeys() {
        assertEquals("abs", OnboardState.targetFocusFor("belly"))
        assertEquals("arm", OnboardState.targetFocusFor("arm"))
        assertEquals("butt", OnboardState.targetFocusFor("butt"))
        assertEquals("thigh", OnboardState.targetFocusFor("leg"))
        assertEquals("body", OnboardState.targetFocusFor("full_body"))
    }

    @Test
    fun prefersSpecificAreaOverFullBody() {
        assertEquals("abs", OnboardState.targetFocusFor("full_body,belly,leg"))
    }

    @Test
    fun emptyOrUnknownGivesNull() {
        assertNull(OnboardState.targetFocusFor(""))
        assertNull(OnboardState.targetFocusFor("chest"))
    }
}
