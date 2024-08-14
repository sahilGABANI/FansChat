package base.ui.fragment.other.chat.viewmodel

import base.data.api.chat.ChatCacheRepository
import base.data.api.chat.ChatRepository
import base.data.api.chat.model.*
import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.friends.FriendsRepository
import base.data.api.users.model.FansChatUserDetails
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.socket.model.*
import base.ui.base.BaseViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val friendsRepository: FriendsRepository,
    private val chatCacheRepository: ChatCacheRepository,
) : BaseViewModel() {

    private val createPostStateSubject: PublishSubject<ChatViewState> = PublishSubject.create()
    val createPostState: Observable<ChatViewState> = createPostStateSubject.hide()

    private var pageNumberGroup: Int = 1
    private var isLoadMoreGroup: Boolean = true
    private var isLoadingGroup: Boolean = false

    private var globalpageNumberGroup: Int = 1
    private var globalisLoadMoreGroup: Boolean = true
    private var globalisLoadingGroup: Boolean = false

    var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false

    private var jpageNumberGroup: Int = 1
    private var jisLoadMoreGroup: Boolean = true
    private var jisLoadingGroup: Boolean = false

    private val PER_PAGE = 20
    private val PER_PAGE_FIFTY = 50

    fun deleteCacheGroups() {
        chatCacheRepository.deleteAllGroup()
        chatCacheRepository.deleteMessages()
    }

    fun createChatGroup(createChatGroupRequest: CreateChatGroupRequest) {
        chatRepository.createChatGroup(createChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { chatGroup ->
                    chatCacheRepository.addInsertOrUpdateChatGroup(listOf(chatGroup))
                    createPostStateSubject.onNext(ChatViewState.CreateChatGroup(chatGroup))
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun removeChatGroup(groupId: String) {
        chatCacheRepository.deleteGroup(groupId)
    }

    fun deleteChatGroup(deleteChatGroupRequest: DeleteChatGroupRequest) {
        chatRepository.deleteChatGroup(deleteChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it.message?.let { message ->
                    deleteChatGroupRequest.id?.let { it1 -> chatCacheRepository.deleteGroup(it1) }
                    createPostStateSubject.onNext(ChatViewState.SuccessMessage(message))
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(ChatViewState.ErrorMessage(it.localizedMessage ?: ""))
            }).autoDispose()
    }

    fun updateChatGroup(updateChatGroupRequest: UpdateChatGroupRequest) {
        chatRepository.updateChatGroup(updateChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { chatGroup ->
                    createPostStateSubject.onNext(ChatViewState.UpdateChatGroup(chatGroup))
                    chatCacheRepository.addInsertOrUpdateChatGroup(listOf(chatGroup))
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun getPublicChatGroup() {
        chatRepository.getPublicChatGroupList(
            GetWallRequest(
                page = jpageNumberGroup,
                PER_PAGE_FIFTY
            )
        )
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                jisLoadingGroup = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                jisLoadingGroup = false

                it?.let { getPublicChatGroupListResponse ->

                    if (getPublicChatGroupListResponse.data?.size ?: 0 < PER_PAGE_FIFTY) {
                        jisLoadMoreGroup = false
                    }

                    createPostStateSubject.onNext(
                        ChatViewState.GetPublicGroupsList(getPublicChatGroupListResponse)
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }


    fun joinChatGroup(joinChatGroupRequest: JoinChatGroupRequest) {
        chatRepository.joinChatGroup(joinChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { joinChatGroup ->
                    createPostStateSubject.onNext(
                        ChatViewState.JoinChatGroup(joinChatGroup)
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun leaveChatGroup(leaveChatGroupRequest: JoinChatGroupRequest) {
        chatRepository.leaveChatGroup(leaveChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                leaveChatGroupRequest.groupId?.let { groupId -> chatCacheRepository.deleteGroup(groupId) }
                it.message?.let { message ->
                    createPostStateSubject.onNext(ChatViewState.LeaveGroupMessage(message))
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(ChatViewState.ErrorMessage(it.localizedMessage ?: ""))
            }).autoDispose()
    }

    fun getAllOfficialChatGroups() {
        chatRepository.getOfficialChatGroup(GetWallRequest(globalpageNumberGroup, PER_PAGE))
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { groupList ->
                    if (groupList.data?.size ?: 0 < PER_PAGE) {
                        isLoadMoreGroup = false
                    }

                    groupList.data?.let {
                        chatCacheRepository.addInsertOrUpdateChatGroup(it)
                    }
                    getUserOfficialCacheChatGroups()
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }


    fun getGlobalOfficialChatGroups() {
        chatRepository.getGlobalChatGroupsList(GetWallRequest(globalpageNumberGroup, PER_PAGE))
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { groupList ->

                    if (groupList.data?.size ?: 0 < PER_PAGE) {
                        isLoadMoreGroup = false
                    }

                    groupList.data?.let {
                        chatCacheRepository.addInsertOrUpdateChatGroup(it)
                    }
                    getUserOfficialCacheChatGroups()

//                    createPostStateSubject.onNext(
//                        ChatViewState.GetGlobalOfficialGroupsList(groupList)
//                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun getUserOfficialChatGroups() {
        chatRepository.getUserChatGroupsList(GetWallRequest(pageNumberGroup, PER_PAGE))
            .subscribeOnIoAndObserveOnMainThread({ groupList ->
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                if (groupList.isEmpty()) {
                    isLoadMoreGroup = false
                }
                try {
                    if (!groupList.isNullOrEmpty()) {
                        chatCacheRepository.deleteAllGroup()
                        chatCacheRepository.addInsertOrUpdateChatGroup(groupList)
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
                getUserOfficialCacheChatGroups()
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun getUserOfficialCacheChatGroups() {
        chatCacheRepository.getListOfChatMessages()
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                createPostStateSubject.onNext(ChatViewState.ChatGroupList(it))
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(ChatViewState.ErrorMessage(it.localizedMessage ?: ""))
            }).autoDispose()
    }

    fun getPublicChatGroups(getOfficialChatGroup: GetWallRequest) {
        chatRepository.getPublicChatGroupsList(getOfficialChatGroup)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { groupList ->
                    createPostStateSubject.onNext(
                        ChatViewState.GetPublicGroupssList(groupList)
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun muteChat(muteChatGroupRequest: JoinChatGroupRequest) {
        chatRepository.muteChat(muteChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it.message?.let { mute ->
                    createPostStateSubject.onNext(
                        ChatViewState.MuteChatMessage(
                            mute
                        )
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(ChatViewState.ErrorMessage(it.localizedMessage ?: ""))
            }).autoDispose()
    }

    fun unmuteChat(unMuteChatGroupRequest: JoinChatGroupRequest) {
        chatRepository.unMuteChat(unMuteChatGroupRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it.message?.let { unmute ->
                    createPostStateSubject.onNext(
                        ChatViewState.UnmuteChatMessage(unmute)

                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun sendMessage(sendMessageRequest: SendMessageRequest) {
        chatRepository.sendMessage(sendMessageRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(false))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                if (it.success == true) {
                    it?.let { sendMessage ->
                        createPostStateSubject.onNext(ChatViewState.SendChatMessage(sendMessage))
                        chatCacheRepository.addNewMessages(
                            listOf(
                                GroupMessages(
                                    true,
                                    message = sendMessage.message.toString(),
                                    id = sendMessage.id.toString(),
                                    groupId = sendMessage.groupId,
                                    sender = sendMessage.sender,
                                    created = sendMessage.created,
                                    updated = sendMessage.updated,
                                    uploadStatus = sendMessage.uploadStatus,
                                    imageAspectRadio = sendMessage.imageAspectRadio,
                                    type = sendMessage.type,
                                    url = sendMessage.url,
                                    thumbnailUrl = sendMessage.thumbnailUrl
                                )
                            )
                        )
                    }
                } else {
                    it.errors?.let { error ->
                        ChatViewState.ErrorMessages(error[0].message.toString())
                    }
                }


            }, {
                Timber.e(it)
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
                createPostStateSubject.onNext(ChatViewState.ErrorMessage(it.localizedMessage ?: ""))
            }).autoDispose()
    }

    fun deleteMessage(deleteMessage: DeleteChatMessageRequest) {
        chatRepository.deleteMessage(deleteMessage)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it.message?.let { sendMessage ->
                    createPostStateSubject.onNext(
                        ChatViewState.DeleteChatMessage(sendMessage)
                    )

                    deleteMessage.id?.let { it1 -> chatCacheRepository.deleteMessage(it1) }
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun getCacheChatMessages(groupId: String) {
        chatCacheRepository.getListOfGroupMessages(groupId)
            .apply {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                let { messages ->
                    createPostStateSubject.onNext(ChatViewState.GetChatGroupMessages(messages as ArrayList<GroupMessages>))
                }
            }
    }

    private fun getChatGroupMessages(groupId: String, isLoadMoreCall: Boolean) {

        chatRepository.getChatGroupMessages(JoinChatGroupRequest(groupId, pageNumber, PER_PAGE))
            .doOnSubscribe {
                if (!isLoadMoreCall) {
//                    createPostStateSubject.onNext(ChatViewState.LoadingState(true))
                }
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                isLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                isLoading = false

                if (it.data?.size ?: 0 == 0) {
                    isLoadMore = false
                }
                it.data?.let { messages ->
//                    createPostStateSubject.onNext(
//                        ChatViewState.GetChatGroupMessages(messages)
//                    )

                    chatCacheRepository.addNewMessages(messages)
                    getCacheChatMessages(groupId)
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun updateMessage(updateChatMessageRequest: UpdateChatMessageRequest) {
        chatRepository.updateMessage(updateChatMessageRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(false))
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { sendMessage ->
                    createPostStateSubject.onNext(
                        ChatViewState.UpdateMessage(sendMessage)
                    )
                    chatCacheRepository.addNewMessages(listOf(sendMessage))
                }
            }, {
                createPostStateSubject.onNext(ChatViewState.ChatMessageSendButton(true))
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun getFriendList(getFriendRequestRequest: GetFriendRequestRequest) {
        friendsRepository.getFriendList(getFriendRequestRequest)
            .doOnSubscribe {
                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { listOfFriends ->
                    createPostStateSubject.onNext(
                        ChatViewState.GetListOfFriends(listOfFriends)
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }

    fun loadMoreGroup() {
        if (!isLoadingGroup) {
            isLoadingGroup = true
            if (isLoadMoreGroup) {
                pageNumberGroup++
//                getUserOfficialCacheChatGroups()
            }
        }
    }

    fun globalloadMoreGroup() {
        if (!globalisLoadingGroup) {
            globalisLoadingGroup = true
            if (globalisLoadMoreGroup) {
                globalpageNumberGroup++
//                getUserOfficialCacheChatGroups()
            }
        }
    }

    fun resetLoadingGroup() {
        globalpageNumberGroup = 1
        globalisLoadMoreGroup = true
        globalisLoadingGroup = false
    }


    fun loadMoreJoin() {
        if (!jisLoadingGroup) {
            jisLoadingGroup = true
            if (jisLoadMoreGroup) {
                jpageNumberGroup++
                getPublicChatGroup()
            }
        }
    }

    fun resetLoadingJoin() {
        jpageNumberGroup = 1
        jisLoadMoreGroup = true
        jisLoadingGroup = false
    }

    fun loadMore(groupId: String) {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                getChatGroupMessages(groupId, isLoadMore)
            }
        }
    }

    fun resetLoading() {
        pageNumberGroup = 1
        isLoadMoreGroup = true
        isLoadingGroup = false
//        getUserOfficialCacheChatGroups()
    }

    fun resetLoadingMessage(groupId: String) {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
        getChatGroupMessages(groupId, false)
    }

    fun getTranslateMessage(postId: String, targetLanguage: String) {
        friendsRepository.getTranslateMessage(postId, targetLanguage)
            .doOnSubscribe {
//                createPostStateSubject.onNext(ChatViewState.LoadingState(true))
            }
            .doAfterTerminate {
//                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                createPostStateSubject.onNext(ChatViewState.LoadingState(false))
                it?.let { listOfFriends ->
                    createPostStateSubject.onNext(
                        ChatViewState.TranslateMessage(it, postId)
                    )
                }
            }, {
                Timber.e(it)
                createPostStateSubject.onNext(
                    ChatViewState.ErrorMessage(
                        it.localizedMessage ?: ""
                    )
                )
            }).autoDispose()
    }
}

sealed class ChatViewState {
    data class ErrorMessages(val errorMessage: String) : ChatViewState()
    data class ErrorMessage(val errorMessage: String) : ChatViewState()
    data class SuccessMessage(val successMessage: String) : ChatViewState()
    data class LeaveGroupMessage(val successMessage: String) : ChatViewState()
    data class MuteChatMessage(val successMessage: String) : ChatViewState()
    data class UnmuteChatMessage(val successMessage: String) : ChatViewState()
    data class SendChatMessage(val groupMessages: SendMessage) : ChatViewState()
    data class DeleteChatMessage(val successMessage: String) : ChatViewState()
    data class TranslateMessage(
        val translateMessage: base.data.api.friends.model.TranslateMessage,
        val messageId: String
    ) : ChatViewState()

    data class CreateChatGroup(val createChatGroupResponse: CreateChatGroupResponse) :
        ChatViewState()

    data class UpdateChatGroup(val createChatGroupResponse: CreateChatGroupResponse) :
        ChatViewState()

    data class JoinChatGroup(val createChatGroupResponse: CreateChatGroupResponse) : ChatViewState()
    data class GetPublicGroupsList(val getPublicChatGroupListResponse: GetPublicChatGroupListResponse) :
        ChatViewState()

    data class GetAllOfficialGroupsList(val getPublicChatGroupListResponse: GetPublicChatGroupListResponse) :
        ChatViewState()

    data class GetGlobalOfficialGroupsList(val getPublicChatGroupListResponse: GetPublicChatGroupListResponse) :
        ChatViewState()

    data class GetUserOfficialGroupsList(val getPublicChatGroupListResponse: GetPublicChatGroupListResponse) :
        ChatViewState()

    data class ChatGroupList(val listOfChatGroup: List<CreateChatGroupResponse>) : ChatViewState()

    data class GetPublicGroupssList(val getPublicChatGroupListResponse: GetPublicChatGroupListResponse) :
        ChatViewState()

    data class GetChatGroupMessages(val groupMessages: ArrayList<GroupMessages>) : ChatViewState()
    data class UpdateMessage(val groupMessages: GroupMessages) : ChatViewState()
    data class GetListOfFriends(val listOfUser: List<FansChatUserDetails>) : ChatViewState()
    data class LoadingState(val isLoading: Boolean) : ChatViewState()
    data class ChatMessageSendButton(val isEnable: Boolean) : ChatViewState()
}
