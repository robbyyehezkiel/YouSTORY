package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bangkit.robbyyehezkiel.androidintermediate.data.model.LoginResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.RegisterResponse
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthenticateViewModel : ViewModel() {

    var progressBar = MutableLiveData(View.GONE)
    val error = MutableLiveData("")
    val preferencesEmail = MutableLiveData("")
    val loginResponseResult = MutableLiveData<LoginResponse>()
    val registerResponseResult = MutableLiveData<RegisterResponse>()

    fun authLogin(email: String, password: String) {
        preferencesEmail.postValue(email)
        progressBar.postValue(View.VISIBLE)
        val client = bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService().postLoginActivity(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    loginResponseResult.postValue(response.body())
                } else {
                    response.errorBody()?.let {
                        val errorResponse = JSONObject(it.string())
                        val errorMessages = errorResponse.getString("message")
                        error.postValue("LOGIN ERROR : $errorMessages")
                    }
                }
                progressBar.postValue(View.GONE)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.postValue(View.GONE)
                Log.e(Constanta.TAG_AUTH, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }
        })
    }

    fun authRegister(name: String, email: String, password: String) {
        progressBar.postValue(View.VISIBLE)
        val client = bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService().postRegisterActivity(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    registerResponseResult.postValue(response.body())
                } else {
                    response.errorBody()?.let {
                        val errorResponse = JSONObject(it.string())
                        val errorMessages = errorResponse.getString("message")
                        error.postValue("REGISTER ERROR : $errorMessages")
                    }
                }
                progressBar.postValue(View.GONE)
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                progressBar.postValue(View.GONE)
                Log.e(Constanta.TAG_AUTH, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }
        })
    }
}