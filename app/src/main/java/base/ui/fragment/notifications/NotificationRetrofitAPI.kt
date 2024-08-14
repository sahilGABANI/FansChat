package base.ui.fragment.notifications

import base.data.model.NotificationResponse
import base.data.network.ErrorType
import base.data.network.model.FansChatError
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationRetrofitAPI {
    @GET("notifications")
    @ErrorType(FansChatError::class)
    fun getNotifications(@Query("page") page: Int, @Query("perPage") perPage: Int): Single<NotificationResponse>
}