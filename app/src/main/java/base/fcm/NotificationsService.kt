package base.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import base.R
import base.application.FansChat
import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.LoggedInUserCache
import base.data.api.authentication.model.FirebaseResponse
import base.data.api.news.NewsCacheRepository
import base.data.api.wall.WallCacheRepository
import base.data.model.CommonFeedItem
import base.data.model.wall.ContentItem
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.socket.SocketService
import base.socket.model.AddNewPostNotification
import base.socket.model.ChatMessageNotification
import base.socket.model.NotificationData
import base.ui.MainActivity
import base.util.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject


const val TAG = "NotificationX"

class NotificationsService : FirebaseMessagingService() {

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var authenticationRepository: AuthenticationRepository

    @Inject
    lateinit var wallCacheRepository: WallCacheRepository

    @Inject
    lateinit var newsCacheRepository: NewsCacheRepository

    override fun onCreate() {
        super.onCreate()
        FansChat.component.inject(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notification = remoteMessage.notification

        Timber.tag(TAG).e("Notification title: " + notification?.title)
        Timber.tag(TAG).e("Data payload: \n" + remoteMessage.data.toString().replace(",", "\n"))

        if (notification != null) {
            addNotification(
                null, remoteMessage.notification?.title ?: getString(R.string.app_name), remoteMessage.notification?.body
                    ?: getString(R.string.app_name)
            ) /*handlePush(remoteMessage) */// messages that came from Firebase web console
        } else {
//            addNotification(getString(R.string.app_name),
//                remoteMessage.data.entries.lastOrNull()?.value
//                    ?: getString(R.string.app_name)) // messages that came from Gamesparks REST console
            handlePush(remoteMessage)
        }
    }

    private fun handlePush(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.containsKey("event")) {
            when (remoteMessage.data["event"]) {

                in wallEventList(), in newsEventList(), in socialEventList(), in rumorEventList(), in videoEventList() -> {
                    //Notification is regarding posts
                    handlePostNotifications(remoteMessage)
                }

                SocketService.EVENT_SEND_MESSAGE -> {
                    val chatMessageNotification = Gson().fromJson(Gson().toJson(remoteMessage.data), ChatMessageNotification::class.java)
                    if (chatMessageNotification != null && !chatMessageNotification.message?.trim().isNullOrEmpty()) {
                        val bundle = Bundle()
                        for ((key, value) in remoteMessage.data) {
                            bundle.putString(key, value)
                        }
                        addNotification(
                            remoteMessage.data["event"],
                            getString(R.string.message_received),
                            getString(R.string.new_msg_msg, chatMessageNotification.senderName),
                            Constants.ACTION_SEND_MESSAGE,
                            bundle
                        )
                    }
                }
                SocketService.EVENT_MESSAGE_UPDATE -> {
                    val chatMessageNotification = Gson().fromJson(Gson().toJson(remoteMessage.data), ChatMessageNotification::class.java)
                    if (chatMessageNotification != null && chatMessageNotification.message?.trim().isNullOrEmpty()) {
                        val bundle = Bundle()
                        for ((key, value) in remoteMessage.data) {
                            bundle.putString(key, value)
                        }
                        addNotification(
                            remoteMessage.data["event"],
                            getString(R.string.message_received),
                            getString(R.string.new_media_msg_msg, chatMessageNotification.senderName),
                            Constants.ACTION_SEND_MESSAGE,
                            bundle
                        )
                    }
                }
                SocketService.EVENT_FRIEND_REQUEST -> {
                    val notificationData = Gson().fromJson(Gson().toJson(remoteMessage.data), NotificationData::class.java)
                    if (notificationData != null) {
                        val bundle = Bundle()
                        for ((key, value) in remoteMessage.data) {
                            bundle.putString(key, value)
                        }
                        addNotification(
                            remoteMessage.data["event"], getString(R.string.received_friend_request), getString(
                                R.string.friend_request_msg, notificationData.message?.split(" ")?.firstOrNull()
                                    ?: ""
                            ), Constants.ACTION_SEND_FRIEND_REQUEST, bundle
                        )
                    }
                }
                SocketService.EVENT_FRIEND_REQUEST_ACCEPTED -> {
                    val notificationData = Gson().fromJson(Gson().toJson(remoteMessage.data), NotificationData::class.java)
                    if (notificationData != null) {
                        val bundle = Bundle()
                        for ((key, value) in remoteMessage.data) {
                            bundle.putString(key, value)
                        }
                        addNotification(
                            remoteMessage.data["event"], getString(R.string.accept_friend_request), getString(
                                R.string.friend_request_accepted_msg, notificationData.message?.split(" ")?.firstOrNull()
                                    ?: ""
                            ), Constants.ACTION_ACCEPT_SEND_FRIEND_REQUEST, bundle
                        )
                        Constants.SHOW_REFRESH_INDICATOR = true
                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
                    }
                }
                else -> {

                }
            }
        } else {
            addNotification(
                null, remoteMessage.notification?.title ?: getString(R.string.app_name), remoteMessage.notification?.body
                    ?: getString(R.string.app_name)
            )
        }
    }

