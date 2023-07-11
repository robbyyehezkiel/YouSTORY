package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import bangkit.robbyyehezkiel.androidintermediate.utils.dataStore
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.SettingViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.StoryListViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelSettingFactory
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityNewStoryBinding
import com.google.android.gms.maps.model.LatLng
import java.io.File

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private var userToken: String? = null
    var location: LatLng? = null
    private val viewModel: StoryListViewModel by viewModels()
    private var isPicked: Boolean? = false
    private var getResult: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "New Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { res ->
                    isPicked = res.getBooleanExtra(Constanta.LocationPicker.IsPicked.name, false)
                    viewModel.isLocationPicked.postValue(isPicked)
                    val lat = res.getDoubleExtra(
                        Constanta.LocationPicker.Latitude.name,
                        0.0
                    )
                    val lon = res.getDoubleExtra(
                        Constanta.LocationPicker.Longitude.name,
                        0.0
                    )
                    binding.fieldLocation.text = Helper.latLonLocation(this, lat, lon)
                    viewModel.coordinateLatitude.postValue(lat)
                    viewModel.coordinateLongitude.postValue(lon)
                }
            }
        }
        val pref = UserPreferences.getPreferenceInstance(dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]
        settingViewModel.getUserPreferences(Constanta.AuthPreferences.UserToken.name)
            .observe(this) { token ->
                userToken = StringBuilder("Bearer ").append(token).toString()
            }

        val myFile = intent?.getSerializableExtra(EXTRA_PHOTO_RESULT) as File
        val isBackCamera = intent?.getBooleanExtra(EXTRA_CAMERA_MODE, true) as Boolean
        val rotatedBitmap = Helper.rotateBitmap(
            BitmapFactory.decodeFile(myFile.path),
            isBackCamera
        )
        binding.storyImage.setImageBitmap(rotatedBitmap)
        binding.btnUpload.setOnClickListener {
            if (binding.storyDescription.text.isNotEmpty()) {
                uploadImage(myFile, binding.storyDescription.text.toString())
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.alert_warning)
                builder.setMessage(R.string.alert_description_not_field)
                builder.setIcon(R.drawable.baseline_icon_warning_yellow)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
        binding.btnSelectLocation.setOnClickListener {
            if (Helper.permissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val intentPickLocation = Intent(this, NewStoryPickLocation::class.java)
                getResult?.launch(intentPickLocation)
            } else {
                ActivityCompat.requestPermissions(
                    this@NewStoryActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Constanta.LOCATION_PERMISSION_CODE
                )
            }
        }
        binding.btnClearLocation.setOnClickListener {
            viewModel.isLocationPicked.postValue(false)
        }
        viewModel.let { vm ->
            vm.isSuccessUploadStory.observe(this) {
                if (it) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.alert_success)
                    builder.setMessage(R.string.alert_success_upload)
                    builder.setIcon(R.drawable.baseline_icon_warning_green)
                    builder.setPositiveButton("Ok") { _, _ ->
                        setResult(RESULT_OK)
                        finish()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
            vm.loading.observe(this) {
                binding.loading.visibility = it
            }
            vm.error.observe(this) {
                if (it.isNotEmpty()) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.alert_warning)
                    builder.setMessage(it)
                    builder.setIcon(R.drawable.baseline_icon_warning_yellow)
                    builder.setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
            vm.isLocationPicked.observe(this) {
                binding.locationSection.isVisible = it
                binding.btnSelectLocation.isVisible = !it
            }
        }
    }

    private fun uploadImage(image: File, description: String) {
        if (userToken != null) {
            if (viewModel.isLocationPicked.value != true) {
                viewModel.uploadNewStory(
                    this,
                    userToken!!,
                    image,
                    description
                )
            } else {
                viewModel.uploadNewStory(
                    this,
                    userToken!!,
                    image,
                    description,
                    true,
                    viewModel.coordinateLatitude.value.toString(),
                    viewModel.coordinateLongitude.value.toString(),
                )
            }
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.alert_warning)
            builder.setMessage(R.string.alert_token_expired)
            builder.setIcon(R.drawable.baseline_icon_warning_yellow)
            builder.setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constanta.LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
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
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        const val EXTRA_PHOTO_RESULT = "PHOTO_RESULT"
        const val EXTRA_CAMERA_MODE = "CAMERA_MODE"
    }
}