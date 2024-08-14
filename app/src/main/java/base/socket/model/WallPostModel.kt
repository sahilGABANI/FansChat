package base.socket.model

import base.data.network.model.ErrorMessages
import com.google.gson.annotations.SerializedName

data class PostWallRequest(
    @field:SerializedName("type")
    val type: Int = 0,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("subTitle")
    val subTitle: String? = null,

    @field:SerializedName("bodyText")
    var bodyText: String? = null,

    @field:SerializedName("coverImageUrl")
    val coverImageUrl: String? = null,

    @field:SerializedName("imageUrl")
    var imageUrl: String? = null,

    @field:SerializedName("videoUrl")
    var videoUrl: String? = null,

    @field:SerializedName("thumbnailUrl")
    var thumbnailUrl: String? = null,

    @field:SerializedName("language")
    val language: String? = null,

    @field:SerializedName("id")
    val id: String? = null,
)

data class PostWallResponse(
    @field:SerializedName("success")
    val success: Boolean = false,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("subTitle")
    val subTitle: String? = null,

    @field:SerializedName("bodyText")
    val bodyText: String? = null,

    @field:SerializedName("coverImageUrl")
    val coverImageUrl: String? = null,

    @field:SerializedName("imageUrl")
    val imageUrl: String? = null,

    @field:SerializedName("videoUrl")
    val videoUrl: String? = null,

    @field:SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,

    @field:SerializedName("language")
    val language: String? = null,

    @field:SerializedName("translations")
    val translations: ArrayList<Translations>? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("updated")
    val updated: String? = null,

)

data class DeleteWallPostRequest(
    @field:SerializedName("id")
    val id: String? = null
)

data class DeleteWallPostResponse(
    @field:SerializedName("success")
    val success: Boolean = false,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("errors")
    val errors: ArrayList<ErrorMessages>? = null,

    )