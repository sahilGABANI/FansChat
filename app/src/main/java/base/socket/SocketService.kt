package base.socket

import base.BuildConfig
import base.data.api.authentication.LoggedInUserCache
import base.extension.onSafeNext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import timber.log.Timber


class SocketService(private val loggedInUserCache: LoggedInUserCache) {

    private lateinit var socket: Socket
    private lateinit var options: IO.Options
    private var gson: Gson = GsonBuilder().create()
    val isConnected: Boolean get() = socket.connected()

    init {
        initSocket()
    }

    private fun initSocket() {
        val socketOptionBuilder = IO.Options.builder().apply {
            setReconnection(true)
        }
        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(Interceptor { chain ->
                Timber.tag(SocketDataManager.TAG).i("Token Call %s", loggedInUserCache.getLoginUserToken())
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                requestBuilder.header("token", loggedInUserCache.getLoginUserToken() ?: "")
                chain.proceed(requestBuilder.build())
            }).build()
        options = socketOptionBuilder.build()
        options.webSocketFactory = httpClient
        options.callFactory = httpClient
        socket = IO.socket(BuildConfig.SOCKET_BASE_URL, options)
        socket.apply {
            io().timeout(-1)
        }
    }

    fun connect() {
        if (!isConnected && !loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            socket.connect() ?: Timber.e("Socket is not init")
        } else {
            Timber.e(
                "Socket connected :::: %s  User token :::: %s ",
                isConnected,
                !loggedInUserCache.getLoginUserToken().isNullOrEmpty()
            )
        }
    }

    fun disconnect() {
        socket.disconnect()
    }

    fun off() {
        socket.off()
    }

    fun getGson() = gson

