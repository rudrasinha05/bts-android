package com.babatiffin.bts.feature.menu

import com.babatiffin.bts.data.menu.Meal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuFoodGroupsTest {
    private fun meal(name: String) = Meal("id", name, "", "breakfast", "veg", "regular", 50.0, emptyList(), emptyList(), null)

    @Test fun mixedDishesUseDishFormBeforeIngredient() {
        mapOf("Paneer Paratha" to "bread", "Paneer Poha" to "breakfast",
            "Chicken Sandwich" to "breakfast", "Chicken Biryani" to "rice",
            "Paneer Thali" to "other").forEach { (name, group) ->
            assertEquals(name, group, meal(name).menuFoodGroupKey())
        }
    }
    @Test fun substringDoesNotTurnKandaIntoEgg() {
        assertEquals("breakfast", meal("Kanda Poha").menuFoodGroupKey())
        assertEquals("egg", meal("Anda Curry").menuFoodGroupKey())
    }
    @Test fun riceVariantsAndCurriesRemainDiscoverable() {
        mapOf("Plain Rice" to "rice", "Jeera Rice" to "rice", "Pulao" to "rice",
            "Paneer Butter Masala" to "paneer", "Chicken Curry" to "chicken",
            "Dal Makhani" to "dal").forEach { (name, group) ->
            assertEquals(name, group, meal(name).menuFoodGroupKey())
        }
    }
    @Test fun punctuationCaseAndUnknownNamesAreSafe() {
        assertEquals("breakfast", meal("  KANDA-POHA ").menuFoodGroupKey())
        assertEquals("other", meal("Chef Special").menuFoodGroupKey())
        assertEquals("other", meal("").menuFoodGroupKey())
        assertTrue(meal("Chef Special").matchesMenuFoodGroup("all"))
    }
}
