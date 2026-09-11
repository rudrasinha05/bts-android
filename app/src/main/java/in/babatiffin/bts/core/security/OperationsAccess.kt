package com.babatiffin.bts.core.security

private val kitchenRoles = setOf("admin", "kitchen_manager", "kitchen_staff")
private val deliveryRoles = setOf("admin", "delivery_manager", "delivery_agent")

fun Set<String>.canAccessKitchenOperations(): Boolean = any(kitchenRoles::contains)
fun Set<String>.canAccessDeliveryOperations(): Boolean = any(deliveryRoles::contains)
