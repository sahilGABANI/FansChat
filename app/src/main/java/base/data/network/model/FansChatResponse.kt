package base.data.network.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FansChatResponse<T>(
    @field:SerializedName("success")
    val success: Int = 0,

    @field:SerializedName("count")
    val count: Int = 0,

    @field:SerializedName("data")
    val data: T? = null,
)

@Keep
data class FansChatSocketResponse(
    @field:SerializedName("success")
    val success: Boolean = false,
)
