package bangkit.robbyyehezkiel.androidintermediate.utils

import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.view.widget.RecentStoryWidget
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object Helper {

    fun askPermissionGranted(context: Context, message: String) {
        val dialog = dialogAlertBuilder(context, message)
        val button = dialog.findViewById<Button>(R.id.button_ok)
        button.setOnClickListener {
            dialog.dismiss()
            permissionSetting(context)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun permissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun permissionSetting(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }


    private var defaultDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())


    private fun currentDate(): Date {
        return Date()
    }

    fun currentDateToString(): String = defaultDate.format(currentDate())

    @SuppressLint("ConstantLocale")
    val currentTimestamp: String = SimpleDateFormat(
        "ddMMyyHHmmssSS",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    private fun dialogAlertBuilder(
        context: Context,
        message: String,
        alignment: Int = Gravity.CENTER
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.fade
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.dialog_alert)
        val tvMessage = dialog.findViewById<TextView>(R.id.message)
        when (alignment) {
            Gravity.CENTER -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER
            Gravity.START -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            Gravity.END -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        tvMessage.text = message
        return dialog
    }

    fun dialogAlertImage(
        context: Context,
        image: Bitmap,
        path: String
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(true)
        dialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.slide_bottom
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.custom_dialog_preview_image)
        val tvPath = dialog.findViewById<TextView>(R.id.image_path)
        tvPath.text = path
        val imageContainer = dialog.findViewById<ImageView>(R.id.image_preview)
        imageContainer.setImageBitmap(image)
        dialog.show()
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(currentTimestamp, ".jpg", storageDir)
    }

    fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)
        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
        return myFile
    }

    private fun getRandomString(len: Int = 20): String {
        val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(len) { alphabet.random() }.joinToString("")
    }

    fun getDefaultFileName(): String {
        return "STORY-${getRandomString()}.jpg"
    }

    fun createFile(
        application: Application,
        folder: String = "story",
        filename: String = getDefaultFileName()
    ): File {
        val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
            File(it, folder).apply { mkdirs() }
        }
        val outputDirectory = if (
            mediaDir != null && mediaDir.exists()
        ) mediaDir else application.filesDir
        return File(outputDirectory, filename)
    }

    fun getImageDownload(path: String): Bitmap? {
        val imgFile = File(path)
        return if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        } else null
    }

    fun bitmapFromURL(context: Context, urlString: String): Bitmap {
        return try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val url = URL(urlString)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.avatar_profile)
        }
    }

    fun resizeBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }

    fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()
        return if (isBackCamera) {
            matrix.postRotate(90f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        } else {
            matrix.postRotate(-90f)
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }
    }

    fun latLonLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): String {
        val geocoder = Geocoder(context)
        val geoLocation =
            geocoder.getFromLocation(lat, lon, 1)
        return if (geoLocation!!.size > 0) {
            val location = geoLocation[0]
            val fullAddress = location.getAddressLine(0)
            StringBuilder("📌 ")
                .append(fullAddress).toString()
        } else {
            "📌 Location Unknown"
        }
    }

    fun updateWidgetData(context: Context) {
        Log.i("TEST_WIDGET", "Requested update data")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids: IntArray = appWidgetManager.getAppWidgetIds(
            ComponentName(context, RecentStoryWidget::class.java)
        )
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
    }

}