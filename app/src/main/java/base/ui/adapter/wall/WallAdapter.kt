package base.ui.adapter.wall

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewpager.widget.ViewPager
import base.R
import base.data.api.authentication.LoggedInUserCache
import base.data.api.wall.WallCacheRepository
import base.data.model.wall.ContentItem
import base.data.model.wall.FeedSetupItem
import base.databinding.*
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.fragment.wall.WallFragment
import base.util.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import kotlin.collections.set
import kotlin.math.ceil

class WallAdapter(private val fragment: WallFragment, val list: ArrayList<FeedSetupItem>, private val wallCacheRepository: WallCacheRepository, private val loggedInUserCache: LoggedInUserCache) : Adapter<RecyclerView.ViewHolder>() {
    private var TYPE_BANNER = 0
    private var TYPE_GALLERY = 1
    private var TYPE_CAROUSEL = 2
    private var TYPE_PAGGER = 3
    private var TYPE_PORTRAIT_CAROUSEL = 4
    private var TYPE_FEED = 5
    private var PER_PAGE = 20
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    val hashMapDisposable = HashMap<Int, Disposable>()
    val compositeDisposable = CompositeDisposable()
    var listCurrentVisibleIndex = SparseIntArray()

    init {
        /*val display =
            (fragment.requireContext()
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight =
            point.y - fragment.resources.getDimensionPixelOffset(R.dimen._44sdp) - CommonUtils.dpToPx(
                fragment.requireContext(),
                56f
            ).toInt() //toolbar height
        */
        videoSurfaceDefaultHeight = CommonUtils.getDeviceWidth(fragment.requireContext())
        screenDefaultHeight = CommonUtils.getDeviceHeight(fragment.requireContext()) - fragment.resources.getDimensionPixelOffset(R.dimen._44sdp) - CommonUtils.dpToPx(fragment.requireContext(), 56f).toInt() //toolbar height
    }

    interface SwitchPager {
        fun switchNext()
    }

