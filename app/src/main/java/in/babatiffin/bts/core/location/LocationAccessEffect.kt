package com.babatiffin.bts.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.babatiffin.bts.data.customer.Address
import com.babatiffin.bts.domain.AppRules

@SuppressLint("MissingPermission")
@Composable
fun LocationAccessEffect(
    authenticated: Boolean,
    address: Address?,
    onLocation: (Address, Double, Double) -> Unit,
) {
    val context = LocalContext.current
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun updateSignedInLocation() {
        if (!authenticated || address == null || !hasPermission()) return
        // A saved delivery pin belongs to that address, not the device's current position.
        if (AppRules.hasValidCoordinates(address.latitude, address.longitude)) return
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        if (location != null) onLocation(address, location.latitude, location.longitude)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) updateSignedInLocation()
    }

    LaunchedEffect(Unit) {
        if (!hasPermission() && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(authenticated, address?.id) {
        if (hasPermission()) updateSignedInLocation()
    }
}