    private fun handlePostNotifications(remoteMessage: RemoteMessage) {
        val postObject: AddNewPostNotification = Gson().fromJson(Gson().toJson(remoteMessage.data), AddNewPostNotification::class.java)
        when (remoteMessage.data["event"]) {

            /*-------------------------------------------NEW POST------------------------------------------------------------*/

            SocketService.EVENT_NEW_WALL_POST, SocketService.EVENT_NEW_NEWS, SocketService.EVENT_NEW_SOCIAL, SocketService.EVENT_NEW_RUMOR, SocketService.EVENT_NEW_VIDEO -> {
                val title =
                    if (remoteMessage.data["event"] == SocketService.EVENT_NEW_WALL_POST) getString(R.string.title_post_created) else getString(
                        R.string.title_club_post_created,
                        getSectionType(remoteMessage.data["event"]!!)
                    )

                val desc = if (remoteMessage.data["event"] == SocketService.EVENT_NEW_WALL_POST) getString(
                    R.string.desc_post_created,
                    postObject.authorName
                ) else getString(R.string.desc_club_post_created, getString(R.string.club_name), postObject.postTitle)

                addNotification(remoteMessage.data["event"], title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                    putString("postId", postObject._id)
                    putString("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                })

                /*Show reload indicator in wall*/
                Constants.SHOW_REFRESH_INDICATOR = true
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))
            }

            /*-------------------------------------------DELETE POST---------------------------------------------------------*/

            SocketService.EVENT_DELETE_WALL_POST, SocketService.EVENT_DELETE_NEWS, SocketService.EVENT_DELETE_SOCIAL, SocketService.EVENT_DELETE_RUMOR, SocketService.EVENT_DELETE_VIDEO -> {

                postObject._id?.let {
                    wallCacheRepository.deletePostById(it, remoteMessage.data["event"].toString().getPostTypeByEvent())
                    newsCacheRepository.deletePostById(it, remoteMessage.data["event"].toString().getPostTypeByEvent())
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                        Intent(Constants.ACTION_POST_DELETED).putExtra("postId", it)
                            .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                    )
                }
            }

            /*-------------------------------------------EDIT POST-----------------------------------------------------------*/

            SocketService.EVENT_UPDATE_WALL_POST, SocketService.EVENT_UPDATE_NEWS, SocketService.EVENT_UPDATE_SOCIAL, SocketService.EVENT_UPDATE_RUMOR, SocketService.EVENT_UPDATE_VIDEO -> {

                val title =
                    if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_WALL_POST) getString(R.string.title_post_updated) else getString(
                        R.string.title_club_post_updated,
                        getSectionType(remoteMessage.data["event"]!!)
                    )

                val desc = if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_WALL_POST) getString(
                    R.string.desc_post_updated,
                    postObject.authorName
                ) else getString(R.string.desc_club_post_updated, getString(R.string.club_name), postObject.postTitle)