    interface LastFeedChecker {
        fun lastFeed(isWallFeed: Boolean, stopPlayers: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_BANNER -> return BannerHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_header_wall, parent, false))
            TYPE_GALLERY -> return NewsHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_wall_news, parent, false))
            TYPE_CAROUSEL -> return WatchNowHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_wall_watch_now, parent, false))
            TYPE_PAGGER -> return SocialHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_wall_social, parent, false))
            TYPE_PORTRAIT_CAROUSEL -> return StreamingHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_wall_streaming, parent, false))
        }
        return FeedHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_feed, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //var callback: ((List<ContentItem>) -> Unit)? = null
        //holder.itemView.id = position
        val wallObject = list[position]
        Timber.e("onBindViewHolder : $position")

        when (holder) {

            /*---------------BannerHolder START---------------*/

            is BannerHolder -> {
                var adapter: BannerPagerAdapter? = null
                holder.apply {

                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            list[layoutPosition].content.clear()
                            list[layoutPosition].content.addAll(posts)
                            hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())

                            if (adapter == null) {
                                adapter = BannerPagerAdapter(list[layoutPosition].content, fragment.fragmentManager, layoutPosition, object : SwitchPager {
                                    override fun switchNext() {
                                        var adIndex = binding.viewPagerBanner.currentItem
                                        if (binding.viewPagerBanner.adapter?.count!! - 1 == adIndex) {
                                            adIndex = 0
                                        } else {
                                            adIndex++
                                        }

                                        listCurrentVisibleIndex.put(layoutPosition, adIndex)
                                        binding.viewPagerBanner.setCurrentItem(adIndex, adIndex != 0)
                                    }
                                })
                                binding.viewPagerBanner.adapter = adapter
                            } else adapter!!.notifyDataSetChanged()

                            if (binding.viewPagerBanner.currentItem != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.viewPagerBanner.currentItem = listCurrentVisibleIndex.get(layoutPosition, 0)
                        }
                    }

                    binding.viewPagerBanner.setScrollDurationFactor(10.0)
                    binding.viewPagerBanner.clearOnPageChangeListeners()
                    binding.viewPagerBanner.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }

                        override fun onPageSelected(position: Int) {
                            listCurrentVisibleIndex.put(layoutPosition, position)
                            binding.viewPagerBanner.reMeasureCurrentPage(binding.viewPagerBanner.currentItem)
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            if (previousState == ViewPager.SCROLL_STATE_DRAGGING && state == ViewPager.SCROLL_STATE_SETTLING) {
                                binding.viewPagerBanner.setScrollDurationFactor(1.0)
                            } else if (previousState == ViewPager.SCROLL_STATE_SETTLING && state == ViewPager.SCROLL_STATE_IDLE) {
                                binding.viewPagerBanner.setScrollDurationFactor(10.0)
                            }
                            previousState = state
                        }
                    })
                }
            }

            /*---------------BannerHolder END---------------*/

            /*---------------WatchNowHolder START---------------*/

            is WatchNowHolder -> {
                var adapter: WatchNowPagerAdapter? = null
                holder.apply {

                    binding.apply {
                        setUpExtraViews(tvTitle, tvWatch, tvLike, tvComment, tvSubTitle, tvSeeAll, ivSeeAll, linRoot, rvTabs, linSeeAll, position)
                    }

                    binding.viewPager.setPadding(fragment.resources.getDimensionPixelSize(if (CommonUtils.isRtl(fragment.requireActivity())) R.dimen._70sdp else R.dimen.spacing_wall_horizontal), fragment.resources.getDimensionPixelSize(R.dimen._10sdp), fragment.resources.getDimensionPixelSize(if (CommonUtils.isRtl(fragment.requireActivity())) R.dimen.spacing_wall_horizontal else R.dimen._70sdp), fragment.resources.getDimensionPixelSize(R.dimen._8sdp))
                    binding.viewPager.pageMargin = fragment.resources.getDimensionPixelSize(R.dimen._30sdp)
                    /*    binding.viewPager.setPageTransformer(
                            true,
                            CarouselEffectTransformer(fragment.requireContext())
                        )*/

                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            list[layoutPosition].content.clear()
                            if (adapter != null) adapter!!.notifyDataSetChanged()
                            list[layoutPosition].content.addAll(posts)
                            if (WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).isNullOrEmpty()) {
                                hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())
                            }
                            binding.tvNoData.isVisible = list[layoutPosition].content.isEmpty()
                            binding.linContent.isInvisible = list[layoutPosition].content.isEmpty()

                            if (adapter == null) {
                                adapter = WatchNowPagerAdapter(list[layoutPosition].content, fragment.fragmentManager, layoutPosition/*, wallObject.dataSource*/)
                                binding.viewPager.adapter = adapter
                            } else adapter!!.notifyDataSetChanged()

                            if (binding.viewPager.currentItem != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.viewPager.currentItem = listCurrentVisibleIndex.get(layoutPosition, 0)

                            if (list[layoutPosition].content.isNotEmpty() && binding.viewPager.currentItem < list[layoutPosition].content.size) {
                                binding.tvWatch.text = "" + list[layoutPosition].content[binding.viewPager.currentItem].watchCount
                                binding.tvLike.text = "" + list[layoutPosition].content[binding.viewPager.currentItem].likeCount
                                binding.tvComment.text = "" + list[layoutPosition].content[binding.viewPager.currentItem].commentsCount
                                binding.tvSubTitle.text = "" + list[layoutPosition].content[binding.viewPager.currentItem].title
                            }
                        }
                    }

                    setUpTabs(position, wallObject, binding.rvTabs) {
                        notifyItemChanged(layoutPosition, WallFragment.listFiltersApplied)
                    }

                    binding.viewPager.clearOnPageChangeListeners()
                    binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                        override fun onPageSelected(position: Int) {
                            listCurrentVisibleIndex.put(layoutPosition, position)
                            if (list.size > -1 && list.size > layoutPosition && list[layoutPosition].content.size > -1 && list[layoutPosition].content.size > position) {
                                binding.tvWatch.text = "" + list[layoutPosition].content[position].watchCount
                                binding.tvLike.text = "" + list[layoutPosition].content[position].likeCount
                                binding.tvComment.text = "" + list[layoutPosition].content[position].commentsCount
                                binding.tvSubTitle.text = "" + list[layoutPosition].content[position].title
                            }
                            if (binding.viewPager.currentItem == binding.viewPager.adapter!!.count - 1) {
                                getNextCarousels(layoutPosition)
                            }
                        }

                        override fun onPageScrollStateChanged(state: Int) {}
                    })
                }
            }

            /*---------------WatchNowHolder END---------------*/

            /*---------------NewsHolder START---------------*/

            is NewsHolder -> {
                var adapter: NewsPagerAdapter? = null
                holder.apply {
                    binding.viewPagerNews.id = position + 1

                    binding.apply {
                        setUpExtraViews(tvTitle, null, null, null, null, tvSeeAll, ivSeeAll, linRoot, rvTabs, linSeeAll, position)
                    }

                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            list[layoutPosition].content.clear()
                            adapter?.notifyDataSetChanged() // java.lang.IllegalStateException: The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged
                            list[layoutPosition].content.addAll(posts)
                            if (WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).isNullOrEmpty()) {
                                hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())
                            }
                            binding.tvNoData.isVisible = list[layoutPosition].content.isEmpty()
                            binding.viewPagerNews.isInvisible = list[layoutPosition].content.isEmpty()
                            if (adapter == null) {
                                adapter = NewsPagerAdapter(list[layoutPosition].content, fragment.fragmentManager)
                                binding.viewPagerNews.adapter = adapter

                            } else adapter!!.notifyDataSetChanged()

                            if (binding.viewPagerNews.currentItem != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.viewPagerNews.currentItem = listCurrentVisibleIndex.get(layoutPosition, 0)
                        }
                    }

                    setUpTabs(position, wallObject, binding.rvTabs) {
                        listCurrentVisibleIndex.put(layoutPosition, 0)
                        notifyItemChanged(layoutPosition, WallFragment.listFiltersApplied)
                    }

                    binding.viewPagerNews.offscreenPageLimit = 1
                    binding.viewPagerNews.clearOnPageChangeListeners()
                    binding.viewPagerNews.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }

                        override fun onPageSelected(position: Int) {
                            listCurrentVisibleIndex.put(layoutPosition, position)
                            if (binding.viewPagerNews.currentItem == binding.viewPagerNews.adapter!!.count - 1) {
                                getNextCarousels(layoutPosition)
                            }
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                        }
                    })
                }
            }

            /*---------------NewsHolder END---------------*/

            /*---------------SocialHolder START---------------*/

            is SocialHolder -> {
                var adapter: WatchNowPagerAdapter? = null
                holder.apply {
                    binding.apply {

                        setUpExtraViews(tvTitle, tvWatch, tvLike, tvComment, tvSubTitle, tvSeeAll, ivSeeAll, linRoot, rvTabs, linSeeAll, position)
                    }

                    binding.viewPagerSocial.setPadding(fragment.resources.getDimensionPixelSize(R.dimen.spacing_wall_horizontal), fragment.resources.getDimensionPixelSize(R.dimen._10sdp), fragment.resources.getDimensionPixelSize(R.dimen.spacing_wall_horizontal), fragment.resources.getDimensionPixelSize(R.dimen._8sdp))
                    binding.viewPagerSocial.pageMargin = fragment.resources.getDimensionPixelSize(R.dimen._25sdp)
                    /* binding.viewPagerSocial.setPageTransformer(
                         true,
                         CarouselEffectTransformer(fragment.requireContext())
                     )*/

                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            list[layoutPosition].content.clear()
                            list[layoutPosition].content.addAll(posts)
                            if (WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).isNullOrEmpty()) {
                                hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())
                            }
                            binding.tvNoData.isVisible = list[layoutPosition].content.isEmpty()
                            binding.linContent.isInvisible = list[layoutPosition].content.isEmpty()
                            if (adapter == null) {
                                adapter = WatchNowPagerAdapter(list[layoutPosition].content, fragment.fragmentManager, layoutPosition/*, wallObject.dataSource*/)
                                binding.viewPagerSocial.adapter = adapter
                            } else adapter!!.notifyDataSetChanged()

                            if (binding.viewPagerSocial.currentItem != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.viewPagerSocial.currentItem = listCurrentVisibleIndex.get(layoutPosition, 0)
                            binding.linNext.isVisible = binding.viewPagerSocial.currentItem < binding.viewPagerSocial.adapter!!.count - 1

                            if (list[layoutPosition].content.isNotEmpty() && binding.viewPagerSocial.currentItem < list[layoutPosition].content.size) {
                                binding.tvWatch.text = "" + list[layoutPosition].content[binding.viewPagerSocial.currentItem].watchCount
                                binding.tvLike.text = "" + list[layoutPosition].content[binding.viewPagerSocial.currentItem].likeCount
                                binding.tvComment.text = "" + list[layoutPosition].content[binding.viewPagerSocial.currentItem].commentsCount
                                binding.tvSubTitle.text = "" + list[layoutPosition].content[binding.viewPagerSocial.currentItem].title
                            }
                        }
                    }

                    setUpTabs(position, wallObject, binding.rvTabs) {
                        notifyItemChanged(layoutPosition, WallFragment.listFiltersApplied)
                    }

                    binding.viewPagerSocial.clearOnPageChangeListeners()
                    binding.viewPagerSocial.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }

                        override fun onPageSelected(position: Int) {
                            binding.linNext.isVisible = binding.viewPagerSocial.currentItem < binding.viewPagerSocial.adapter!!.count - 1
                            listCurrentVisibleIndex.put(layoutPosition, position)
                            if (list.size > -1 && list.size > layoutPosition && list[layoutPosition].content.size > -1 && list[layoutPosition].content.size > position) {
                                binding.tvWatch.text = "" + list[layoutPosition].content[position].watchCount
                                binding.tvLike.text = "" + list[layoutPosition].content[position].likeCount
                                binding.tvComment.text = "" + list[layoutPosition].content[position].commentsCount
                                binding.tvSubTitle.text = "" + list[layoutPosition].content[position].title
                            }
                            if (binding.viewPagerSocial.currentItem == binding.viewPagerSocial.adapter!!.count - 1) {
                                getNextCarousels(layoutPosition)
                            }
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                        }
                    })

                    binding.linNext.onClick {
                        if (binding.viewPagerSocial.currentItem < binding.viewPagerSocial.adapter!!.count - 1) binding.viewPagerSocial.currentItem = binding.viewPagerSocial.currentItem + 1
                        else binding.viewPagerSocial.currentItem = 0
                    }
                }
            }

            /*---------------SocialHolder END---------------*/

            /*---------------StreamingHolder START---------------*/

            is StreamingHolder -> {
                var adapter: StreamingAdapter? = null
                holder.apply {

                    binding.apply {
                        setUpExtraViews(tvTitle, null, null, null, null, tvSeeAll, ivSeeAll, linRoot, rvTabs, linSeeAll, position)
                    }
                    (binding.rvStreaming.layoutManager as LinearLayoutManager).initialPrefetchItemCount = 3
                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            list[layoutPosition].content.clear()
                            list[layoutPosition].content.addAll(posts)
                            if (WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).isNullOrEmpty()) {
                                hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())
                            }
                            binding.tvNoData.isVisible = list[layoutPosition].content.isEmpty()
                            binding.rvStreaming.isVisible = list[layoutPosition].content.isNotEmpty()


                            if (adapter == null) {
                                adapter = StreamingAdapter(fragment, list[layoutPosition].content)
                                binding.rvStreaming.adapter = adapter
                            } else adapter!!.notifyDataSetChanged()

                            if ((binding.rvStreaming.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.rvStreaming.scrollToPosition(listCurrentVisibleIndex.get(layoutPosition, 0))
                        }
                    }
                    setUpTabs(position, wallObject, binding.rvTabs) {
                        notifyItemChanged(layoutPosition, WallFragment.listFiltersApplied)
                    }

                    binding.rvStreaming.clearOnScrollListeners()
                    binding.rvStreaming.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            listCurrentVisibleIndex.put(layoutPosition, (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition())
                        }

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (!recyclerView.canScrollHorizontally(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                                getNextCarousels(layoutPosition)
                            }
                        }
                    })
                }
            }

            /*---------------StreamingHolder END---------------*/

            /*---------------FeedHolder START---------------*/

            is FeedHolder -> {
                var adapter: WallFeedAdapter? = null
                holder.apply {
                    binding.apply {
                        setUpExtraViews(null, null, null, null, null, null, null, linRoot, rvTabs, null, position)
                    }
                    fetchContent(position, wallObject) { posts ->
                        if (layoutPosition != -1) {
                            if (list[layoutPosition].content.size != posts.size) {
                                if (adapter != null) adapter?.reset()
                            }
                            list[layoutPosition].content.clear()
                            list[layoutPosition].content.addAll(posts)
                            if (WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).isNullOrEmpty()) {
                                hideShowItemView(holder.itemView, list[layoutPosition].content.isNotEmpty())
                            }
                            binding.tvNoData.isVisible = list[layoutPosition].content.isEmpty()
                            binding.rvFeedWall.isVisible = list[layoutPosition].content.isNotEmpty()

                            if (adapter == null) {
                                adapter = WallFeedAdapter(fragment, list[layoutPosition].content, loggedInUserCache, layoutPosition)
                                binding.rvFeedWall.adapter = adapter
                            } else {
                                adapter!!.notifyDataSetChanged()
                            }

                            if ((binding.rvFeedWall.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != listCurrentVisibleIndex.get(layoutPosition, 0) && listCurrentVisibleIndex.get(layoutPosition, 0) < list[layoutPosition].content.size) binding.rvFeedWall.smoothScrollToPosition(listCurrentVisibleIndex.get(layoutPosition, 0))
                        }
                    }

                    setUpTabs(position, wallObject, binding.rvTabs) {
                        notifyItemChanged(layoutPosition, WallFragment.listFiltersApplied)
                    }

                    binding.rvFeedWall.clearOnScrollListeners()
                    fragment.lastFeedChecker = object : LastFeedChecker {
                        override fun lastFeed(isWallFeed: Boolean, stopPlayers: Boolean) {
                            if (stopPlayers) {
                                if (adapter != null) adapter?.reset()
                            } else if (isWallFeed) {
                                var firstVisible = list[layoutPosition].content.size
                                var lastVisible = 0

                                list[layoutPosition].content.forEachIndexed { index, contentItem ->
                                    if (isItemVisible(binding.rvFeedWall, index)/* > 0*/) {
                                        if (index <= firstVisible) firstVisible = index

                                        if (lastVisible < index) lastVisible = index
                                    }
                                }
                                if (list.size > layoutPosition) {
                                    if (list[layoutPosition].content.size > firstVisible + 1 && getItemVisibleHeight(binding.rvFeedWall, firstVisible + 1) > getItemVisibleHeight(binding.rvFeedWall, firstVisible)) {
                                        firstVisible += 1
                                    }
                                    if (list[layoutPosition].content.size > firstVisible) {
                                        listCurrentVisibleIndex.put(layoutPosition, firstVisible)
                                        if (adapter != null) adapter?.listenVisiblePos?.value = firstVisible
                                    }
                                }
                                if (adapter != null && lastVisible == adapter?.itemCount!!.minus(1)) {
                                    getNextCarousels(layoutPosition)
                                    Timber.e("$firstVisible/$lastVisible : can scroll more > No")
                                }
                            } else {
                                if (adapter != null) adapter?.listenVisiblePos?.value = -1 //Reset
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchContent(position: Int, wallObject: FeedSetupItem, callback: ((List<ContentItem>) -> Unit)?) {
        if (hashMapDisposable.containsKey(position)) {
            hashMapDisposable[position]?.dispose()
            hashMapDisposable.remove(position)
        }
        if (callback != null) {
            wallCacheRepository.getContentList(wallObject.dataSource, wallObject.title/*type*/, WallFragment.listFiltersApplied.get(position, arrayListOf())).subscribeOnIoAndObserveOnMainThread({
//                Timber.e(">> getContentList >$position : ${it.size}")
                it.let { posts ->
                    if (list.isNotEmpty()) {
                        callback.invoke(posts)
                    }
                }
            }, {
                Timber.e(it)
            }).autoDispose(position)
        }
    }

    private fun RecyclerView.ViewHolder.setUpTabs(position: Int, wallObject: FeedSetupItem, rvTabs: RecyclerView, callback: (() -> Unit)) {
        if (wallObject.filters.isNotEmpty()) {
            rvTabs.adapter = TabAdapter(wallObject.filters, WallFragment.listFiltersApplied.get(position, arrayListOf()).toMutableList(), if (wallObject.filterSelectedColor != null) Color.parseColor(wallObject.filterSelectedColor) else Color.WHITE, Color.parseColor(wallObject.filterColor)) { pos: Int, isChecked: Boolean ->
                if (isChecked) WallFragment.listFiltersApplied.put(layoutPosition, WallFragment.listFiltersApplied.get(layoutPosition, arrayListOf()).apply { add(list[layoutPosition].filters[pos]) })
                else {
                    WallFragment.listFiltersApplied[layoutPosition].remove(list[layoutPosition].filters[pos])
                    if (WallFragment.listFiltersApplied[layoutPosition].isNullOrEmpty()) WallFragment.listFiltersApplied.remove(layoutPosition)
                }
                callback.invoke()
            }
        }
    }

    private fun RecyclerView.ViewHolder.setUpExtraViews(tvTitle: TextView?, tvWatch: TextView?, tvLike: TextView?, tvComment: TextView?, tvSubTitle: TextView?, tvSeeAll: TextView?, ivSeeAll: ImageView?, linRoot: LinearLayout?, rvTabs: RecyclerView?, linSeeAll: LinearLayout?, position: Int) {

        tvTitle?.text = list[position].title

        if (list[position].content.isNotEmpty()) {
            tvWatch?.text = "" + list[position].content[0].watchCount
            tvLike?.text = "" + list[position].content[0].likeCount
            tvComment?.text = "" + list[position].content[0].commentsCount
            tvSubTitle?.text = "" + list[position].content[0].title
        }

        list[position].titleColor?.let {
            tvTitle?.setTextColor(Color.parseColor(it))
        }
        list[position].seeAllColor?.let {
            tvSeeAll?.setTextColor(Color.parseColor(it))
            ivSeeAll?.setColorFilter(Color.parseColor(it), PorterDuff.Mode.SRC_ATOP)
        }
        list[position].background?.let {
            linRoot?.setBackgroundColor(Color.parseColor(list[position].background))
        }
        rvTabs?.isVisible = list[position].filters.isNotEmpty()
        linSeeAll?.onClick {
            clickedSeeAll(list[layoutPosition].seeAll)
        }
    }

    private fun clickedSeeAll(seeAll: String?) {
        when (seeAll) {
            Constants.SEE_ALL_POST_TYPE_NEWS -> {
                fragment.open(R.id.news)
            }
            Constants.SEE_ALL_POST_TYPE_SOCIAL -> {
                fragment.open(R.id.social)
            }
            Constants.SEE_ALL_POST_TYPE_RUMOURS -> {
                fragment.open(R.id.rumours)
            }
            Constants.SEE_ALL_POST_TYPE_CLUB_TV -> {
                fragment.open(R.id.tv)
            }
        }
    }

    private fun getItemVisibleHeight(rvWall: RecyclerView, playPosition: Int): Int {
        val at = playPosition - (rvWall.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child: View = rvWall.getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    fun isItemVisible(rvWall: RecyclerView, playPosition: Int): Boolean {
        val at = playPosition - (rvWall.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val view: View = (rvWall.getChildAt(at) ?: return false)

        if (!view.isShown) {
            return false
        }
        val actualPosition = Rect()
        view.getGlobalVisibleRect(actualPosition)
        val screen = Rect(0, 0, CommonUtils.getDeviceWidth(fragment.requireContext()), CommonUtils.getDeviceHeight(fragment.requireContext()))
        return actualPosition.intersect(screen)
    }

    private fun getNextCarousels(position: Int) { //        fragment.toast("Loading..")
        Timber.e("isLastPage > $position:" + list[position].isLastPage)
        if (WallFragment.listFiltersApplied.get(position, arrayListOf()).isNullOrEmpty() && !list[position].isLastPage) fragment.getNextCarousels(list[position].dataSource, ceil(list[position].content.size.toDouble() / PER_PAGE).toInt() + 1, PER_PAGE, position, list[position].title/*type*/)
    }

    private fun hideShowItemView(itemView: View, toShow: Boolean) {
        itemView.isVisible = toShow
        itemView.layoutParams.height = if (toShow) ViewGroup.LayoutParams.WRAP_CONTENT else 0
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].type) {
            Constants.Type_Banner -> TYPE_BANNER
            Constants.Type_Gallery -> TYPE_GALLERY
            Constants.Type_Carousel -> TYPE_CAROUSEL
            Constants.Type_Pager -> TYPE_PAGGER
            Constants.Type_PortraitCarousel -> TYPE_PORTRAIT_CAROUSEL
            else -> TYPE_FEED
        }
    }

    fun Disposable.autoDispose(position: Int) {
        hashMapDisposable[position] = this
        compositeDisposable.add(this)
    }

    class FeedHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutFeedBinding = LayoutFeedBinding.bind(view)
    }

    class BannerHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var previousState = 0
        val binding: LayoutHeaderWallBinding = LayoutHeaderWallBinding.bind(view)
    }

    class WatchNowHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutWallWatchNowBinding = LayoutWallWatchNowBinding.bind(view)
    }

    class NewsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutWallNewsBinding = LayoutWallNewsBinding.bind(view)
    }

    class SocialHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutWallSocialBinding = LayoutWallSocialBinding.bind(view)
    }

    class StreamingHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutWallStreamingBinding = LayoutWallStreamingBinding.bind(view)
    }

/*
    private class CarouselEffectTransformer(context: Context) : ViewPager.PageTransformer {
        private val maxTranslateOffsetX: Int = context.dp2px(180f)
        private var viewPager: ViewPager? = null
        override fun transformPage(view: View, position: Float) {
            if (viewPager == null) {
                viewPager = view.parent as ViewPager
            }
            val leftInScreen = view.left - viewPager!!.scrollX
            val centerXInViewPager = leftInScreen + view.measuredWidth / 2
            val offsetX = centerXInViewPager - viewPager!!.measuredWidth / 2
            val offsetRate = offsetX.toFloat() * 0.15f / viewPager!!.measuredWidth
            val scaleFactor = 1 - abs(offsetRate)
            if (scaleFactor > 0) {
                view.scaleX = scaleFactor
//                view.scaleY = scaleFactor
                view.translationX = -maxTranslateOffsetX * offsetRate
            }
            ViewCompat.setElevation(view, scaleFactor)
        }
    }
*/
}