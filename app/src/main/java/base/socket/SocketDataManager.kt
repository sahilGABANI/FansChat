package base.socket

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.chat.model.*
import base.socket.model.*
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class SocketDataManager(private val appSocket: SocketService) : Connectible {
    companion object {
        const val TAG: String = "Socket"
    }

    override val isConnected: Boolean
        get() = appSocket.isConnected

    private val connectionEmitter by lazy {
        Observable.create<Unit> { emitter ->
            appSocket.on(SocketService.EVENT_CONNECT) {
                Timber.tag(TAG).i("ON Event Name : ${SocketService.EVENT_CONNECT}")
                Timber.tag(TAG).i("ON Socket connected $it")
                emitter.onNext(Unit)
            }
        }.share()
    }

    private val connectionErrorEmitter by lazy {
        Observable.create<Unit> { emitter ->
            appSocket.on(SocketService.EVENT_CONNECT_ERROR) {
                Timber.tag(TAG).i("ON Event Name : ${SocketService.EVENT_CONNECT_ERROR}")
                Timber.tag(TAG).i("ON Socket connection error $it")
                emitter.onNext(Unit)
            }
        }.share()
    }

    private val disconnectEmitter by lazy {
        Observable.create<Unit> { emitter ->
            appSocket.on(SocketService.EVENT_DISCONNECT) {
                Timber.tag(TAG).i("ON Event Name : ${SocketService.EVENT_DISCONNECT}")
                Timber.tag(TAG).i("ON Socket disconnected $it")
                emitter.onNext(Unit)
            }
        }.share()
    }

    private val someoneLeftGroupEmitter by lazy {
        Observable.create<SomeOneLeftGroupPayload> { emitter ->
            appSocket.on(SocketService.EVENT_LEFT_GROUP_CHAT) {
                emitter.onNext(it.getResponse(SomeOneLeftGroupPayload::class.java))
            }
        }.share()
    }

    private val updateMessageEmitter by lazy {
        Observable.create<MessageGroupPayload> { emitter ->
            appSocket.on(SocketService.EVENT_UPDATE_MESSAGE) {
                emitter.onNext(it.getResponse(MessageGroupPayload::class.java))
            }
        }.share()
    }

    private val messageEmitter by lazy {
        Observable.create<MessageGroupPayload> { emitter ->
            appSocket.on(SocketService.EVENT_MESSAGE) {
                Timber.tag(TAG).i("EVENT_MESSAGE %s", it[0].toString())
                emitter.onNext(it.getResponse(MessageGroupPayload::class.java))
            }
        }.share()
    }

    private val someoneJoinedChatGroupEmitter by lazy {
        Observable.create<SomeOneLeftGroupPayload> { emitter ->
            appSocket.on(SocketService.EVENT_SOMEONE_JOINED_GROUP_CHAT) {
                emitter.onNext(it.getResponse(SomeOneLeftGroupPayload::class.java))
            }
        }.share()
    }

    private val someOneCreatedNewGroupWithYouEmitter by lazy {
        Observable.create<NewChatGroupCreatedResponse> { emitter ->
            appSocket.on(SocketService.EVENT_NEW_GROUP_CREATED) {
                emitter.onNext(it.getResponse(NewChatGroupCreatedResponse::class.java))
            }
        }.share()
    }

    private val deleteMessageEmitter by lazy {
        Observable.create<SomeOneLeftGroupPayload> { emitter ->
            appSocket.on(SocketService.EVENT_DELETED_MESSAGE) {
                emitter.onNext(it.getResponse(SomeOneLeftGroupPayload::class.java))
            }
        }.share()
    }

    private val notificationsDataEmitter by lazy {
        Observable.create<NotificationPayload> { emitter ->
            appSocket.on(SocketService.EVENT_NOTIFICATIONS) {
                emitter.onNext(it.getResponse(NotificationPayload::class.java))
            }
        }.share()
    }

    private val notificationDataEmitter by lazy {
        Observable.create<NotificationData> { emitter ->
            appSocket.on(SocketService.EVENT_NOTIFICATION) {
                Timber.tag(TAG).e("D: ${it[0]}")
                emitter.onNext(it.getResponse(NotificationData::class.java))
            }
        }.share()
    }

    private fun observePostUpdateEvents(eventName: String): Observable<AddNewPostNotification> {
        return Observable.create<AddNewPostNotification> { emitter ->
            appSocket.on(eventName) {
                Timber.tag(TAG).e("J: ${it[0]}")
                emitter.onNext(it.getResponse(AddNewPostNotification::class.java))
            }
        }.share()
    }

    private fun String.toJsonObject() = JSONObject(this)

    private fun <T> Array<Any>.getResponse(clazz: Class<T>): T {
        return appSocket.getGson().fromJson(this[0].toString(), clazz)
    }

    override fun connect() = appSocket.connect()
    override fun connectionEmitter(): Observable<Unit> = connectionEmitter
    override fun connectionError(): Observable<Unit> = connectionErrorEmitter
    override fun disconnect() = appSocket.disconnect()
    override fun off() = appSocket.off()
    override fun disconnectEmitter(): Observable<Unit> = disconnectEmitter

    override fun someoneLeftGroup(): Observable<SomeOneLeftGroupPayload> = someoneLeftGroupEmitter
    override fun updateMessage(): Observable<MessageGroupPayload> = updateMessageEmitter
    override fun messageNotification(): Observable<MessageGroupPayload> = messageEmitter
    override fun someoneJoinedChatGroup(): Observable<SomeOneLeftGroupPayload> = someoneJoinedChatGroupEmitter
    override fun someOneCreatedNewGroupWithYou(): Observable<NewChatGroupCreatedResponse> = someOneCreatedNewGroupWithYouEmitter
    override fun deleteMessage(): Observable<SomeOneLeftGroupPayload> = deleteMessageEmitter

    override fun notificationsData(): Observable<NotificationPayload> = notificationsDataEmitter
    override fun notificationData(): Observable<NotificationData> = notificationDataEmitter

    override fun observePostsUpdates(eventName: String): Observable<AddNewPostNotification> = observePostUpdateEvents(eventName)

  /*  override fun newWallPostData(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_NEW_WALL_POST)
    override fun deleteWallPostData(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_DELETE_WALL_POST)
    override fun updateWallPostData(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_UPDATE_WALL_POST)
    override fun newComment(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_NEW_POST_COMMENT)
    override fun deleteComment(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_DELETE_POST_COMMENT)
    override fun updateComment(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_UPDATE_POST_COMMENT)
    override fun likePostData(): Observable<AddNewPostNotification> = observePostUpdateEvents(SocketService.EVENT_UPDATE_POST_LIKE)
*/
    override fun createChatGroup(createChatGroupRequest: CreateChatGroupRequest): Observable<CreateChatGroupResponse> {
        return Observable.create<CreateChatGroupResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_CREATE_CHAT_GROUP,
                appSocket.getGson().toJson(createChatGroupRequest).toJsonObject(),
                emitter,
                CreateChatGroupResponse::class.java
            )
        }.share()
    }

    override fun deleteChatGroup(deleteChatGroupRequest: DeleteChatGroupRequest): Observable<DeleteChatGroupResponse> {
        return Observable.create<DeleteChatGroupResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_DELETE_CHAT_GROUP,
                appSocket.getGson().toJson(deleteChatGroupRequest).toJsonObject(),
                emitter,
                DeleteChatGroupResponse::class.java
            )
        }.share()
    }

    override fun updateChatGroup(updateChatGroupRequest: UpdateChatGroupRequest): Observable<CreateChatGroupResponse> {
        return Observable.create<CreateChatGroupResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_UPDATE_CHAT_GROUP,
                appSocket.getGson().toJson(updateChatGroupRequest).toJsonObject(),
                emitter,
                CreateChatGroupResponse::class.java
            )
        }.share()
    }

    override fun getPublicChatGroupList(getPublicChatGroupListRequest: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return Observable.create<GetPublicChatGroupListResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_PUBLIC_CHAT_GROUP_LIST,
                appSocket.getGson().toJson(getPublicChatGroupListRequest).toJsonObject(),
                emitter,
                GetPublicChatGroupListResponse::class.java
            )
        }.share()
    }

    override fun joinChatGroup(joinChatGroupRequest: JoinChatGroupRequest): Observable<CreateChatGroupResponse> {
        return Observable.create<CreateChatGroupResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_JOIN_CHAT_GROUP,
                appSocket.getGson().toJson(joinChatGroupRequest).toJsonObject(),
                emitter,
                CreateChatGroupResponse::class.java
            )
        }.share()
    }

    override fun leaveChatGroup(leaveChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return Observable.create<FansChatCommonMessage> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_LEAVE_CHAT_GROUP,
                appSocket.getGson().toJson(leaveChatGroupRequest).toJsonObject(),
                emitter,
                FansChatCommonMessage::class.java
            )
        }.share()
    }

    override fun getOfficialChatGroup(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return Observable.create<GetPublicChatGroupListResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_ALL_OFFICIAL_CHAT_GROUP_LIST,
                appSocket.getGson().toJson(getOfficialChatGroup).toJsonObject(),
                emitter,
                GetPublicChatGroupListResponse::class.java
            )
        }.share()
    }

    override fun getGlobalChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return Observable.create<GetPublicChatGroupListResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_GET_GLOBAL_CHAT_GROUP,
                appSocket.getGson().toJson(getOfficialChatGroup).toJsonObject(),
                emitter,
                GetPublicChatGroupListResponse::class.java
            )
        }.share()
    }

    override fun getUserChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return Observable.create<GetPublicChatGroupListResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_GET_USER_CHAT_GROUP,
                appSocket.getGson().toJson(getOfficialChatGroup).toJsonObject(),
                emitter,
                GetPublicChatGroupListResponse::class.java
            )
        }.share()
    }

    override fun getPublicChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return Observable.create<GetPublicChatGroupListResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_GET_PUBLIC_CHAT_GROUP_LIST_FOR_USER,
                appSocket.getGson().toJson(getOfficialChatGroup).toJsonObject(),
                emitter,
                GetPublicChatGroupListResponse::class.java
            )
        }.share()
    }

    override fun muteChat(muteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return Observable.create<FansChatCommonMessage> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_MUTE_CHAT,
                appSocket.getGson().toJson(muteChatGroupRequest).toJsonObject(),
                emitter,
                FansChatCommonMessage::class.java
            )
        }.share()
    }

    override fun unMuteChat(unMuteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return Observable.create<FansChatCommonMessage> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_UNMUTE_CHAT,
                appSocket.getGson().toJson(unMuteChatGroupRequest).toJsonObject(),
                emitter,
                FansChatCommonMessage::class.java
            )
        }.share()
    }

    override fun sendMessage(sendMessageRequest: SendMessageRequest): Observable<SendMessage> {
        return Observable.create<SendMessage> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_SEND_MESSAGE,
                appSocket.getGson().toJson(sendMessageRequest).toJsonObject(),
                emitter,
                SendMessage::class.java
            )
        }.share()
    }

    override fun deleteMessage(deleteMessage: DeleteChatMessageRequest): Observable<FansChatCommonMessage> {
        return Observable.create<FansChatCommonMessage> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_MESSAGE_DELETE,
                appSocket.getGson().toJson(deleteMessage).toJsonObject(),
                emitter,
                FansChatCommonMessage::class.java
            )
        }.share()
    }

    override fun getChatGroupMessages(getChatGroupRequest: JoinChatGroupRequest): Observable<GetGroupUserChatMessagesResponse> {
        return Observable.create<GetGroupUserChatMessagesResponse> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_GET_CHAT_GROUP_MESSAGES,
                appSocket.getGson().toJson(getChatGroupRequest).toJsonObject(),
                emitter,
                GetGroupUserChatMessagesResponse::class.java
            )
        }.share()
    }

    override fun updateMessage(updateChatMessageRequest: UpdateChatMessageRequest): Observable<GroupMessages> {
        return Observable.create<GroupMessages> { emitter ->
            appSocket.requestWithAck(
                SocketService.EVENT_MESSAGE_UPDATE,
                appSocket.getGson().toJson(updateChatMessageRequest).toJsonObject(),
                emitter,
                GroupMessages::class.java
            )
        }.share()
    }
}
