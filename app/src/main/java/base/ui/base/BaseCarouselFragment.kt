package base.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import base.FansChatApplication
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.wall.ContentItem
import base.ui.MainActivity
import base.ui.adapter.wall.WallAdapter
import base.ui.fragment.wall.WallFragment
import base.ui.fragment.wall.WatchNowWallPagerFragment
import base.util.Constants
import base.util.onClick
import base.util.open
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import javax.inject.Inject

open class BaseCarouselFragment(open var switchPager: WallAdapter.SwitchPager? = null) :
    BaseFragment() {
    private lateinit var contentItem: ContentItem
    private var player: ExoPlayer? = null
    private var groupPosition: Int = 0
    private var pos: Int = 0
    private var handler = Handler(Looper.getMainLooper())
    private var bannerHandler: Handler = Handler(Looper.getMainLooper())
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupPosition = requireArguments().getInt("groupPosition", 0)
        pos =
            requireArguments().getInt(
                "pos",
                0
            ) //type = requireArguments().getString("type", Constants.POST_TYPE_WALL)
        contentItem = requireArguments().getSerializable("contentItem") as ContentItem
    }

    protected open fun getImageViewRef(): ImageView? {
        return null
    }

    protected open fun getVideoRootRef(): FrameLayout? {
        return null
    }

    protected open fun getPlayerViewRef(): StyledPlayerView? {
        return null
    }

    protected open fun getPlayPauseViewRef(): AppCompatImageView? {
        return null
    }

    protected open fun getProgressBarRef(): ProgressBar? {
        return null
    }

    protected open fun getClickableViewRef(): LinearLayout? {
        return null
    }

    protected open fun getRootViewRef(): CardView? {
        return null
    }

    protected fun setUpViews() {
        httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        Glide.with(FansChat.context).load(contentItem.imageUrl).centerCrop()
            .error(R.drawable.placeholder)
            .placeholder(R.drawable.placeholder).into(getImageViewRef()!!)

        if (contentItem.isVideoPost()) {
            getPlayPauseViewRef()!!.onClick {
                if (player?.isPlaying == true) {
                    if (WallFragment.listPausedVideos.containsKey(groupPosition)) {
                        WallFragment.listPausedVideos.getValue(groupPosition).add(pos)
                    } else WallFragment.listPausedVideos[groupPosition] = arrayListOf(pos)
                    pause()
                } else {
                    if (WallFragment.listPausedVideos.containsKey(groupPosition)) {
                        WallFragment.listPausedVideos.getValue(groupPosition).let {
                            if (it.size > 1) it.remove(pos)
                            else WallFragment.listPausedVideos.remove(groupPosition)
                        }
                    }
                    play()
                }
            }
            getClickableViewRef()!!.onClick {
                if (!getProgressBarRef()!!.isVisible) {
                    handler.removeCallbacksAndMessages(null)
                    getPlayPauseViewRef()!!.isVisible = true
                    if (player != null && player?.playWhenReady == true) handler.postDelayed({
                        getPlayPauseViewRef()!!.isVisible = false
                    }, Constants.DELAY_CONTROLLER_HIDE)
                }
            }

            initializePlayer()
        } /*else {
            Glide.with(requireActivity()).load(contentItem.imageUrl).centerCrop()
                .error(R.drawable.placeholder).placeholder(R.drawable.placeholder)
                .into(getImageViewRef()!!)
        }*/

        getRootViewRef()?.onClick {
            if (this is WatchNowWallPagerFragment) {/*if (loggedInUserCache.getLoginUserToken().isNullOrEmpty())
                    open(R.id.authDecideFragment)
                else*/
                contentItem._id.let {
                    open(R.id.details, hashMapOf("postId" to it, "type" to contentItem.type))
                }
            }
        }
        getVideoRootRef()!!.isVisible =
            contentItem.isVideoPost() //contentItem.type!!.equals(Constants.POST_TYPE_VIDEO)
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(requireContext()).build()
            getPlayerViewRef()!!.player = player
            val mediaItem = MediaItem.fromUri(contentItem.videoUrl ?: "")
            player!!.setMediaSource(buildMediaSource(mediaItem))

            player!!.addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    handler.removeCallbacksAndMessages(null)
                    getPlayPauseViewRef()!!.setImageResource(R.drawable.ic_play)
                    getPlayPauseViewRef()!!.isVisible = true
                    getProgressBarRef()!!.isVisible = false
                    error.printStackTrace()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(
                        playWhenReady,
                        reason
                    ) //Timber.tag("onPlayWhenReadyChanged").e("Module: $groupPosition Child: $pos > $playWhenReady")

                    if (playWhenReady && player!!.playbackState == Player.STATE_READY) {
                        (requireActivity() as MainActivity).requestAudioFocus() // media actually playing
                        getPlayPauseViewRef()!!.setImageResource(R.drawable.ic_pause_black)
                        if (getPlayPauseViewRef()!!.isVisible) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                getPlayPauseViewRef()!!.isVisible = false
                            }, Constants.DELAY_CONTROLLER_HIDE)
                        }
                    } else if (playWhenReady) {
                        (requireActivity() as MainActivity).requestAudioFocus() // might be idle (plays after prepare()),
                        // buffering (plays when data available)
                        // or ended (plays when seek away from end)
                    } else {
                        (requireActivity() as MainActivity).leaveAudioFocus() // getPlayPauseViewRef()!!.volume = 1f
                        // player paused in any state
                        getPlayPauseViewRef()!!.setImageResource(R.drawable.ic_play)
                        handler.removeCallbacksAndMessages(null)
                        getPlayPauseViewRef()!!.isVisible = true
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        ExoPlayer.STATE_IDLE -> {
                        }
                        ExoPlayer.STATE_BUFFERING -> {
                            getProgressBarRef()!!.isVisible = true
                            getPlayPauseViewRef()!!.isVisible =
                                false // Timber.tag("onPlaybackStateChanged").e("Module: $groupPosition Child: $pos >Buffering video")
                        }
                        ExoPlayer.STATE_READY -> {
                            handler.removeCallbacksAndMessages(null)
                            getPlayPauseViewRef()!!.setImageResource(if (player!!.playWhenReady) R.drawable.ic_pause_black else R.drawable.ic_play)
                            getProgressBarRef()!!.isVisible = false
                            getPlayPauseViewRef()!!.isVisible =
                                false //Timber.tag("onPlaybackStateChanged").e("Module: $groupPosition Child: $pos >Ready to play")
                        }
                        ExoPlayer.STATE_ENDED -> {
                            handler.removeCallbacksAndMessages(null)
                            getPlayPauseViewRef()!!.setImageResource(R.drawable.ic_play)
                            player!!.seekTo(0)
                            if (switchPager != null) switchPager!!.switchNext() //Timber.tag("onPlaybackStateChanged").e("Module: $groupPosition Child: $pos >Video ended")

                        }
                    }
                }
            })

            player!!.prepare()
            val indexInMap =
                WallFragment.playerMap.indexOfFirst { it.first == groupPosition && it.second == pos }

            if (indexInMap == -1) //not added yet
                WallFragment.playerMap.add(Triple(groupPosition, pos, player))
            else {
                player!!.seekTo(
                    WallFragment.playerMap[indexInMap].third?.currentWindowIndex!!,
                    WallFragment.playerMap[indexInMap].third?.currentPosition!!
                )
                WallFragment.playerMap[indexInMap] = Triple(groupPosition, pos, player)
            }
        }
        val isVideoFocusing =
            isVisible && ((parentFragment as NavHostFragment).childFragmentManager.primaryNavigationFragment as WallFragment).isItemFirstVisible(
                groupPosition
            )