    companion object {
        const val EVENT_CONNECT = Socket.EVENT_CONNECT
        const val EVENT_CONNECT_ERROR = Socket.EVENT_CONNECT_ERROR
        const val EVENT_DISCONNECT = Socket.EVENT_DISCONNECT

        const val EVENT_CREATE_CHAT_GROUP: String = "imsCreateChatGroup"
        const val EVENT_DELETE_CHAT_GROUP: String = "imsDeleteChatGroup"
        const val EVENT_UPDATE_CHAT_GROUP: String = "imsUpdateChatGroup"
        const val EVENT_PUBLIC_CHAT_GROUP_LIST: String = "imsGetPublicChatGroupList"

        const val EVENT_JOIN_CHAT_GROUP: String = "imsJoinChatGroup"
        const val EVENT_LEAVE_CHAT_GROUP: String = "imsLeaveChatGroup"

        const val EVENT_ALL_OFFICIAL_CHAT_GROUP_LIST: String = "imsGetAllOfficialChatGroupsList"
        const val EVENT_GET_GLOBAL_CHAT_GROUP: String = "imsGetGlobalChatGroupsList"
        const val EVENT_GET_USER_CHAT_GROUP: String = "imsGetUserChatGroupsList"
        const val EVENT_GET_PUBLIC_CHAT_GROUP_LIST_FOR_USER: String =
            "imsGetPublicChatGroupsListForUser"
        const val EVENT_MUTE_CHAT: String = "imsMuteChat"
        const val EVENT_UNMUTE_CHAT: String = "imsUnMuteChat"
        const val EVENT_SEND_MESSAGE: String = "imsSendMessage"
        const val EVENT_MESSAGE_DELETE: String = "imsMessageDelete"
        const val EVENT_GET_CHAT_GROUP_MESSAGES: String = "imsGetChatGroupsMessages"
        const val EVENT_MESSAGE_UPDATE: String = "imsMessageUpdate"
        const val EVENT_LEFT_GROUP_CHAT: String = "someoneLeftGroupChat"
        const val EVENT_UPDATE_MESSAGE: String = "updateMessage"
        const val EVENT_MESSAGE: String = "message"
        const val EVENT_SOMEONE_JOINED_GROUP_CHAT: String = "someoneJoinedGroupChat"
        const val EVENT_DELETED_MESSAGE: String = "deleteMessage"
        const val EVENT_NEW_GROUP_CREATED = "imsNewGroupCreated"

        const val EVENT_NOTIFICATIONS: String = "notifications"
        const val EVENT_NOTIFICATION: String = "notification"

        const val EVENT_FRIEND_REQUEST: String = "friendsRequestReceived"
        const val EVENT_FRIEND_REQUEST_ACCEPTED: String = "friendsRequestAccepted"

        /*Wall Events*/
        const val EVENT_NEW_WALL_POST: String = "newWallPost"
        const val EVENT_DELETE_WALL_POST: String = "deletedWallPost"
        const val EVENT_UPDATE_WALL_POST: String = "updatedWallPost"
        const val EVENT_UPDATE_POST_LIKE: String = "updatedWallPostLike"
        const val EVENT_NEW_POST_COMMENT: String = "newWallPostComment"
        const val EVENT_DELETE_POST_COMMENT: String = "deletedWallPostComment"
        const val EVENT_UPDATE_POST_COMMENT: String = "updatedWallPostComment"

        /*News Events*/
        const val EVENT_NEW_NEWS: String = "newNews"
        const val EVENT_UPDATE_NEWS: String = "updatedNewsPost"
        const val EVENT_DELETE_NEWS: String = "deletedNewsPost"
        const val EVENT_UPDATE_NEWS_LIKE: String = "updatedNewsLike"
        const val EVENT_NEW_NEWS_COMMENT: String = "newNewsComment"
        const val EVENT_UPDATE_NEWS_COMMENT: String = "updatedNewsPostComment"
        const val EVENT_DELETE_NEWS_COMMENT: String = "deletedNewsPostComment"

        /*Social Events*/
        const val EVENT_NEW_SOCIAL: String = "newSocial"
        const val EVENT_UPDATE_SOCIAL: String = "updatedSocialPost"
        const val EVENT_DELETE_SOCIAL: String = "deletedSocialPost"
        const val EVENT_UPDATE_SOCIAL_LIKE: String = "updatedSocialLike"
        const val EVENT_NEW_SOCIAL_COMMENT: String = "newSocialComment"
        const val EVENT_UPDATE_SOCIAL_COMMENT: String = "updatedSocialPostComment"
        const val EVENT_DELETE_SOCIAL_COMMENT: String = "deletedSocialPostComment"

        /*Rumors Events*/
        const val EVENT_NEW_RUMOR: String = "newRumor"
        const val EVENT_UPDATE_RUMOR: String = "updatedRumorPost"
        const val EVENT_DELETE_RUMOR: String = "deletedRumorPost"
        const val EVENT_UPDATE_RUMOR_LIKE: String = "updatedRumorLike"
        const val EVENT_NEW_RUMOR_COMMENT: String = "newRumorComment"
        const val EVENT_UPDATE_RUMOR_COMMENT: String = "updatedRumorPostComment"
        const val EVENT_DELETE_RUMOR_COMMENT: String = "deletedRumorPostComment"

        /*Video Events*/
        const val EVENT_NEW_VIDEO: String = "newVideo"
        const val EVENT_UPDATE_VIDEO: String = "updatedVideoPost"
        const val EVENT_DELETE_VIDEO: String = "deletedVideoPost"
        const val EVENT_UPDATE_VIDEO_LIKE: String = "updatedVideoLike"
        const val EVENT_NEW_VIDEO_COMMENT: String = "newVideoComment"
        const val EVENT_UPDATE_VIDEO_COMMENT: String = "updatedVideoPostComment"
        const val EVENT_DELETE_VIDEO_COMMENT: String = "deletedVideoComment"

    }

    fun request(name: String, any: Any): Completable =
        if (isConnected) {
            Completable.fromCallable {
                socket.emit(name, any) ?: Timber.e("Socket is not init")
            }
        } else {
            Completable.error(SocketNotConnectedException())
        }

    fun on(name: String, listener: Emitter.Listener) {
        socket.on(name, listener)
    }

    fun <T> requestWithAck(name: String, any: Any, emitter: ObservableEmitter<T>, clazz: Class<T>) {
        if (isConnected)
            socket.emit(name, any, SocketAck(emitter, gson, clazz))
                ?: Timber.e("Socket is not init")
    }
}

class SocketNotConnectedException : Throwable()

class SocketAck<T>(
    private val emitter: ObservableEmitter<T>,
    private val gson: Gson,
    private val clazz: Class<T>,
) : Ack {
    private var TAG = "SocketAck"
    override fun call(vararg args: Any?) {
        val response = args.firstOrNull()?.toString()
        if (response == null) {
            emitter.onError(Exception("Socket error"))
        } else {
            Timber.tag(TAG).i(response)
            emitter.onSafeNext(gson.fromJson(args[0].toString(), clazz))
        }
    }
}