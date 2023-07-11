package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Size
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityNewStoryCameraBinding

class NewStoryCameraActivity : AppCompatActivity() {

    private var imageStoryCapture: ImageCapture? = null
    private var cameraStoryCapture: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var openGalleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityNewStoryCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "New Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initGallery()
        binding.let {
            it.btnShutter.setOnClickListener {
                takePhoto()
            }
            it.btnSwitch.setOnClickListener {
                cameraStoryCapture =
                    if (cameraStoryCapture == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA
                startCamera()
            }
            it.btnGallery.setOnClickListener {
                requestGallery()
            }
        }
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 720))
                .build()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageStoryCapture = ImageCapture.Builder().setTargetResolution(Size(480, 720)).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraStoryCapture,
                    preview,
                    imageStoryCapture, imageAnalysis
                )
            } catch (e: Exception) {
                val builder = AlertDialog.Builder(this@NewStoryCameraActivity)
                builder.setTitle(R.string.alert_failed)
                builder.setMessage("${getString(R.string.alert_launch_camera)} : ${e.message}")
                builder.setIcon(R.drawable.baseline_icon_warning_red)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initGallery() {
        openGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImg: Uri = result.data?.data as Uri
                val myFile = Helper.uriToFile(selectedImg, this@NewStoryCameraActivity)
                val intent = Intent(this@NewStoryCameraActivity, NewStoryActivity::class.java)
                intent.putExtra(NewStoryActivity.EXTRA_PHOTO_RESULT, myFile)
                intent.putExtra(
                    NewStoryActivity.EXTRA_CAMERA_MODE,
                    cameraStoryCapture == CameraSelector.DEFAULT_BACK_CAMERA
                )
                intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                startActivity(intent)
                this@NewStoryCameraActivity.finish()
            }
        }
    }

    private fun requestGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_image))
        openGalleryLauncher.launch(chooser)
    }

    private fun takePhoto() {
        val imageCapture = imageStoryCapture ?: return
        val photoFile = Helper.createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    val builder = AlertDialog.Builder(this@NewStoryCameraActivity)
                    builder.setTitle(R.string.alert_failed)
                    builder.setMessage("${getString(R.string.alert_take_picture)} : ${exc.message}")
                    builder.setIcon(R.drawable.baseline_icon_warning_red)
                    builder.setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent(this@NewStoryCameraActivity, NewStoryActivity::class.java)
                    intent.putExtra(NewStoryActivity.EXTRA_PHOTO_RESULT, photoFile)
                    intent.putExtra(
                        NewStoryActivity.EXTRA_CAMERA_MODE,
                        cameraStoryCapture == CameraSelector.DEFAULT_BACK_CAMERA
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                    startActivity(intent)
                    this@NewStoryCameraActivity.finish()
                }
            }
        )
    }
}