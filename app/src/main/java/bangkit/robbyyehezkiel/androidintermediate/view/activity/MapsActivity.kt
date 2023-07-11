package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityMapsBinding
import bangkit.robbyyehezkiel.androidintermediate.databinding.ItemStoryMapBinding
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import bangkit.robbyyehezkiel.androidintermediate.utils.dataStore
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.SettingViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.StoryListViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelSettingFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter,
    AdapterView.OnItemSelectedListener {

    private lateinit var locationMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val storyViewModel: StoryListViewModel by viewModels()
    private val pref = UserPreferences.getPreferenceInstance(dataStore)
    private val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
    private var token = ""
    private val jambiLocation = LatLng(-1.6146, 103.5199)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.title = "Map Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        settingViewModel.getUserPreferences(Constanta.AuthPreferences.UserToken.name)
            .observe(this) {
                token = "Bearer $it"
            }
        storyViewModel.loadStoryLocationData(
            this,
            token
        )

        locationMap = googleMap
        locationMap.uiSettings.isZoomControlsEnabled = true
        locationMap.uiSettings.isIndoorLevelPickerEnabled = true
        locationMap.uiSettings.isCompassEnabled = true
        locationMap.uiSettings.isMapToolbarEnabled = true

        locationMap.setInfoWindowAdapter(this)

        locationMap.setOnInfoWindowClickListener { marker ->
            val data: Story = marker.tag as Story
            switchToStory(data)
        }


        locationMap.addMarker(
            MarkerOptions()
                .position(jambiLocation)
                .title("Fakultas Sains dan Teknologi")
        )
        locationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jambiLocation, 15f))

        locationMap.setOnMapLongClickListener { latLng ->
            locationMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Marker")
                    .snippet("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
                    .icon(vectorToBitmap(R.drawable.baseline_icon_pin, Color.parseColor("#3DDC84")))
            )
        }

        storyViewModel.storyList.observe(this) { storyList ->
            for (story in storyList) {
                locationMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            story.lat?.toDouble() ?: 0.0,
                            story.lon?.toDouble() ?: 0.0
                        )
                    )
                )?.tag = story
            }
        }

        locationMap.setOnPoiClickListener {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
        }

        getMyLocation()
        setMapStyle()

        storyViewModel.coordinateTemp.observe(this) {
            CameraUpdateFactory.newLatLngZoom(jambiLocation, 5f)
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.alert_warning)
                builder.setMessage(getString(R.string.permission_location))
                builder.setIcon(R.drawable.baseline_icon_warning_yellow)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    storyViewModel.coordinateTemp.postValue(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                } else {
                    storyViewModel.coordinateTemp.postValue(jambiLocation)
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                locationMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.styles_google_map))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun switchToStory(data: Story) {
        val intent = Intent(this, DetailStoryActivity::class.java)
        intent.putExtra(Constanta.DetailStory.UserName.name, data.name)
        intent.putExtra(Constanta.DetailStory.ImageURL.name, data.photoUrl)
        intent.putExtra(
            Constanta.DetailStory.ContentDescription.name,
            data.description
        )
        intent.putExtra(Constanta.DetailStory.Latitude.name, data.lat.toString())
        intent.putExtra(Constanta.DetailStory.Longitude.name, data.lon.toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }

    override fun getInfoWindow(marker: Marker): View {
        val bindingTooltips =
            ItemStoryMapBinding.inflate(LayoutInflater.from(this))
        val data: Story = marker.tag as Story
        bindingTooltips.name.text = StringBuilder("Story by ").append(data.name)
        bindingTooltips.image.setImageBitmap(Helper.bitmapFromURL(this, data.photoUrl))
        return bindingTooltips.root
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val level: Float = when (position) {
            0 -> 4f
            1 -> 8f
            2 -> 11f
            3 -> 14f
            4 -> 17f
            else -> 4f
        }
        locationMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(storyViewModel.coordinateTemp.value!!, level)
        )
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        locationMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(jambiLocation, 4f)
        )
    }

    companion object {
        private const val TAG = "MapsActivity"
    }

}