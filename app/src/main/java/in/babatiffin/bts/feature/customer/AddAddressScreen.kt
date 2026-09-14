package com.babatiffin.bts.feature.customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.babatiffin.bts.R
import com.babatiffin.bts.data.customer.DeliveryAddressDraft
import com.babatiffin.bts.data.customer.Address
import com.babatiffin.bts.domain.AppRules
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("MissingPermission")
@Composable
fun AddAddressScreen(
    state: CustomerState,
    initialAddress: Address? = null,
    onSave: (DeliveryAddressDraft, () -> Unit) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var locating by remember { mutableStateOf(false) }
    var pinConfirmed by rememberSaveable { mutableStateOf(false) }
    var showLocationPrompt by rememberSaveable { mutableStateOf(initialAddress == null) }
    var latitude by rememberSaveable { mutableStateOf(initialAddress?.latitude) }
    var longitude by rememberSaveable { mutableStateOf(initialAddress?.longitude) }
    var locationError by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf(state.profile?.fullName.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(state.profile?.phone.orEmpty().removePrefix("+91")) }
    var label by rememberSaveable { mutableStateOf(initialAddress?.label ?: "Home") }
    var house by rememberSaveable { mutableStateOf(initialAddress?.line1.orEmpty()) }
    var building by rememberSaveable { mutableStateOf("") }
    var floor by rememberSaveable { mutableStateOf("") }
    var locality by rememberSaveable { mutableStateOf(initialAddress?.line2.orEmpty()) }
    var landmark by rememberSaveable { mutableStateOf(initialAddress?.landmark.orEmpty()) }
    var stateName by rememberSaveable { mutableStateOf(initialAddress?.state ?: "Uttar Pradesh") }
    var district by rememberSaveable { mutableStateOf(initialAddress?.city ?: "Greater Noida") }
    var pincode by rememberSaveable { mutableStateOf(initialAddress?.pincode.orEmpty()) }

    fun captureLocation() {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locating) return
        showLocationPrompt = false
        locating = true
        locationError = null
        scope.launch {
            try {
                val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                    .firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
                val location = if (provider == null) null else withTimeoutOrNull(15_000L) {
                    suspendCancellableCoroutine<android.location.Location?> { continuation ->
                        val cancellation = CancellationSignal()
                        continuation.invokeOnCancellation { cancellation.cancel() }
                        LocationManagerCompat.getCurrentLocation(manager, provider, cancellation,
                            ContextCompat.getMainExecutor(context)) { result ->
                            if (continuation.isActive) continuation.resume(result)
                        }
                    }
                }
                if (location == null) locationError = "Could not get a current fix. Turn on device location and retry."
                else {
                    latitude = location.latitude
                    longitude = location.longitude
                    pinConfirmed = false
                }
            } catch (_: SecurityException) {
                locationError = "Location permission is required. Allow it and retry."
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (_: Exception) {
                locationError = "Location is unavailable. Check device location settings and retry."
            } finally { locating = false }
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        showLocationPrompt = false
        if (result.values.any { it }) captureLocation() else locationError = "Location permission denied. You can enable it in app settings and retry."
    }
    fun useCurrentLocation() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) captureLocation()
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(if (initialAddress == null) "Save delivery address" else "Edit delivery address", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Confirm your GPS pin, then complete the address details.")
        }
        item {
            if (AppRules.hasValidCoordinates(latitude, longitude)) {
                LocationMap(latitude!!, longitude!!) { lat, lon -> latitude = lat; longitude = lon; pinConfirmed = false }
                Text("Blue pin · ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}", color = MaterialTheme.colorScheme.primary)
                Text("Drag the pin to your delivery entrance, then confirm it.")
                Button(onClick = { pinConfirmed = true }, enabled = !locating && !pinConfirmed) { Text(if (pinConfirmed) "Pin confirmed" else "Confirm this pin") }
            }
            OutlinedButton(onClick = ::useCurrentLocation, enabled = !locating && !state.addressSaving, modifier = Modifier.fillMaxWidth()) { Text(if (locating) "Finding current location…" else "Use current location") }
            if (locating) CircularProgressIndicator()
            locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AddressField("Full name *", name) { name = it }
                AddressField("Contact number *", phone, KeyboardType.Phone) { phone = it.filter(Char::isDigit).take(10) }
                AddressField("Save as (Home/Work)", label) { label = it }
                AddressField(if (initialAddress == null) "House/Flat number *" else "House/Flat and building *", house) { house = it }
                AddressField("Building name", building) { building = it }
                AddressField("Floor number", floor) { floor = it }
                AddressField("Locality/Area *", locality) { locality = it }
                AddressField("Landmark", landmark) { landmark = it }
                AddressField("District/City *", district) { district = it }
                AddressField("State *", stateName) { stateName = it }
                AddressField("Pincode *", pincode, KeyboardType.Number) { pincode = it.filter(Char::isDigit).take(6) }
                if (state.addressSaving) CircularProgressIndicator()
                state.message?.let { Text(it, color = if (it == "Address added.") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        onSave(
                            DeliveryAddressDraft(name, phone, label, house, building, floor, locality, landmark, district, stateName, pincode, latitude!!, longitude!!),
                            onSaved,
                        )
                    },
                    enabled = !state.addressSaving && !locating && pinConfirmed && AppRules.hasValidCoordinates(latitude, longitude),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.addressSaving) "Saving…" else "Save address") }
            }
        }
    }

    if (showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { showLocationPrompt = false },
            title = { Text("Set delivery location") },
            text = { Text("Use your current GPS location to place the delivery pin accurately.") },
            confirmButton = { Button(onClick = ::useCurrentLocation) { Text("Use current location") } },
            dismissButton = { OutlinedButton(onClick = { showLocationPrompt = false }) { Text("Not now") } },
        )
    }
}

@Composable
private fun AddressField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LocationMap(latitude: Double, longitude: Double, onPinChanged: (Double, Double) -> Unit) {
    val context = LocalContext.current
    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(17.0)
        }
    }
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
    AndroidView(
        factory = { mapView },
        update = { map ->
            val point = GeoPoint(latitude, longitude)
            map.controller.setCenter(point)
            map.overlays.clear()
            map.overlays.add(
                Marker(map).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin_blue)
                    title = "Current delivery location"
                    isDraggable = true
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker) = Unit
                        override fun onMarkerDragStart(marker: Marker) = Unit
                        override fun onMarkerDragEnd(marker: Marker) { onPinChanged(marker.position.latitude, marker.position.longitude) }
                    })
                },
            )
            map.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
    )
}
