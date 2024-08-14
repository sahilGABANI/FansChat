package base.data.network.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class FansChatError(
    @field:SerializedName("errors")
    val param: List<ErrorMessages>?
)

@Parcelize
data class ErrorMessages(
    @field:SerializedName("param")
    val param: String?,

    @field:SerializedName("message")
    val message: String?,
) : Parcelable

sealed class ErrorResult {
    data class ErrorMessage(
        val errorMessage: String,
        val fansChatError: FansChatError? = null,

        ) : ErrorResult()

    data class ErrorThrowable(val throwable: Throwable) : ErrorResult()
}
