package base.data.api.chat

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.chat.model.*
import base.data.cache.Cache
import base.socket.SocketDataManager
import base.socket.model.*
import io.reactivex.Observable
import io.reactivex.Single

class ChatRepository(private val socketDataManager: SocketDataManager) {

    fun createChatGroup(createChatGroupRequest: CreateChatGroupRequest): Observable<CreateChatGroupResponse> {
        return socketDataManager.createChatGroup(createChatGroupRequest)
            .map { it }
    }

    fun deleteChatGroup(deleteChatGroupRequest: DeleteChatGroupRequest): Observable<DeleteChatGroupResponse> {
        return socketDataManager.deleteChatGroup(deleteChatGroupRequest)
            .map { it }
    }

    fun updateChatGroup(updateChatGroupRequest: UpdateChatGroupRequest): Observable<CreateChatGroupResponse> {
        return socketDataManager.updateChatGroup(updateChatGroupRequest)
            .map { it }
    }

    fun getPublicChatGroupList(getPublicChatGroupListRequest: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return socketDataManager.getPublicChatGroupList(getPublicChatGroupListRequest)
            .map { it }
    }

    fun joinChatGroup(joinChatGroupRequest: JoinChatGroupRequest): Observable<CreateChatGroupResponse> {
        return socketDataManager.joinChatGroup(joinChatGroupRequest)
            .map { it }
    }

    fun leaveChatGroup(leaveChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return socketDataManager.leaveChatGroup(leaveChatGroupRequest)
            .map { it }
    }

    fun getOfficialChatGroup(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return socketDataManager.getOfficialChatGroup(getOfficialChatGroup)
            .map { it }
    }

    fun getGlobalChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return socketDataManager.getGlobalChatGroupsList(getOfficialChatGroup)
            .map { it }
    }

    fun getUserChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<List<CreateChatGroupResponse>> {
        return socketDataManager.getUserChatGroupsList(getOfficialChatGroup).map { it.data ?: listOf() }
    }

    fun getPublicChatGroupsList(getOfficialChatGroup: GetWallRequest): Observable<GetPublicChatGroupListResponse> {
        return socketDataManager.getPublicChatGroupsList(getOfficialChatGroup)
            .map { it }
    }

    fun muteChat(muteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return socketDataManager.muteChat(muteChatGroupRequest)
            .map { it }
    }

    fun unMuteChat(unMuteChatGroupRequest: JoinChatGroupRequest): Observable<FansChatCommonMessage> {
        return socketDataManager.unMuteChat(unMuteChatGroupRequest)
            .map { it }
    }

    fun sendMessage(sendMessageRequest: SendMessageRequest): Observable<SendMessage> {
        return socketDataManager.sendMessage(sendMessageRequest)
            .map { it }
    }

    fun deleteMessage(deleteMessage: DeleteChatMessageRequest): Observable<FansChatCommonMessage> {
        return socketDataManager.deleteMessage(deleteMessage)
            .map { it }
    }

    fun getChatGroupMessages(getChatGroupRequest: JoinChatGroupRequest): Observable<GetGroupUserChatMessagesResponse> {
        return socketDataManager.getChatGroupMessages(getChatGroupRequest)
            .map { it }
    }

    fun updateMessage(updateChatMessageRequest: UpdateChatMessageRequest): Observable<GroupMessages> {
        return socketDataManager.updateMessage(updateChatMessageRequest)
            .map { it }
    }
}


class ChatCacheRepository {

    fun getListOfChatMessages(): Single<List<CreateChatGroupResponse>> {
        return Cache.cache.daoConversationList().getAll().map { it }
    }


    fun addInsertOrUpdateChatGroup(listContent: List<CreateChatGroupResponse>) {
        //Insert or Update as table may have same data
        Cache.cache.daoConversationList().insertOrUpdate(listContent)
    }

    fun deleteGroup(groupId: String) {
        Cache.cache.daoConversationList().deleteGroup(groupId)
    }

    fun deleteAllGroup() {
        Cache.cache.daoConversationList().deleteAll()
    }


    fun addChatMessage(item: CreateChatGroupResponse) {
//        Cache.cache.daoConversationList().insert(item)
    }

    fun updateChatMessage(item: CreateChatGroupResponse) {
        Cache.cache.daoConversationList().update(item)
    }

    fun deleteChatMessage(item: CreateChatGroupResponse) {
        Cache.cache.daoConversationList().delete(item)
    }

    fun getListOfGroupMessages(groupId: String): List<GroupMessages> {
        return Cache.cache.daoConversation().getAllByOrder(groupId)
    }

    fun addNewMessages(message: List<GroupMessages>) {
        Cache.cache.daoConversation().insertOrUpdate(message)
    }

    fun deleteMessages() {
        Cache.cache.daoConversation().deleteAll()
    }

    fun deleteMessage(id: String) {
        Cache.cache.daoConversation().deleteMsgById(id)
    }

}