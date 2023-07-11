package bangkit.robbyyehezkiel.androidintermediate.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("token")
    val token: String
)