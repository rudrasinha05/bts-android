package com.babatiffin.bts.feature.customer

import com.babatiffin.bts.data.customer.Address
import com.babatiffin.bts.data.customer.AddressWrite
import com.babatiffin.bts.data.customer.DeliveryAddressDraft
import com.babatiffin.bts.domain.AppRules

fun DeliveryAddressDraft.validationError(): String? = when {
    name.isBlank() || house.isBlank() || locality.isBlank() || district.isBlank() || state.isBlank() ->
        "Complete the name, house, locality, city and state."
    !phone.matches(Regex("[6-9][0-9]{9}")) -> "Enter a valid 10-digit mobile number."
    !pincode.matches(Regex("[1-9][0-9]{5}")) -> "Enter a valid 6-digit pincode."
    !AppRules.hasValidCoordinates(latitude, longitude) -> "Choose a valid delivery pin."
    else -> null
}

fun DeliveryAddressDraft.toAddressWrite(userId: String, isDefault: Boolean) = AddressWrite(
    userId = userId, label = label.trim().ifBlank { "Home" },
    line1 = listOf(house.trim(), building.trim()).filter(String::isNotBlank).joinToString(", "),
    line2 = listOfNotNull(floor.trim().takeIf(String::isNotBlank)?.let { "Floor $it" }, locality.trim())
        .joinToString(", "),
    landmark = landmark.trim().ifBlank { null }, city = district.trim(), state = state.trim(),
    pincode = pincode, isDefault = isDefault, latitude = latitude, longitude = longitude,
)

fun selectedDeliveryAddress(addresses: List<Address>, selectedId: String?): Address? =
    addresses.firstOrNull { it.id == selectedId } ?: addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
