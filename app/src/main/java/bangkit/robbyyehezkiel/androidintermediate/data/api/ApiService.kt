package bangkit.robbyyehezkiel.androidintermediate.data.api

import bangkit.robbyyehezkiel.androidintermediate.data.model.LoginResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.RegisterResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.ListStoryResponse
import bangkit.robbyyehezkiel.androidintermediate.data.model.NewStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    fun postLoginActivity(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @POST("register")
    @FormUrlEncoded
    fun postRegisterActivity(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories")
    suspend fun getStoryList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ListStoryResponse

    @GET("stories")
    suspend fun getStoryListWidget(
        @Header("Authorization") token: String,
        @Query("size") size: Int = 10
    ): Response<ListStoryResponse>

    @GET("stories?location=1")
    fun getStoryListLocation(
        @Header("Authorization") token: String,
        @Query("size") size: Int
    ): Call<ListStoryResponse>

    @Multipart
    @POST("stories")
    fun doUploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<NewStoryResponse>

    @Multipart
    @POST("stories")
    fun doUploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody
    ): Call<NewStoryResponse>
}