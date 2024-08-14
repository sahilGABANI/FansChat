package base.ui.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.CommonFeedItem
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentNewsBinding
import base.extension.getViewModelFromFactory
import base.extension.showToast
import base.extension.subscribeAndObserveOnMainThread
import base.ui.adapter.feed.NewsAdapter
import base.ui.base.BaseFragment
import base.ui.fragment.other.news.NewsPostState
import base.ui.fragment.other.news.NewsViewModel
import base.util.Analytics.trackClubTvSectionOpened
import base.util.Analytics.trackNewsSectionOpened
import base.util.Analytics.trackRumoursOpened
import base.util.Analytics.trackSocialSectionOpened
import base.util.CommonUtils
import base.util.Constants
import timber.log.Timber
import javax.inject.Inject

open class NewsFragment : BaseFragment() {

    private lateinit var adapter: NewsAdapter
    open val type = Constants.POST_TYPE_NEWS
    lateinit var binding: FragmentNewsBinding

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<NewsViewModel>

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private lateinit var newsViewModel: NewsViewModel
    private var listPosts: MutableList<CommonFeedItem> = mutableListOf()
    private var isAdapterAttaching = true
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var lastVisiblePos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        newsViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        if (!::binding.isInitialized) binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        /*   val display =
               (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
           val point = Point()
           display.getSize(point)
           videoSurfaceDefaultHeight = point.x
           screenDefaultHeight = point.y*/

        videoSurfaceDefaultHeight = CommonUtils.getDeviceWidth(requireContext())
        screenDefaultHeight = CommonUtils.getDeviceHeight(requireContext())

        when (type) {
            Constants.POST_TYPE_NEWS -> {
                trackNewsSectionOpened()
            }
            Constants.POST_TYPE_RUMOURS -> {
                trackRumoursOpened()
            }
            Constants.POST_TYPE_SOCIAL -> {
                trackSocialSectionOpened()
            }
            Constants.POST_TYPE_CLUB_TV -> {
                trackClubTvSectionOpened()
            }
        } //        listPosts = arrayListOf()
        if (!::adapter.isInitialized) {
            adapter = NewsAdapter(this, listPosts, type, loggedInUserCache)
            binding.list.adapter = adapter
            newsViewModel.getNewsFromCache(type, true)
        } else {
            newsViewModel.getNewsFromCache(type)
        }

        binding.refreshLayout.setOnRefreshListener {
            adapter.reset()
            newsViewModel.resetLoading(type)
        }

        listenToViewModel()

        binding.list.clearOnScrollListeners()
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    newsViewModel.loadMore(type)
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val startPosition: Int = getMostVisibleItemPosition()
                    lastVisiblePos = startPosition
                    Timber.e("Visible pos: $startPosition")
                    if (startPosition != -1) adapter.listenVisiblePos.value = startPosition
                }
            }
        })
    }

    private fun getMostVisibleItemPosition(): Int {
        var targetPosition: Int

        val startPosition =
            (binding.list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
        targetPosition = if (listPosts.size <= startPosition) startPosition
        else {
            val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition + 1)
            if (startPositionVideoHeight >= endPositionVideoHeight) startPosition else startPosition + 1
        }
        if (targetPosition < 0) targetPosition = 0

        Timber.tag("MostVisibleItemPosition").e("> $targetPosition")
        return targetPosition
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (binding.list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child: View = binding.list.getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) location[1] + videoSurfaceDefaultHeight //        else if (CommonUtils.isAppBarExpanded(binding.headerNews.appBarLayout)) (screenDefaultHeight - location[1] - CommonUtils.dpToPx(requireContext(), 152f)).roundToInt()
        else screenDefaultHeight - location[1]
    }

    private fun listenToViewModel() {
        newsViewModel.newsPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is NewsPostState.ErrorMessage -> {
                    binding.refreshLayout.isRefreshing = false
                    showToast(it.errorMessage) //                    binding.tvNoData.isVisible = listPosts.isEmpty()
                }
                is NewsPostState.LoadingState -> {
                    binding.progressBar.visibility =
                        if (it.isLoading && (it.isForceShow || listPosts.isEmpty())) View.VISIBLE else View.GONE
                }

                is NewsPostState.ListOfNewsPost -> {
                    binding.refreshLayout.isRefreshing = false
                    isAdapterAttaching = true
                    adapter.reset(lastVisiblePos)
                    listPosts.clear()
                    listPosts.addAll(it.wallFeed)
                    adapter.listPausedVideos.clear()
                    adapter.notifyDataSetChanged()

//                    if (listPosts.isNotEmpty()) binding.progressBar.visibility = View.GONE
                    binding.tvNoData.isVisible = listPosts.isEmpty()
                }
                is NewsPostState.ClearOrAddObservables -> {
                    if (it.isToClear) clearObservables() else newsViewModel.getNewsFromCache(type)
                }
            }
        }.autoDispose()
    }

    fun scrollToTop() {
        try {
            binding.list.smoothScrollToPosition(0)
            binding.headerNews.appBarLayout.setExpanded(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearObservables() {
        if (::newsViewModel.isInitialized) newsViewModel.compositeDisposableTemp.clear()
    }

    override fun onPause() {
        super.onPause()
        adapter.reset()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        if (::newsViewModel.isInitialized) {
            newsViewModel.compositeDisposable.clear()
            newsViewModel.compositeDisposableTemp.clear()
        }
        try {
            adapter.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}