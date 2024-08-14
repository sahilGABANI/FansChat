package base.ui.fragment.other.friends.my

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
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.friendrequest.model.FriendRequestRequest
import base.data.api.friendrequest.model.ReportRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.cache.Cache
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentFriendDetailBinding
import base.databinding.ToolbarFriendDetailBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.other.friends.requests.FriendRequest
import base.ui.fragment.other.friends.requests.viewmodel.FriendRequestViewModel
import base.ui.fragment.other.friends.requests.viewmodel.FriendRequestViewState
import base.util.*
import base.util.json.PaginationListener
import javax.inject.Inject

class FriendDetailFragment : BaseFragment() {
    private var fromRequest: Boolean? = false
    private lateinit var user: FansChatUserDetails
    private var friendRequest: FriendRequest? = null
    private var listOfFriends: ArrayList<FansChatUserDetails> = arrayListOf()
    private var isLastPage = false
    private var isLoading = false
    private var userId: String? = null

    private lateinit var friendsAdapter: FriendsAdapter

    private var _friendDetailsBinding: ToolbarFriendDetailBinding? = null
    private val friendDetailsBinding get() = _friendDetailsBinding!!

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_OPEN_FRIEND_PROFILE && isVisible && isAdded && intent.hasExtra(
                    "_mID"
                )
            ) {
                val _mID = intent.getStringExtra("_mID")
                if (isAdded && isVisible && ::user.isInitialized && !_mID.isNullOrBlank() && _mID == user.id) {
                    val mUser = Cache.cache.daoUsers().getImmediately(_mID)
                    if (mUser != null) {
//                        user = mUser
                        setUpViews()
                        manageViewVisibilities()
                    }
                }
            }
        }
    }

    private var _binding: FragmentFriendDetailBinding? = null
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
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter(Constants.ACTION_OPEN_FRIEND_PROFILE))

        _binding = FragmentFriendDetailBinding.inflate(inflater, container, false)
        _friendDetailsBinding = ToolbarFriendDetailBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        userId = requireArguments().getString("userId")
        if (userId != null) {
            friendRequestViewModel.getSpecificUserDetails(userId!!)
            friendRequestViewModel.getListOfFriends(userId!!)
        }

        friendDetailsBinding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }

        fromRequest = arguments?.getBoolean("fromRequest", false)
        if (fromRequest == true) friendRequest =
            arguments?.getSerializable("friendRequest") as FriendRequest

        setClickListeners()

//        listMutuals.clear()
//        mutualAdapter = FriendsAdapter(isGrid = false, items = listMutuals, loggedInUserCache = loggedInUserCache)
//        listMutual.show(mutualAdapter)

        loadMutualFriends()
        listenToViewModel()
        binding.listMutual.addOnScrollListener(object : PaginationListener(
            binding.listMutual.layoutManager as LinearLayoutManager,
            PAGE_SIZE_FIFTY,
            0
        ) {
            override fun loadMoreItems() {
                isLoading = true
                loadMutualFriends()
            }

            override val ismLastPage: Boolean
                get() = isLastPage

            override val ismLoading: Boolean
                get() = isLoading
        })

        friendsAdapter = FriendsAdapter(
            isGrid = false,
            items = listOfFriends,
            loggedInUserCache = loggedInUserCache,
            context = requireContext()
        )
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.friendsList.layoutManager = layoutManager
        binding.friendsList.show(friendsAdapter)

        binding.friendsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dx > 0) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() >= listOfFriends.size - 3) {
                        if (::user.isInitialized) {
                            user.id.let { friendRequestViewModel.loadMoreFriends(it) }
                        }
                    }
                }
            }
        })

