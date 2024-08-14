package base.ui.adapter.feed

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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import base.FansChatApplication
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.CommonFeedItem
import base.databinding.ItemNewsBinding
import base.extension.subscribeAndObserveOnMainThread
import base.ui.MainActivity
import base.ui.fragment.main.NewsFragment
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
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
import timber.log.Timber
import kotlin.math.roundToInt

class NewsAdapter(
    val fragment: NewsFragment,
    val items: List<Any>,
    val type: String,
    val loggedInUserCache: LoggedInUserCache
) : RecyclerView.Adapter<NewsAdapter.FeedItemHolder>() {
    private var player: ExoPlayer? = null
    private var playerMap: ArrayList<Triple<Int, Int, Long>> = arrayListOf()
    private val width = fragment.resources.displayMetrics.widthPixels
    private val height by lazy {
        (if (MainActivity.height == 0) fragment.resources.displayMetrics.heightPixels else MainActivity.height) - (CommonUtils.dpToPx(
            fragment.requireContext(),
            72f
        ) + fragment.resources.getDimensionPixelOffset(R.dimen._56sdp))//52sdp
    }
    private var childVisiblePosition = 0
    private var lastVisiblePosition = 0
    val listenVisiblePos: MutableLiveData<Int> = MutableLiveData()
    private var ivPlayPause: AppCompatImageView? = null
    private var progressBar: ProgressBar? = null
    private var videoView: StyledPlayerView? = null
    private var imageView: ImageView? = null
    private var httpDataSourceFactory: HttpDataSource.Factory =
        DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
    private val requestOptionCircleCrop: RequestOptions by lazy {
        return@lazy RequestOptions()
            .override(50, 50)
            .circleCrop()
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_placeholder)
    }
    val listPausedVideos = arrayListOf<Int>()

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

    override fun onBindViewHolder(
        holder: FeedItemHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = items[position] as CommonFeedItem
            if (item.isVideoPost()) {
                holder.apply { setUpPlayerView(position) }
            }
        } else super.onBindViewHolder(holder, position, payloads)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedItemHolder {
        return FeedItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FeedItemHolder, position: Int) {
        val item = items[position] as CommonFeedItem
        holder.apply {

            binding.source.isVisible =
                item.type.lowercase() == Constants.POST_TYPE_SOCIAL && item.source != null
            binding.source.setImageResource(
                when (item.source) {
                    "facebook" -> R.drawable.ic_facebook
                    "twitter" -> R.drawable.ic_twitter
                    "instagram" -> R.drawable.ic_instagram
                    "gplus" -> R.drawable.ic_gplus
                    else -> 0
                }
            )

            /*From Feed Adapter*/

            binding.title.text = item.title
            binding.date.isVisible = !loggedInUserCache.getLoginUserToken().isNullOrEmpty()

            CoroutineScope(Dispatchers.IO).launch {
                val date = item.created?.toRelative()
                withContext(Dispatchers.Main) {
                    binding.date.text = date
                }
            }

            binding.likesCount.text = item.likeCount.toString()
            binding.commentsCount.text = item.commentsCount.toString()

            //>= 1? Landscape : portrait
            var imageViewHeight = width / item.coverAspectRatio

            val extraHeight = getTitleHeight(binding.rlBar, binding.relativeLayout6)
            if (imageViewHeight > height - extraHeight) {
                imageViewHeight = (height - extraHeight).toDouble()
            } else if (item.coverAspectRatio < .2 && imageViewHeight < fragment.resources.getDimensionPixelOffset(
                    R.dimen._170sdp
                )
            ) {
                imageViewHeight =
                    fragment.resources.getDimensionPixelOffset(R.dimen._170sdp).toDouble()
            }

            if (item.imageUrl?.isNotBlank() == true) {
                binding.image.layoutParams.height = imageViewHeight.roundToInt()
                binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
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
                binding.image.layoutParams.height =
                    fragment.resources.getDimensionPixelOffset(R.dimen._170sdp)
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
                Glide.with(FansChat.context).load(author.avatarUrl)
                    .apply(requestOptionCircleCrop).into(binding.avatar)
                //                    binding.sharedUsername?.text = author.displayName
            } //                binding.messageContainer?.isVisible = author != null
            //            messageContainer?.visible = item is PostShare && item.author.name != "Sokkaa"

            //            sharedUsername?.text = item.owner!!.displayName

            //            if (!item.sharedMessage.isNullOrBlank()) {
            //                sharedMessage?.let {
            //
            //                    sharedMessage.visibility = View.VISIBLE
            //                    sharedMessage.text = item.sharedMessage
            //                }
            //            } else {
            //                sharedMessage?.visibility = View.GONE
            //            }
            //            sharedMessageAvatar?.srcRound = item.author.avatar

            itemView.onClick {
                item._id.let {
                    itemView.open(
                        R.id.details,
                        hashMapOf(
                            "postId" to it,
                            "type" to type
                        )
                    ) //                    itemView.open(R.id.details, "post" to item)
                }
            }
            binding.ivPlayPause.onClick {
                when {
                    binding.videoView.player == null -> {
                        listPausedVideos.remove(position)
                        playForceFully(position, true)
                    }
                    player?.isPlaying == true -> {
                        listPausedVideos.add(childVisiblePosition)
                        pause()
                    }
                    else -> {
                        listPausedVideos.remove(childVisiblePosition)
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

            //            fragment.translateIfNeeded(post) {
            //                title.text = it.title
            //            }

            //            if (post.type != FeedItem.Type.social) {
            //                socialIcon?.visibility = View.GONE
            //                authorName?.visibility = View.VISIBLE
            //                authorImage?.visibility = View.VISIBLE
            //
            //            } else {
            //                socialIcon?.visibility = View.VISIBLE
            //                authorName?.visibility = View.GONE
            //                authorImage?.visibility = View.GONE
            //
            //                socialIcon?.apply {
            //                    Glide.with(fragment)
            //                        .load(R.drawable.avatar_placeholder)
            //                        .apply(requestOptionCircleCrop)
            //                        .into(this)
            //                }
            //            }
        }
    }

    private fun getTitleHeight(v: View, parent: View): Int {
        v.measure(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return v.measuredHeight
    }

    private fun playForceFully(position: Int, isForced: Boolean = false) {
        if (isForced || childVisiblePosition != position) {
            initializePlayer()
            childVisiblePosition =
                position //            Timber.e("lastVisiblePosition> $childVisiblePosition / childVisiblePosition> $position")
            if (lastVisiblePosition != -1 && lastVisiblePosition != position) {
                removePlayer(lastVisiblePosition)
            }
            lastVisiblePosition = position
            if (childVisiblePosition != -1) {
                if (items.size > childVisiblePosition) {
                    if ((items[childVisiblePosition] as CommonFeedItem).isVideoPost()) notifyItemChanged(
                        childVisiblePosition,
                        listOf("")
                    )
                    else { //It is image/text post so remove older one player
                        removePlayer(childVisiblePosition)
                        releasePlayer()
                    }
                }
            } else {
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
    private fun FeedItemHolder.setUpPlayerView(pos: Int) {
        val isVideoFocusing =
            childVisiblePosition == pos /*&& ((fragment.parentFragment as NavHostFragment).childFragmentManager.primaryNavigationFragment as NewsFragment).isItemFirstVisible(pos)*/
        Timber.e(">>$childVisiblePosition:$pos>>$isVideoFocusing")
        if (isVideoFocusing) {
            initializePlayer()
//            binding.videoView.isInvisible = false
            ivPlayPause = binding.ivPlayPause
            progressBar = binding.progressBar
            videoView = binding.videoView
            imageView = binding.image

            binding.videoView.player = player
            val mediaItem = MediaItem.fromUri((items[pos] as CommonFeedItem).videoUrl ?: "")
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
            stopPlayer()
            if (!listPausedVideos.contains(pos)) player!!.playWhenReady = true
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
                    imageView?.isInvisible = false
                    error.printStackTrace()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    Timber.tag("onPlayWhenReadyChanged")
                        .e("Module: $childVisiblePosition > $playWhenReady")

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
//                            imageView?.isInvisible = false
                        }
                        ExoPlayer.STATE_BUFFERING -> {
                            progressBar?.isVisible = true
                            ivPlayPause?.isVisible = false
//                            imageView?.isInvisible = true
                        }
                        ExoPlayer.STATE_READY -> {
                            ivPlayPause?.setImageResource(if (player!!.playWhenReady) R.drawable.ic_pause_black else R.drawable.ic_play)
                            progressBar?.isVisible = false
                            ivPlayPause?.isVisible = !player!!.playWhenReady
                            videoView?.isInvisible = false
                            imageView?.postDelayed({ imageView?.isInvisible = true }, 50)
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

    private fun FeedItemHolder.play() {
        if (player != null) {
            stopPlayer()
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

    private fun FeedItemHolder.pause() {
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

    private fun FeedItemHolder.playPauseVideo(isPlaying: Boolean) {
        handler.removeCallbacksAndMessages(null)
        binding.ivPlayPause.isVisible = true
        binding.ivPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play)
        if (isPlaying) handler.postDelayed({
            binding.ivPlayPause.isVisible = false
        }, Constants.DELAY_CONTROLLER_HIDE)
    }

    //Remove player from player view to use it with other player views
    private fun removePlayer(posToRemove: Int) {
        imageView?.isInvisible = false
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

    private fun stopPlayer() {
        if (player != null) {
            player?.playWhenReady = false
        }
    }

    fun reset(lastVisiblePos: Int = 0) {
        if (childVisiblePosition != -1) removePlayer(childVisiblePosition)
        childVisiblePosition = lastVisiblePos
        lastVisiblePosition = 0
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


    override fun getItemCount(): Int {
        return items.size
    }

    class FeedItemHolder(val view: View) : ViewHolder(view) {
        var handler = Handler(Looper.getMainLooper())
        var binding: ItemNewsBinding = ItemNewsBinding.bind(view)
    }


}