package base.ui.fragment.other.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.chat.ChatCacheRepository
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.chat.model.JoinChatGroupRequest
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentChatJoinBinding
import base.extension.getViewModelFromFactory
import base.extension.goBack
import base.extension.showToast
import base.extension.subscribeAndObserveOnMainThread
import base.socket.SocketDataManager
import base.ui.adapter.other.ChatsSearchAdapter
import base.ui.adapter.other.PublicChatAdapter
import base.ui.base.BaseFragment
import base.ui.fragment.other.chat.viewmodel.ChatViewModel
import base.ui.fragment.other.chat.viewmodel.ChatViewState
import base.util.get
import base.util.json.PaginationListener
import base.util.show
import javax.inject.Inject

class ChatJoinFragment : BaseFragment() {

    private var listChats: ArrayList<CreateChatGroupResponse> = ArrayList()
    private lateinit var topAdapter: ChatsSearchAdapter
    private lateinit var bottomAdapter: PublicChatAdapter
    private var listTotalChats: ArrayList<CreateChatGroupResponse> = arrayListOf()
    private var isLastPage = false
    private var isLoading = false

    private var _binding: FragmentChatJoinBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var chatCacheRepository: ChatCacheRepository

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ChatViewModel>
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        chatViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatJoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {

        chatViewModel.resetLoadingJoin()
        bottomAdapter = PublicChatAdapter(
            requireContext(),
            loggedInUserCache.getLoggedInUserId().toString(),
            listTotalChats
        ) { chat: CreateChatGroupResponse, _: Int ->
            showCustomDialog(
                getString(R.string.chat_join),
                getString(R.string.you_will_join_chat),
                true
            ) {
                joinChat(chat)
            }
        }
        binding.friendChats.show(bottomAdapter)

        val userId = loggedInUserCache.getLoggedInUserId()
        topAdapter = ChatsSearchAdapter(
            context,
            listChats,
            userId ?: ""
        ) { chat: CreateChatGroupResponse, _: Int ->
            showCustomDialog(
                getString(R.string.chat_join),
                getString(R.string.you_will_join_chat),
                true
            ) {
                joinChat(chat)
            }
        }
        binding.list.show(topAdapter)

        topAdapter.filter(binding.input.get())

        binding.input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                topAdapter.filter(binding.input.get())
            }
        })

        getChatGroups()

        binding.friendChats.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollHorizontally(1)) {
                    chatViewModel.loadMoreJoin()
                }
            }
        })

        listenToViewModel()
    }

    private fun getChatGroups() {
        getAllChats()
//        {
//            if (it.isNotEmpty()) {
//                listTotalChats.addAll(it)
////                Log.e(">>", "" + it.size + "/" + listTotalChats.size)
//                bottomAdapter.notifyItemRangeInserted(listTotalChats.size - it.size, it.size)
//
//                listChats.addAll(it)
//                topAdapter.filter(input.get())
//            }
//            if (it.size < PaginationListener.PAGE_SIZE)
//                isLastPage = true
//            isLoading = false
//        }

//        chatViewModel.getPublicChatGroup(GetWallRequest(1, 50))
    }

    /* API CALLS */

    private fun getAllChats() {
//        disposables.add(
//            Api.getAllChats(offset)
//                .subscribe(
//                    { callback.invoke(it) },
//                    { it.printStackTrace(); callback.invoke(listOf()) })
//        )

        chatViewModel.getPublicChatGroup()
    }

    private fun joinChat(item: CreateChatGroupResponse) {
//        disposables.add(
//            Api.joinChat(item)
//                .subscribe(
//                    {
//                        if (!Cache.cache.isNotSaved(it))
//                            Cache.cache.save(it)
//                        callback.invoke()
//                    }, { toast(it.msg) })
//        )

        chatViewModel.joinChatGroup(JoinChatGroupRequest(item.id))
    }

    /* END API CALLS */
    private fun listenToViewModel() {
        chatViewModel.createPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is ChatViewState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is ChatViewState.LoadingState -> {
                    binding.loadingOverlay.visibility =
                        if (it.isLoading) View.VISIBLE else View.GONE
                }
                is ChatViewState.GetPublicGroupsList -> {
                    val data = it.getPublicChatGroupListResponse.data
                    if (data != null) {
                        if (data.isNotEmpty()) {
                            listTotalChats.addAll(data)
                            //bottomAdapter.notifyItemRangeInserted(listTotalChats.size - data.size, data.size)
                            bottomAdapter.updatePublicChat(listTotalChats)
                            //listChats.addAll(data)
                            listChats.addAll(data)
                            topAdapter.updatePublicChat(listChats)
//                            topAdapter.filter(binding.input.get())
                        }
                        if (data.size < PaginationListener.PAGE_SIZE)
                            isLastPage = true
                    }
                    isLoading = false
                }
                is ChatViewState.JoinChatGroup -> {
                    chatCacheRepository.addInsertOrUpdateChatGroup(arrayListOf(it.createChatGroupResponse))
                    goBack()
                }
                else -> {}
            }
        }.autoDispose()
    }
}