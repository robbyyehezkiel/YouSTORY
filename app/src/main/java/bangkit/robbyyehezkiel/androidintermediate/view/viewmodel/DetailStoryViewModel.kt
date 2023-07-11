package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection


class DetailStoryViewModel : ViewModel() {

    val error = MutableLiveData("")

    val isDownloading = MutableLiveData(false)

    @Suppress("DEPRECATION")
    fun saveImage(context: Context, fileUrl: String) {
        isDownloading.postValue(true)
        try {

            val url = URL(fileUrl)
            val mediaDir = context.externalMediaDirs.firstOrNull().let { File(it, "download") }
            val filename = Helper.getDefaultFileName()
            val myDir = File(mediaDir, filename)
            Log.i("DOWNLOAD", "Requested download image from $fileUrl into $mediaDir")
            myDir.createNewFile()


            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)


            val connection: URLConnection = url.openConnection()
            var inputStream: InputStream? = null
            val httpConn: HttpURLConnection = connection as HttpURLConnection
            httpConn.requestMethod = "GET"
            httpConn.connect()
            if (httpConn.responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.inputStream
            }
            val fos = FileOutputStream(myDir)
            val totalSize: Int = httpConn.contentLength
            var downloadedSize = 0
            val buffer = ByteArray(1024)
            var bufferLength = 0
            while (inputStream?.read(buffer).also {
                    if (it != null) {
                        bufferLength = it
                    }
                }!! > 0) {
                fos.write(buffer, 0, bufferLength)
                downloadedSize += bufferLength
                val progress = (downloadedSize * 100) / totalSize
                Log.i(
                    Constanta.TAG_DOWNLOAD,
                    "downloading ${progress}% : $downloadedSize of $totalSize"
                )
            }
            fos.close()
            error.postValue(context.getString(R.string.image_download))
        } catch (io: IOException) {
            io.printStackTrace()
            error.postValue(io.stackTraceToString())
            Log.e(Constanta.TAG_DOWNLOAD, io.stackTraceToString())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            error.postValue(e.stackTraceToString())
            Log.e(Constanta.TAG_DOWNLOAD, e.stackTraceToString())
        }
        isDownloading.postValue(false)
    }
}