//        Timber.e("$groupPosition:$pos:>>$isVideoFocusing")
        if (isVideoFocusing) { //First pause other players
            ((parentFragment as NavHostFragment).childFragmentManager.primaryNavigationFragment as WallFragment).stopAllThePlayers(
                groupPosition,
                pos
            )
            if (!WallFragment.listPausedVideos.containsKey(groupPosition) || !WallFragment.listPausedVideos[groupPosition]?.contains(
                    pos
                )!!
            ) player!!.playWhenReady = true
        } //player!!.seekTo(currentWindow, playbackPosition)
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory().setCache(FansChatApplication.simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
    }

    private fun play() {
        if (player != null) { //First pause other players
            ((parentFragment as NavHostFragment).childFragmentManager.primaryNavigationFragment as WallFragment).stopAllThePlayers(
                groupPosition,
                pos
            )
            if (player!!.playbackState == Player.STATE_ENDED) {
                player!!.seekTo(0)
            }

            if (player!!.playWhenReady && !player!!.isPlaying) {
                handler.removeCallbacksAndMessages(null)
                getPlayPauseViewRef()!!.isVisible = false
                getProgressBarRef()!!.isVisible = true
                handler.postDelayed({
                    getPlayPauseViewRef()!!.isVisible = true
                    getProgressBarRef()!!.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            } else {
                player!!.playWhenReady = true
                playPauseVideo(true)
            }
        }
    }

    private fun pause() {
        if (player != null) {
            try {
                player!!.playWhenReady = false
            } catch (e: Exception) {
//                e.printStackTrace()
            } finally {
                playPauseVideo(false)
            }
        }
    }

    private fun playPauseVideo(isPlaying: Boolean) {
        handler.removeCallbacksAndMessages(null)
        getPlayPauseViewRef()!!.isVisible = true
        getPlayPauseViewRef()!!.setImageResource(if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play)
        if (isPlaying) handler.postDelayed({
            getPlayPauseViewRef()!!.isVisible = false
        }, Constants.DELAY_CONTROLLER_HIDE)
    }

    private fun releasePlayer() {
        player?.release()
        player =
            null //        WallFragment.playerMap.removeAll { it.first == groupPosition && it.second == pos }
    }

    override fun onResume() {
        super.onResume()
        if (contentItem.isVideoPost()) initializePlayer()
        else if (switchPager != null) {
            bannerHandler.removeCallbacksAndMessages(null)
            bannerHandler.postDelayed(
                { if (isAdded && isVisible) switchPager!!.switchNext() },
                Constants.DELAY_WALL_BANNER
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (contentItem.isVideoPost()) pause()
        else if (switchPager != null) bannerHandler.removeCallbacksAndMessages(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (contentItem.isVideoPost()) releasePlayer()
        else if (switchPager != null) bannerHandler.removeCallbacksAndMessages(null)
    }
}