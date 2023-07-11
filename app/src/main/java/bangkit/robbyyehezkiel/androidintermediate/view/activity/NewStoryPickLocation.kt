package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.StoryListViewModel
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityNewStoryPickLocationBinding
import bangkit.robbyyehezkiel.androidintermediate.databinding.ItemSelectLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class NewStoryPickLocation : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationMap: GoogleMap
    private lateinit var binding: ActivityNewStoryPickLocationBinding
    private val viewModel: StoryListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.title = "New Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnCancel.setOnClickListener {
            viewModel.isLocationPicked.postValue(false)
            finish()
        }
        binding.btnSelectLocation.setOnClickListener {
            if (viewModel.isLocationPicked.value == true) {
                val intent = Intent()
                intent.putExtra(
                    Constanta.LocationPicker.IsPicked.name,
                    viewModel.isLocationPicked.value
                )
                intent.putExtra(
                    Constanta.LocationPicker.Latitude.name,
                    viewModel.coordinateLatitude.value
                )
                intent.putExtra(
                    Constanta.LocationPicker.Longitude.name,
                    viewModel.coordinateLongitude.value
                )
                setResult(RESULT_OK, intent)
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.alert_warning)
                builder.setMessage(R.string.alert_maps)
                builder.setIcon(R.drawable.baseline_icon_warning_yellow)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        locationMap = googleMap
        locationMap.uiSettings.isZoomControlsEnabled = true
        locationMap.uiSettings.isIndoorLevelPickerEnabled = true
        locationMap.uiSettings.isCompassEnabled = true
        locationMap.uiSettings.isMapToolbarEnabled = true
        locationMap.uiSettings.isMyLocationButtonEnabled = true
        locationMap.uiSettings.isTiltGesturesEnabled = true
        locationMap.setInfoWindowAdapter(this)

        locationMap.setOnInfoWindowClickListener { marker ->
            postLocationSelected(marker.position.latitude, marker.position.longitude)
            marker.hideInfoWindow()
        }

        val jambiLocation = LatLng(-1.6146, 103.5199)
        locationMap.addMarker(
            MarkerOptions()
                .position(jambiLocation)
                .title("Fakultas Sains dan Teknologi")
        )
        locationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jambiLocation, 15f))

        locationMap.setOnMapClickListener {
            locationMap.clear()
            locationMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
            )?.showInfoWindow()
        }

        locationMap.setOnPoiClickListener {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            locationMap.clear()
            locationMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latLng.latitude,
                            it.latLng.longitude
                        )
                    )
            )?.showInfoWindow()
        }

        setMapStyle()
        getMyLastLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                locationMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.satellite_type -> {
                locationMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_type -> {
                locationMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            R.id.hybrid_type -> {
                locationMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun postLocationSelected(lat: Double, lon: Double) {
        val address =
            Helper.latLonLocation(
                this,
                lat,
                lon
            )
        binding.addressBar.text = address
        viewModel.isLocationPicked.postValue(true)
        viewModel.coordinateLatitude.postValue(lat)
        viewModel.coordinateLongitude.postValue(lon)
    }

    private fun setMapStyle() {
        try {
            val success =
                locationMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.styles_google_map
                    )
                )
            if (!success) {
                Log.e("MAPS", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("MAPS", "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }

                else -> {
                    Toast.makeText(this, "No Location Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            locationMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val bindingTooltips =
            ItemSelectLocationBinding.inflate(LayoutInflater.from(this))
        bindingTooltips.location.text = Helper.latLonLocation(
            this,
            marker.position.latitude, marker.position.longitude
        )
        return bindingTooltips.root
    }
}