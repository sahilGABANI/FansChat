package base.ui

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import base.BaseActivity
import base.R
import base.application.FansChat
import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.LoggedInUserCache
import base.data.api.authentication.model.FirebaseResponse
import base.data.api.news.NewsCacheRepository
import base.data.api.wall.WallCacheRepository
import base.data.model.CommonFeedItem
import base.data.model.wall.ContentItem
import base.data.model.wall.TickerData
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.ActivityMainBinding
import base.extension.getViewModelFromFactory
import base.extension.subscribeAndObserveOnMainThread
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.fcm.TAG
import base.socket.ClearService
import base.socket.SocketDataManager
import base.socket.SocketService
import base.socket.model.AddNewPostNotification
import base.socket.model.InAppNotificationType
import base.ui.adapter.sidemenu.SideMenuAdapter
import base.ui.fragment.PostCreateFragment
import base.ui.fragment.details.DetailFragment
import base.ui.fragment.main.ChatFragment
import base.ui.fragment.main.NewsFragment
import base.ui.fragment.other.profile.viewmodel.EditProfileViewModel
import base.ui.fragment.wall.WallFragment
import base.util.*
import base.util.Analytics.trackHomeScreenDisplayed
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt


private const val KEYBOARD_MIN_HEIGHT_RATIO = 0.15

class MainActivity : BaseActivity() {
    companion object {
        var height: Int = 0
    }

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var wallCacheRepository: WallCacheRepository

    @Inject
    lateinit var newsCacheRepository: NewsCacheRepository

