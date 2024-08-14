package base.ui.fragment.wall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.wall.WallCacheRepository
import base.data.model.wall.FeedSetupItem
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentWallBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.MainActivity
import base.ui.adapter.wall.WallAdapter
import base.ui.base.BaseFragment
import base.ui.fragment.wall.viewmodel.WallPostState
import base.ui.fragment.wall.viewmodel.WallViewModel
import base.util.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber
import javax.inject.Inject

class WallFragment : BaseFragment() {
    private val listFeedSetup: ArrayList<FeedSetupItem> = arrayListOf()
    private lateinit var adapter: WallAdapter
    private lateinit var binding: FragmentWallBinding
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var isAdapterAttaching = true
    lateinit var lastFeedChecker: WallAdapter.LastFeedChecker

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var wallCacheRepository: WallCacheRepository

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<WallViewModel>
    private lateinit var wallViewModel: WallViewModel

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    companion object {
        var listFiltersApplied = SparseArray<ArrayList<String>>()
        var playerMap: ArrayList<Triple<Int, Int, ExoPlayer?>> = arrayListOf()
        var listPausedVideos: HashMap<Int, ArrayList<Int>> = hashMapOf()
    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_WALL_RELOAD_INDICATOR) {
                showReload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FansChat.component.inject(this)
        wallViewModel = getViewModelFromFactory(viewModelFactory)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        if (!::binding.isInitialized) binding = FragmentWallBinding.inflate(inflater, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter(Constants.ACTION_WALL_RELOAD_INDICATOR)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        /*val display =
            (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y*/
        videoSurfaceDefaultHeight = CommonUtils.getDeviceWidth(requireContext())
        screenDefaultHeight = CommonUtils.getDeviceHeight(requireContext())

        (activity as MainActivity).toggleNotificationIcon()

        binding.refreshLayout.setOnRefreshListener {
            fetchWall(true)
        }
        if (!::adapter.isInitialized) {
            adapter = WallAdapter(this, listFeedSetup, wallCacheRepository, loggedInUserCache)
            binding.rvWall.setItemViewCacheSize(6)
            //binding.rvWall.layoutManager = PreLoadingLinearLayoutManager(activity)
            binding.rvWall.adapter = adapter
        } else {
            wallViewModel.getWallFromCache()
        }

        showReload()
        listenToViewModel()
        listenToViewEvents()
        fetchWall()

        binding.tvAuthTitle.text = if (flavor == Flavors.MTN) getString(R.string.access_full_mtn) else getString(R.string.access_full)
        binding.login.whenClicked()
        binding.register.whenClicked()

        binding.rvWall.clearOnScrollListeners()
        binding.rvWall.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val startPosition: Int = getMostVisibleItemPosition()
                    if (::lastFeedChecker.isInitialized && listFeedSetup.size > startPosition && startPosition != -1) {
                        val posOfWallPost = listFeedSetup.indexOfFirst { it.dataSource.lowercase() == Constants.POST_TYPE_WALL }
                        Timber.e("posOfWallPost: $posOfWallPost & startPosition: $startPosition")
                        lastFeedChecker.lastFeed(
                            /*listFeedSetup[startPosition].dataSource.lowercase() == Constants.POST_TYPE_WALL ||*/
                            posOfWallPost != -1 && posOfWallPost <= startPosition, false
                        )
                    } //Check if it is wall feed

                    playerMap.forEach {
                        it.third?.let { exoPlayer ->
                            val playWhenReady = it.first == startPosition && it.second == adapter.listCurrentVisibleIndex.get(
                                it.first, 0
                            )
                            if (playWhenReady && exoPlayer.playbackState == Player.STATE_ENDED) {
                                exoPlayer.seekTo(0)
                            }
                            if (playWhenReady && listPausedVideos.containsKey(startPosition) && listPausedVideos[startPosition]?.contains(
                                    it.second
                                ) == true
                            ) {
                                //Skip player when for paused video
                                exoPlayer.playWhenReady = false
                            } else exoPlayer.playWhenReady = playWhenReady
                        }
                    }
                }
            }
        })


        binding.createPost.onClick {
            if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) open(R.id.authDecideFragment) else open(R.id.createPost)
        }

        Constants.liveDataAudioFocusObserver.subscribeAndObserveOnMainThread {
            playerMap.forEach {
                it.third?.let { exoPlayer ->
                    if (exoPlayer.isPlaying) exoPlayer.playWhenReady = false
                }
            }
        }
    }

    private fun listenToViewEvents() {
        binding.cardReload.onClick {
            binding.refreshLayout.isRefreshing = false
            fetchWall(true)
        }
    }

    fun showReload() {
        binding.cardReload.isVisible = Constants.SHOW_REFRESH_INDICATOR
        binding.refreshLayout.isRefreshing = false
    }

    private fun getMostVisibleItemPosition(): Int {
        val targetPosition: Int

        when { //Adapter is not laid out so needs to keep this check
            isAdapterAttaching -> {
                targetPosition = 0
                isAdapterAttaching = false
            }
            binding.rvWall.canScrollVertically(1) -> {
                val startPosition = (binding.rvWall.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                var endPosition = (binding.rvWall.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                // if there is more than 2 list-items on the screen, set the difference to be 1
                if (endPosition - startPosition > 1) {
                    endPosition = startPosition + 1
                }

                if (adapter.list.size > endPosition && adapter.list[endPosition].content.isEmpty()) {
                    endPosition += 1
                }

                // something is wrong. return.
                if (startPosition < 0 || endPosition < 0) {
                    return 0
                }

                // if there is more than 1 list-item on the screen
                targetPosition = if (startPosition != endPosition) {
                    val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
                    val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(endPosition)
                    if (startPositionVideoHeight > endPositionVideoHeight) //                        if (playerMap.any { it.first == startPosition } || endPositionVideoHeight < 100) startPosition
                        if (playerMap.any {
                                it.first == startPosition && it.second == adapter.listCurrentVisibleIndex.get(
                                    it.first, -1
                                )
                            } || endPositionVideoHeight < resources.getDimensionPixelOffset(R.dimen._140sdp)) startPosition
                        else endPosition
                    else endPosition
                } else startPosition
            }
            else -> {
                targetPosition = listFeedSetup.size - 1
            }
        }
        Timber.tag("MostVisibleItemPosition").e("> $targetPosition")
        return targetPosition

    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at = playPosition - (binding.rvWall.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child: View = binding.rvWall.getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    private fun fetchWall(isToRefresh: Boolean = false) {
        val lp: CoordinatorLayout.LayoutParams = binding.refreshLayout.layoutParams as CoordinatorLayout.LayoutParams
        lp.behavior = if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) AppBarLayout.ScrollingViewBehavior() else null
        binding.refreshLayout.layoutParams = lp
        binding.refreshLayout.requestLayout() //        binding.createPost.visibility = if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) View.GONE else View.VISIBLE
        if (isToRefresh || Constants.IS_FORCE_REFRESH) {
            Constants.IS_FORCE_REFRESH = false
            if (::lastFeedChecker.isInitialized) lastFeedChecker.lastFeed(
                isWallFeed = false, stopPlayers = true
            )
            binding.cardReload.isVisible = false
            listFiltersApplied.clear()
            if (::adapter.isInitialized) adapter.listCurrentVisibleIndex.clear() //Clear cached visible indexes
            wallViewModel.getWallFromRemote()
        }
    }

    private fun listenToViewModel() {
        wallViewModel.wallPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is WallPostState.ErrorMessage -> {
                    binding.refreshLayout.isRefreshing = false
                    if (it.errorCode != 401) showToast(it.errorMessage)
                }
                is WallPostState.LoadingState -> {
                    binding.progressBar.visibility = if (it.isLoading && listFeedSetup.isEmpty()) View.VISIBLE else View.GONE
                }
                is WallPostState.ListTicker -> {
                    (requireActivity() as MainActivity).showTicker(it.listTicker)
                }
                is WallPostState.ListWall -> {
                    Timber.e("getWallFromCache> %s", it.list.size)
                    binding.refreshLayout.isRefreshing = false

                    playerMap.forEach { map -> map.third?.release() }
                    playerMap.clear()
                    listPausedVideos.clear()
                    if (listFeedSetup != it.list) {
                        if (::adapter.isInitialized) {
                            adapter.compositeDisposable.clear()
                            adapter.hashMapDisposable.clear()
                        }
                        listFeedSetup.clear()
                        listFeedSetup.addAll(it.list)
                        adapter.notifyDataSetChanged()
                        binding.cardReload.isVisible = Constants.SHOW_REFRESH_INDICATOR
                    }
                }
                is WallPostState.ClearOrAddObservables -> {
                    if (it.isToClear) clearObservables() else wallViewModel.getWallFromCache()
                }
                is WallPostState.CarouselPagingResponse -> {
                    if (adapter.list.size > it.pos) {
                        adapter.list[it.pos].isLastPage = it.isLastPage
                    }
                }
                is WallPostState.DisplayFriendsBadgeCount -> {
                    showBadge(it.showBadge)
                }
                else -> {}
            }
        }.autoDispose()
    }

    fun getNextCarousels(
        dataSource: String, page: Int, perPage: Int, position: Int, moduleType: String
    ) {
        wallViewModel.getNextCarousels(dataSource, page, perPage, position, moduleType)
    }

    fun scrollToTop() {
        try {
            binding.rvWall.smoothScrollToPosition(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAllThePlayers(modulePosition: Int, childPosition: Int) {
        if (::lastFeedChecker.isInitialized) lastFeedChecker.lastFeed(
            isWallFeed = false, stopPlayers = true
        )
        playerMap.filter { it.first != modulePosition || it.second != childPosition }.forEach {
            it.third?.let { exoPlayer ->
                if (exoPlayer.playWhenReady) exoPlayer.playWhenReady = false
            }
        }
    }

    fun isItemFirstVisible(pos: Int): Boolean {
        return getMostVisibleItemPosition() == pos
    }

    private fun clearObservables() {
        if (::adapter.isInitialized) {
            adapter.compositeDisposable.clear()
            adapter.hashMapDisposable.clear()
        }
        if (::wallViewModel.isInitialized) wallViewModel.compositeDisposableTemp.clear()
    }

    override fun onPause() {
        super.onPause()
        if (::lastFeedChecker.isInitialized) lastFeedChecker.lastFeed(
            isWallFeed = false/*Whatever value it can have*/, stopPlayers = true
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
            compositeDisposable.clear()
            if (::adapter.isInitialized) {
                adapter.compositeDisposable.clear()
                adapter.hashMapDisposable.clear()
            }
            if (::wallViewModel.isInitialized) {
                wallViewModel.compositeDisposable.clear()
                wallViewModel.compositeDisposableTemp.clear()
            }
            playerMap.forEach { it.third?.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        playerMap.clear()
        listPausedVideos.clear()
    }

    override fun onResume() {
        super.onResume()
        if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            showBadge(false)
            binding.loginInvite.visibility = View.VISIBLE
        } else {
            binding.loginInvite.visibility = View.GONE
            wallViewModel.pendingFriendsRequestCount()
        }
    }

    private fun showBadge(showBadge: Boolean) {
        if (requireActivity() is MainActivity) {
            (requireActivity() as MainActivity).showBadge(showBadge)
        }
    }
}