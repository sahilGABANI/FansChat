package base.ui.fragment.other.friends.my

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentMyFriendsBinding
import base.extension.getViewModelFromFactory
import base.extension.goBack
import base.extension.showToast
import base.extension.subscribeAndObserveOnMainThread
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.other.friends.my.viewmodel.MyFriendsViewModel
import base.ui.fragment.other.friends.my.viewmodel.MyFriendsViewState
import base.util.Constants
import base.util.LinearLayoutManagerWrapper
import base.util.json.PaginationListener
import base.util.open
import javax.inject.Inject


class MyFriendsFragment : BaseFragment() {

    private var _binding: FragmentMyFriendsBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<MyFriendsViewModel>
    private lateinit var myFriendsViewModel: MyFriendsViewModel

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var socketDataManager: SocketDataManager

    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private var isLastPage = false
    private var isLoading = false
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_OPEN_FRIEND_PROFILE && isVisible && isAdded) {
                loadFriendsList()
            }
        }
    }

    private var broadcastReceiverRemove = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_REMOVE_FRIENDS && isAdded) {
                loadFriendsList()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        myFriendsViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewEvents()
        listenToViewModel()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter(Constants.ACTION_OPEN_FRIEND_PROFILE))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiverRemove,
            IntentFilter(Constants.ACTION_REMOVE_FRIENDS)
        )
        loadFriendsList()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(broadcastReceiverRemove)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun listenToViewModel() {
        myFriendsViewModel.myFriendsViewState.subscribeAndObserveOnMainThread {
            when (it) {
                is MyFriendsViewState.ErrorMessage -> {
                    if (it.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else showToast(it.errorMessage)
                }
                is MyFriendsViewState.LoadingState -> {
//                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                }
                is MyFriendsViewState.ListOfFriends -> {
                    friendRequestAdapter.updateAdapter(it.listOfFriends)
                }
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        socketDataManager.notificationData().subscribeAndObserveOnMainThread {
            loadFriendsList()
        }.autoDispose()
        friendRequestAdapter = FriendRequestAdapter(
            requireContext(),
            isGrid = true,
            userId = loggedInUserCache.getLoggedInUserId()
        )
        binding.list.apply {
            layoutManager = LinearLayoutManagerWrapper(context, 3)
            adapter = friendRequestAdapter
            addOnScrollListener(object :
                PaginationListener(
                    binding.list.layoutManager as LinearLayoutManager,
                    PAGE_SIZE_FIFTY,
                    0
                ) {
                override fun loadMoreItems() {
                    isLoading = true
                    myFriendsViewModel.loadMoreFriends()
                }

                override val ismLastPage: Boolean
                    get() = isLastPage

                override val ismLoading: Boolean
                    get() = isLoading
            })
        }
    }

    private fun loadFriendsList() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            myFriendsViewModel.resetLoading()
            myFriendsViewModel.loadMyFriends()
        }
    }
}