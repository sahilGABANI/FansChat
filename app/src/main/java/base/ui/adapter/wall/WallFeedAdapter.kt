package base.ui.adapter.wall

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import base.FansChatApplication
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.wall.ContentItem
import base.databinding.ItemFeedWallBinding
import base.extension.subscribeAndObserveOnMainThread
import base.ui.MainActivity
import base.ui.fragment.wall.WallFragment
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class WallFeedAdapter(
    val fragment: WallFragment,
    val items: List<ContentItem>,
    val loggedInUserCache: LoggedInUserCache,
    private val modulePosition: Int
) : RecyclerView.Adapter<WallFeedAdapter.ViewHolder>() {

    private var playerMap: ArrayList<Triple<Int, Int, Long>> = arrayListOf()
    private var player: ExoPlayer? = null
    private val width = fragment.resources.displayMetrics.widthPixels
    private val height by lazy {
        (if (MainActivity.height == 0) fragment.resources.displayMetrics.heightPixels else MainActivity.height) - (CommonUtils.dpToPx(
            fragment.requireContext(),
            118f
        ) + fragment.resources.getDimensionPixelOffset(R.dimen._56sdp))//52sdp
    }
    private var childVisiblePosition = -1
    private var lastVisiblePosition = -1
    val listenVisiblePos: MutableLiveData<Int> = MutableLiveData()
    private var ivPlayPause: AppCompatImageView? = null
    private var progressBar: ProgressBar? = null
    private var videoView: StyledPlayerView? = null
    private var defaultSize = fragment.resources.getDimensionPixelOffset(R.dimen._170sdp)
    private var httpDataSourceFactory: HttpDataSource.Factory =
        DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)

    init { //Observe index to auto play, stop other player if playing and start playing on new index
        listenVisiblePos.observe(fragment) {
            playForceFully(it)
        }
        Constants.liveDataAudioFocusObserver.subscribeAndObserveOnMainThread {
            try {
                if (player != null && player!!.isPlaying) player!!.playWhenReady = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_feed_wall, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = items[position]
            if (item.isVideoPost()) {
                holder.apply { setUpPlayerView(position) }
            }
        } else super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]

        holder.apply {
            binding.title.text = /*"$position" +*/ item.title
            binding.date.isVisible = !loggedInUserCache.getLoginUserToken().isNullOrEmpty()
            binding.likesCount.text = item.likeCount.toString()
            binding.commentsCount.text = item.commentsCount.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val date = item.created?.toRelative()
                withContext(Dispatchers.Main) {
                    binding.date.text = date
                }
            }

            //>= 1? Landscape : portrait
            var imageViewHeight = width / item.coverAspectRatio

            val extraHeight = getTitleHeight(binding.title, binding.linText)
            if (imageViewHeight > height - extraHeight) {
                imageViewHeight = (height - extraHeight).toDouble()
            } else if (item.coverAspectRatio < .2 && imageViewHeight < defaultSize) {
                imageViewHeight = defaultSize.toDouble()
            }

            if (item.imageUrl?.isNotBlank() == true) {
                binding.image.layoutParams.height = imageViewHeight.roundToInt()
                binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
//                binding.image.backgroundResource = R.drawable.placeholder_flex
                Glide.with(FansChat.context).load(item.imageUrl)
                    .override(width, imageViewHeight.roundToInt())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.image.setBackgroundResource(if (item.isVideoPost()) R.color.colorPrimary else R.drawable.placeholder_flex)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.image.setBackgroundResource(R.color.colorPrimary)
                            return false
                        }

                    }).into(binding.image)
            } else {
                binding.image.layoutParams.height = defaultSize
                binding.image.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.image.setImageResource(R.drawable.placeholder)
            }

            binding.flVideo.isVisible = item.isVideoPost()

            if (item.isVideoPost()) {
                binding.flVideo.layoutParams.height = imageViewHeight.roundToInt()
                setUpPlayerView(position)
            }

            val author = item.owner
            author?.let {
                binding.name.text = author.displayName
                Glide.with(FansChat.context).load(author.avatarUrl).override(50, 50)
                    .placeholder(R.drawable.avatar_placeholder).error(R.drawable.avatar_placeholder)
                    .into(binding.avatar)
//                binding.sharedUsername.text = author.displayName
            } ?: kotlin.run {
                binding.name.text = ""
                binding.avatar.setImageResource(R.drawable.avatar_placeholder)
//                binding.sharedUsername.text = ""
            }
            /*  binding.messageContainer.isVisible =
                  false //author != null todo removed as of now due to pin post params are not there*/
            itemView.onClick {
                item._id.let {
                    itemView.open(
                        R.id.details,
                        hashMapOf("postId" to it, "type" to item.type/*Constants.POST_TYPE_WALL*/)
                    )
                }
            }
            binding.ivPlayPause.onClick {
                when {
                    binding.videoView.player == null -> { //playForceFully(position, true)
                        initializePlayer()
                        childVisiblePosition = position
                        if (lastVisiblePosition != -1 && lastVisiblePosition != position) {
                            removePlayer(lastVisiblePosition)
                        }
                        lastVisiblePosition = position
                        if (childVisiblePosition != -1 && items.size > childVisiblePosition && items[childVisiblePosition].isVideoPost()) {
                            if (WallFragment.listPausedVideos.containsKey(modulePosition)) {
                                WallFragment.listPausedVideos.getValue(modulePosition).let {
                                    if (it.size > 1) it.remove(childVisiblePosition)
                                    else WallFragment.listPausedVideos.remove(modulePosition)
                                }
                            }
                            setUpPlayerView(position, true)
                        }
                    }
                    player?.isPlaying == true -> {
                        if (WallFragment.listPausedVideos.containsKey(modulePosition)) {
                            WallFragment.listPausedVideos.getValue(modulePosition)
                                .add(childVisiblePosition)
                        } else WallFragment.listPausedVideos[modulePosition] =
                            arrayListOf(childVisiblePosition)
                        pause()
                    }
                    else -> {
                        if (WallFragment.listPausedVideos.containsKey(modulePosition)) {
                            WallFragment.listPausedVideos.getValue(modulePosition).let {
                                if (it.size > 1) it.remove(childVisiblePosition)
                                else WallFragment.listPausedVideos.remove(modulePosition)
                            }
                        }
                        play()
                    }
                }
            }
            binding.llClickable.onClick {
                handler.removeCallbacksAndMessages(null)
                binding.ivPlayPause.isVisible = true
                if (player != null && player?.playWhenReady == true) handler.postDelayed({
                    binding.ivPlayPause.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            }
        }
    }

    private fun getTitleHeight(v: View, parent: View): Int {
        v.measure(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return v.measuredHeight
    }

    private fun playForceFully(position: Int) {
        if (childVisiblePosition != position) {
            initializePlayer()
            childVisiblePosition =
                position //            Timber.e("lastVisiblePosition> $childVisiblePosition / childVisiblePosition> $position")
            if (lastVisiblePosition != -1 && lastVisiblePosition != position) {
                removePlayer(lastVisiblePosition)
            }
            lastVisiblePosition = position
            if (childVisiblePosition != -1) {
                if (items.size > childVisiblePosition) {
                    if (items[childVisiblePosition].isVideoPost()) notifyItemChanged(
                        childVisiblePosition,
                        listOf("")
                    )
                    else { //It is image/text post so remove older one player
                        removePlayer(childVisiblePosition)
                        releasePlayer()
                    }
                }
            } else { //Other module focused, so stop playing
                removePlayer(childVisiblePosition)
                releasePlayer()
            }
        }
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory().setCache(FansChatApplication.simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
    }

    //Set player to player view and start playing else stop playing & remove player
    private fun ViewHolder.setUpPlayerView(pos: Int, isForced: Boolean = false) {
        val isVideoFocusing =
            childVisiblePosition == pos && (isForced || ((fragment.parentFragment as NavHostFragment).childFragmentManager.primaryNavigationFragment as WallFragment).isItemFirstVisible(
                modulePosition
            ))
//        Timber.e(">>$modulePosition:$childVisiblePosition:$pos>>$isVideoFocusing")
        if (isVideoFocusing) {

            initializePlayer()
//            binding.videoView.isInvisible = false
            ivPlayPause = binding.ivPlayPause
            progressBar = binding.progressBar
            videoView = binding.videoView

            binding.videoView.player = player
            val mediaItem = MediaItem.fromUri(items[pos].videoUrl ?: "")
            player!!.setMediaSource(buildMediaSource(mediaItem))
            player!!.prepare()

            val indexInMap = playerMap.indexOfFirst { it.first == pos }
            if (indexInMap == -1) //not added yet
                playerMap.add(Triple(pos, player?.currentWindowIndex!!, player?.currentPosition!!))
            else {
                player!!.seekTo(playerMap[indexInMap].second, playerMap[indexInMap].third)
                playerMap[indexInMap] =
                    Triple(pos, player?.currentWindowIndex!!, player?.currentPosition!!)
            }

            stopOtherPlayers()
            if (!WallFragment.listPausedVideos.containsKey(modulePosition) || !WallFragment.listPausedVideos[modulePosition]?.contains(
                    pos
                )!!
            ) player?.playWhenReady = true
        } else {
            binding.videoView.isInvisible = true
            if (binding.videoView.player != null) {
                val indexInMap = playerMap.indexOfFirst { it.first == pos }
                if (indexInMap == -1) //not added yet
                    playerMap.add(
                        Triple(
                            pos,
                            binding.videoView.player?.currentWindowIndex!!,
                            binding.videoView.player?.currentPosition!!
                        )
                    )
                else {
                    playerMap[indexInMap] = Triple(
                        pos,
                        binding.videoView.player?.currentWindowIndex!!,
                        binding.videoView.player?.currentPosition!!
                    )
                }
                binding.videoView.player!!.playWhenReady = false
                binding.videoView.player = null
                binding.ivPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(fragment.requireContext()).build()
            player!!.addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    ivPlayPause?.setImageResource(R.drawable.ic_play)
                    progressBar?.isVisible = false
                    ivPlayPause?.isVisible = true
                    if (childVisiblePosition != -1) removePlayer(childVisiblePosition)
                    error.printStackTrace()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(
                        playWhenReady,
                        reason
                    ) //                    Timber.tag("onPlayWhenReadyChanged").e("Module: $modulePosition Child: $childVisiblePosition > $playWhenReady")

                    if (playWhenReady && player!!.playbackState == Player.STATE_READY) {
                        (fragment.requireContext() as MainActivity).requestAudioFocus()
                        ivPlayPause?.setImageResource(R.drawable.ic_pause_black)
                    } else if (playWhenReady) {
                        (fragment.requireContext() as MainActivity).requestAudioFocus()
                    } else {
                        (fragment.requireContext() as MainActivity).leaveAudioFocus()
                        ivPlayPause?.setImageResource(R.drawable.ic_play)
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        ExoPlayer.STATE_IDLE -> {
                        }
                        ExoPlayer.STATE_BUFFERING -> {
                            progressBar?.isVisible = true
                            ivPlayPause?.isVisible = false
                        }
                        ExoPlayer.STATE_READY -> {
                            ivPlayPause?.setImageResource(if (player!!.playWhenReady) R.drawable.ic_pause_black else R.drawable.ic_play)
                            progressBar?.isVisible = false
                            ivPlayPause?.isVisible = !player!!.playWhenReady
                            videoView?.isInvisible = false
                        }
                        ExoPlayer.STATE_ENDED -> {
                            ivPlayPause?.setImageResource(R.drawable.ic_play)
                            player!!.seekTo(0)
                        }
                    }
                }
            })
        }
    }

    private fun ViewHolder.play() {
        if (player != null) {
            stopOtherPlayers()
            if (player!!.playbackState == Player.STATE_ENDED) {
                player!!.seekTo(0)
            }
            if (player!!.playWhenReady && !player!!.isPlaying) {
                handler.removeCallbacksAndMessages(null)
                binding.ivPlayPause.isVisible = false
                binding.progressBar.isVisible = true
                handler.postDelayed({
                    binding.ivPlayPause.isVisible = true
                    binding.progressBar.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            } else {
                player!!.playWhenReady = true
                playPauseVideo(true)
            }
        }
    }

    private fun ViewHolder.pause() {
        if (player != null) {
            try {
                player!!.playWhenReady = false
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                playPauseVideo(false)
            }
        }
    }

    private fun ViewHolder.playPauseVideo(isPlaying: Boolean) {
        handler.removeCallbacksAndMessages(null)
        binding.ivPlayPause.isVisible = true
        binding.ivPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play)
        if (isPlaying) handler.postDelayed({
            binding.ivPlayPause.isVisible = false
        }, Constants.DELAY_CONTROLLER_HIDE)
    }

    private fun stopOtherPlayers() {/*Stop player from current module*/
        if (player != null && player?.playWhenReady == true) {
            player?.playWhenReady = false
        }/*Stop player from other module*/
        /*    fragment.stopAllThePlayers(modulePosition, childVisiblePosition)*/ WallFragment.playerMap.filter { it.first != modulePosition || it.second != childVisiblePosition }
            .forEach {
                it.third?.let { exoPlayer ->
                    if (exoPlayer.playWhenReady) exoPlayer.playWhenReady = false
                }
            }
    }

    //Remove player from player view to use it with other player views
    private fun removePlayer(posToRemove: Int) {
        videoView?.isInvisible = true
        progressBar?.isVisible = false
        if (videoView?.player != null) {
            val indexInMap = playerMap.indexOfFirst { it.first == posToRemove }
            if (indexInMap == -1) //not added yet
                playerMap.add(
                    Triple(
                        posToRemove,
                        player?.currentWindowIndex!!,
                        player?.currentPosition!!
                    )
                )
            else {
                playerMap[indexInMap] =
                    Triple(posToRemove, player?.currentWindowIndex!!, player?.currentPosition!!)
            }
            player!!.playWhenReady = false
            player!!.stop()
            videoView?.player = null
            ivPlayPause?.setImageResource(R.drawable.ic_play)
            ivPlayPause?.isVisible = true
        }
    }

    fun reset() {
        if (childVisiblePosition != -1) removePlayer(childVisiblePosition)
        childVisiblePosition = -1
        lastVisiblePosition = -1
        playerMap.clear()
        releasePlayer()
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.stop()
            player?.release()
            player = null
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var handler = Handler(Looper.getMainLooper())
        var binding: ItemFeedWallBinding = ItemFeedWallBinding.bind(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}