    @Inject
    lateinit var authenticationRepository: AuthenticationRepository

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<EditProfileViewModel>
    private lateinit var profileViewModel: EditProfileViewModel

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    lateinit var builder: NotificationCompat.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"
    private var DELAY_CAROUSAL = 3000L
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var mAudioManager: AudioManager? = null
    private val mAudioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Timber.tag("focusChange").e("Pause")
                //Pause players by observer
                Constants.liveDataAudioFocusObserver.onNext(true)
            }
        }
    }
    private lateinit var sideMenuAdapter: SideMenuAdapter

    private val navController
        get() = Navigation.findNavController(this, R.id.fragmentsContainer)

    private val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        enableSideMenu(destination.id != R.id.loginFragment && destination.id != R.id.registerFragment && destination.id != R.id.authDecideFragment)
        if (::sideMenuAdapter.isInitialized) {
            sideMenuAdapter.currentDest = destination.id
            sideMenuAdapter.notifyDataSetChanged()
        }
    }

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var wasOpened = false
        override fun onGlobalLayout() {
            val isOpen = isKeyboardVisible(this@MainActivity)
            if (isOpen == wasOpened) {
                // keyboard state has not changed
                return
            }
            wasOpened = isOpen
            onVisibilityChanged(isOpen)
        }
    }

    fun showBadge(toShow: Boolean) {
        try {
            binding.toolbarMain.friendsBadge.isVisible = toShow
        } catch (e: Exception) {
        }
    }

    private fun enableSideMenu(enable: Boolean) {
        binding.drawerLayout.setDrawerLockMode(if (enable) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        if (!enable && binding.drawerLayout.isDrawerOpen(GravityCompat.END)) binding.drawerLayout.closeDrawers()
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.linRoot.post { height = binding.linRoot.height }
        FansChat.component.inject(this)
        profileViewModel = getViewModelFromFactory(viewModelFactory)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        trackHomeScreenDisplayed()
        connectToolbar()
        connectSideMenu()
        connectBottomBar()
        registerKeyboardListener()
        navController.addOnDestinationChangedListener(listener)
        onNewIntent(intent)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listenToSocketEvents()
        listenToViewModel()
        loggedInUserCache.friendRequestScreenOpen.subscribeOnIoAndObserveOnMainThread({
            //showBadge(false)
        }, {
            Timber.e(it)
        }).autoDispose()
        startService(Intent(baseContext, ClearService::class.java))
    }
/*
    override fun onStart() {
        super.onStart()
        //It will be only connect if user is login otherwise not
        socketDataManager.connect()
    }

    override fun onStop() {
        super.onStop()
        socketDataManager.disconnect()
    }*/

    private fun listenToSocketEvents() {
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_WALL_POST).handleNewPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_NEWS).handleNewPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_SOCIAL).handleNewPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_RUMOR).handleNewPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_VIDEO).handleNewPost(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_WALL_POST).handleDeletedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_NEWS).handleDeletedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_SOCIAL).handleDeletedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_RUMOR).handleDeletedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_VIDEO).handleDeletedPost(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_WALL_POST).handleUpdatedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_NEWS).handleUpdatedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_SOCIAL).handleUpdatedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_RUMOR).handleUpdatedPost(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_VIDEO).handleUpdatedPost(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_POST_COMMENT).handleNewComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_NEWS_COMMENT).handleNewComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_SOCIAL_COMMENT).handleNewComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_RUMOR_COMMENT).handleNewComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_NEW_VIDEO_COMMENT).handleNewComment(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_POST_COMMENT).handleDeletedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_NEWS_COMMENT).handleDeletedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_SOCIAL_COMMENT).handleDeletedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_RUMOR_COMMENT).handleDeletedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_DELETE_VIDEO_COMMENT).handleDeletedComment(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_POST_COMMENT).handleUpdatedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_NEWS_COMMENT).handleUpdatedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_SOCIAL_COMMENT).handleUpdatedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_RUMOR_COMMENT).handleUpdatedComment(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_VIDEO_COMMENT).handleUpdatedComment(this)

        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_POST_LIKE).handleUpdatedLike(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_NEWS_LIKE).handleUpdatedLike(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_SOCIAL_LIKE).handleUpdatedLike(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_RUMOR_LIKE).handleUpdatedLike(this)
        socketDataManager.observePostsUpdates(SocketService.EVENT_UPDATE_VIDEO_LIKE).handleUpdatedLike(this)

        socketDataManager.notificationData().subscribeOnIoAndObserveOnMainThread({
            when (it.event) {
                InAppNotificationType.friendsRequestDecline -> {
//                    addNotification("Decline friend request", it.message.toString(), Constants.ACTION_DECLINE_SEND_FRIEND_REQUEST)
                }
                InAppNotificationType.friendsRequestAccepted -> {
                    addNotification(
                        it.event.name,
                        resources.getString(R.string.accept_friend_request),
                        it.message.toString(),
                        Constants.ACTION_ACCEPT_SEND_FRIEND_REQUEST
                    )
                    Constants.SHOW_REFRESH_INDICATOR = true
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
                }
                InAppNotificationType.friendsRequestReceived -> {
                    binding.toolbarMain.friendsBadge.visibility = View.VISIBLE
                    addNotification(
                        it.event.name,
                        getString(R.string.received_friend_request),
                        it.message.toString(),
                        Constants.ACTION_SEND_FRIEND_REQUEST
                    )
                }
                else -> {
                    Constants.SHOW_REFRESH_INDICATOR = true
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
                }
            }
        }, {
            Timber.e(it)
        })
        socketDataManager.messageNotification().subscribeOnIoAndObserveOnMainThread({
            if (navController.currentDestination?.id != R.id.chat) {
                if (it.safeMessage != null) {
                    val messageTitle: String = if (it.safeMessage?.type.equals("text")) {
                        "${it.safeMessage?.sender?.displayName} sent a message"
                    } else {
                        "${it.safeMessage?.sender?.displayName} sent a ${it.safeMessage?.type.toString()}"
                    }
                    addNotification(
                        it.event,
                        getString(R.string.message_received),
                        messageTitle,
                        Constants.ACTION_SEND_MESSAGE,
                        Bundle().apply {
                            putString("groupId", it.groupChatId)
                            putBoolean("isFromNotification", true)
                        })
                }
            }
        }, {
            Timber.e(it)
        })
