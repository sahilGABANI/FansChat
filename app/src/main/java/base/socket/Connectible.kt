package base.socket

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.chat.model.*
import base.socket.model.*
import io.reactivex.Observable

interface Connectible {
    val isConnected: Boolean
    fun connect()
    fun connectionEmitter(): Observable<Unit>
    fun connectionError(): Observable<Unit>
    fun disconnect()
    fun off()
    fun disconnectEmitter(): Observable<Unit>

    fun createChatGroup(createChatGroupRequest: CreateChatGroupRequest): Observable<CreateChatGroupResponse>
    fun deleteChatGroup(deleteChatGroupRequest: DeleteChatGroupRequest): Observable<DeleteChatGroupResponse>
    fun updateChatGroup(updateChatGroupRequest: UpdateChatGroupRequest): Observable<CreateChatGroupResponse>
    fun getPublicChatGroupList(getPublicChatGroupListRequest: GetWallRequest): Observable<GetPublicChatGroupListResponse>
    fun joinChatGroup(joinChatGroupRequest: JoinChatGroupRequest): Observable<CreateChatGroupResponse>
    fun leaveChatGroup(leaveChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage>
    fun getOfficialChatGroup(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse>
    fun getGlobalChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse>
    fun getUserChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse>
    fun getPublicChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse>
    fun muteChat(muteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage>
    fun unMuteChat(unMuteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage>
    fun sendMessage(sendMessageRequest: SendMessageRequest): Observable<SendMessage>
    fun deleteMessage(deleteMessage: DeleteChatMessageRequest): Observable<FansChatCommonMessage>
    fun getChatGroupMessages(getChatGroupRequest: JoinChatGroupRequest): Observable<GetGroupUserChatMessagesResponse>
    fun updateMessage(updateChatMessageRequest: UpdateChatMessageRequest): Observable<GroupMessages>

    fun someoneLeftGroup(): Observable<SomeOneLeftGroupPayload>
    fun updateMessage(): Observable<MessageGroupPayload>
    fun messageNotification(): Observable<MessageGroupPayload>
    fun someoneJoinedChatGroup(): Observable<SomeOneLeftGroupPayload>
    fun someOneCreatedNewGroupWithYou(): Observable<NewChatGroupCreatedResponse>
    fun deleteMessage(): Observable<SomeOneLeftGroupPayload>

    fun notificationsData(): Observable<NotificationPayload>
    fun notificationData(): Observable<NotificationData>

    fun observePostsUpdates(eventName: String): Observable<AddNewPostNotification>
   /* fun newWallPostData(): Observable<AddNewPostNotification>
    fun deleteWallPostData(): Observable<AddNewPostNotification>
    fun updateWallPostData(): Observable<AddNewPostNotification>
    fun newComment(): Observable<AddNewPostNotification>
    fun deleteComment(): Observable<AddNewPostNotification>
    fun updateComment(): Observable<AddNewPostNotification>
    fun likePostData(): Observable<AddNewPostNotification>*/
}