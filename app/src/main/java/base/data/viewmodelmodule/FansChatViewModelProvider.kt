package base.data.viewmodelmodule

import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.ProfileCacheDataRepository
import base.data.api.chat.ChatCacheRepository
import base.data.api.chat.ChatRepository
import base.data.api.friendrequest.FriendRequestRepository
import base.data.api.friends.FriendsRepository
import base.data.api.news.NewsCacheRepository
import base.data.api.news.NewsPostRepository
import base.data.api.users.UserRepository
import base.data.api.wall.WallCacheRepository
import base.data.api.wall.WallPostsRepository
import base.ui.fragment.details.viewmodel.DetailViewModel
import base.ui.fragment.dialog.viewmodel.PinDialogViewModel
import base.ui.fragment.login.viewmodel.LoginViewModel
import base.ui.fragment.login.viewmodel.RegisterViewModel
import base.ui.fragment.login.viewmodel.RestorePasswordViewModel
import base.ui.fragment.notifications.NotificationCacheRepository
import base.ui.fragment.notifications.NotificationRepository
import base.ui.fragment.notifications.viewmodel.NotificationViewModel
import base.ui.fragment.other.chat.viewmodel.ChatViewModel
import base.ui.fragment.other.friends.my.viewmodel.MyFriendsViewModel
import base.ui.fragment.other.friends.requests.viewmodel.FriendRequestViewModel
import base.ui.fragment.other.friends.search.viewmodel.SearchFriendViewModel
import base.ui.fragment.other.news.NewsViewModel
import base.ui.fragment.other.profile.viewmodel.EditProfileViewModel
import base.ui.fragment.wall.viewmodel.WallViewModel
import dagger.Module
import dagger.Provides

@Module
class FansChatViewModelProvider {

    @Provides
    fun provideLoginViewModel(
        authenticationRepository: AuthenticationRepository,
    ): LoginViewModel {
        return LoginViewModel(authenticationRepository)
    }

    @Provides
    fun provideRegisterViewModel(
        authenticationRepository: AuthenticationRepository,
    ): RegisterViewModel {
        return RegisterViewModel(authenticationRepository)
    }

    @Provides
    fun provideEditProfileViewModel(
        authenticationRepository: AuthenticationRepository,
        profileCacheDataRepository: ProfileCacheDataRepository,
    ): EditProfileViewModel {
        return EditProfileViewModel(authenticationRepository, profileCacheDataRepository)
    }

    @Provides
    fun provideSearchFriendViewModel(
        userRepository: UserRepository,
    ): SearchFriendViewModel {
        return SearchFriendViewModel(userRepository)
    }

    @Provides
    fun provideGetFriendRequestViewModel(friendRequestRepository: FriendRequestRepository,
        userRepository: UserRepository): FriendRequestViewModel {
        return FriendRequestViewModel(friendRequestRepository, userRepository)
    }

    @Provides
    fun provideChatViewModelViewModel(chatRepository: ChatRepository,
        friendsRepository: FriendsRepository,
        chatCacheRepository: ChatCacheRepository): ChatViewModel {
        return ChatViewModel(chatRepository, friendsRepository, chatCacheRepository)
    }

    @Provides
    fun provideWallPostViewModelViewModel(
        wallPostRepository: WallPostsRepository,
        wallCacheRepository: WallCacheRepository,
        friendRequestRepository: FriendRequestRepository
    ): WallViewModel {
        return WallViewModel(wallPostRepository, wallCacheRepository, friendRequestRepository)
    }

    @Provides
    fun provideDetailPostViewModelViewModel(wallPostRepository: WallPostsRepository): DetailViewModel {
        return DetailViewModel(wallPostRepository)
    }

    @Provides
    fun provideNewsPostViewModelViewModel(newsPostRepository: NewsPostRepository,
        newsCacheRepository: NewsCacheRepository): NewsViewModel {
        return NewsViewModel(newsPostRepository, newsCacheRepository)
    }


    @Provides
    fun providePinDialogViewModel(wallPostRepository: WallPostsRepository): PinDialogViewModel {
        return PinDialogViewModel(wallPostRepository)
    }

    @Provides
    fun provideMyFriendsViewModel(friendsRepository: FriendsRepository): MyFriendsViewModel {
        return MyFriendsViewModel(friendsRepository)
    }

    @Provides
    fun provideRestorePasswordViewModel(authenticationRepository: AuthenticationRepository): RestorePasswordViewModel {
        return RestorePasswordViewModel(authenticationRepository)
    }

    @Provides
    fun provideNotificationsViewModel(notificationRepository: NotificationRepository,
        notificationCacheRepository: NotificationCacheRepository): NotificationViewModel {
        return NotificationViewModel(notificationCacheRepository, notificationRepository)
    }

}