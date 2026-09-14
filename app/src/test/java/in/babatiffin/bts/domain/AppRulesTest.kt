package com.babatiffin.bts.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRulesTest {
    @Test fun coordinatesRejectMissingNonFiniteAndOutOfRangeValues() {
        assertFalse(AppRules.hasValidCoordinates(null, 77.0))
        assertFalse(AppRules.hasValidCoordinates(28.0, null))
        assertFalse(AppRules.hasValidCoordinates(Double.NaN, 77.0))
        assertFalse(AppRules.hasValidCoordinates(28.0, Double.POSITIVE_INFINITY))
        assertFalse(AppRules.hasValidCoordinates(91.0, 77.0))
        assertFalse(AppRules.hasValidCoordinates(28.0, -181.0))
        assertTrue(AppRules.hasValidCoordinates(28.47, 77.50))
        assertTrue(AppRules.hasValidCoordinates(0.0, 0.0))
        assertTrue(AppRules.hasValidCoordinates(-90.0, 180.0))
    }

    @Test fun breakfastWindowIncludesBoundaries() {
        assertEquals("breakfast", AppRules.mealCategoryForHour(5))
        assertEquals("breakfast", AppRules.mealCategoryForHour(10))
    }

    @Test fun lunchWindowIncludesBoundaries() {
        assertEquals("lunch", AppRules.mealCategoryForHour(11))
        assertEquals("lunch", AppRules.mealCategoryForHour(16))
    }

    @Test fun remainingHoursUseDinner() {
        assertEquals("dinner", AppRules.mealCategoryForHour(0))
        assertEquals("dinner", AppRules.mealCategoryForHour(23))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidHourIsRejected() { AppRules.mealCategoryForHour(24) }

    @Test fun discountChoosesBestOffer() {
        assertEquals(200.0, AppRules.discount(1_000.0, 100.0, 20.0), 0.001)
    }

    @Test fun discountNeverExceedsSubtotal() {
        assertEquals(500.0, AppRules.discount(500.0, 900.0, null), 0.001)
    }

    @Test fun checkoutRequiresEverySafetyGate() {
        assertTrue(AppRules.isCheckoutReady(true, true, true, true, true, false))
        assertFalse(AppRules.isCheckoutReady(true, true, true, false, true, false))
        assertFalse(AppRules.isCheckoutReady(true, true, true, true, true, true))
    }

    @Test fun upgradesOnlyAllowHigherPricedPlans() {
        assertTrue(AppRules.isSuccessorPlan(999.0, 1_499.0))
        assertFalse(AppRules.isSuccessorPlan(999.0, 999.0))
        assertFalse(AppRules.isSuccessorPlan(999.0, 749.0))
    }
}
