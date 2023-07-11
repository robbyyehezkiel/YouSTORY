package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bangkit.robbyyehezkiel.androidintermediate.data.model.ListStoryResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.NewStoryResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.R
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryListViewModel : ViewModel() {
    val loading = MutableLiveData(View.GONE)
    val isSuccessUploadStory = MutableLiveData(false)
    val error = MutableLiveData("")
    val storyList = MutableLiveData<List<Story>>()
    val isError = MutableLiveData(true)
    val isLocationPicked = MutableLiveData(false)
    val coordinateLatitude = MutableLiveData(0.0)
    val coordinateLongitude = MutableLiveData(0.0)
    private val jambiLocation = LatLng(-1.6146, 103.5199)
    val coordinateTemp = MutableLiveData(jambiLocation)

    fun loadStoryLocationData(context: Context, token: String) {
        val client = bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService().getStoryListLocation(token, 100)
        client.enqueue(object : Callback<ListStoryResponse> {
            override fun onResponse(call: Call<ListStoryResponse>, response: Response<ListStoryResponse>) {
                if (response.isSuccessful) {
                    isError.postValue(false)
                    storyList.postValue(response.body()?.listStory)
                } else {
                    isError.postValue(true)
                    error.postValue("ERROR ${response.code()} : ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListStoryResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                isError.postValue(true)
                Log.e(Constanta.TAG_STORY, "onFailure Call: ${t.message}")
                error.postValue("${context.getString(R.string.ErrorDataFetch)} : ${t.message}")
            }
        })
    }

    fun uploadNewStory(
        context: Context,
        token: String,
        image: File,
        description: String,
        withLocation: Boolean = false,
        lat: String? = null,
        lon: String? = null
    ) {
        loading.postValue(View.VISIBLE)
        "${image.length() / 1024 / 1024} MB"
        val storyDescription = description.toRequestBody("text/plain".toMediaType())

        val requestImageFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            image.name,
            requestImageFile
        )
        val client = if (withLocation) {
            val positionLat = lat?.toRequestBody("text/plain".toMediaType())
            val positionLon = lon?.toRequestBody("text/plain".toMediaType())
            bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService()
                .doUploadImage(
                    token,
                    imageMultipart,
                    storyDescription,
                    positionLat!!,
                    positionLon!!
                )
        } else {
            bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService()
                .doUploadImage(token, imageMultipart, storyDescription)
        }
        client.enqueue(object : Callback<NewStoryResponse> {
            override fun onResponse(call: Call<NewStoryResponse>, response: Response<NewStoryResponse>) {
                when (response.code()) {
                    401 -> error.postValue(context.getString(R.string.alert_token_expired))
                    413 -> error.postValue(context.getString(R.string.error_large))
                    201 -> isSuccessUploadStory.postValue(true)
                    else -> error.postValue("Error ${response.code()} : ${response.message()}")
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<NewStoryResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(Constanta.TAG_STORY, "onFailure Call: ${t.message}")
                error.postValue("${context.getString(R.string.error_send)} : ${t.message}")
            }
        })
    }


}