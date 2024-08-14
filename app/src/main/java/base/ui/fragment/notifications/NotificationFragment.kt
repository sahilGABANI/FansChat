package base.ui.fragment.notifications

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
import androidx.recyclerview.widget.RecyclerView
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.NotificationsData
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentNotificationBinding
import base.extension.*
import base.socket.SocketService
import base.ui.MainActivity
import base.ui.base.BaseFragment
import base.ui.fragment.notifications.viewmodel.NotificationViewModel
import base.ui.fragment.notifications.viewmodel.NotificationViewState
import base.util.*
import javax.inject.Inject

class NotificationFragment : BaseFragment() {
    private val listNotifications = arrayListOf<NotificationsData>()
    lateinit var adapter: NotificationAdapter
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<NotificationViewModel>
    private lateinit var notificationViewModel: NotificationViewModel
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_NOTIFICATION_RELOAD_INDICATOR) {
                showReload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FansChat.component.inject(this)
        notificationViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                broadcastReceiver,
                IntentFilter(Constants.ACTION_NOTIFICATION_RELOAD_INDICATOR)
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenToViewEvents()
        adapter = NotificationAdapter(requireContext(), listNotifications, loggedInUserCache.getLoggedInUserId()) {
            handleScreenRedirection(it)
        }
        binding.rvNotifications.adapter = adapter

        notificationViewModel.getNotificationsFromCache(true)

        binding.refreshLayout.setOnRefreshListener {
            binding.cardReload.isVisible = false
            notificationViewModel.resetLoading()
        }
        listenToViewModel()

        binding.rvNotifications.clearOnScrollListeners()
        binding.rvNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    notificationViewModel.loadMore()
                }
            }
        })
    }

    private fun listenToViewEvents() {
        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }
        binding.cardReload.onClick {
            binding.cardReload.isVisible = false
            binding.refreshLayout.isRefreshing = false
            notificationViewModel.resetLoading()
        }
    }

    fun showReload() {
        binding.cardReload.isVisible = true
        binding.refreshLayout.isRefreshing = false
    }

    private fun listenToViewModel() {
        notificationViewModel.notificationState.subscribeAndObserveOnMainThread {
            when (it) {
                is NotificationViewState.ErrorMessage -> {
                    binding.refreshLayout.isRefreshing = false
                    showToast(it.errorMessage)
                }
                is NotificationViewState.LoadingState -> {
                    binding.progressBar.visibility =
                        if (it.isLoading && (it.isForceShow || listNotifications.isEmpty())) View.VISIBLE else View.GONE
                }

                is NotificationViewState.ListNotifications -> {
                    binding.refreshLayout.isRefreshing = false
                    listNotifications.clear()
                    listNotifications.addAll(it.listNotifications)
                    adapter.notifyDataSetChanged()

                    binding.tvNoData.isVisible = listNotifications.isEmpty()
                }
                is NotificationViewState.ClearOrAddObservables -> {
                    if (it.isToClear) clearObservables() else notificationViewModel.getNotificationsFromCache()
                }
            }
        }.autoDispose()
    }

    private fun handleScreenRedirection(notificationsData: NotificationsData) {
        when (notificationsData.event) {
            in wallEventList() -> {
                //postId
                (requireActivity() as MainActivity).redirectToPostDetail(
                    notificationsData.postId,
                    Constants.POST_TYPE_WALL
                )
            }
            in newsEventList() -> {
                (requireActivity() as MainActivity).redirectToPostDetail(
                    notificationsData.postId,
                    Constants.POST_TYPE_NEWS
                )
            }
            in rumorEventList() -> {
                (requireActivity() as MainActivity).redirectToPostDetail(
                    notificationsData.postId,
                    Constants.POST_TYPE_RUMOURS
                )
            }
            in socialEventList() -> {
                (requireActivity() as MainActivity).redirectToPostDetail(
                    notificationsData.postId,
                    Constants.POST_TYPE_SOCIAL
                )
            }
            in videoEventList() -> {
                (requireActivity() as MainActivity).redirectToPostDetail(
                    notificationsData.postId,
                    Constants.POST_TYPE_CLUB_TV
                )
            }
            SocketService.EVENT_SEND_MESSAGE, SocketService.EVENT_MESSAGE_UPDATE -> {
                //groupId
                (requireActivity() as MainActivity).redirectToChat(notificationsData.groupChatId)
            }
            SocketService.EVENT_FRIEND_REQUEST -> {
                (requireActivity() as MainActivity).redirectToFriendRequestScreen(true)
            }
            SocketService.EVENT_FRIEND_REQUEST_ACCEPTED -> {
                (requireActivity() as MainActivity).redirectToFriendRequestScreen(false)
            }
        }
    }

    private fun clearObservables() {
        if (::notificationViewModel.isInitialized) notificationViewModel.compositeDisposableTemp.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        compositeDisposable.clear()
        if (::notificationViewModel.isInitialized) {
            notificationViewModel.compositeDisposable.clear()
            notificationViewModel.compositeDisposableTemp.clear()
        }
        _binding = null
    }
}