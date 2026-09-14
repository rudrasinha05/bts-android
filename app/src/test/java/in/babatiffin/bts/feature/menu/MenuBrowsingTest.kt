package com.babatiffin.bts.feature.menu

import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.data.cart.CartAddOn
import com.babatiffin.bts.data.cart.CartLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuBrowsingTest {
    private fun meal(id: String, name: String, category: String = "lunch", type: String = "veg") =
        Meal(id, name, "", category, type, "regular", 50.0, emptyList(), emptyList(), null)

    @Test fun dishAndDietAndMealTimeFiltersIntersect() {
        val rice = meal("1", "Jeera Rice")
        val state = MealDiscoveryState(meals = listOf(rice, meal("2", "Chicken Rice", type = "chicken"),
            meal("3", "Plain Rice", category = "dinner")), category = "lunch", foodType = "veg")
        assertEquals(listOf(rice), state.menuItemsForGroup("rice"))
    }
    @Test fun extrasRemainAvailableAcrossBaseMealFiltersAndAreDeduplicated() {
        val extra = meal("extra", "Raita")
        val state = MealDiscoveryState(addOns = listOf(extra, extra), category = "dinner", foodType = "chicken")
        assertEquals(listOf(extra), state.menuItemsForGroup("addons"))
        assertTrue(state.menuItemsForGroup("rice").isEmpty())
    }
    @Test fun cartTotalsIncludeExtrasForEachMealPortion() {
        val line = CartLine("line", "meal", "Meal", 100.0, 2,
            listOf(CartAddOn("extra", "Raita", 20.0, 1)))
        assertEquals(240.0, line.lineTotal, 0.001)
        assertEquals(200.0, line.copy(addOns = emptyList()).lineTotal, 0.001)
    }
}