//        listOfFriends.clear()
        socketDataManager.notificationData().subscribeAndObserveOnMainThread {
            if (userId != null) {
                listOfFriends.clear()
                resetActions()
                friendRequestViewModel.getSpecificUserDetails(userId!!)
                friendRequestViewModel.getListOfFriends(userId!!)
            }
        }.autoDispose()
    }

    private fun resetActions() {
        binding.linActions.isVisible = false
        binding.linRequestActions.isVisible = false
        binding.friendRequestPending.isVisible = false
    }

    private fun loadMutualFriends() {
//        getMutualFriends(listMutuals.size) {
//            if (it.isNotEmpty()) {
//                listMutuals.addAll(it)
//                mutualAdapter.notifyItemRangeInserted(listMutuals.size - it.size, it.size)
//                linMutual.isVisible = it.isNotEmpty()
//            }
//            if (it.size < PaginationListener.PAGE_SIZE)
//                isLastPage = true
//            isLoading = false
//        }
    }

    private fun setUpViews() {
        binding.name.text = user.displayName
        binding.avatar.srcRound = user.avatarUrl

        binding.pinnedCount.text =
            if (user.wallPosts == null || user.wallPosts ?: 0 < 0) "0" else user.wallPosts.toString()
//        friendsCount.text = if (user.friends.isNullOrEmpty()) "0" else user.friends?.size.toString()
        binding.commentsCount.text = user.commentsCount.toString()
        binding.tvUserFriends.text = (user.displayName).plus(" Friends list")
        binding.tvMutualFriends.text = "Friends you and " + user.displayName + " have in common"

        fromRequest = user.isFriendRequestPending

    }

    private fun manageViewVisibilities() {
        if (!user.isFriend && user.isFriendRequestSent) {
            binding.linRequestActions.isVisible = true
        } else {
            binding.linRequestActions.isVisible = false
            binding.tvMineFriend.isVisible = user.isFriend
            binding.friendRequestPending.isVisible = user.isFriendRequestPending && !user.isFriend
            binding.linActions.isVisible = !user.isFriendRequestSent && !user.isFriendRequestPending
            binding.btnChat.isVisible = user.isFriend
            binding.btnFollow.isVisible = false
            binding.tvFriendRequest.text =
                if (user.isFriend) getString(R.string.remove_friend) else getString(R.string.friend_request_send)
            binding.ivFriendRequest.src =
                if (user.isFriend) R.drawable.ic_unfriend else R.drawable.ic_add_friend
//            tvFollow.text = if (user.following) getString(R.string.friend_unfollow) else getString(R.string.friend_follow)
//            ivFollow.src = if (user.following) R.drawable.ic_unfollow else R.drawable.ic_follow
        }
    }

    private fun setClickListeners() {
        binding.ibAccept.onClick {
            user.id?.let {
                showCustomDialog(
                    getString(R.string.accepted_friend_request),
                    getString(R.string.you_want_to_accept_friend_request),
                    true
                ) {
                    friendRequestViewModel.acceptDetails(it)
                }
            }
//            accept(friendRequest!!) {
//                showToast("Friends request accepted. You're now friends!")
//                fromRequest = false
//                user.friend = true
//                user.friendsCount++
//                setUpViews()
//                manageViewVisibilities()
//            }
        }
        binding.ibDecline.onClick {
            user.id?.let {
                showCustomDialog(
                    getString(R.string.decline_friend_request),
                    getString(R.string.you_want_to_decline_friend_request),
                    true
                ) {
                    friendRequestViewModel.declineDetails(it)
                }
            }
//            decline(friendRequest!!) {
//                showToast("Friends request declined")
//                fromRequest = false
//                user.friend = false
//                manageViewVisibilities()
//            }
        }
        binding.btnChat.onClick {
            if (!user.isFriend) {
                showToast("Error: Please add a user to friends first")
                return@onClick
            }
            open(R.id.chat, hashMapOf("friendUser" to user, "isFromFriendDetail" to true))
//            open(R.id.chat)
        }


        binding.btnFollow.onClick {
//            if (user.following)
//                unfollow(user) {
//                    user.following = false
//                    manageViewVisibilities()
//                }
//            else follow(user) {
//                user.following = true
//                manageViewVisibilities()
//            }
        }
        binding.btnSendFriendReq.onClick {

            if (!user.isFriendRequestPending && !user.isFriend) {
                user.id?.let {
                    showCustomDialog(
                        getString(R.string.friend_request_send),
                        getString(R.string.you_want_to_send_friend_request),
                        true
                    ) {
                        friendRequestViewModel.sendFriendsRequest(FriendRequestRequest(it, " "))
                    }
                }
            } else if (user.isFriend) {
                user.id?.let {
                    showCustomDialog(
                        getString(R.string.remove_user),
                        getString(R.string.you_want_to_remove_this_user),
                        true
                    ) {
                        friendRequestViewModel.deleteFriends(it)
                    }
                }

            }
        }

        binding.btnReport.onClick {
//            report(user)
            if (!user.id.isNullOrEmpty() && !CLUB_ID.toString().isNullOrEmpty()) {
                showCustomWithEditTextDialog(
                    getString(R.string.report_user),
                    getString(R.string.you_want_to_report_user),
                    showCancel = true,
                    editText = true
                ) {
                    friendRequestViewModel.reportUser(
                        ReportRequest(
                            it,
                            CLUB_ID.toString(),
                            user.id
                        )
                    )
                }
            }
        }
    }

    private fun listenToViewModel() {
        friendRequestViewModel.friendRequestState.subscribeAndObserveOnMainThread {
            when (it) {
                is FriendRequestViewState.ErrorMessage -> {
                    if (it.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else {
                        showToast(it.errorMessage)
                        goBack()
                    }
                }
                is FriendRequestViewState.ErrorMessages -> {
                    showToast(it.errorMessage)
                }
                is FriendRequestViewState.LoadingStates -> {
                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                }
                is FriendRequestViewState.AcceptFriendRequest -> {
                    showToast("Friends request accepted. You're now friends!")
                    fromRequest = false
                    user.isFriend = true
                    user.isFriendRequestPending = false
                    user.isFriendRequestSent = false
//                    user.friends++
                    setUpViews()
                    manageViewVisibilities()
                    binding.friendsCount.text =
                        Integer.parseInt(binding.friendsCount.text.toString()).plus(1).toString()

                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
                        Intent(Constants.ACTION_REMOVE_FRIENDS).putExtra(
                            "_mID",
                            user.id
                        )
                    )
                }
                is FriendRequestViewState.DeclineFriendRequest -> {
                    showToast("Friends request declined")
                    fromRequest = false
                    user.isFriend = false
                    user.isFriendRequestPending = false
                    user.isFriendRequestSent = false
                    setUpViews()
                    manageViewVisibilities()

                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
                        Intent(Constants.ACTION_FRIEND_REQUEST_DECISION).putExtra(
                            "_mID",
                            user.id
                        )
                    )
                }
                is FriendRequestViewState.SuccessMessage -> {
                    showToast("Friends request sent successfully")
                    user.isFriendRequestPending = true
                    manageViewVisibilities()
                }
                is FriendRequestViewState.UserDetails -> {
                    user = it.userDetails
                    setUpViews()
                    manageViewVisibilities()
                }
                is FriendRequestViewState.DeleteFriends -> {
                    showToast(it.successMessage)
                    fromRequest = false
                    user.isFriend = false
                    manageViewVisibilities()
                    Constants.IS_FORCE_REFRESH = true

                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
                        Intent(Constants.ACTION_REMOVE_FRIENDS).putExtra(
                            "_mID",
                            user.id
                        )
                    )
                }

                is FriendRequestViewState.ListofFriends -> {
                    listOfFriends.addAll(it.listofuser)
                    val friendsData = listOfFriends.distinct()
                    listOfFriends.clear()
                    listOfFriends.addAll(friendsData)
                    friendsAdapter.notifyDataSetChanged()
                    binding.friendsCount.text = it.count.toString()
                    binding.tvUserFriends.visibility = if (it.count == 0) View.GONE else View.VISIBLE
                }
                is FriendRequestViewState.ReportUserSuccess -> {
                    showToast(it.successMessage)
                }
                else -> {}
            }
        }.autoDispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (userId != null) {
            listOfFriends.clear()
            friendRequestViewModel.getSpecificUserDetails(userId!!)
            friendRequestViewModel.getListOfFriends(userId!!)
        }
    }

    override fun onDestroy() {
        _binding = null
        _friendDetailsBinding = null
        super.onDestroy()
    }

}