                addNotification(remoteMessage.data["event"], title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                    putString("postId", postObject._id)
                    putString("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                })

                /*Show reload indicator in wall*/
                Constants.SHOW_REFRESH_INDICATOR = true
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_WALL_RELOAD_INDICATOR))

                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", postObject._id)
                        .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                )
            }

            /*----------------------------------------------NEW COMMENT------------------------------------------------------*/

            SocketService.EVENT_NEW_POST_COMMENT, SocketService.EVENT_NEW_NEWS_COMMENT, SocketService.EVENT_NEW_SOCIAL_COMMENT, SocketService.EVENT_NEW_RUMOR_COMMENT, SocketService.EVENT_NEW_VIDEO_COMMENT -> {

                val title =
                    if (remoteMessage.data["event"] == SocketService.EVENT_NEW_POST_COMMENT) getString(R.string.title_new_comment) else getString(
                        R.string.title_new_club_comment,
                        getSectionType(remoteMessage.data["event"]!!)
                    )

                val desc = if (remoteMessage.data["event"] == SocketService.EVENT_NEW_POST_COMMENT) getString(
                    if (postObject.senderId == postObject.wallId && postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_new_comment_owner else if (postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_new_comment_friend else R.string.desc_new_comment,
                    postObject.authorName
                ) else getString(R.string.desc_new_club_comment, postObject.authorName, postObject.postTitle)

                addNotification(remoteMessage.data["event"], title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                    putString("postId", postObject.postId)
                    putString("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                })

                //If detail is open for same post it should update UI
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", postObject.postId)
                        .putExtra("commentId", postObject.commentId)
                        .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                )

                //If We have post in local then it should update count in post cell
                getLocalPost(postObject.postId!!, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.commentsCount = localPost.commentsCount + 1
                    wallCacheRepository.updatePost(localPost)
                }
                getLocalNewsPost(postObject.postId, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.commentsCount = localPost.commentsCount + 1
                    newsCacheRepository.updatePost(localPost)
                }
            }

            /*---------------------------------------------DELETE COMMENT----------------------------------------------------*/
            //not coming
            SocketService.EVENT_DELETE_POST_COMMENT, SocketService.EVENT_DELETE_NEWS_COMMENT, SocketService.EVENT_DELETE_SOCIAL_COMMENT, SocketService.EVENT_DELETE_RUMOR_COMMENT, SocketService.EVENT_DELETE_VIDEO_COMMENT -> {

                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", postObject.postId)
                        .putExtra("commentId", postObject.commentId)
                        .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                )
                getLocalPost(postObject.postId!!, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.commentsCount = if (localPost.commentsCount.minus(1) > 0) localPost.commentsCount.minus(1) else 0
                    wallCacheRepository.updatePost(localPost)
                }
                getLocalNewsPost(postObject.postId, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.commentsCount = if (localPost.commentsCount.minus(1) > 0) localPost.commentsCount.minus(1) else 0
                    newsCacheRepository.updatePost(localPost)
                }
            }

            /*---------------------------------------------EDIT COMMENT------------------------------------------------------*/

            SocketService.EVENT_UPDATE_POST_COMMENT, SocketService.EVENT_UPDATE_NEWS_COMMENT, SocketService.EVENT_UPDATE_SOCIAL_COMMENT, SocketService.EVENT_UPDATE_RUMOR_COMMENT, SocketService.EVENT_UPDATE_VIDEO_COMMENT -> {

                val title =
                    if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_POST_COMMENT) getString(R.string.title_comment_updated) else getString(
                        R.string.title_club_comment_updated,
                        getSectionType(remoteMessage.data["event"]!!)
                    )

                val desc = if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_POST_COMMENT) getString(
                    if (postObject.senderId == postObject.wallId && postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_comment_updated_owner else if (postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_comment_updated_friend else R.string.desc_comment_updated,
                    postObject.authorName
                ) else getString(R.string.desc_club_comment_updated, postObject.authorName, postObject.postTitle)

                addNotification(remoteMessage.data["event"], title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                    putString("postId", postObject.postId)
                    putString("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                })
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", postObject.postId)
                        .putExtra("commentId", postObject.commentId)
                        .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                )
            }

            /*-------------------------------------------LIKE/DISLIKE--------------------------------------------------------*/

            SocketService.EVENT_UPDATE_POST_LIKE, SocketService.EVENT_UPDATE_NEWS_LIKE, SocketService.EVENT_UPDATE_SOCIAL_LIKE, SocketService.EVENT_UPDATE_RUMOR_LIKE, SocketService.EVENT_UPDATE_VIDEO_LIKE -> {

                if (postObject.like) {

                    val title =
                        if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_POST_LIKE) getString(R.string.title_like_post) else getString(
                            R.string.title_like_club_post,
                            getSectionType(remoteMessage.data["event"]!!)
                        )

                    val desc = if (remoteMessage.data["event"] == SocketService.EVENT_UPDATE_POST_LIKE) getString(
                        if (postObject.senderId == postObject.wallId && postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_like_post_owner else if (postObject.wallId != loggedInUserCache.getLoggedInUserId()) R.string.desc_like_post_friend else R.string.desc_like_post,
                        postObject.authorName
                    ) else getString(R.string.desc_like_club_post, postObject.authorName, postObject.postTitle)

                    addNotification(remoteMessage.data["event"], title, desc, Constants.ACTION_OPEN_POST_DETAIL, Bundle().apply {
                        putString("postId", postObject._id)
                        putString("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                    })
                }

                LocalBroadcastManager.getInstance(this).sendBroadcast(
                    Intent(Constants.ACTION_OPEN_POST_DETAIL).putExtra("postId", postObject._id).putExtra("likeCount", postObject.likeCount)
                        .putExtra("type", remoteMessage.data["event"].toString().getPostTypeByEvent())
                )
                getLocalPost(postObject._id!!, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.likeCount = postObject.likeCount
                    wallCacheRepository.updatePost(localPost)
                }
                getLocalNewsPost(postObject._id, remoteMessage.data["event"].toString().getPostTypeByEvent())?.let { localPost ->
                    localPost.likeCount = postObject.likeCount
                    newsCacheRepository.updatePost(localPost)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun addNotification(eventName: String?, title: String, message: String, action: String = "", bundle: Bundle = Bundle()) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /*If app is in foreground or not login ignore push notification*/
//        if (FansChat.isAppForeGrounded || loggedInUserCache.getLoginUserToken().isNullOrEmpty()) return

        if (eventName != null && !isNotificationOn(eventName)) return

        val mIntent = Intent(this, MainActivity::class.java)
        mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mIntent.action = action.ifBlank { Constants.ACTION_OPEN_WALL }
        mIntent.putExtras(bundle)
        val pendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH //Important for heads-up notification
            val channel = NotificationChannel(getString(R.string.notification_channel_id), name, importance)
            channel.description = description
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder: Builder =
            Builder(this, getString(R.string.notification_channel_id)).setSmallIcon(R.drawable.logo_fanschat).setContentTitle(title)
                .setContentText(message).setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE) //Important for heads-up notification
                .setContentIntent(pendingIntent).setGroup("defaultGroup").setPriority(PRIORITY_HIGH).setAutoCancel(true)
        notificationManager.notify(java.util.Random().nextInt(), mBuilder.build())

        /*Show reload indicator in notification listing*/
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.ACTION_NOTIFICATION_RELOAD_INDICATOR))
    }


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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // get a token from here to use as Push ID in Gamesparks REST console, for testing
        saveString("deviceToken", token)
        Timber.tag("ApiToken").d("" + token)

        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            authenticationRepository.updateFirebaseToken(FirebaseResponse(token))
                .subscribeOnIoAndObserveOnMainThread({
                    Timber.tag(TAG).e(it.toString())
                }, {
                    Timber.tag(TAG).e(it.message)
                })
        }
//        if (isLoggedIn) Api.registerForPushNotifications(token)
    }
}