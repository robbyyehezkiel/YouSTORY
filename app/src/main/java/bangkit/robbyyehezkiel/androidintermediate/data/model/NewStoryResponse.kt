package bangkit.robbyyehezkiel.androidintermediate.data.model

import com.google.gson.annotations.SerializedName

data class NewStoryResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)