package base.ui.fragment.main

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.chat.ChatCacheRepository
import base.data.api.chat.model.CreateChatGroupRequest
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.chat.model.DeleteChatMessageRequest
import base.data.api.chat.model.JoinChatGroupRequest
import base.data.api.file_upload.UploadFile
import base.data.api.users.model.FansChatUserDetails
import base.data.cache.Cache.Companion.cache
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.ChatFooterBinding
import base.databinding.ChatHeaderBinding
import base.databinding.ChatLoginPromptBinding
import base.databinding.FragmentChatBinding
import base.extension.*
import base.popup.Tooltip
import base.popup.TooltipAnimation
import base.socket.SocketDataManager
import base.socket.SocketService
import base.socket.model.GroupMessages
import base.socket.model.Owner
import base.socket.model.SendMessageRequest
import base.socket.model.UpdateChatMessageRequest
import base.ui.MainActivity
import base.ui.adapter.other.ConversationAdapter
import base.ui.adapter.other.MessagesAdapter
import base.ui.base.BaseFragment
import base.ui.fragment.other.chat.viewmodel.ChatViewModel
import base.ui.fragment.other.chat.viewmodel.ChatViewState
import base.util.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.iceteck.silicompressorr.SiliCompressor
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class ChatFragment : BaseFragment() {
    private var customTooltip: Tooltip? = null
    private var listOfMessage = arrayListOf<GroupMessages>()
    private var listChats = mutableListOf<CreateChatGroupResponse>()
    private lateinit var msgAdapter: MessagesAdapter
    lateinit var chatAdapter: ConversationAdapter
    private var isLastPage = false
    private var isLoading = false
    private var currentOpenChat: CreateChatGroupResponse? = null
    private var clickedMsg: GroupMessages? = null
    private var isFromNotification: Boolean = false
    private var isFromFriendDetail: Boolean =
        false //open chat with user or redirect to create chat with that user
    private var chatId: String? = null
    private var groupId: String? = null
    private var friendUser: FansChatUserDetails? = null

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var _chatHeaderBinding: ChatHeaderBinding? = null
    private val chatHeaderBinding get() = _chatHeaderBinding!!

    private var _chatFooterBinding: ChatFooterBinding? = null
    private val chatFooterBinding get() = _chatFooterBinding!!

    private var _chatLoginPromptBinding: ChatLoginPromptBinding? = null
    private val chatLoginPromptBinding get() = _chatLoginPromptBinding!!

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var chatCacheRepository: ChatCacheRepository

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ChatViewModel>
    private lateinit var chatViewModel: ChatViewModel

    private var editMessage: GroupMessages? = null

    private var uploadingMessage: GroupMessages? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "chat.notifications"
    private val description = "Chat Notification"

    private val chatCompositeDisposable: CompositeDisposable = CompositeDisposable()

    private var colorMDGrey600 = 0
    private var colorBlack = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        chatViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        _chatHeaderBinding = ChatHeaderBinding.bind(binding.root)
        _chatFooterBinding = ChatFooterBinding.bind(binding.root)
        _chatLoginPromptBinding = ChatLoginPromptBinding.bind(binding.root)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, state: Bundle?) {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        chatLoginPromptBinding.background.src = R.drawable.background
        notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        chatId = arguments?.getString("chatId")
        friendUser = arguments?.getSerializable("friendUser") as FansChatUserDetails?
        isFromNotification = arguments?.getBoolean("isFromNotification") ?: false
        isFromFriendDetail = arguments?.getBoolean("isFromFriendDetail") ?: false
        if (::msgAdapter.isLateinit) {
            listOfMessage.clear()
        }
        if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            askToLogin()
        }

        colorMDGrey600 = ContextCompat.getColor(requireContext(), R.color.md_grey600)
        colorBlack = ContextCompat.getColor(requireContext(), R.color.black)

        listenToViewModel()
        listenToViewEvents()
    }

    override fun onResume() {
        super.onResume()
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            Timber.e("Resume")
            disposeMessageNotificationDisposable()
            chatViewModel.getUserOfficialCacheChatGroups()
            socketDataManager.messageNotification().subscribeOnIoAndObserveOnMainThread({
                when {
                    it.event.equals(SocketService.EVENT_UPDATE_CHAT_GROUP) -> {
                        chatViewModel.getUserOfficialChatGroups()
                    }
                    it.event.equals(SocketService.EVENT_DELETE_CHAT_GROUP) -> {
                        //chatViewModel.resetLoading()
                        chatViewModel.getUserOfficialChatGroups()
                    }
                    groupId != null -> {
                        it.safeMessage?.let { message ->
                            if (groupId == message.groupId) {
                                val sender = Owner(
                                    firstName = message.sender?.firstName,
                                    lastName = message.sender?.lastName,
                                    displayName = message.sender?.displayName,
                                    created = message.sender?.created,
                                    id = message.sender?.id,
                                    isAdmin = message.sender?.isAdmin,
                                    updated = message.sender?.updated,
                                    avatarUrl = message.sender?.avatarUrl,
                                    email = message.sender?.email
                                )
                                val groupMessage = GroupMessages(
                                    true,
                                    id = message.id.toString(),
                                    sender = sender,
                                    groupId = message.groupId,
                                    message = message.message,
                                    type = message.type,
                                    uploadStatus = message.uploadStatus,
                                    url = message.url,
                                    thumbnailUrl = message.thumbnailUrl,
                                    created = message.created,
                                    updated = message.updated,
                                    imageAspectRadio = message.imageAspectRadio
                                )

                                if (message.type.equals("text")) {
                                    addReceivedMessage(groupMessage)
                                } else {
                                    if (message.uploadStatus.equals("uploaded")) addReceivedMessage(
                                        groupMessage
                                    )
                                    else uploadingMessage = groupMessage
                                }
                            } else {
                                chatCacheRepository.addNewMessages(
                                    arrayListOf(
                                        GroupMessages(
                                            id = message.id ?: "",
                                            groupId = message.groupId,
                                            message = message.message,
                                            type = message.type,
                                            uploadStatus = message.uploadStatus,
                                            url = message.url,
                                            thumbnailUrl = message.thumbnailUrl,
                                            created = message.created,
                                            updated = message.updated,
                                            imageAspectRadio = message.imageAspectRadio
                                        )
                                    )
                                )
                                chatAdapter.notifyDataSetChanged()
                                addNotification(
                                    getString(R.string.message_received),
                                    if (message.type.equals("text")) message.message
                                        ?: "" else message.type.toString(),
                                    it.groupChatId ?: ""
                                )
                            }
                        }
                    }
                }
            }, {
                Timber.e(it)
                FirebaseCrashlytics.getInstance().recordException(it)
            }).addToChatDisposable()
            socketDataManager.updateMessage().subscribeOnIoAndObserveOnMainThread({
                it.safeMessage?.let { message ->
                    if (currentOpenChat?.id == message.groupId) {
                        if (message.id == uploadingMessage?.id) {
                            uploadingMessage?.let { upload ->
                                upload.message =
                                    if (!message.message.isNullOrEmpty()) message.message else upload.message
                                upload.uploadStatus = message.uploadStatus
                                upload.url = message.url
                                upload.thumbnailUrl = message.thumbnailUrl
                                addReceivedMessage(upload)
                                uploadingMessage = null
                            }
                        }
                        if (uploadingMessage == null) {
                            val sender = Owner(
                                firstName = message.sender?.firstName,
                                lastName = message.sender?.lastName,
                                displayName = message.sender?.displayName,
                                created = message.sender?.created,
                                id = message.sender?.id,
                                isAdmin = message.sender?.isAdmin,
                                updated = message.sender?.updated,
                                avatarUrl = message.sender?.avatarUrl,
                                email = message.sender?.email
                            )
                            val groupMessage = GroupMessages(
                                true,
                                id = message.id.toString(),
                                sender = sender,
                                groupId = message.groupId,
                                message = message.message,
                                type = message.type,
                                uploadStatus = message.uploadStatus,
                                url = message.url,
                                thumbnailUrl = message.thumbnailUrl,
                                created = message.created,
                                updated = message.updated,
                                imageAspectRadio = message.imageAspectRadio
                            )
                            updateMessage(groupMessage)
                        }
                    }
                }
            }, {
                Timber.e(it)
                FirebaseCrashlytics.getInstance().recordException(it)
            }).addToChatDisposable()
            socketDataManager.deleteMessage().subscribeOnIoAndObserveOnMainThread({ mes ->
                listOfMessage.filter { it.id == mes.messageId }.apply {
                    if (isNotEmpty()) {
                        val posToDel = listOfMessage.indexOf(first())
                        listOfMessage.removeAt(posToDel)
                        msgAdapter.notifyItemRemoved(posToDel)
                        sortListAndSetAdapter(openChat = false)
                    }
                }
                mes.messageId?.let {
                    chatCacheRepository.deleteMessage(it)
                }
            }, {
                Timber.e(it)
                FirebaseCrashlytics.getInstance().recordException(it)
            }).addToChatDisposable()
            socketDataManager.someOneCreatedNewGroupWithYou().subscribeOnIoAndObserveOnMainThread({
                chatViewModel.getUserOfficialChatGroups()
            }, {
                Timber.e(it)
                FirebaseCrashlytics.getInstance().recordException(it)
            }).addToChatDisposable()
            callGroupRefresh()
        }
    }

    private fun callGroupRefresh() {
        if (socketDataManager.isConnected) {
            chatViewModel.getUserOfficialChatGroups()
        } else {
            Handler().postDelayed({
                callGroupRefresh()
            }, 200)
        }
    }

    fun sortListAndSetAdapter(isReloadChat: Boolean = false, openChat: Boolean) {
        Collections.sort(listChats, object : Comparator<CreateChatGroupResponse> {
            override fun compare(
                c1: CreateChatGroupResponse,
                c2: CreateChatGroupResponse,
            ): Int {
                val listMsg1 = cache.daoConversation().getAllByOrder(c1.id.toString())
                val listMsg2 = cache.daoConversation().getAllByOrder(c2.id.toString())

                var m1: GroupMessages? = null
                var m2: GroupMessages? = null
                var unr1: GroupMessages? = null
                var unr2: GroupMessages? = null

                if (listMsg1.isNotEmpty()) {
                    m1 = listMsg1.first()
                    unr1 =
                        if (listMsg1.any { it.readFlag == false }) listMsg1.first { it.readFlag == false } else null
                }
                if (listMsg2.isNotEmpty()) {
                    m2 = listMsg2.first()
                    unr2 =
                        if (listMsg2.any { it.readFlag == false }) listMsg2.first { it.readFlag == false } else null
                }
                if (m1 == null && m2 == null) {
                    return c1.name?.compareTo(c2.name.toString())!!
                }
                if (m1 == null && m2 != null) {
                    return 1
                }
                if (m1 != null && m2 == null) {
                    return -1
                }

                if (unr1 == null && unr2 == null) {
                    return m2?.created?.toDate()!!.time.compareTo(m1?.created?.toDate()!!.time)
                }

                if (unr1 == null && unr2 != null) {
                    return 1
                }
                if (unr1 != null && unr2 == null) {
                    return -1
                }

                //! Booth has unread
                if (unr1 != null && unr2 != null) return m2?.created?.toDate()!!.time.compareTo(m1?.created?.toDate()!!.time)

                return -1
            }
        })

        if (isFromFriendDetail) {
            isFromFriendDetail = false
            requireArguments().remove("isFromFriendDetail")
            requireArguments().remove("friendUser")
            var isChatExistWithFriend = false
            var friendChat: CreateChatGroupResponse? = null

            for (chat in listChats) {
                val chatMemberId = chat.members.mapNotNull { it.id }
                if (chat.members.size == 2 && chatMemberId.contains(friendUser!!.id) && chatMemberId.contains(
                        loggedInUserCache.getLoggedInUserId().toString()
                    )
                ) {
                    friendChat = chat
                    isChatExistWithFriend = true
                    break
                } else {
                    isChatExistWithFriend = false
                }
//                if ((!chat.isGlobal && chat.usersIds.contains(friendUser!!.id)) || (friendUser!!.isGlobal == true && chat.usersIds.contains(friendUser!!.id))
//                ) {
//                    friendChat = chat
//                    isChatExistWithFriend = true
//                    break
//                }
            }

            if (isChatExistWithFriend) {
                if (friendChat != null) {
                    currentOpenChat = friendChat
                    updateViewsForSelectedChat()
                }
            } else {
                val user = loggedInUserCache.getUserName()
                friendUser?.let {
                    chatViewModel.createChatGroup(
                        CreateChatGroupRequest(
                            "$user and ${it.displayName}",
                            null,
                            isPublic = false,
                            isOfficial = false,
                            usersIds = arrayListOf(it.id)
                        )
                    )
                }
            }
        }


        chatAdapter.currentOpenChat =
            if (currentOpenChat == null || listChats.none { it.id == currentOpenChat!!.id }) 0
            else listChats.indexOf(listChats.find {
                it.id == currentOpenChat!!.id
            })
        chatAdapter.notifyDataSetChanged()

        if (currentOpenChat != null && listChats.none { it.id == currentOpenChat!!.id }) currentOpenChat =
            null

        if (openChat) {
            //Open first chat as default initially
            if (listChats.isNotEmpty()) {
                when {
                    currentOpenChat == null -> {
                        currentOpenChat = listChats[0]
                    }
                    isReloadChat -> {
                    }
                    else -> {
                        chatFooterBinding.rlFooter.isVisible = !currentOpenChat!!.isGlobal
                        listChats.first { it.id == currentOpenChat!!.id }.let {
                            currentOpenChat = it
                            chatHeaderBinding.chatName.text = ""
                            chatHeaderBinding.chatName.text = getUsersNameInChatText()
                        }
                    }
                }
                updateViewsForSelectedChat()
            } else {
                currentOpenChat = null
            }
        }


        currentOpenChat?.let { it ->
            chatFooterBinding.tvAlert.text = getString(R.string.alerts_on)
            chatFooterBinding.tvAlert.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

            if (it.mutedList.size > 0) {
                it.mutedList.forEach {
                    if (it.equals(loggedInUserCache.getLoggedInUserId().toString(), true)) {
                        chatFooterBinding.tvAlert.text = getString(R.string.alerts_off)
                        chatFooterBinding.tvAlert.backgroundTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.dark_gray
                                )
                            )
                    } else {
                        chatFooterBinding.tvAlert.text = getString(R.string.alerts_on)
                        chatFooterBinding.tvAlert.backgroundTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.black
                                )
                            )
                    }
                }
            }
        }
    }

    private fun updateViewsForSelectedChat() {
        try {
            if (currentOpenChat == null) {
                currentOpenChat = listChats.firstOrNull() ?: return
            }
            isLastPage = false
            isLoading = false
            chatHeaderBinding.chatName.text = getUsersNameInChatText()
            if (currentOpenChat!!.blockUsers.size > 0) {
                view?.let { hideKeyboards(it) }
                chatFooterBinding.attach.isEnabled = false
                chatFooterBinding.input.isEnabled = false
                chatFooterBinding.input.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_lock,
                    0
                )
            } else {
                chatFooterBinding.attach.isEnabled = true
                chatFooterBinding.input.isEnabled = true
                chatFooterBinding.input.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            if (chatFooterBinding.linMedia.visibility == VISIBLE) {
                chatFooterBinding.linMedia.visibility = GONE //Hide media controls if visible
            }
            if (chatFooterBinding.linActions.visibility == VISIBLE) {
                chatFooterBinding.linActions.visibility = GONE
                chatFooterBinding.menu.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            }
            chatFooterBinding.attach.visibility = VISIBLE
            if (currentOpenChat!!.isGlobal) {
                chatFooterBinding?.let { footerbinding ->
                    footerbinding.rlFooter.isVisible = false
                }
            } else {
                chatFooterBinding?.let { footerbinding ->
                    footerbinding.rlFooter.isVisible = true
                    footerbinding.attach.isVisible = true
                    footerbinding.input.isVisible = true
                    footerbinding.tvCreate.isVisible = true
                    footerbinding.tvSearch.isVisible = true
                    footerbinding.tvAlert.isVisible = true
                    footerbinding.ivSendMessage.isVisible = true
                }
            }
            if (chatFooterBinding.rlFooter.visibility != VISIBLE) view?.let { hideKeyboards(it) } //Hide keyboard if input bar is not visible

            //Load from db first
            listOfMessage.clear()
            msgAdapter.notifyDataSetChanged()
            getMessagesFromLocal(currentOpenChat!!)
        } catch (e: Exception) {

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenToViewEvents() {
        //Click events
        chatFooterBinding.menu.onClick {
            showMenuInBottomInput(true)
        }
        chatFooterBinding.tvCreate.onClick {
            open(R.id.chatCreate)
            if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
        }
        chatFooterBinding.tvEdit.onClick {
            if (currentOpenChat != null) open(
                R.id.chatEdit,
                hashMapOf("chatToEdit" to currentOpenChat)
            )
            if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
        }
        chatFooterBinding.tvSearch.onClick {
            open(R.id.chatSearch)
            if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
        }
        chatFooterBinding.tvAlert.onClick {
            currentOpenChat?.apply {
                toggleAlerts(this)
            }
        }
        chatFooterBinding.tvLeave.onClick {
            showCustomDialog(
                getString(R.string.leave_chat),
                getString(R.string.you_will_leave_chat),
                true
            ) {
                currentOpenChat?.let { currentOpenChat ->
                    leaveChat(currentOpenChat)
                }
            }
        }
        chatFooterBinding.tvEditMsg.onClick {
            clickedMsg?.apply {
                if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
                chatFooterBinding.input.setText(message)
                chatFooterBinding.input.setSelection(chatFooterBinding.input.text.toString().length)
                editMessage = clickedMsg
            }
        }
        chatFooterBinding.tvDeleteMsg.onClick {
            clickedMsg?.apply {
                if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
                chatCacheRepository.deleteMessage(id)
                listOfMessage.filter { it.id == id }.apply {
                    if (isNotEmpty()) {
                        val posToDel = listOfMessage.indexOf(first())
                        listOfMessage.removeAt(posToDel)
                        msgAdapter.notifyItemRemoved(posToDel)
                        sortListAndSetAdapter(openChat = false)
                    }
                }
                deleteMessage(this)
                clickedMsg = null
            }
        }
        chatFooterBinding.ibMic.onClick {
            audioCallback = {
                chatFooterBinding.audio.isVisible = true
                chatFooterBinding.attachment.isVisible = false
                chatFooterBinding.attachmentContainer.isVisible = true
            }
            recordAudio()
        }
        chatFooterBinding.ibCamera.onClick {
            takePhotoOrVideo {
                chatFooterBinding.audio.isVisible = false
                chatFooterBinding.attachment.isVisible = true
                chatFooterBinding.attachment.src = selectedFile;
                chatFooterBinding.attachmentContainer.isVisible = true
            }
        }
        chatFooterBinding.ibGallery.onClick {
            selectImageOrVideo {
                chatFooterBinding.audio.isVisible = false
                chatFooterBinding.attachment.isVisible = true
                chatFooterBinding.attachment.src = selectedFile;
                chatFooterBinding.attachmentContainer.isVisible = true
            }
        }
        chatFooterBinding.removeAttachment.onClick {
            chatFooterBinding.attachmentContainer.isVisible = false; selectedFile = null
        }
        chatFooterBinding.input.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && chatFooterBinding.linMedia.visibility == VISIBLE) {
                if (chatFooterBinding.linMedia.visibility == VISIBLE) chatFooterBinding.linMedia.visibility =
                    GONE
                chatFooterBinding.attach.visibility = VISIBLE
            }
            false
        }
        chatFooterBinding.attach.onClick {
            chatFooterBinding.attach.visibility = GONE
            Handler().postDelayed({
                chatFooterBinding.linMedia.visibility = VISIBLE
            }, 200)
        }
        chatFooterBinding.input.onSubmit { text ->
            sendTextMsg(text)
        }
        chatFooterBinding.input.onTextChange { text ->
            if (text.isNullOrEmpty()) {
                chatFooterBinding.ivSendMessage.setColorFilter(colorMDGrey600)
            } else {
                if (text.trim().isEmpty()) {
                    chatFooterBinding.ivSendMessage.setColorFilter(colorMDGrey600)
                } else {
                    chatFooterBinding.ivSendMessage.setColorFilter(colorBlack)
                }
            }
        }
        chatFooterBinding.ivSendMessage.onClick {
            val typedMsg = chatFooterBinding.input.text.toString()
            if (typedMsg.isNotEmpty()) {
                sendTextMsg(typedMsg)
            }
        }
        chatFooterBinding.submit.onClick {

            chatFooterBinding.submit.visibility = View.GONE
            chatFooterBinding.showProgressBar.visibility = View.VISIBLE

            if (editMessage != null) {
                editMessage?.let { editMessage ->
                    editMessage!!.message = chatFooterBinding.input.get()
                    chatViewModel.updateMessage(
                        UpdateChatMessageRequest(
                            editMessage.id,
                            editMessage.message,
                            null,
                            null,
                            "text"
                        )
                    )
                }
            } else {
                if (currentOpenChat != null) {
                    sendMessage(chatFooterBinding.input.get(), currentOpenChat!!)
                }
            }
        }

        chatAdapter = ConversationAdapter(
            requireContext(),
            listChats,
            loggedInUserCache.getLoggedInUserId() ?: ""
        ) { addChat: Boolean, chat: CreateChatGroupResponse ->
            if (addChat) {
                open(R.id.chatCreate)
            } else {
                if (currentOpenChat == null || chat.id != currentOpenChat!!.id) {
                    currentOpenChat = chat
                    updateViewsForSelectedChat()
                }
            }
        }
        chatHeaderBinding.rvConversations.show(chatAdapter)
        msgAdapter = MessagesAdapter(
            requireContext(),
            this,
            loggedInUserCache.getLoggedInUserId().toString(),
            listOfMessage
        ) { _: Int, message: GroupMessages ->
            clickedMsg = message
            showMenuInBottomInput(false)
        }
        (binding.list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.list.show(msgAdapter)
        chatHeaderBinding.rvConversations.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    chatViewModel.loadMoreGroup()
                    chatViewModel.globalloadMoreGroup()
                }
            }
        })
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    groupId?.let {
                        chatViewModel.loadMore(groupId = it)
                    }
                }
            }
        })
    }

    private fun sendTextMsg(text: String) {
        if (currentOpenChat != null) {
            if (editMessage != null) {
                editMessage?.let { editMessage ->
                    editMessage.message = chatFooterBinding.input.get()
                    chatViewModel.updateMessage(
                        UpdateChatMessageRequest(
                            editMessage.id,
                            editMessage.message,
                            null,
                            null,
                            "text"
                        )
                    )
                }
            } else {
                if (clickedMsg != null) {
                    clickedMsg!!.message = chatFooterBinding.input.get()
                    sendMessage(text, currentOpenChat!!)
                    clickedMsg = null
                } else sendMessage(text, currentOpenChat!!)
            }
        }
    }

    private fun showMenuInBottomInput(isClickedMenu: Boolean) {
        if (isClickedMenu) {

            val userid = loggedInUserCache.getLoggedInUserId()
            if (currentOpenChat == null) {
                chatFooterBinding.tvEdit.visibility = GONE
                chatFooterBinding.tvLeave.visibility = GONE
                chatFooterBinding.tvAlert.visibility = GONE
            } else {
                chatFooterBinding.tvEdit.visibility =
                    if (currentOpenChat!!.owner == userid) VISIBLE else GONE
                chatFooterBinding.tvLeave.visibility =
                    if (currentOpenChat!!.owner != userid) VISIBLE else GONE
                chatFooterBinding.tvAlert.visibility =
                    if (currentOpenChat!!.isGlobal && !user.isGlobal) GONE else VISIBLE

                chatFooterBinding.tvAlert.text = getString(R.string.alerts_on)
                chatFooterBinding.tvAlert.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

                if (currentOpenChat?.mutedList?.size ?: 0 > 0) {
                    currentOpenChat?.mutedList?.forEach {
                        if (it.equals(loggedInUserCache.getLoggedInUserId().toString(), true)) {
                            chatFooterBinding.tvAlert.text = getString(R.string.alerts_off)
                            chatFooterBinding.tvAlert.backgroundTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.dark_gray
                                    )
                                )
                        } else {
                            chatFooterBinding.tvAlert.text = getString(R.string.alerts_on)
                            chatFooterBinding.tvAlert.backgroundTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.black
                                    )
                                )
                        }
                    }

                }
            }

            if (chatFooterBinding.linActions.visibility == VISIBLE) {
                SlideAnimationUtil.slideOutToRight(requireContext(), chatFooterBinding.linActions) {
                    if (!it) chatFooterBinding.linActions.visibility = GONE
                }
                chatFooterBinding.menu.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            } else {
                chatFooterBinding.linGlobalMenu.visibility = VISIBLE
                chatFooterBinding.linMsgMenu.visibility = GONE
                SlideAnimationUtil.slideInFromRight(
                    requireContext(),
                    chatFooterBinding.linActions
                ) {
                    if (it) {
                        chatFooterBinding.linActions.visibility = VISIBLE
                        view?.let { it1 -> hideKeyboards(it1) }
                    }
                }
                chatFooterBinding.menu.setImageResource(R.drawable.ic_keyboard_arrow_right_gray_24dp)
            }
        } else {
            chatFooterBinding.linMsgMenu.visibility = VISIBLE
            chatFooterBinding.linGlobalMenu.visibility = GONE
            if (chatFooterBinding.linActions.visibility != VISIBLE) {
                SlideAnimationUtil.slideInFromRight(
                    requireContext(),
                    chatFooterBinding.linActions
                ) {
                    if (it) {
                        chatFooterBinding.linActions.visibility = VISIBLE
                        view?.let { it1 -> hideKeyboards(it1) }
                    }
                }
                chatFooterBinding.menu.setImageResource(R.drawable.ic_keyboard_arrow_right_gray_24dp)
            }
        }
    }

    private fun getUsersNameInChatText(): String {
        var title: String

        title = currentOpenChat!!.name + if (currentOpenChat!!.name!!.isNotBlank()) ": " else ""
//
//        //! For global chat (temporary solution)
        val isGlobal: Boolean? = currentOpenChat!!.isGlobal
        var count: Int = (currentOpenChat!!.usersIds.size)
        // temporary code for SCP group user count
        val isSCPGroup = currentOpenChat?.name?.equals("SCP") ?: false
        if (isSCPGroup) {
            count += currentOpenChat?.members?.size ?: 0
        }
        if (isGlobal == true) {
            title = "$title$count users"
            return title
        }
//
//        var usersString = ""
//
//        for ((i, userId) in currentOpenChat!!.usersIds.withIndex()) {
//            val user = cache.daoUsers().getImmediately(userId)
//            if (user?.name == null) {
//                continue
//            }
//            val temp: String = if (usersString.isEmpty()) {
//                usersString + "" + user.name
//            } else {
//                usersString + ", " + user.name
//            }
//            if (temp.length + title.length > 50) {
//                val more = currentOpenChat!!.usersIds.size - i - 1
//                if (more > 0) {
//                    usersString += " + $more more..."
//                }
//                break
//            }
//            usersString = temp
//        }
//        if (usersString.isNotEmpty()) {
//            title += usersString
//        }
        currentOpenChat?.let { group ->
            group.members.forEachIndexed { index, owner ->
                if (index > 2) return@forEachIndexed

                if (index < 2) {
                    title += owner.displayName

                    if (index != (group.members.size - 1) && index < 1) title += ", "
                } else {
                    title += " +".plus(group.members.size - 2).plus(" Others")
                }
            }

        }

        return title
    }

    private fun addReceivedMessage(msg: GroupMessages) {
        if (currentOpenChat != null && msg.groupId == currentOpenChat!!.id) {
            if (!listOfMessage.contains(msg)) {
                if (listOfMessage.none { it.id == msg.id }) {
                    listOfMessage.add(0, msg)
                    listOfMessage.distinct()
                    msgAdapter.notifyItemInserted(0)
                }

                if (isAdded && isVisible) binding.list.post { binding.list.smoothScrollToPosition(0) }
            }
        } else sortListAndSetAdapter(openChat = false)
    }

    private fun updateMessage(msg: GroupMessages) {
        if (currentOpenChat != null && msg.groupId == currentOpenChat!!.id) {
            val index: Int = listOfMessage.indexOf(listOfMessage.filter { it.id == msg.id }[0])
            listOfMessage[index] = msg
            msgAdapter.notifyItemChanged(index)
        }
    }

    @Throws(Exception::class)
    private fun sendMessage(text: String, chat: CreateChatGroupResponse) {
        if (selectedFile == null && text.isBlank()) {
            return
        }
        val message = GroupMessages(
            message = text,
            id = chat.id,
            sender = Owner(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatar,
                displayName = user.displayName
            ),
            created = System.currentTimeMillis().toString()
        )

        if (selectedFile != null) {
            when {
                selectedFile!!.absolutePath.extension.isImage -> {
                    message.localPath = selectedFile!!.absolutePath
                    message.type = "image"
                    message.uploadStatus = "uploading"
                    message.imageAspectRatio = calculateAspectRatio(selectedFile)
                }
                selectedFile!!.absolutePath.extension.isVideo -> {
                    message.localPath = selectedFile!!.absolutePath
                    message.type = "video"
                    message.uploadStatus = "uploading"
                    message.imageAspectRatio = 1.77777777778 //Fix for video same as in iOS
                }
                selectedFile!!.absolutePath.extension.isAudio -> {
                    message.localPath = selectedFile!!.absolutePath
                    message.type = "audio"
                    message.uploadStatus = "uploading"
                    message.imageAspectRatio = 0.5625 //Fix for audio same as in iOS
                }
                else -> {
                    message.type = "text"
                }
            }
        } else {
            message.type = "text"
        }

//        if (message != null) {
//            storeUnsentMsg(message)
//        }

        message.let {
            //            binding.progressBar.visibility = VISIBLE
            if (message.type != "text") {
                selectedFile?.let { uploadingFile ->
                    var filePath: String? = null

                    if (!message.type.equals("video", true)) {
                        var actualfilepath: String? = null

                        if (message.type.equals("image", true)) {
                            filePath = SiliCompressor.with(requireContext())
                                .compress(Uri.fromFile(selectedFile).toString(), selectedFile)

                            filePath?.let {
                                actualfilepath =
                                    FileUtils.getPath(requireContext(), Uri.parse(filePath))
                            }
                        }

                        val selectFile: File =
                            if (actualfilepath != null) File(actualfilepath) else selectedFile!!

                        uploadFileOnServer(selectFile, chat.id, message)
                    }

                    if (message.type.equals("video", true)) {
                        val videoThumbnail = uploadingFile.createVideoThumb(requireActivity())
                        UploadFile.upload(videoThumbnail).subscribe { thumbnailPath ->
                            uploadFileOnServer(uploadingFile, chat.id, message, thumbnailPath)
                        }
                    }
                }
            } else {
                chatViewModel.sendMessage(
                    SendMessageRequest(
                        chat.id,
                        CLUB_ID.toString(),
                        text,
                        message.type
                    )
                )
            }
        }
        view?.let { hideKeyboards(it) }
    }

    private fun uploadFileOnServer(
        uploadingFile: File,
        chatId: String,
        message: GroupMessages,
        thumbnailPath: String? = null
    ) {
        UploadFile.upload(uploadingFile).subscribe({ path ->
            chatViewModel.sendMessage(
                SendMessageRequest(
                    chatId,
                    CLUB_ID.toString(),
                    "  ",
                    message.type,
                    message.uploadStatus,
                    message.imageAspectRatio?.toInt(),
                    url = path,
                    thumbnailUrl = if (!thumbnailPath.isNullOrEmpty()) thumbnailPath else null
                )
            )
        }, {
            showToast(resources.getString(R.string.try_to_upload_file_again))
        })
    }

    private fun calculateAspectRatio(filePath: File?): Double {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath!!.absolutePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return (imageWidth / imageHeight.toDouble())
    }

    fun openChatFromNotification(chatsId: String?) {
        if (currentOpenChat == null || currentOpenChat!!.id != chatsId) {
            if (listChats.any { chatsId == it.id }) {
                val chat = listChats.first { chatsId == it.id }
                currentOpenChat = chat
                isFromNotification = false
                sortListAndSetAdapter(true, openChat = true)
            }
        }
    }

    private fun deleteMessage(item: GroupMessages) {
        chatViewModel.deleteMessage(DeleteChatMessageRequest(item.id))
    }

    private fun leaveChat(chat: CreateChatGroupResponse) {
        chatViewModel.leaveChatGroup(JoinChatGroupRequest(chat.id))
    }

    private fun toggleAlerts(chat: CreateChatGroupResponse) {
        if (chat.mutedList.size == 0) {
            chatViewModel.muteChat(JoinChatGroupRequest(chat.id))
        }

        chat.mutedList.forEach {
            if (it.equals(loggedInUserCache.getLoggedInUserId(), true)) {
                chatViewModel.unmuteChat(JoinChatGroupRequest(chat.id))
            } else {
                chatViewModel.muteChat(JoinChatGroupRequest(chat.id))
            }
        }
    }

    private fun addNotification(title: String, message: String, groupId: String) {

        val bundle = Bundle().apply {
            putString("groupId", groupId)
            putBoolean("isFromNotification", true)
        }
        val mIntent = Intent(requireContext(), MainActivity::class.java)
        mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mIntent.action = Constants.ACTION_SEND_MESSAGE
        mIntent.putExtras(bundle)

        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            mIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.logo_fanschat)
                .setContentTitle(title).setContentText(message)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.logo_fanschat
                    )
                )
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(requireContext()).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

    private fun getMessagesFromLocal(chat: CreateChatGroupResponse) {
        groupId = chat.id
        //chatViewModel.getCacheChatMessages(chat.id)
        if (groupId != null) {
            chatViewModel.resetLoadingMessage(groupId!!)
        }
    }

    /* END LOCAL DB CALLS */

    private fun listenToViewModel() {
        chatViewModel.createPostState.subscribeOnIoAndObserveOnMainThread({
            when (it) {
                is ChatViewState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is ChatViewState.ErrorMessages -> {
                    showToast(it.errorMessage)
                }
                is ChatViewState.LoadingState -> {
                    binding.progressBar.visibility = if (it.isLoading) VISIBLE else GONE
                }
                is ChatViewState.TranslateMessage -> {
                    val messageId = it.messageId
                    val message = listOfMessage.find { it.id == messageId }
                    val index = listOfMessage.indexOf(message)
                    message?.let { msg ->
                        msg.message = it.translateMessage.translations?.message ?: ""
                        listOfMessage[index] = message
                        msgAdapter.notifyItemChanged(index)
                    }
                }
                is ChatViewState.GetUserOfficialGroupsList -> {
                    listChats.clear()
                    it.getPublicChatGroupListResponse.data?.let { it1 -> listChats.addAll(it1) }
                    if (isFromNotification) {
                        openChatFromNotification(chatId)
                    } else {
                        sortListAndSetAdapter(openChat = true)
                    }
                    chatAdapter.notifyDataSetChanged()
                }
                is ChatViewState.ChatGroupList -> {
                    listChats.clear()
                    listChats.addAll(it.listOfChatGroup)
                    if (isFromNotification) {
                        openChatFromNotification(chatId)
                    } else {
                        sortListAndSetAdapter(openChat = true)
                    }
                    chatAdapter.notifyDataSetChanged()
                }
                is ChatViewState.GetChatGroupMessages -> {
                    listOfMessage.clear()
                    listOfMessage.addAll(it.groupMessages)
                    listOfMessage.distinct()
                    msgAdapter.notifyDataSetChanged()
                    if (chatViewModel.pageNumber == 1 && listOfMessage.isNotEmpty() && isAdded && isVisible && binding.list != null) {
                        binding.list.scrollToPosition(0)
                    }
                    isLoading = false
                }
                is ChatViewState.UnmuteChatMessage -> {
                    var index = listChats.indexOf(currentOpenChat)

                    if (listChats[index].mutedList.contains(loggedInUserCache.getLoggedInUserId())) {
                        listChats[index].mutedList.remove(loggedInUserCache.getLoggedInUserId())

                        chatFooterBinding.tvAlert.text = getString(R.string.alerts_on)
                        chatFooterBinding.tvAlert.backgroundTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.black
                                )
                            )
                    }
                }
                is ChatViewState.MuteChatMessage -> {

                    var index = listChats.indexOf(currentOpenChat)

                    if (!listChats[index].mutedList.contains(loggedInUserCache.getLoggedInUserId())) listChats[index].mutedList.add(
                        loggedInUserCache.getLoggedInUserId().toString()
                    )

                    chatFooterBinding.tvAlert.text = getString(R.string.alerts_off)
                    chatFooterBinding.tvAlert.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.dark_gray
                            )
                        )
                }
                is ChatViewState.LeaveGroupMessage -> {
                    if (chatFooterBinding.linActions.visibility == VISIBLE) chatFooterBinding.menu.performClick()
                    chatViewModel.getUserOfficialCacheChatGroups()
//                    listChats.remove(currentOpenChat)
//                    this.currentOpenChat = null
//                    if (listChats.size > 0) {
//                        this.currentOpenChat = listChats[0]
//                        chatAdapter.currentOpenChat = 0
//                        updateViewsForSelectedChat()
//                    }
//                    chatAdapter.notifyDataSetChanged()
                }
                is ChatViewState.DeleteChatMessage -> {
                }
                is ChatViewState.SendChatMessage -> {
                    binding.progressBar.visibility = GONE
                    chatFooterBinding?.let { footer ->
                        footer.input.clear()
                        footer.attachmentContainer.isVisible = false
                        footer.submit.isVisible = true
                        footer.showProgressBar.isVisible = false
                    }

                    it.groupMessages?.let { message ->
                        val sender: Owner = Owner(
                            firstName = message.sender?.firstName,
                            lastName = message.sender?.lastName,
                            displayName = message.sender?.displayName,
                            created = message.sender?.created,
                            id = message.sender?.id,
                            isAdmin = message.sender?.isAdmin,
                            updated = message.sender?.updated,
                            avatarUrl = message.sender?.avatarUrl,
                            email = message.sender?.email
                        )
                        val groupMessage = GroupMessages(
                            true,
                            id = message.id.toString(),
                            sender = sender,
                            groupId = message.groupId,
                            message = message.message,
                            type = message.type,
                            uploadStatus = message.uploadStatus,
                            url = message.url,
                            thumbnailUrl = message.thumbnailUrl,
                            created = message.created,
                            updated = message.updated,
                            imageAspectRadio = message.imageAspectRadio
                        )
                        addReceivedMessage(groupMessage)

                        if (!message.type.equals("text")) {
                            chatViewModel.updateMessage(
                                UpdateChatMessageRequest(
                                    message.id,
                                    message.message,
                                    "uploaded",
                                    message.imageAspectRadio,
                                    message.type,
                                    message.url,
                                    thumbnailUrl = message.thumbnailUrl
                                )
                            )
                        }
                        selectedFile = null
                    }
                }
                is ChatViewState.UpdateMessage -> {
                    it.groupMessages.let { message ->
                        val sender = Owner(
                            firstName = message.sender?.firstName,
                            lastName = message.sender?.lastName,
                            displayName = message.sender?.displayName,
                            created = message.sender?.created,
                            id = message.sender?.id,
                            isAdmin = message.sender?.isAdmin,
                            updated = message.sender?.updated,
                            avatarUrl = message.sender?.avatarUrl,
                            email = message.sender?.email
                        )
                        val groupMessage = GroupMessages(
                            true,
                            id = message.id,
                            sender = sender,
                            groupId = message.groupId,
                            message = message.message,
                            type = message.type,
                            uploadStatus = message.uploadStatus,
                            url = message.url,
                            thumbnailUrl = message.thumbnailUrl,
                            created = message.created,
                            updated = message.updated,
                            imageAspectRadio = message.imageAspectRadio
                        )
                        updateMessage(groupMessage)
                    }
                    editMessage = null
                    chatFooterBinding.input.text?.clear()
                    view?.let { it1 -> hideKeyboards(it1) }
                }
                is ChatViewState.CreateChatGroup -> {

                    listChats.add(it.createChatGroupResponse)

                    currentOpenChat = it.createChatGroupResponse
                    chatAdapter.currentOpenChat = listChats.indexOf(it.createChatGroupResponse)

                    chatAdapter.notifyDataSetChanged()
                    updateViewsForSelectedChat()
                    chatHeaderBinding.rvConversations.scrollToPosition(listChats.size - 1)
                }
                is ChatViewState.ChatMessageSendButton -> {
                    chatFooterBinding.ivSendMessage.isEnabled = it.isEnable
                }
                else -> {}
            }
        }, {
            FirebaseCrashlytics.getInstance().recordException(it)
        }).autoDispose()
    }

    fun dialogTranslate(layout: View, message: GroupMessages, callback: (GroupMessages) -> Unit) {
        val content: View = View.inflate(requireContext(), R.layout.popup_translation, null)
        if (customTooltip != null) customTooltip!!.dismiss()
        var position = Tooltip.TOP
        if (isOnTopScreen(layout)) {
            position = Tooltip.BOTTOM
        }
        customTooltip =
            Tooltip.Builder(requireContext()).anchor(layout, position)
                .animate(TooltipAnimation(TooltipAnimation.NONE))
                .autoAdjust(true).withPadding(10).content(content)
                .withTip(Tooltip.Tip(30, 30, Color.WHITE, 10))
                .into(binding.root).debug(true).show()

        val listView = content.findViewById<ListView>(R.id.listView)
        listView.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languageNames)
        listView.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, position, p3 ->
            chatViewModel.getTranslateMessage(message.id, languageCodes[position])
            customTooltip?.dismiss()
        }
    }

    private fun isOnTopScreen(@NonNull content: View): Boolean {
        val location = IntArray(2)
        content.getLocationOnScreen(location)
        return location[1] < getScreenHeight() / 2
    }

    private fun getScreenHeight(): Int {
        return requireContext().resources.displayMetrics.heightPixels
    }

    override fun onDestroyView() {
        super.onDestroyView()
        customTooltip?.dismiss()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDestroy() {
        disposeMessageNotificationDisposable()
        _binding = null
        _chatFooterBinding = null
        _chatHeaderBinding = null
        _chatFooterBinding = null
        _chatLoginPromptBinding = null
        super.onDestroy()
    }

    private fun disposeMessageNotificationDisposable() {
        chatCompositeDisposable.clear()
    }

    private fun Disposable.addToChatDisposable() {
        chatCompositeDisposable.add(this)
    }

    fun askToLogin() {
        chatLoginPromptBinding.loginPrompt.isVisible = true
        chatLoginPromptBinding.login.whenClicked()
        chatLoginPromptBinding.register.whenClicked()
    }
}