package base.ui.fragment.other.friends.requests

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.friendrequest.model.FriendRequest
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentFriendRequestsBinding
import base.extension.getViewModelFromFactory
import base.extension.goBack
import base.extension.showToast
import base.extension.subscribeAndObserveOnMainThread
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.other.friends.requests.view.FriendRequestAdapter
import base.ui.fragment.other.friends.requests.viewmodel.FriendRequestViewModel
import base.ui.fragment.other.friends.requests.viewmodel.FriendRequestViewState
import base.util.Constants
import base.util.json.PaginationListener
import base.util.open
import timber.log.Timber
import javax.inject.Inject

class FriendRequestsFragment : BaseFragment() {
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private val listRequests: ArrayList<FriendRequest> = arrayListOf()

    private var isLastPage = false
    private var isLoading = false
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_OPEN_FRIEND_REQUEST && isVisible && isAdded) {
                reloadList()
            }
        }
    }

    private var broadcastReceiverDecision = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_FRIEND_REQUEST_DECISION && isAdded) {
                reloadList()
            }
        }
    }

    private var _binding: FragmentFriendRequestsBinding? = null
    private val binding get() = _binding!!


    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<FriendRequestViewModel>
    private lateinit var friendRequestViewModel: FriendRequestViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        friendRequestViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewModel()
        listenToViewEvents()
    }

    override fun onResume() {
        super.onResume()

        Timber.e("Resume")
        resetFriendsRequestScreen()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter(Constants.ACTION_OPEN_FRIEND_REQUEST))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiverDecision,
            IntentFilter(Constants.ACTION_FRIEND_REQUEST_DECISION)
        )
        loggedInUserCache.friendRequestScreenOpen()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(broadcastReceiverDecision)
    }

    private fun listenToViewModel() {
        friendRequestViewModel.friendRequestState.subscribeAndObserveOnMainThread { state ->
            when (state) {
                is FriendRequestViewState.ErrorMessage -> {
                    if (state.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else showToast(state.errorMessage)
                }
                is FriendRequestViewState.LoadingState -> {

                }
                is FriendRequestViewState.ListofFriendRequest -> {
                    if (friendRequestViewModel.pageNumber == 1)
                        listRequests.clear()
                    listRequests.addAll(state.listofuser)

                    friendRequestAdapter.listOfFriendRequest = listRequests
                    friendRequestAdapter.notifyDataSetChanged()
                    binding.empty.isVisible = listRequests.size <= 0

                }
                is FriendRequestViewState.AcceptFriendRequest -> {
                    state.friendRequest?.let { listRequests.remove(it) }
                    friendRequestAdapter.listOfFriendRequest = listRequests
                    binding.empty.isVisible = listRequests.size <= 0

                    Constants.IS_FORCE_REFRESH = true
                }
                is FriendRequestViewState.DeclineFriendRequest -> {
                    showToast(state.successMessage)
                    state.friendRequest?.let { listRequests.remove(it) }
                    binding.empty.isVisible = listRequests.size <= 0

                    friendRequestAdapter.listOfFriendRequest = listRequests
                }
                else -> {}
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        friendRequestAdapter = FriendRequestAdapter(requireContext()).apply {
            acceptRequestViewClicks.subscribeAndObserveOnMainThread {
                val userId = it.sender?.id ?: return@subscribeAndObserveOnMainThread
                showCustomDialog(
                    getString(R.string.accepted_friend_request),
                    getString(R.string.you_want_to_accept_friend_request),
                    true
                ) {
                    friendRequestViewModel.acceptDetails(userId, it)
                }
            }.autoDispose()
            declineRequestViewClicks.subscribeAndObserveOnMainThread {
                val userId = it.sender?.id ?: return@subscribeAndObserveOnMainThread
                showCustomDialog(
                    getString(R.string.decline_friend_request),
                    getString(R.string.you_want_to_decline_friend_request),
                    true
                ) {
                    friendRequestViewModel.declineDetails(userId, it)
                }
            }.autoDispose()
            openProfileViewClicks.subscribeAndObserveOnMainThread {
                val userId = it.sender?.id ?: return@subscribeAndObserveOnMainThread
                open(R.id.friendDetails, "userId" to userId)
            }.autoDispose()
        }
        binding.list.apply {
            adapter = friendRequestAdapter
            addOnScrollListener(object : PaginationListener(
                this@apply.layoutManager as LinearLayoutManager,
                PAGE_SIZE_FIFTY,
                0
            ) {
                override fun loadMoreItems() {
                    isLoading = true
                    friendRequestViewModel.loadMore()
                }

                override val ismLastPage: Boolean
                    get() = isLastPage

                override val ismLoading: Boolean
                    get() = isLoading
            })
        }
        socketDataManager.notificationData().subscribeAndObserveOnMainThread {
            resetFriendsRequestScreen()
        }.autoDispose()
    }

    private fun resetFriendsRequestScreen() {
        isLastPage = false
        isLoading = false
        listRequests.clear()
        friendRequestViewModel.resetLoading()
        friendRequestViewModel.getFriendRequestList()
    }

    fun reloadList() {
        resetFriendsRequestScreen()
    }
}