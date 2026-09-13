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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("MissingPermission")
@Composable
fun AddAddressScreen(
    state: CustomerState,
    onSave: (DeliveryAddressDraft, () -> Unit) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showLocationPrompt by rememberSaveable { mutableStateOf(true) }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var locationError by rememberSaveable { mutableStateOf<String?>(null) }
    var name by remember(state.profile) { mutableStateOf(state.profile?.fullName.orEmpty()) }
    var phone by remember(state.profile) { mutableStateOf(state.profile?.phone.orEmpty()) }
    var label by rememberSaveable { mutableStateOf("Home") }
    var house by rememberSaveable { mutableStateOf("") }
    var building by rememberSaveable { mutableStateOf("") }
    var floor by rememberSaveable { mutableStateOf("") }
    var locality by rememberSaveable { mutableStateOf("") }
    var landmark by rememberSaveable { mutableStateOf("") }
    var stateName by rememberSaveable { mutableStateOf("Uttar Pradesh") }
    var district by rememberSaveable { mutableStateOf("Greater Noida") }
    var pincode by rememberSaveable { mutableStateOf("") }

    fun captureLocation() {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (location == null) locationError = "Current location unavailable. Turn on device location and retry."
        else {
            latitude = location.latitude
            longitude = location.longitude
            locationError = null
            showLocationPrompt = false
        }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) captureLocation() else locationError = "Location permission is required to save this delivery address."
    }
    fun useCurrentLocation() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) captureLocation()
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Save delivery address", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Confirm your GPS pin, then complete the address details.")
        }
        item {
            if (latitude != null && longitude != null) {
                LocationMap(latitude!!, longitude!!)
                Text("Blue pin · ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}", color = MaterialTheme.colorScheme.primary)
            } else {
                OutlinedButton(onClick = ::useCurrentLocation, modifier = Modifier.fillMaxWidth()) { Text("Use current location") }
            }
            locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AddressField("Full name *", name) { name = it }
                AddressField("Contact number *", phone, KeyboardType.Phone) { phone = it.filter(Char::isDigit).take(10) }
                AddressField("Save as (Home/Work)", label) { label = it }
                AddressField("House/Flat number *", house) { house = it }
                AddressField("Building name", building) { building = it }
                AddressField("Floor number", floor) { floor = it }
                AddressField("Locality/Area *", locality) { locality = it }
                AddressField("Landmark", landmark) { landmark = it }
                AddressField("District/City *", district) { district = it }
                AddressField("State *", stateName) { stateName = it }
                AddressField("Pincode *", pincode, KeyboardType.Number) { pincode = it.filter(Char::isDigit).take(6) }
                if (state.loading) CircularProgressIndicator()
                state.message?.let { Text(it, color = if (it == "Address added.") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        onSave(
                            DeliveryAddressDraft(name, phone, label, house, building, floor, locality, landmark, district, stateName, pincode, latitude!!, longitude!!),
                            onSaved,
                        )
                    },
                    enabled = !state.loading && latitude != null && longitude != null && name.isNotBlank() && phone.length == 10 && house.isNotBlank() && locality.isNotBlank() && district.isNotBlank() && stateName.isNotBlank() && pincode.length == 6,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save address") }
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
private fun LocationMap(latitude: Double, longitude: Double) {
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
                },
            )
            map.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
    )
}
