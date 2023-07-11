package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bangkit.robbyyehezkiel.androidintermediate.data.model.FolderResponse
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import java.io.File

class FolderViewModel : ViewModel() {

    val error = MutableLiveData("")
    val assetImageDownload = MutableLiveData<ArrayList<FolderResponse>>()

    val loadingStory = MutableLiveData(true)
    val loadingDownload = MutableLiveData(true)

    @Suppress("DEPRECATION")
    fun loadImage(context: Context) {
        assetImageDownload.postValue(fetchImageData(context, mode = "download"))
    }

    private fun fetchImageData(context: Context, mode: String = "story"): ArrayList<FolderResponse> {
        loadingState(mode, true)
        val folderData = ArrayList<FolderResponse>()

        @Suppress("DEPRECATION")
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, mode).apply { mkdirs() }
        }

        val files = mediaDir?.listFiles()
        for (i in files!!.indices) {
            val path = "${mediaDir.absolutePath}/${files[i].name}"
            val bitmap =
                Helper.getImageDownload(path)?.let { Helper.resizeBitmap(it, 200, 200) }
            bitmap?.let {
                val folder = FolderResponse(it, path)
                folderData.add(folder)
            }
        }
        folderData.reverse()
        loadingState(mode, false)
        return folderData
    }

    private fun loadingState(mode: String, state: Boolean) {
        when (mode) {
            "download" -> loadingDownload.postValue(state)
        }
    }
}