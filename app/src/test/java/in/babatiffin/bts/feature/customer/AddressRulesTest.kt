package com.babatiffin.bts.feature.customer

import com.babatiffin.bts.data.customer.Address
import com.babatiffin.bts.data.customer.DeliveryAddressDraft
import org.junit.Assert.*
import org.junit.Test

class AddressRulesTest {
    private val draft = DeliveryAddressDraft("Customer", "9876543210", "Home", "A-1", "Tower B", "2", "Sector 1", "Gate 2", "Greater Noida", "Uttar Pradesh", "201310", 28.47, 77.50)
    private fun address(id: String, default: Boolean = false) = Address(id, "u", "Home", "A-1", pincode = "201310", isDefault = default)

    @Test fun explicitSelectionWinsWithoutChangingDefault() {
        val first = address("first", true); val second = address("second")
        assertEquals(second, selectedDeliveryAddress(listOf(first, second), "second"))
        assertTrue(first.isDefault)
    }
    @Test fun deletedSelectionFallsBackAndEmptyListHasNoAddress() {
        val first = address("first", true)
        assertEquals(first, selectedDeliveryAddress(listOf(first), "deleted"))
        assertNull(selectedDeliveryAddress(emptyList(), "deleted"))
    }
    @Test fun validatesPhonePincodeAndGps() {
        assertNull(draft.validationError())
        assertNotNull(draft.copy(phone = "123").validationError())
        assertNotNull(draft.copy(pincode = "000000").validationError())
        assertNotNull(draft.copy(latitude = Double.NaN).validationError())
        assertNotNull(draft.copy(locality = " ").validationError())
    }
    @Test fun mapsExistingSchemaWithoutLosingFloorOrLandmark() {
        val write = draft.toAddressWrite("owner", true)
        assertEquals("owner", write.userId)
        assertEquals("A-1, Tower B", write.line1)
        assertEquals("Floor 2, Sector 1", write.line2)
        assertEquals("Gate 2", write.landmark)
        assertTrue(write.isDefault)
    }
}
