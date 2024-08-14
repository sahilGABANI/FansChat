package base.ui.fragment.notifications

import base.data.cache.Cache
import base.data.model.GetNotificationRequest
import base.data.model.NotificationResponse
import base.data.model.NotificationsData
import io.reactivex.Flowable
import io.reactivex.Single

class NotificationRepository(private val notificationRetrofitAPI: NotificationRetrofitAPI) {
    fun getNotifications(getNotificationRequest: GetNotificationRequest): Single<NotificationResponse> {
        return notificationRetrofitAPI.getNotifications(getNotificationRequest.page!!, getNotificationRequest.perPage!!)
            .map { it }
    }
}

class NotificationCacheRepository {
    fun getNotifications(): Flowable<List<NotificationsData>> {
        return Cache.cache.daoNotifications().getNotifications().map { it }
    }

    fun addNotifications(list: List<NotificationsData>) {
        Cache.cache.daoNotifications().insert(list)
    }

    fun deleteAll() {
        Cache.cache.daoNotifications().deleteAll()
    }

}
