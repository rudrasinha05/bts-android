package com.babatiffin.bts.domain

object AppRules {
    fun mealCategoryForHour(hour: Int): String {
        require(hour in 0..23)
        return when (hour) {
            in 5..10 -> "breakfast"
            in 11..16 -> "lunch"
            else -> "dinner"
        }
    }

    fun discount(subtotal: Double, amount: Double?, percent: Double?): Double {
        if (subtotal <= 0.0) return 0.0
        val fixed = amount?.coerceAtLeast(0.0) ?: 0.0
        val proportional = subtotal * (percent?.coerceAtLeast(0.0) ?: 0.0) / 100.0
        return maxOf(fixed, proportional).coerceAtMost(subtotal)
    }

    fun isCheckoutReady(
        hasItems: Boolean,
        hasAddress: Boolean,
        hasCoordinates: Boolean,
        locationConfirmed: Boolean,
        confirmationAccepted: Boolean,
        loading: Boolean,
    ): Boolean = hasItems && hasAddress && hasCoordinates && locationConfirmed && confirmationAccepted && !loading

    fun isSuccessorPlan(currentPrice: Double?, candidatePrice: Double): Boolean =
        currentPrice == null || candidatePrice > currentPrice
}
