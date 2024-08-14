package base.data.api.notifications

import base.ui.fragment.notifications.NotificationCacheRepository
import base.ui.fragment.notifications.NotificationRepository
import base.ui.fragment.notifications.NotificationRetrofitAPI
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class NotificationsModule {
    @Provides
    @Singleton
    fun provideNotificationRetrofitAPI(retrofit: Retrofit): NotificationRetrofitAPI {
        return retrofit.create(NotificationRetrofitAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationCacheRepository(): NotificationCacheRepository {
        return NotificationCacheRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationRetrofitAPI: NotificationRetrofitAPI): NotificationRepository {
        return NotificationRepository(notificationRetrofitAPI)
    }
}