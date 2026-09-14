package com.babatiffin.bts.feature.customer

import androidx.lifecycle.ViewModelStore
import com.babatiffin.bts.data.customer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerAddressTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var repo: FakeCustomer
    private lateinit var vm: CustomerViewModel
    private val draft = DeliveryAddressDraft("Name", "9876543210", "Work", "B-2", "", "", "Sector 1", "", "Noida", "UP", "201310", 28.5, 77.5)
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeCustomer(); vm = CustomerViewModel(repo); store.put("customer", vm)
        vm.load("owner"); dispatcher.scheduler.runCurrent()
    }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    @Test fun editUpdatesExistingIdAndSelectsReturnedRecord() = runTest(dispatcher) {
        var finished = false
        vm.saveDeliveryAddress("existing", draft) { finished = true }; runCurrent()
        assertEquals(1, repo.edits); assertEquals(0, repo.inserts)
        assertTrue(finished)
        assertEquals("existing", vm.state.value.selectedAddressId)
        assertEquals("B-2", vm.state.value.addresses.single().line1)
    }
    @Test fun rapidSaveIsSingleWriteAndForeignIdIsRejected() = runTest(dispatcher) {
        vm.saveDeliveryAddress("foreign-id", draft) { fail("Unknown address") }; runCurrent()
        assertEquals(0, repo.edits)
        vm.saveDeliveryAddress(null, draft) { }
        vm.saveDeliveryAddress(null, draft) { }
        runCurrent()
        assertEquals(1, repo.inserts)
        assertEquals("new", vm.state.value.selectedAddressId)
    }
    @Test fun failedSaveKeepsExistingAddressAndDoesNotNavigate() = runTest(dispatcher) {
        repo.failSave = true
        vm.saveDeliveryAddress("existing", draft) { fail("Must stay in editor") }; runCurrent()
        assertFalse(vm.state.value.addressSaving)
        assertNotNull(vm.state.value.message)
        assertEquals("A-1", vm.state.value.addresses.single().line1)
    }
    @Test fun loadFailureHasRetryAndLateLoadCannotRestoreLoggedOutData() = runTest(dispatcher) {
        repo.failRead = true
        vm.reloadAddresses(); runCurrent()
        assertNotNull(vm.state.value.addressError)
        repo.failRead = false
        vm.reloadAddresses(); runCurrent()
        assertNull(vm.state.value.addressError)
        vm.load("other"); vm.load(null); runCurrent()
        assertNull(vm.state.value.userId)
        assertTrue(vm.state.value.addresses.isEmpty())
    }

    private class FakeCustomer : CustomerRepository {
        var inserts = 0; var edits = 0; var failSave = false; var failRead = false
        override suspend fun profile(userId: String) = CustomerProfile(userId, "Name", "9876543210")
        override suspend fun saveProfile(profile: CustomerProfile) = Unit
        override suspend fun addresses(userId: String): List<Address> {
            if (failRead) error("offline")
            return listOf(Address("existing", userId, "Home", "A-1", pincode = "201310", isDefault = true))
        }
        private fun saved(id: String, a: AddressWrite): Address {
            if (failSave) error("offline")
            return Address(id, a.userId, a.label, a.line1, a.line2, a.landmark, a.city, a.state, a.pincode, a.isDefault, a.latitude, a.longitude)
        }
        override suspend fun addAddress(address: AddressWrite): Address { inserts++; return saved("new", address) }
        override suspend fun editAddress(id: String, address: AddressWrite): Address { edits++; return saved(id, address) }
        override suspend fun deleteAddress(id: String, userId: String) = Unit
        override suspend fun setDefaultAddress(id: String, userId: String) = Unit
        override suspend fun updateLocation(id: String, userId: String, latitude: Double, longitude: Double) = Unit
        override suspend fun nutritionProfile(userId: String): NutritionProfile? = null
        override suspend fun saveNutrition(profile: NutritionProfile) = Unit
        override suspend fun mealNutrition() = emptyList<MealNutrition>()
        override suspend fun tickets(userId: String) = emptyList<SupportTicket>()
        override suspend fun createTicket(userId: String, subject: String, message: String, category: String) = Unit
    }
}
