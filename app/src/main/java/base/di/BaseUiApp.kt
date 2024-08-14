package base.di

import android.app.Application
import base.fcm.NotificationsService
import base.socket.ClearService
import base.ui.MainActivity
import base.ui.fragment.PostCreateFragment
import base.ui.fragment.details.DetailFragment
import base.ui.fragment.dialog.PinDialog
import base.ui.fragment.login.LoginFragment
import base.ui.fragment.login.RegisterFragment
import base.ui.fragment.login.RestoreFragment
import base.ui.fragment.main.ChatFragment
import base.ui.fragment.main.NewsFragment
import base.ui.fragment.wall.NewsWallPagerFragment
import base.ui.fragment.notifications.NotificationFragment
import base.ui.fragment.other.chat.ChatCreateFragment
import base.ui.fragment.other.chat.ChatJoinFragment
import base.ui.fragment.other.friends.FriendsContainerFragment
import base.ui.fragment.other.friends.my.FriendDetailFragment
import base.ui.fragment.other.friends.my.MyFriendsFragment
import base.ui.fragment.other.friends.requests.FriendRequestsFragment
import base.ui.fragment.other.friends.search.FriendsSearchFragment
import base.ui.fragment.other.profile.ProfileEditFragment
import base.ui.fragment.other.profile.ProfileFragment
import base.ui.fragment.menu.ClubTvFragment
import base.ui.fragment.wall.WallFragment
import base.util.WebFragment

/**
 *
 * This base app class will be extended by either Main or Demo project.
 *
 * It then will provide library project app component accordingly.
 *
 */
abstract class BaseUiApp : Application() {
    abstract fun getAppComponent(): BaseAppComponent
    abstract fun setAppComponent(baseAppComponent: BaseAppComponent)
}

/**
 * Base app component
 *
 * This class should have all the inject targets classes
 *
 */
interface BaseAppComponent {
    fun inject(app: Application)
    fun inject(loginFragment: LoginFragment)
    fun inject(registerFragment: RegisterFragment)
    fun inject(profileEditFragment: ProfileEditFragment)
    fun inject(friendsSearchFragment: FriendsSearchFragment)
    fun inject(myFriendsFragment: MyFriendsFragment)
    fun inject(friendDetailFragment: FriendDetailFragment)
    fun inject(friendRequestsFragment: FriendRequestsFragment)
    fun inject(postCreateFragment: PostCreateFragment)
    fun inject(detailFragment: DetailFragment)
    fun inject(chatJoinFragment: ChatJoinFragment)
    fun inject(chatCreateFragment: ChatCreateFragment)
    fun inject(chatFragment: ChatFragment)
    fun inject(friendsContainerFragment: FriendsContainerFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(clubTvFragment: ClubTvFragment)
    fun inject(wallFragment: WallFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(newsFragment: NewsFragment)
    fun inject(newsWallPagerFragment: NewsWallPagerFragment)
    fun inject(pinDialog: PinDialog)
    fun inject(restoreFragment: RestoreFragment)
    fun inject(webFragment: WebFragment)
    fun inject(notificationFragment: NotificationFragment)
    fun inject(notificationsService: NotificationsService)
    fun inject(clearService: ClearService)
}

/**
 * Extension for getting component more easily
 */
fun BaseUiApp.getComponent(): BaseAppComponent {
    return this.getAppComponent()
}
