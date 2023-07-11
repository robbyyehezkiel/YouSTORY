package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.DetailStoryViewModel
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private val viewModel: DetailStoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Detail Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.storyName.text =
            intent.getData(Constanta.DetailStory.UserName.name, "Name")
        Glide.with(binding.root)
            .load(intent.getData(Constanta.DetailStory.ImageURL.name, ""))
            .centerCrop()
            .dontAnimate()
            .into(binding.storyImage)
        binding.storyDescription.text =
            intent.getData(Constanta.DetailStory.ContentDescription.name, "Caption")
        binding.btnDownload.setOnClickListener {
            if (Helper.permissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val url = intent.getData(Constanta.DetailStory.ImageURL.name, "")
                viewModel.saveImage(this, url)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constanta.STORAGE_PERMISSION_CODE
                )
            }
        }

        viewModel.let { vm ->
            vm.isDownloading.observe(this) { isDownloading ->
                if (isDownloading == true) {
                    binding.progressBar.isVisible = true
                    binding.btnDownload.isVisible = false
                } else {
                    binding.progressBar.isVisible = false
                    binding.btnDownload.isVisible = true
                }

            }
            vm.error.observe(this) {
                if (it.isNotEmpty()) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.alert_success)
                    builder.setMessage(it)
                    builder.setIcon(R.drawable.baseline_icon_warning_green)
                    builder.setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        }
        try {
            val lat = intent.getData(Constanta.DetailStory.Latitude.name)
            val lon = intent.getData(Constanta.DetailStory.Longitude.name)
            binding.locationStory.text =
                Helper.latLonLocation(this, lat.toDouble(), lon.toDouble())
            binding.locationStory.isVisible = true
        } catch (e: Exception) {
            binding.locationStory.isVisible = false
        }
    }

    private fun Intent.getData(key: String, defaultValue: String = "None"): String {
        return getStringExtra(key) ?: defaultValue
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constanta.STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.askPermissionGranted(
                        this,
                        "Give this application permission to access your storage"
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }


}