//        socketDataManager.connectionEmitter().subscribeAndObserveOnMainThread {
//            //Toast.makeText(this,"Connected", Toast.LENGTH_SHORT).show()
//            Timber.tag(SocketDataManager.TAG).i("Connected")
//        }
//        socketDataManager.disconnectEmitter().subscribeAndObserveOnMainThread {
//            //Toast.makeText(this,"Disconnected", Toast.LENGTH_SHORT).show()
//            Timber.tag(SocketDataManager.TAG).i("Disconnected")
//        }
    }

    private fun Observable<AddNewPostNotification>.handleNewPost(context: Context) {
        subscribeAndObserveOnMainThread {
            //indicator -> detail
            val title = if (it.event == SocketService.EVENT_NEW_WALL_POST) getString(R.string.title_post_created) else getString(
                R.string.title_club_post_created,
                getSectionType(it.event.toString())
            )

            val desc = if (it.event == SocketService.EVENT_NEW_WALL_POST) getString(
                R.string.desc_post_created,
                it.authorName
            ) else getString(R.string.desc_club_post_created, getString(R.string.club_name), it.postTitle)

            addNotification(it.event, title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                putString("postId", it._id)
                putString("type", it.event.toString().getPostTypeByEvent())
            })

            Constants.SHOW_REFRESH_INDICATOR = true
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
        }
    }

    private fun Observable<AddNewPostNotification>.handleDeletedPost(context: Context) {
        subscribeAndObserveOnMainThread {
            //auto delete -> wall
            /* addNotification(
                 getString(R.string.post_deleted),
                 getString(R.string.post_deleted_msg, it.authorName, it.postTitle)
             )*/
            it._id?.let { id -> wallCacheRepository.deletePostById(id, it.event.toString().getPostTypeByEvent()) }
            it._id?.let { id -> newsCacheRepository.deletePostById(id, it.event.toString().getPostTypeByEvent()) }
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_POST_DELETED).putExtra("postId", it._id).putExtra("type", it.event.toString().getPostTypeByEvent())
            )
        }
    }

    private fun Observable<AddNewPostNotification>.handleUpdatedPost(context: Context) {
        subscribeAndObserveOnMainThread {
            //indicator/refresh post on detail -> detail
            val title = if (it.event == SocketService.EVENT_UPDATE_WALL_POST) getString(R.string.title_post_updated) else getString(
                R.string.title_club_post_updated,
                getSectionType(it.event!!)
            )

            val desc = if (it.event == SocketService.EVENT_UPDATE_WALL_POST) getString(
                R.string.desc_post_updated,
                it.authorName
            ) else getString(R.string.desc_club_post_updated, getString(R.string.club_name), it.postTitle)

            addNotification(it.event, title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                putString("postId", it._id)
                putString("type", it.event.toString().getPostTypeByEvent())
            })
            Constants.SHOW_REFRESH_INDICATOR = true
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", it._id)
                    .putExtra("type", it.event.toString().getPostTypeByEvent())
            )
        }
    }

    private fun Observable<AddNewPostNotification>.handleNewComment(context: Context) {
        subscribeAndObserveOnMainThread {
            // auto update wall post & refresh comments on detail -> detail
            val title = if (it.event == SocketService.EVENT_NEW_POST_COMMENT) getString(R.string.title_new_comment) else getString(
                R.string.title_new_club_comment,
                getSectionType(it.event!!)
            )

            val desc = if (it.event == SocketService.EVENT_NEW_POST_COMMENT) getString(
                if (it.senderId == it.wallId && it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_new_comment_owner else if (it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_new_comment_friend else R.string.desc_new_comment,
                it.authorName
            ) else getString(R.string.desc_new_club_comment, it.authorName, it.postTitle)

            addNotification(it.event, title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                putString("postId", it.postId)
                putString("type", it.event.toString().getPostTypeByEvent())
            })

            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", it.postId).putExtra("commentId", it.commentId)
                    .putExtra("type", it.event.toString().getPostTypeByEvent())
            )

            getLocalPost(it.postId!!, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.commentsCount = localPost.commentsCount + 1
                wallCacheRepository.updatePost(localPost)
            }
            getLocalNewsPost(it.postId, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.commentsCount = localPost.commentsCount + 1
                newsCacheRepository.updatePost(localPost)
            }
        }
    }

    private fun Observable<AddNewPostNotification>.handleDeletedComment(context: Context) {
        subscribeAndObserveOnMainThread {
            //auto update wall post &  refresh comments on detail -> detail
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", it.postId).putExtra("commentId", it.commentId)
                    .putExtra("type", it.event.toString().getPostTypeByEvent())
            )
            getLocalPost(it.postId!!, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.commentsCount = if (localPost.commentsCount.minus(1) > 0) localPost.commentsCount.minus(1) else 0
                wallCacheRepository.updatePost(localPost)
            }
            getLocalNewsPost(it.postId, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.commentsCount = if (localPost.commentsCount.minus(1) > 0) localPost.commentsCount.minus(1) else 0
                newsCacheRepository.updatePost(localPost)
            }
        }
    }

    private fun Observable<AddNewPostNotification>.handleUpdatedComment(context: Context) {
        subscribeAndObserveOnMainThread {
            // refresh comments on detail -> detail
            val title = if (it.event == SocketService.EVENT_UPDATE_POST_COMMENT) getString(R.string.title_comment_updated) else getString(
                R.string.title_club_comment_updated,
                getSectionType(it.event!!)
            )

            val desc = if (it.event == SocketService.EVENT_UPDATE_POST_COMMENT) getString(
                if (it.senderId == it.wallId && it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_comment_updated_owner else if (it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_comment_updated_friend else R.string.desc_comment_updated,
                it.authorName
            ) else getString(R.string.desc_club_comment_updated, it.authorName, it.postTitle)

            addNotification(it.event, title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                putString("postId", it.postId)
                putString("type", it.event.toString().getPostTypeByEvent())
            })
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", it.postId).putExtra("commentId", it.commentId)
                    .putExtra("type", it.event.toString().getPostTypeByEvent())
            )
        }
    }

    private fun Observable<AddNewPostNotification>.handleUpdatedLike(context: Context) {
        subscribeAndObserveOnMainThread {
            // auto update wall post & refresh like in detail -> detail
            if (it.like) {
                val title = if (it.event == SocketService.EVENT_UPDATE_POST_LIKE) getString(R.string.title_like_post) else getString(
                    R.string.title_like_club_post,
                    getSectionType(it.event!!)
                )

                val desc = if (it.event == SocketService.EVENT_UPDATE_POST_LIKE) getString(
                    if (it.senderId == it.wallId && it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_like_post_owner else if (it.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_like_post_friend else R.string.desc_like_post,
                    it.authorName
                ) else getString(R.string.desc_like_club_post, it.authorName, it.postTitle)

                addNotification(it.event, title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                    putString("postId", it._id)
                    putString("type", it.event.toString().getPostTypeByEvent())
                })
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", it._id).putExtra("likeCount", it.likeCount)
                    .putExtra("type", it.event.toString().getPostTypeByEvent())
            )
            getLocalPost(it._id!!, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.likeCount = it.likeCount
                wallCacheRepository.updatePost(localPost)
            }
            getLocalNewsPost(it._id, it.event.toString().getPostTypeByEvent())?.let { localPost ->
                localPost.likeCount = it.likeCount
                newsCacheRepository.updatePost(localPost)
            }
        }
    }

    /*ContentItem*/
    private fun getLocalPost(postId: String, type: String): ContentItem? {
        return wallCacheRepository.getPost(type, postId)
    }

    private fun getLocalNewsPost(postId: String, type: String): CommonFeedItem? {
        return newsCacheRepository.getPost(
            if (type.equals(
                    Constants.POST_TYPE_STREAMING,
                    true
                )
            ) Constants.POST_ORIGINAL_TYPE_CLUB_TV else type, postId
        )
    }

    private fun addNotification(eventName: String?, title: String, message: String, action: String = "", bundle: Bundle = Bundle()) {
        /*Show reload indicator in notification listing*/
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_NOTIFICATION_RELOAD_INDICATOR))
        if (eventName != null && !isNotificationOn(eventName)) return

        val mIntent = Intent(this, MainActivity::class.java)
        mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mIntent.action = action.ifBlank { Constants.ACTION_OPEN_WALL }
        mIntent.putExtras(bundle)

        val pendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder = NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.logo_fanschat).setContentTitle(title)
            .setContentText(message).setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_MAX).setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(1234, builder.build())
    }

    private fun connectSideMenu() {
        loadProfileImage()
        if (loggedInUserCache.getLoggedInUserId() != null) {
            profileViewModel.getUserProfile()
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard()
            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}

        })

        sideMenuAdapter = SideMenuAdapter(this) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            if (navController.currentDestination?.id != it.id) {
                navController.navigate(it.id)
            }
        }
        val layoutManager = GridLayoutManager(this, 2).apply {
            orientation = LinearLayoutManager.VERTICAL
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (sideMenuAdapter.list.size % 2 == 0 && position == sideMenuAdapter.list.size) 2
                    else 1
                }
            }
        }

        binding.sideMenu.rvSideMenu.layoutManager = layoutManager
        binding.sideMenu.rvSideMenu.setHasFixedSize(true)
        binding.sideMenu.rvSideMenu.adapter = sideMenuAdapter

        binding.sideMenu.profile.onClick {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
                open(R.id.authDecideFragment)
            } else {
                open(R.id.profile)
            }
        }
    }

    private fun connectBottomBar() {
        binding.bottomBar.menu.findItem(R.id.menu).setOnMenuItemClickListener {
            toggleDrawer()
            true
        }
        NavigationUI.setupWithNavController(binding.bottomBar, navController)

        binding.bottomBar.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                navController.navigate(item.itemId)
            } else {
                val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentsContainer) as NavHostFragment?
                navHostFragment?.let { navFragment ->
                    navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                        when {
                            fragment is WallFragment -> {
                                fragment.scrollToTop()
                            }
                            (navController.currentDestination?.id == R.id.news || navController.currentDestination?.id == R.id.social || navController.currentDestination?.id == R.id.tv) && fragment is NewsFragment -> {
                                fragment.scrollToTop()
                            }
                        }
                    }
                }
            }

            true
        }

        navController.onNavigate {
//            binding.toolbarMain.appBarLayout.isVisible = it.hasBottomBar()
            // binding.bottomBar.isVisible = it.hasBottomBar()
            hideShowToolBar(it.hasToolBar())
            hideShowBottomBar(it.hasBottomBar())
        }
    }

    private fun hideShowBottomBar(toShow: Boolean) {
        if (toShow) {
            binding.bottomBar.expand(CommonUtils.dpToPx(this, 56f).roundToInt())
        } else {
            binding.bottomBar.collapse()
        }
    }

    private fun hideShowToolBar(toShow: Boolean) {
        if (toShow) {
            binding.toolbarMain.appBarLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            binding.toolbarMain.appBarLayout.expand(binding.toolbarMain.appBarLayout.measuredHeight)
        } else {
            binding.toolbarMain.appBarLayout.collapse()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(binding.drawerView)) {
            binding.drawerLayout.closeDrawer(binding.drawerView)
        } else {
            loadProfileImage()
            if (loggedInUserCache.getLoggedInUserId() != null) {
                profileViewModel.getUserProfile()
            }
            binding.drawerLayout.openDrawer(binding.drawerView)
        }
    }

    private fun connectToolbar() {
        //        toolbarCarousal.visibility = if (flavor != Flavors.MTN) View.VISIBLE else View.GONE
        //        tvCarousal.visibility = if (flavor == Flavors.MTN) View.VISIBLE else View.GONE
        binding.toolbarMain.ivCaption2.visibility = /*if (flavor == Flavors.MTN) View.VISIBLE else */View.GONE

        binding.toolbarMain.showFriends.onClick {
            open(R.id.showFriends)
        }
        binding.toolbarMain.notifications.isVisible = !loggedInUserCache.getLoggedInUserId().isNullOrBlank()
        binding.toolbarMain.notifications.onClick {
            open(R.id.notification)
        }
        //        showBadgeOnFriendRequest()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
//        Observable.timer(300, TimeUnit.MILLISECONDS).subscribeAndObserveOnMainThread {
//            if (!socketDataManager.isConnected) {
//                socketDataManager.connect()
//            }
//            openScreen(intent)
//        }.autoDispose()
        openScreen(intent)
    }

    private fun dumpExtras(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                Timber.e("Push: " + key + " : " + if (bundle[key] != null) bundle[key] else "NULL")
            }
        }
    }

    private fun openScreen(intent: Intent?) {
        if (intent == null) return
        dumpExtras(intent)
        when {
            intent.data != null -> {
                val url = intent.data.toString()
                var id: String? = null
                var type = ""

                val listComponent = url.split("/")
                if (listComponent.isNotEmpty()) {
                    id = listComponent.last()
                    type = when (listComponent[listComponent.lastIndex - 1]) {
                        Constants.SHARED_POST_TYPE_WALL -> Constants.POST_TYPE_WALL
                        Constants.SHARED_POST_TYPE_NEWS -> Constants.POST_TYPE_NEWS
                        Constants.SHARED_POST_TYPE_SOCIAL -> Constants.POST_TYPE_SOCIAL
                        Constants.SHARED_POST_TYPE_RUMOURS -> Constants.POST_TYPE_RUMOURS
                        Constants.SHARED_POST_TYPE_CLUB_TV -> Constants.POST_TYPE_CLUB_TV
                        else -> ""
                    }
                }

                id?.let {
                    open(R.id.details, hashMapOf("postId" to it, "type" to type))
                }
                intent.data = null
            }
            intent.action != null -> {
                if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) return
                when (intent.action) {
                    //Open wall
                    Constants.ACTION_OPEN_WALL -> {
                        redirectToWall()
                    }
                    //Open detail
                    Constants.ACTION_OPEN_POST_DETAIL -> {
                        if (intent.extras?.containsKey("postId") == true) {
                            val navHostFragment: NavHostFragment? =
                                supportFragmentManager.findFragmentById(R.id.fragmentsContainer) as NavHostFragment?
                            navHostFragment?.let { navFragment ->
                                navFragment.childFragmentManager.primaryNavigationFragment?.let { _ ->
                                    open(
                                        R.id.details,
                                        hashMapOf(
                                            "postId" to intent.extras?.getString("postId")!!,
                                            "type" to intent.extras?.getString("type")!!
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Constants.ACTION_SEND_FRIEND_REQUEST -> {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_OPEN_FRIEND_REQUEST))
                        open(R.id.showFriends, hashMapOf("friendRequest" to true))
                    }
                    Constants.ACTION_ACCEPT_SEND_FRIEND_REQUEST -> {
                        open(R.id.showFriends)
                    }
                    Constants.ACTION_DECLINE_SEND_FRIEND_REQUEST -> {
                        open(R.id.showFriends, hashMapOf("friendRequest" to false))
                    }
                    Constants.ACTION_REMOVE_FRIEND -> {
                        open(R.id.showFriends)
                    }
                    Constants.ACTION_SEND_MESSAGE -> {
                        val bundle = intent.extras

                        if (intent.extras?.containsKey("groupId") == true) {
                            redirectToChat((bundle!!["groupId"] as String))
                        } else open(R.id.chat)
                    }
                    else -> {
                        // handlePush(intent)
                    }
                }
            }
        }
    }

    fun redirectToPostDetail(postId: String, postType: String) {
        open(R.id.details, hashMapOf("postId" to postId, "type" to postType))
    }

    private fun redirectToWall() {
        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentsContainer) as NavHostFragment?
        navHostFragment?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                if (fragment !is WallFragment) {
                    navController.navigate(R.id.feed)
                }
            }
        }
    }

    fun redirectToChat(chatId: String?) {
        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentsContainer) as NavHostFragment?
        navHostFragment?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                if (fragment is ChatFragment) {
                    fragment.openChatFromNotification(chatId)
                } else {
                    open(R.id.chat, hashMapOf("chatId" to chatId, "isFromNotification" to true))
                }
            }
        }
    }

    fun redirectToFriendRequestScreen(toFriendRequest: Boolean) {
        if (toFriendRequest) open(R.id.showFriends, hashMapOf("friendRequest" to true))
        else open(R.id.showFriends)
    }

    private fun onVisibilityChanged(isOpen: Boolean) {
        if (navController.currentDestination?.id == R.id.chat) {
            binding.bottomBar.visibility = if (isOpen) View.GONE else View.VISIBLE
        }
    }

    private fun registerKeyboardListener() {
        val activityRoot = getActivityRoot(this)
        activityRoot.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun unregisterKeyboardListener() {
        val activityRoot = getActivityRoot(this)
        activityRoot.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }

    /**
     * Determine if keyboard is visible
     *
     * @param activity Activity
     * @return Whether keyboard is visible or not
     */
    private fun isKeyboardVisible(activity: Activity): Boolean {
        val r = Rect()

        val activityRoot = getActivityRoot(activity)

        activityRoot.getWindowVisibleDisplayFrame(r)

        val location = IntArray(2)
        getContentRoot(activity).getLocationOnScreen(location)

        val screenHeight = activityRoot.rootView.height
        val heightDiff = screenHeight - r.height() - location[1]

        return heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO
    }

    private fun getActivityRoot(activity: Activity): View {
        return getContentRoot(activity).rootView
    }

    private fun getContentRoot(activity: Activity): ViewGroup {
        return activity.findViewById(android.R.id.content)
    }

    private fun listenToViewModel() {
        profileViewModel.editprofileState.subscribeAndObserveOnMainThread {
            when (it) {
                is EditProfileViewModel.EditProfileViewState.UserDetails -> {
                    binding.sideMenu.progressBarAvatar.progress = ((it.fansChatUser.points
                        ?: 0) * 100)
                    val user = it.fansChatUser
                    val pointsText = getString(R.string.points).replace("X", user.points.toString())
                    binding.sideMenu.pointsTextView.text = pointsText
                    binding.sideMenu.capLvlTextView.text = user.capsLvL?.toString() ?: "0"
                    loggedInUserCache.setLoggedInUserProfile(user.avatarUrl)
                    loadProfileImage()
                }
                else -> {}
            }
        }.autoDispose()
    }

    override fun onDestroy() {
        navController.removeOnDestinationChangedListener(listener)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        //socketDataManager.disconnect()
        unregisterKeyboardListener()
    }

    fun showTicker(listTicker: List<TickerData>) {
        var nextNews = 0
        handler.removeCallbacksAndMessages(null)
        runnable = Runnable {
            if (listTicker.isNotEmpty()) {
                showTextWithAnimations(
                    binding.toolbarMain.toolbarCarousal, if (nextNews >= listTicker.size) {
                        nextNews = 0
                        listTicker[0].ticker
                    } else listTicker[nextNews].ticker
                )
            }
            nextNews++
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, DELAY_CAROUSAL)
        }
        handler.post(runnable)
    }

    //Show/hide notification icon
    fun toggleNotificationIcon() {
        if (::binding.isInitialized) binding.toolbarMain.notifications.let {
            it.isVisible = !loggedInUserCache.getLoggedInUserId().isNullOrBlank()
        }
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawers()
        } else {
            val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentsContainer) as NavHostFragment?
            navHostFragment?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                    if (fragment is PostCreateFragment) {
                        fragment.onBackPress()
                    } else if (fragment is DetailFragment) {
                        if (!fragment.goneBack()) {
                            super.onBackPressed()
                        }
                    } else {
                        super.onBackPressed()
                    }
                }
            }
        }
    }

    fun hideShowExtraBars(toShow: Boolean) {
        try {
            binding.toolbarMain.appBarLayout.isVisible = toShow
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager!!.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener(mAudioFocusListener).build()
            )
        } else {
            mAudioManager!!.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    fun leaveAudioFocus() {
        mAudioManager!!.abandonAudioFocus(mAudioFocusListener)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        loadProfileImage()
    }

    private fun showTextWithAnimations(tvCarousal: TextView, text: String) {
        tvCarousal.text = text
        //    val anim = AlphaAnimation(1.0f, 0.6f)
        //    anim.duration = 600
        //    anim.repeatCount = 1
        //    anim.repeatMode = Animation.REVERSE
        //
        //    anim.setAnimationListener(object : Animation.AnimationListener {
        //        override fun onAnimationEnd(animation: Animation?) {}
        //        override fun onAnimationStart(animation: Animation?) {}
        //        override fun onAnimationRepeat(animation: Animation?) {
        //            tvCarousal.text = text
        //        }
        //    })
        //
        //    tvCarousal.startAnimation(anim)
    }

    override fun onStart() {
        super.onStart()
        //socketDataManager.connect()
    }

    override fun onPause() {
        super.onPause()
        //socketDataManager.disconnect()
    }

    private fun loadProfileImage() {
        Glide.with(this).load(loggedInUserCache.getLoggedInUserProfile()).error(R.drawable.avatar_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).dontAnimate().into(binding.sideMenu.avatars)
    }

    override fun onResume() {
        super.onResume()
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                task.result?.let {
                    authenticationRepository.updateFirebaseToken(FirebaseResponse(it))
                        .subscribeOnIoAndObserveOnMainThread({
                            Timber.tag(TAG).e(it.toString())
                        }, {
                            Timber.tag(TAG).e(it.message)
                        })
                }
            })
        }
    }
}