package base.data.api.chat

import base.socket.SocketDataManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ChatModule {

    @Provides
    @Singleton
    fun provideUserRepository(socketDataManager: SocketDataManager): ChatRepository {
        return ChatRepository(socketDataManager)
    }

    @Provides
    @Singleton
    fun provideChatCacheRepository(
    ): ChatCacheRepository {
        return ChatCacheRepository()
    }
}