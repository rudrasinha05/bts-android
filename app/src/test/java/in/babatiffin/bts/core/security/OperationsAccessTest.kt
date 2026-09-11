package com.babatiffin.bts.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationsAccessTest {
    @Test fun customerCannotAccessOperations() {
        assertFalse(setOf("customer").canAccessKitchenOperations())
        assertFalse(setOf("customer").canAccessDeliveryOperations())
    }

    @Test fun kitchenRolesOnlyAccessKitchen() {
        assertTrue(setOf("kitchen_staff").canAccessKitchenOperations())
        assertFalse(setOf("kitchen_staff").canAccessDeliveryOperations())
    }

    @Test fun deliveryRolesOnlyAccessDelivery() {
        assertFalse(setOf("delivery_agent").canAccessKitchenOperations())
        assertTrue(setOf("delivery_agent").canAccessDeliveryOperations())
    }

    @Test fun adminAccessesAllOperations() {
        assertTrue(setOf("admin").canAccessKitchenOperations())
        assertTrue(setOf("admin").canAccessDeliveryOperations())
    }
}
