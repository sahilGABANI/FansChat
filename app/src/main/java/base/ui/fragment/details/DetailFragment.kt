package base.ui.fragment.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import base.FansChatApplication
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.file_upload.UploadFile
import base.data.api.news.NewsCacheRepository
import base.data.api.wall.WallCacheRepository
import base.data.api.wall.model.*
import base.data.model.CommonFeedItem
import base.data.model.other.Translation
import base.data.model.wall.ContentItem
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentDetailBinding
import base.extension.*
import base.popup.AnimationUtils
import base.popup.Tooltip
import base.popup.TooltipAnimation
import base.socket.SocketDataManager
import base.ui.MainActivity
import base.ui.adapter.other.CommentsAdapter
import base.ui.base.BaseFragment
import base.ui.fragment.details.viewmodel.DetailViewModel
import base.ui.fragment.details.viewmodel.DetailsViewState
import base.ui.fragment.dialog.PinDialog
import base.util.*
import base.util.Analytics.trackNewsShared
import base.util.Analytics.trackPostShared
import base.util.Analytics.trackRumourShared
import base.util.Analytics.trackSocialShared
import base.util.Analytics.trackVideoShared
import base.util.CommonUtils.Companion.selector
import base.util.CommonUtils.Companion.share
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

open class DetailFragment : BaseFragment() {

    private val THRESHOLD = 30
    private var fromBroadcast: Boolean = false
    lateinit var binding: FragmentDetailBinding
    private var customTooltip: Tooltip? = null

    private var post: CommonFeedItem? = null
    private var postId: String? = null
    private var type: String = Constants.POST_TYPE_WALL

    private var wallId: String? = null
    private var paginatedRV: RecyclerView? = null
    private var player: ExoPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    private val width by lazy { resources.displayMetrics.widthPixels }
    private val height by lazy {
        (if (MainActivity.height == 0) resources.displayMetrics.heightPixels else MainActivity.height) - (CommonUtils.dpToPx(
            requireContext(),
            16f
        ) + resources.getDimensionPixelOffset(R.dimen._62sdp) + resources.getDimensionPixelOffset(R.dimen._34sdp) + CommonUtils.dpToPx(
            requireContext(),
            135f
        ))
    }
    private var listComments: ArrayList<Comment> = arrayListOf()
    private lateinit var commentAdapter: CommentsAdapter
    private var deletedListItem: Comment? = null
    private var isFullScreen = false
    var isAnyBarIsInTouchMode = false
    var isTimeLineBarIsInTouchMode = false
    var isChange = true
    private var isSettingObsReg = false
    private var mAudioManager: AudioManager? = null
    private lateinit var mSettingsContentObserver: ContentObserver

    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private val scrollChangeListener = ViewTreeObserver.OnScrollChangedListener {
        //If the paginated rv is not calculated already
        if (paginatedRV == null) {
            //Get the parent holder
            val holder = binding.nestedScrollView.getChildAt(0) as ViewGroup
            //Loop through all children of parent holder
            for (i in 0 until holder.childCount) {
                //Pull the pagination recyclerview child
                if (holder.getChildAt(i).id == binding.list.id) {
                    paginatedRV = holder.getChildAt(i) as RecyclerView
                    break
                }
            }
        }
        paginatedRV?.let {
            //Identify if recyclerview is scrolled to bottom
            if (it.bottom - (binding.nestedScrollView.height + binding.nestedScrollView.scrollY) == 0) detailsViewModel.loadMore(
                postId!!,
                type
            )
        }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<DetailViewModel>
    private lateinit var detailsViewModel: DetailViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    lateinit var wallCacheRepository: WallCacheRepository

    @Inject
    lateinit var newsCacheRepository: NewsCacheRepository
    private val mProgressHandler: Handler = Handler(Looper.getMainLooper())
    private var isLandScape = false
    private var rotation = 90f
    private var prevMode = false
    private var prevRotation = -1f
    private var mOrientationEventListener: OrientationEventListener? = null

    var isPostDeletedByOwner = false
    var deletedPostId = ""

    var progressRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                if (player != null) {
                    binding.tvExoCurrentDuration.text =
                        CommonUtils.timeConversion(player!!.currentPosition)
                    if (!isTimeLineBarIsInTouchMode) binding.seekbar.progress =
                        player!!.currentPosition.toInt()
                    mProgressHandler.postDelayed(this, 500)
                }
            } catch (ed: IllegalStateException) {
                ed.printStackTrace()
            }
        }
    }
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action == Constants.ACTION_OPEN_POST_DETAIL) {
                val localPostId = intent.getStringExtra("postId")
                val localCommentId = intent.getStringExtra("commentId")
                val localType = intent.getStringExtra("type")
                val likeCount = intent.getIntExtra("likeCount", -1)

                if (localPostId != null && localType != null && localPostId == postId && localType == type && ::binding.isInitialized) {
                    if (likeCount != -1) {
                        //Like
                        binding.detailActionBar.likeText.text = "" + likeCount
                    } else {
                        if (localCommentId == null || localCommentId.isBlank()) {
                            //Post
                            fromBroadcast = true
                        }//else will be comment
                        detailsViewModel.getPostDetail(postId!!, type)
                    }
                }
            } else if (intent?.action != null && intent.action == Constants.ACTION_POST_DELETED) {
                val localPostId = intent.getStringExtra("postId")
                val localType = intent.getStringExtra("type")
                if (localPostId != null && localType != null && localPostId == postId && localType == type) {
                    //Post deleted by owner
                    isPostDeletedByOwner = true
                    deletedPostId = postId!!
                }
            }
        }
    }
    var initialViewHeight: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        detailsViewModel = getViewModelFromFactory(viewModelFactory)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(Constants.ACTION_OPEN_POST_DETAIL)
                addAction(Constants.ACTION_POST_DELETED)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        if (context != null) (context as MainActivity).binding.bottomBar.isVisible = false

        if (postId != null && isPostDeletedByOwner && postId == deletedPostId) {
            goBack()
            return
        }

        postId = requireArguments().getString("postId")
        type = requireArguments().getString("type", Constants.POST_TYPE_WALL).lowercase()
        listComments = arrayListOf()
        listenToEvents()
        listenToViewModel()
        listenToViewEvents()

        httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)

        Constants.liveDataAudioFocusObserver.subscribeAndObserveOnMainThread {
            try {
                if (player != null && player!!.isPlaying) player!!.playWhenReady = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (post?.isVideoPost() == true) {
                    isLandScape = isLandscape(orientation)
                    if (prevMode != isLandScape) {
                        rotation = if (isLandScape) {
                            if (isLeft(orientation)) 270f else 90f
                        } else {
                            0f
                        }
                        if (isFullScreen && (!isLandScape || rotation != prevRotation)) {
                            Timber.tag(">>")
                                .e("isLandScape = $isLandScape isFullScreen = $isFullScreen")
                            changeOrientation(!isFullScreen)
                        }
                    }
                    prevMode = isLandScape
                    prevRotation = rotation
                }
            }
        }
        mOrientationEventListener!!.enable()

    }

    private fun listenToViewEvents() {
        mAudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mSettingsContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                Timber.tag("LOG").d("Settings change detected")
                if (isChange) {
                    binding.volumeSeekBar.progress =
                        mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                }
                isChange = true
            }
        }

        requireContext().contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            mSettingsContentObserver
        )
        isSettingObsReg = true

        binding.volumeSeekBar.max = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.volumeSeekBar.progress = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                isChange = false
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isAnyBarIsInTouchMode = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isAnyBarIsInTouchMode = false
                handler.postDelayed({
                    if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            }
        })

        //seekbar change listner
        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isAnyBarIsInTouchMode = true
                isTimeLineBarIsInTouchMode = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isAnyBarIsInTouchMode = false
                isTimeLineBarIsInTouchMode = false
                if (player != null && seekBar != null) {
                    player!!.seekTo(seekBar.progress.toLong())
                    binding.tvExoCurrentDuration.text =
                        CommonUtils.timeConversion(seekBar.progress.toLong())
                }
                handler.postDelayed({
                    if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            }
        })
    }

    private fun listenToEvents() {
        postId?.let {
            detailsViewModel.getPostDetail(it, type)
        }

        commentAdapter = CommentsAdapter(
            requireContext(),
            listComments,
            this,
            loggedInUserCache,
            wallCacheRepository
        )
        binding.list.adapter = commentAdapter

        binding.backButtonMain.throttleClicks().subscribeAndObserveOnMainThread {
            requireActivity().onBackPressed()
        }
        binding.ivFullScreen.throttleClicks().subscribeAndObserveOnMainThread {
            post?.let {
                changeOrientation(isFullScreen)
                isFullScreen = !isFullScreen
            }
        }
        sendClickEvent()

        binding.postActions.delete.throttleClicks().subscribeAndObserveOnMainThread {
            when {
                loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                    open(R.id.authDecideFragment)
                }
                isPostDeletedByOwner && postId == deletedPostId -> {
                    showToast(getString(R.string.post_no_longer_available))
                }
                else -> {
                    showCustomDialog(getString(R.string.delete_post), "", true) {
                        post?.let {
                            detailsViewModel.deleteWallPost(it._id)
                        }
                    }
                }
            }
        }

        binding.postActions.reportPost.throttleClicks().subscribeAndObserveOnMainThread {
            post?.let {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        open(R.id.authDecideFragment)
                    }
                    isPostDeletedByOwner && postId == deletedPostId -> {
                        showToast(getString(R.string.post_no_longer_available))
                    }
                    else -> {
                        postId?.let {
                            showCustomWithEditTextDialog(
                                getString(R.string.report_post),
                                getString(R.string.write_complaint),
                                showCancel = true,
                                editText = true
                            ) {
                                detailsViewModel.reportPost(type, ReportPostRequest(postId!!, it))
                            }
                        }
                    }
                }
            }
        }

        binding.postActions.translate.onClick {
            post?.let {
                if (isPostDeletedByOwner && postId == deletedPostId) {
                    showToast(getString(R.string.post_no_longer_available))
                } else {
                    val translation: Translation? =
                        wallCacheRepository.getPostTranslation(Translation().apply {
                            this.postId = post!!._id; this.oriTitle =
                            post!!.title!!;this.oriBodyText =
                            post!!.bodyText ?: ""
                        })

                    if (translation != null) {
                        //Just Undo it
                        wallCacheRepository.deleteTranslation(translation)
                        binding.title.setText(post!!.title)
                        binding.body.setText(post!!.bodyText ?: "")
                        binding.postActions.translate.text = getString(R.string.translate)
                        binding.postActions.translate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                        binding.body.post {
                            if (binding.body.lineCount <= 3) {
                                binding.postActions.readMore.visibility = View.GONE
                            } else {
                                binding.postActions.readMore.visibility = View.VISIBLE
                                binding.body.maxLines = 3
                                binding.postActions.readMore.setText(R.string.read_more)
                            }
                        }

                    } else {
                        //Just translate it
                        postId?.let {
                            dialogTranslate(binding.postActions.translate, postId!!, true)
                        }
                    }
                }
            }
        }

        binding.postActions.readMore.onClick {
            if (binding.body.maxLines == 3) {
                binding.body.maxLines = Integer.MAX_VALUE
                binding.postActions.readMore.setText(R.string.read_less)
            } else {
                if (binding.body.lineCount < 3) {
                    binding.postActions.readMore.visibility = View.GONE
                } else {
                    binding.postActions.readMore.visibility = View.VISIBLE
                    binding.body.maxLines = 3
                    binding.postActions.readMore.setText(R.string.read_more)
                }
            }
        }

        binding.postActions.tvSave.onClick {
            if (isPostDeletedByOwner && postId == deletedPostId) {
                showToast(getString(R.string.post_no_longer_available))
            } else if (binding.title.get().isEmpty()) {
                showToast(getString(R.string.enter_title))
            } else if (post?.videoUrl!!.isNotBlank() && !post?.videoUrl!!.startsWith(
                    "http",
                    true
                )
            ) {
                /*Video post*/
                binding.progressBarPage.visibility = View.VISIBLE
                UploadFile.upload(File(post?.imageUrl!!)).subscribe({ videoThumbnailUrl ->
                    post?.videoUrl?.let { _ ->
                        UploadFile.upload(File(post?.videoUrl!!)).subscribe({ videoFileUrl ->
                            binding.progressBarPage.visibility = View.GONE
                            updateWallPost(
                                videoThumbnailUrl,
                                videoFileUrl,
                                Constants.POST_TYPE_VIDEO,
                                post?.coverAspectRatio!!
                            )
                        }, {
                            binding.progressBarPage.visibility = View.GONE
                            showToast(getString(R.string.failed_to_upload))
                        })
                    }
                }, {
                    binding.progressBarPage.visibility = View.GONE
                    showToast("Failed to upload file")
                })
            } else if (post?.imageUrl!!.isNotBlank() && !post?.imageUrl!!.startsWith(
                    "http",
                    true
                )
            ) {
                /*Image post*/
                binding.progressBarPage.visibility = View.VISIBLE
                UploadFile.upload(selectedFile!!).subscribe({
                    binding.progressBarPage.visibility = View.GONE
                    updateWallPost(
                        it,
                        contentType = Constants.POST_TYPE_IMAGE,
                        aspectRatio = post?.coverAspectRatio!!
                    )

                }, {
                    binding.progressBarPage.visibility = View.GONE
                    showToast("Failed to upload file")
                })
            } else {
                /*Text post*/
                updateWallPost()
            }
        }

        binding.postActions.edit.onClick {
            when {
                loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                    open(R.id.authDecideFragment)
                }
                isPostDeletedByOwner && postId == deletedPostId -> {
                    showToast(getString(R.string.post_no_longer_available))
                }
                else -> {
                    binding.postActions.edit.isVisible = false
                    binding.postActions.tvSave.isVisible = true
                    binding.linEditMedia.isVisible = true
                    binding.flController.isVisible = false
                    //                binding.title.isEnabled = true
                    //                binding.body.isEnabled = true
                    binding.title.movementMethod = ArrowKeyMovementMethod.getInstance()
                    binding.title.isFocusable = true
                    binding.title.isFocusableInTouchMode = true

                    binding.body.movementMethod = ArrowKeyMovementMethod.getInstance()
                    binding.body.isFocusable = true
                    binding.body.isFocusableInTouchMode = true
                    binding.body.isCursorVisible = true

                    showKeyboards(requireActivity())
                    binding.title.isCursorVisible = true

                    binding.title.requestFocus()
                    binding.title.setSelection(0, binding.title.get().length)
                    binding.body.setSelection(0, binding.body.get().length)

                }
            }
        }

        binding.detailActionBar.like.onClick {
            AnimationUtils.bounceAnimation(binding.detailActionBar.like)
            var authorId: String = ""
            //            if (post !is PostShare) {
            //                authorId = post.authorId
            //            }
            post?.let {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        open(R.id.authDecideFragment)
                    }
                    isPostDeletedByOwner && postId == deletedPostId -> {
                        showToast(getString(R.string.post_no_longer_available))
                    }
                    else -> detailsViewModel.likeWallPost(
                        postId.toString(),
                        type,
                        UpdateLike(!post?.likedByMe!!)
                    )
                }
                //            like(post, authorId)
            }
        }

        binding.detailActionBar.pin.onClick {
            post?.let {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        open(R.id.authDecideFragment)
                    }
                    isPostDeletedByOwner && postId == deletedPostId -> {
                        showToast(getString(R.string.post_no_longer_available))
                    }
                    else -> postId?.let {
                        /*  if (post?.type.name.equals(FeedItem.Type.social.name)) trackSocialItemPinned()
                                      else if (post.type.name.equals(FeedItem.Type.post.name)) trackPostPin(it)*/
                        open(
                            R.id.pinDialog,
                            hashMapOf(PinDialog.POST_ID to it, PinDialog.POST_TYPE to type)
                        )
                    }
                }
            }
        }

        binding.detailActionBar.share.onClick {
            post?.let {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        open(R.id.authDecideFragment)
                    }
                    isPostDeletedByOwner && postId == deletedPostId -> {
                        showToast(getString(R.string.post_no_longer_available))
                    }
                    else -> postId?.let {
                        when {
                            post!!.type.lowercase() == Constants.POST_TYPE_WALL -> {
                                trackPostShared(postId!!)
                            }
                            post!!.type.lowercase() == Constants.POST_TYPE_NEWS -> {
                                trackNewsShared(postId!!)
                            }
                            post!!.type.lowercase() == Constants.POST_TYPE_SOCIAL -> {
                                trackSocialShared(postId!!)
                            }
                            post!!.type.lowercase() == Constants.POST_TYPE_RUMOURS -> {
                                trackRumourShared(postId!!)
                            }
                            post!!.type.lowercase() == Constants.POST_TYPE_CLUB_TV -> {
                                trackVideoShared(postId!!)
                            }
                        }
                        detailsViewModel.getSharingUrl(postId!!, type)
                    }
                }
            }
        }

        binding.image.onClick {
            viewImage(
                if (post == null || post!!.imageUrl == null || post!!.imageUrl?.isBlank() == true) ("android.resource://" + requireContext().packageName + "/" + R.drawable.placeholder) else post!!.imageUrl
            )
        }

        binding.imageButton.throttleClicks().subscribeAndObserveOnMainThread {
            requireActivity().hideKeyboard()

            if (isPostDeletedByOwner && postId == deletedPostId) {
                showToast(getString(R.string.post_no_longer_available))
            } else {
                val actions =
                    arrayOf(getString(R.string.from_gallery), getString(R.string.from_camera))

                selector(items = actions, onClick = { _, i ->
                    when (i) {
                        0 -> selectImages {
                            Glide.with(FansChat.context).load(selectedFile).into(binding.image)
                            post?.let {
                                post!!.videoUrl = ""
                                post!!.imageUrl = selectedFile?.absolutePath!!
                                post!!.coverAspectRatio =
                                    selectedFile?.calculateAspectRatio() ?: 0.0
                                post!!.contentType = Constants.POST_TYPE_IMAGE
                                loadImageVideo(post!!)
                            }
                        }
                        1 -> takePhotos {
                            Glide.with(FansChat.context).load(selectedFile).into(binding.image)
                            post?.let {
                                post!!.videoUrl = ""
                                post!!.imageUrl = selectedFile?.absolutePath!!
                                post!!.coverAspectRatio =
                                    selectedFile?.calculateAspectRatio() ?: 0.0
                                post!!.contentType = Constants.POST_TYPE_IMAGE
                                loadImageVideo(post!!)
                            }
                        }
                    }
                })
            }
        }
        //
        binding.videoButton.onClick {
            requireActivity().hideKeyboard()
            if (isPostDeletedByOwner && postId == deletedPostId) {
                showToast(getString(R.string.post_no_longer_available))
            } else {
                val actions =
                    arrayOf(getString(R.string.pick_video), getString(R.string.record_video))
                selector(items = actions, onClick = { _, i ->
                    when (i) {
                        0 -> selectVideos {
                            Glide.with(FansChat.context).load(selectedFile).into(binding.image)
                            post?.let {
                                val videoThumbnail =
                                    selectedFile?.createVideoThumb(requireActivity())
                                post!!.imageUrl = videoThumbnail?.absolutePath!!
                                post!!.videoUrl = selectedFile?.absolutePath!!
                                post!!.coverAspectRatio = videoThumbnail.calculateAspectRatio()
                                post!!.contentType = Constants.POST_TYPE_VIDEO
                                binding.flController.isVisible = false
                                loadImageVideo(post!!)
                            }
                        }
                        1 -> recordVideos {
                            Glide.with(FansChat.context).load(selectedFile).into(binding.image)
                            post?.let {
                                val videoThumbnail =
                                    selectedFile?.createVideoThumb(requireActivity())
                                post!!.imageUrl = videoThumbnail?.absolutePath!!
                                post!!.videoUrl = selectedFile?.absolutePath!!
                                post!!.coverAspectRatio = videoThumbnail.calculateAspectRatio()
                                post!!.contentType = Constants.POST_TYPE_VIDEO
                                binding.flController.isVisible = false
                                loadImageVideo(post!!)
                            }
                        }
                    }
                })
            }
        }
        binding.avatar.onClick {
            post?.let {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        open(R.id.authDecideFragment)
                    }
                    it.owner != null -> {
                        if (it.owner!!.id != loggedInUserCache.getLoggedInUserId())
                            open(R.id.friendDetails, "userId" to it.owner!!.id.toString())
                        else open(R.id.profile)
                    }
                }
            }
        }

        enablePagingForComment()
    }

    private fun isLandscape(orientation: Int): Boolean {
        return orientation >= 90 - THRESHOLD && orientation <= 90 + THRESHOLD || orientation >= 270 - THRESHOLD && orientation <= 270 + THRESHOLD
    }

    private fun isLeft(orientation: Int): Boolean {
        return orientation >= 90 - THRESHOLD && orientation <= 90 + THRESHOLD
    }

    private fun updateWallPost(
        imageUrl: String? = null,
        videoUrl: String? = null,
        contentType: String? = null,
        aspectRatio: Double? = null
    ) {
        detailsViewModel.updateWallPost(
            postId.toString(),
            WallPostRequest(
                post?.type ?: Constants.POST_ORIGINAL_TYPE_WALL,
                binding.title.text.toString(),
                null,
                binding.body.text.toString(),
                null,
                imageUrl,
                videoUrl,
                null,
                "en",
                contentType,
                aspectRatio
            )
        )

    }

    fun dialogTranslate(layout: View, postId: String, isPost: Boolean = false) {
        if (isPostDeletedByOwner && postId == deletedPostId) {
            showToast(getString(R.string.post_no_longer_available))
        } else {
            val content: View = View.inflate(requireContext(), R.layout.popup_translation, null)
            if (customTooltip != null) customTooltip!!.dismiss()

            customTooltip = Tooltip.Builder(requireContext()).anchor(
                layout, when {
//                    isPost -> Tooltip.BOTTOM
                    CommonUtils.isRtl(requireActivity()) -> Tooltip.LEFT
                    else -> Tooltip.RIGHT
                }
            ).animate(TooltipAnimation(TooltipAnimation.NONE)).autoAdjust(true).withPadding(10)
                .content(content)
                .withTip(Tooltip.Tip(30, 30, Color.WHITE, 10)).into(binding.root).debug(true).show()

            val listView = content.findViewById<ListView>(R.id.listView)
            listView.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languageNames)
            listView.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, position, p3 ->
                when {
                    isPostDeletedByOwner && postId == deletedPostId -> {
                        showToast(getString(R.string.post_no_longer_available))
                    }
                    isPost -> detailsViewModel.translatePost(postId, languageCodes[position], type)
                    else -> detailsViewModel.translatePostComment(
                        postId,
                        languageCodes[position],
                        type
                    )
                }
                if (customTooltip != null) customTooltip!!.dismiss()
            }
        }
    }

    private fun sendClickEvent() {
        /* find<ImageButton>(R.id.send).isEnabled =
             !(loggedInUserCache.getLoginUserToken().isNullOrEmpty())*/
        binding.includePostContainer.send.throttleClicks().subscribeAndObserveOnMainThread {
            if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
                hideKeyboards(binding.includePostContainer.send)

                open(R.id.authDecideFragment)
            } else if (isPostDeletedByOwner && postId == deletedPostId) {
                showToast(getString(R.string.post_no_longer_available))
            } else {
                val comment = binding.includePostContainer.input.text.toString()
                if (comment.isNotEmpty()) {
                    postId?.let {
                        detailsViewModel.addWallPostComment(
                            AddCommentRequest(
                                comment,
                                wallId = wallId ?: "",
                                postId = it
                            ), type
                        )
                    }
                }
            }
        }
    }

    private fun enablePagingForComment() {
        //Disable nested recyclerview from scrolling
        ViewCompat.setNestedScrollingEnabled(binding.list, false)
        //Attach scroll listener to nested scrollview
        binding.nestedScrollView.viewTreeObserver?.removeOnScrollChangedListener(
            scrollChangeListener
        )
        binding.nestedScrollView.viewTreeObserver?.addOnScrollChangedListener(scrollChangeListener)
    }

    private fun listenToViewModel() {
        detailsViewModel.feedPostState.subscribeAndObserveOnMainThread { it ->
            when (it) {
                is DetailsViewState.ErrorMessage -> {
                    when (it.errorCode) {
                        401 -> {
                            goBack()
                            open(R.id.authDecideFragment)
                        }
                        404 -> {
                            showToast(getString(R.string.post_not_found))
                            goBack()
                        }
                        else -> showToast(it.errorMessage)
                    }
                }
                is DetailsViewState.WallPostDetails -> {
                    setDetails(it.wallPost)
                    if (!fromBroadcast) {
                        //clear comments and get all from first
                        listComments.clear()
                        commentAdapter.notifyDataSetChanged()
                        detailsViewModel.resetLoadingComments(it.wallPost._id, type)
                    }
                    fromBroadcast = false
                }
                is DetailsViewState.LoadingState -> {
                    binding.progressBarPage.isVisible = it.isLoading
                }
                is DetailsViewState.AddPostComment -> {
                    requireActivity().hideKeyboard()
                    binding.includePostContainer.input.clear()
                    if (post != null && post?.commentsCount != null) {
                        //if (post?.commentsCount!!.plus(1) > 0) post?.commentsCount!!.plus(1) else 1
                        post?.commentsCount = max(post?.commentsCount!!.plus(1), 1)
                        binding.detailActionBar.commentsText.text = post?.commentsCount.toString()
                        listComments.add(0, it.addCommentResponse.comment!!)
                        commentAdapter.notifyItemInserted(0)

                        /*Updating tables*/
                        getLocalPost()?.let { localPost ->
                            localPost.commentsCount = post?.commentsCount!!
                            wallCacheRepository.updatePost(localPost)
                        }
                        getLocalNewsPost()?.let { localPost ->
                            localPost.commentsCount = post?.commentsCount!!
                            newsCacheRepository.updatePost(localPost)
                        }
                        //                        detailsViewModel.resetLoadingComments(post?._id!!, type)
                    } else postId?.let { postId ->
                        listComments.clear()
                        commentAdapter.notifyDataSetChanged()
                        detailsViewModel.getPostDetail(postId, type)
                    }
                }
                is DetailsViewState.ListOfComments -> {
                    it.addCommentResponse.data?.let { comments ->
                        listComments.addAll(comments)
                        commentAdapter.notifyDataSetChanged()
                    }
                }
                is DetailsViewState.UpdatePostComment -> {
                    val id = it.commentList.id
                    listComments.filter { it.id == id }[0].comment = it.commentList.comment
                    commentAdapter.notifyDataSetChanged()

                    binding.includePostContainer.input.setText("")
                    requireActivity().hideKeyboard()
                    sendClickEvent()
                }
                is DetailsViewState.DeletePostComment -> {
                    if (deletedListItem != null) {
                        listComments.remove(deletedListItem)
                        commentAdapter.notifyDataSetChanged()

                        if (post != null && post?.commentsCount != null) {
                            post?.commentsCount =
                                if (post?.commentsCount!!.minus(1) > 0) post?.commentsCount!!.minus(
                                    1
                                ) else 0
                            binding.detailActionBar.commentsText.text =
                                post?.commentsCount.toString()

                            /*Updating tables*/
                            getLocalPost()?.let { localPost ->
                                localPost.commentsCount = post?.commentsCount!!
                                wallCacheRepository.updatePost(localPost)
                            }
                            getLocalNewsPost()?.let { localPost ->
                                localPost.commentsCount = post?.commentsCount!!
                                newsCacheRepository.updatePost(localPost)
                            }

                        } else postId?.let { postId ->
                            detailsViewModel.getPostDetail(postId, type)
                        }
                    }
                }
                is DetailsViewState.DeletePostDetails -> {
                    /*Updating tables*/
                    getLocalPost()?.let { localPost ->
                        wallCacheRepository.deletePost(localPost)
                    }
                    getLocalNewsPost()?.let { localPost ->
                        newsCacheRepository.deletePost(localPost)
                    }
                    goBack()
                }
                is DetailsViewState.UpdatePost -> {
                    requireActivity().hideKeyboard()

                    //                    binding.title.isEnabled = false
                    //                    binding.body.isEnabled = false

                    binding.title.movementMethod = LinkMovementMethod.getInstance()
                    binding.title.isFocusable = false
                    binding.title.isFocusableInTouchMode = false
                    binding.title.isCursorVisible = false
                    binding.title.text = binding.title.text

                    binding.body.movementMethod = LinkMovementMethod.getInstance()
                    binding.body.isFocusable = false
                    binding.body.isFocusableInTouchMode = false
                    binding.body.isCursorVisible = false
                    binding.body.text = binding.body.text

                    binding.postActions.edit.isVisible = true
                    binding.postActions.tvSave.isVisible = false
                    binding.linEditMedia.isVisible = false
                    binding.flController.isVisible = true

                    binding.title.setSelection(0, 0)
                    binding.body.setSelection(0, 0)

                    binding.body.post {
                        if (binding.body.lineCount <= 3) {
                            binding.postActions.readMore.visibility = View.GONE
                        } else {
                            binding.postActions.readMore.visibility = View.VISIBLE
                            binding.body.maxLines = 3
                            binding.postActions.readMore.setText(R.string.read_more)
                        }
                    }

                    post?.let { _ ->
                        post!!.videoUrl = it.postWallResponse.videoUrl
                        post!!.imageUrl = it.postWallResponse.imageUrl
                        post!!.coverAspectRatio = it.postWallResponse.coverAspectRatio
                        post!!.contentType = it.postWallResponse.contentType
                    }
                    /*Updating tables*/
                    getLocalPost()?.let { localPost ->
                        localPost.title = binding.title.text.toString()
                        localPost.bodyText = binding.body.text.toString()

                        localPost.videoUrl = it.postWallResponse.videoUrl
                        localPost.imageUrl = it.postWallResponse.imageUrl
                        localPost.coverAspectRatio = it.postWallResponse.coverAspectRatio
                        localPost.contentType = it.postWallResponse.contentType

                        wallCacheRepository.updatePost(localPost)
                    }
                    getLocalNewsPost()?.let { localPost ->
                        localPost.title = binding.title.text.toString()
                        localPost.bodyText = binding.body.text.toString()

                        localPost.videoUrl = it.postWallResponse.videoUrl
                        localPost.imageUrl = it.postWallResponse.imageUrl
                        localPost.coverAspectRatio = it.postWallResponse.coverAspectRatio
                        localPost.contentType = it.postWallResponse.contentType

                        newsCacheRepository.updatePost(localPost)
                    }

                }
                is DetailsViewState.LikePostResponse -> {
                    post = it.specificItemResponse
                    binding.detailActionBar.likeText.text =
                        it.specificItemResponse.likeCount.toString()
                    binding.detailActionBar.likeText.isActivated = post?.likedByMe == true
                    binding.detailActionBar.likeImage.isActivated = post?.likedByMe == true

                    /*Updating tables*/
                    getLocalPost()?.let { localPost ->
                        localPost.likeCount = it.specificItemResponse.likeCount
                        localPost.likedByMe = it.specificItemResponse.likedByMe
                        wallCacheRepository.updatePost(localPost)
                    }
                    getLocalNewsPost()?.let { localPost ->
                        localPost.likeCount = it.specificItemResponse.likeCount
                        localPost.likedByMe = it.specificItemResponse.likedByMe
                        newsCacheRepository.updatePost(localPost)
                    }
                }
                is DetailsViewState.TranslateComment -> {
                    it.translatedComment.translations?.let { _ ->
                        //Just update index, it will show translated comment from local list of fragment
                        val indexToTranslate =
                            listComments.indexOfFirst { c -> c.id == it.translatedComment._id }
                        wallCacheRepository.addCommentTranslation(it.translatedComment.translations.apply {
                            this.postId = post!!._id
                            this.commentId = it.translatedComment._id
                            this.oriTitle = listComments[indexToTranslate].comment!!
                        })
                        commentAdapter.notifyItemChanged(indexToTranslate)
                    }
                }
                is DetailsViewState.TranslatePost -> {
                    it.translatedPost.translations?.let { translation ->
                        /*post!!.title = it.title
                        post!!.subTitle = it.subtitle
                        post!!.bodyText = it.bodyText
                        post!!.language = it.language*/
                        wallCacheRepository.addPostTranslation(translation.apply {
                            this.postId = post!!._id
                            this.commentId = it.translatedPost._id
                            this.oriTitle = post!!.title!!
                            this.oriBodyText = post!!.bodyText ?: ""
                        })

                        binding.title.setText(it.translatedPost.translations.title)
                        binding.body.setText(it.translatedPost.translations.bodyText)

                        binding.postActions.translate.text = getString(R.string.undo_translate)
                        binding.postActions.translate.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                        binding.body.post {
                            if (binding.body.lineCount <= 3) {
                                binding.postActions.readMore.visibility = View.GONE
                            } else {
                                binding.postActions.readMore.visibility = View.VISIBLE
                                binding.body.maxLines = 3
                                binding.postActions.readMore.setText(R.string.read_more)
                            }
                        }

                    }
                }
                is DetailsViewState.SharePost -> {
                    if (isPostDeletedByOwner && postId == deletedPostId) {
                        showToast(getString(R.string.post_no_longer_available))
                    } else {
                        selector(getString(R.string.share_using),
                            arrayOf(
                                getString(R.string.facebook),
                                getString(R.string.other_services)
                            ),
                            onClick = { _, i ->
                                if (i == 0) {
                                    val shareLinkContent =
                                        ShareLinkContent.Builder().setQuote(post!!.title)
                                            .setContentUrl(Uri.parse(it.sharingResponse.url))
                                            .build()
                                    ShareDialog.show(act, shareLinkContent)
                                } else {
                                    requireActivity().share(it.sharingResponse.url, post!!.title ?: "")
                                }
                            })
                        // increment share count
                        post!!.shareCount++
                        binding.detailActionBar.shareText.text = post!!.shareCount.toString()

                        /*Updating tables*/
                        getLocalPost()?.let { localPost ->
                            localPost.shareCount++
                            wallCacheRepository.updatePost(localPost)
                        }
                        getLocalNewsPost()?.let { localPost ->
                            localPost.shareCount++
                            newsCacheRepository.updatePost(localPost)
                        }
                    }
                }
                is DetailsViewState.ReportPost -> {
                    showToast(getString(R.string.reported_post))
                }
            }
        }.autoDispose()
    }

    /*ContentItem*/
    private fun getLocalPost(): ContentItem? {
        return wallCacheRepository.getPost(type, post!!._id)
    }

    private fun getLocalNewsPost(): CommonFeedItem? {
        return newsCacheRepository.getPost(
            if (type.equals(
                    Constants.POST_TYPE_STREAMING,
                    true
                )
            ) Constants.POST_ORIGINAL_TYPE_CLUB_TV else type, post!!._id
        )
    }

    private fun setDetails(wallPost: CommonFeedItem) {
        binding.linContent.isVisible = true
        wallPost.owner?.let { author ->
            binding.tvNameDate.text = author.displayName + " - " + wallPost.created?.toRelative()
            Glide.with(FansChat.context).load(author.avatarUrl).override(100, 100)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(binding.avatar)
        } ?: kotlin.run {
            binding.tvNameDate.text = ""
            binding.avatar.setImageResource(R.drawable.avatar_placeholder)
        }

        binding.postActions.translate.isVisible = loggedInUserCache.getLoggedInUserId()
            .isNullOrBlank() || wallPost.owner?.id != loggedInUserCache.getLoggedInUserId()

        val translation: Translation? = wallCacheRepository.getPostTranslation(Translation().apply {
            this.postId = wallPost._id; this.oriTitle = wallPost.title!!;this.oriBodyText =
            wallPost.bodyText ?: ""
        })

        if (translation != null) {
            binding.title.setText(translation.title)
            binding.body.setText(translation.bodyText)
            //Undo
            binding.postActions.translate.text = getString(R.string.undo_translate)
            binding.postActions.translate.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            binding.title.setText(wallPost.title.toString())
            binding.body.setText(wallPost.bodyText)
            //Translate
            binding.postActions.translate.text = getString(R.string.translate)
            binding.postActions.translate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        binding.title.movementMethod = LinkMovementMethod.getInstance()
        binding.body.movementMethod = LinkMovementMethod.getInstance()

        post = wallPost
        wallId = wallPost.wallId

        Timber.e("LineCount: ${binding.body.lineCount}")

        binding.body.post {
            if (binding.body.lineCount <= 3) {
                binding.postActions.readMore.visibility = View.GONE
            } else {
                binding.postActions.readMore.visibility = View.VISIBLE
                binding.body.maxLines = 3
                binding.postActions.readMore.setText(R.string.read_more)
            }
        }

        loadImageVideo(wallPost)
        binding.detailActionBar.commentsText.text = wallPost.commentsCount.toString()

        binding.detailActionBar.likeText.text = wallPost.likeCount.toString()
        binding.detailActionBar.likeText.isActivated = wallPost.likedByMe == true
        binding.detailActionBar.likeImage.isActivated = wallPost.likedByMe == true
        binding.title.requestFocus()

        binding.detailActionBar.shareText.text = wallPost.shareCount.toString()

        val isMyPost =
            loggedInUserCache.getLoggedInUserId() != null && wallPost.wallId != null && wallPost.wallId == loggedInUserCache.getLoggedInUserId()
        binding.postActions.delete.isVisible = isMyPost
        binding.postActions.edit.isVisible = isMyPost
        binding.postActions.reportPost.isVisible = !isMyPost

        /*Updating tables*/
        getLocalPost()?.let { localPost ->
            localPost.likeCount = wallPost.likeCount
            localPost.likedByMe = wallPost.likedByMe
            localPost.commentsCount = wallPost.commentsCount
            localPost.watchCount =
                ++wallPost.watchCount //increase it by one as it has increased by this call
            localPost.shareCount = wallPost.shareCount

            localPost.title = wallPost.title
            localPost.bodyText = wallPost.bodyText
            localPost.videoUrl = wallPost.videoUrl
            localPost.imageUrl = wallPost.imageUrl
            localPost.coverAspectRatio = wallPost.coverAspectRatio
            localPost.contentType = wallPost.contentType

            wallCacheRepository.updatePost(localPost)
        }
        getLocalNewsPost()?.let { localPost ->
            localPost.likeCount = wallPost.likeCount
            localPost.likedByMe = wallPost.likedByMe
            localPost.commentsCount = wallPost.commentsCount
            localPost.watchCount =
                ++wallPost.watchCount //increase it by one as it has increased by this call
            localPost.shareCount = wallPost.shareCount

            localPost.title = wallPost.title
            localPost.bodyText = wallPost.bodyText
            localPost.videoUrl = wallPost.videoUrl
            localPost.imageUrl = wallPost.imageUrl
            localPost.coverAspectRatio = wallPost.coverAspectRatio
            localPost.contentType = wallPost.contentType

            newsCacheRepository.updatePost(localPost)
        }
    }

    private fun loadImageVideo(wallPost: CommonFeedItem) {
        releasePlayer()
        initialViewHeight =/*
            if (wallPost.coverAspectRatio <= .2) resources.getDimensionPixelOffset(R.dimen._170sdp).toDouble()
            else */width / wallPost.coverAspectRatio

        if (initialViewHeight > height) {
            initialViewHeight = height.toDouble()
        } else if (wallPost.coverAspectRatio < .2 && initialViewHeight < resources.getDimensionPixelOffset(
                R.dimen._170sdp
            )
        ) {
            initialViewHeight = resources.getDimensionPixelOffset(R.dimen._170sdp).toDouble()
        }

        binding.flVideo.isVisible = wallPost.isVideoPost()

        if (wallPost.imageUrl?.isNotBlank() == true) {
            binding.image.layoutParams.height = initialViewHeight.roundToInt()
            binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(FansChat.context).load(wallPost.imageUrl)
                .override(width, initialViewHeight.roundToInt())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.image.setBackgroundResource(if (wallPost.isVideoPost()) R.color.colorPrimary else R.drawable.placeholder_flex)
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
            binding.image.layoutParams.height = resources.getDimensionPixelOffset(R.dimen._170sdp)
            binding.image.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(FansChat.context).load(R.drawable.placeholder).into(binding.image)
        }


        if (wallPost.isVideoPost()) {
            binding.flVideo.layoutParams.height = initialViewHeight.roundToInt()

            binding.ivPlayPause.onClick {
                if (player != null && !player!!.isPlaying && player!!.playWhenReady) {
                    releasePlayer()
                    initializePlayer()
                } else if (player?.isPlaying == true) pause()
                else {
                    play()
                }
            }
            binding.llClickable.onClick {
                handler.removeCallbacksAndMessages(null)
                binding.flController.isVisible = true
                handler.postDelayed({
                    if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                }, Constants.DELAY_CONTROLLER_HIDE)
            }

            initializePlayer()
        }
    }

    fun editCommentData(comment: Comment) {
        if (isPostDeletedByOwner && postId == deletedPostId) {
            showToast(getString(R.string.post_no_longer_available))
        } else {
            binding.includePostContainer.input.setText(comment.comment)
            binding.includePostContainer.send.onClick {
                detailsViewModel.updateWallPostComment(
                    comment.id.toString(),
                    UpdateComment(
                        binding.includePostContainer.input.text.toString(),
                        postId.toString()
                    ),
                    type
                )
            }
            binding.includePostContainer.input.requestFocus()
            binding.includePostContainer.input.selectAll()
        }
    }

    fun deleteCommentData(comment: Comment) {
        if (isPostDeletedByOwner && postId == deletedPostId) {
            showToast(getString(R.string.post_no_longer_available))
        } else {
            deletedListItem = comment
            detailsViewModel.deleteWallPostComment(DeleteWallPostCommentRequest(comment.id), type)
        }
    }

    /*Video player code Start*/
    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(requireContext()).build()
            binding.videoView.player = player
            val mediaItem = MediaItem.fromUri(post?.videoUrl!!)
            player!!.setMediaSource(buildMediaSource(mediaItem))

            player!!.addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    handler.removeCallbacksAndMessages(null)
                    binding.ivPlayPause.setImageResource(R.drawable.ic_play)
                    if (!binding.linEditMedia.isVisible) binding.flController.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.image.isVisible = true
                    binding.videoView.postDelayed({ binding.videoView.isVisible = false }, 50)
                    error.printStackTrace()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)

                    if (playWhenReady && player!!.playbackState == Player.STATE_READY) {
                        (requireActivity() as MainActivity).requestAudioFocus()
                        // media actually playing
                        binding.ivPlayPause.setImageResource(R.drawable.ic_pause_black)
                        if (binding.flController.isVisible) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                            }, Constants.DELAY_CONTROLLER_HIDE)
                        }
                    } else if (playWhenReady) {
                        (requireActivity() as MainActivity).requestAudioFocus()
                    } else {
                        (requireActivity() as MainActivity).leaveAudioFocus()
                        binding.ivPlayPause.setImageResource(R.drawable.ic_play)
                        handler.removeCallbacksAndMessages(null)
                        if (!binding.linEditMedia.isVisible) binding.flController.isVisible = true
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        ExoPlayer.STATE_IDLE -> {
                        }
                        ExoPlayer.STATE_BUFFERING -> {
                            binding.progressBar.isVisible = true
                            if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                            //                            Timber.tag("onPlaybackStateChanged").e("Module: $groupPosition Child: $pos >Buffering video")
                        }
                        ExoPlayer.STATE_READY -> {
                            binding.videoView.keepScreenOn = true
                            binding.tvExoTotalDuration.text =
                                CommonUtils.timeConversion(player!!.duration)
                            binding.seekbar.max = player!!.duration.toInt()
                            handler.removeCallbacksAndMessages(null)
                            binding.ivPlayPause.setImageResource(if (player!!.playWhenReady) R.drawable.ic_pause_black else R.drawable.ic_play)
                            binding.progressBar.isVisible = false
                            if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
                            binding.videoView.isInvisible = false
                            binding.image.postDelayed({ binding.image.isVisible = false }, 50)
                            mProgressHandler.removeCallbacks(progressRunnable)
                            mProgressHandler.postDelayed(progressRunnable, 500)
                            //                            Timber.tag("onPlaybackStateChanged").e("Module: $groupPosition Child: $pos >Ready to play")
                        }
                        ExoPlayer.STATE_ENDED -> {
                            handler.removeCallbacksAndMessages(null)
                            binding.ivPlayPause.setImageResource(R.drawable.ic_play)
                            player!!.seekTo(0)
                        }
                    }
                }
            })

            player!!.prepare()
        }

        player!!.playWhenReady = true
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        //A DataSource that reads and writes a Cache.
        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory().setCache(FansChatApplication.simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
    }

    private fun play() {
        if (player != null) {
            if (player!!.playbackState == Player.STATE_ENDED) {
                player!!.seekTo(0)
            }
            player!!.playWhenReady = true
            playPauseVideo(true)
        }
    }

    private fun pause() {
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

    private fun playPauseVideo(isPlaying: Boolean) {
        handler.removeCallbacksAndMessages(null)
        binding.flController.isVisible = true
        binding.ivPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play)
        handler.postDelayed({
            if (!isAnyBarIsInTouchMode) binding.flController.isVisible = false
        }, Constants.DELAY_CONTROLLER_HIDE)
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    /*Video player code End*/

    override fun onResume() {
        super.onResume()
        if (post != null && post?.isVideoPost()!!) {
            initializePlayer()

        }
    }

    override fun onPause() {
        super.onPause()
        if (post != null && post?.isVideoPost()!!) {
            pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().hideKeyboard()
        if (post != null && post?.isVideoPost()!!) releasePlayer()
        compositeDisposable.clear()
        detailsViewModel.compositeDisposable.clear()
        if (isSettingObsReg) {
            requireContext().contentResolver.unregisterContentObserver(mSettingsContentObserver)
        }
        mProgressHandler.removeCallbacks(progressRunnable)
        mOrientationEventListener!!.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    private fun changeOrientation(isNotFullScreen: Boolean) {

        binding.ivFullScreen.setImageResource(if (isNotFullScreen) R.drawable.ic_fullscreen else R.drawable.ic_fullscreen_exit)
        (activity as MainActivity).hideShowExtraBars(isNotFullScreen)
        binding.includePostContainer.rlCommentInput.isVisible = isNotFullScreen
        val w: Int = CommonUtils.getDeviceWidth(requireContext())
        val h: Int = (activity as MainActivity).binding.drawerView.height//getDeviceHeight() - getNavigationBarHeight() + getStatusBarHeight()
        binding.linExtra.isVisible = isNotFullScreen

        binding.nestedScrollView.isFillViewport = isNotFullScreen

        if (isLandScape) {
            binding.linRoot.post {
                binding.linRoot.rotation = if (isNotFullScreen) 0f else rotation
                binding.linRoot.layoutParams.height =
                    if (isNotFullScreen) LinearLayout.LayoutParams.WRAP_CONTENT else w
                binding.linRoot.layoutParams.width =
                    if (isNotFullScreen) LinearLayout.LayoutParams.MATCH_PARENT else h

                binding.frame.layoutParams.height =
                    if (isNotFullScreen) FrameLayout.LayoutParams.WRAP_CONTENT else w
                binding.frame.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else h

                binding.flVideo.layoutParams.height =
                    if (isNotFullScreen) initialViewHeight.roundToInt()/*resources.getDimensionPixelOffset(R.dimen._170sdp)*/ else w
                binding.flVideo.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else h

                binding.videoView.layoutParams.height =
                    if (isNotFullScreen) initialViewHeight.roundToInt()/*resources.getDimensionPixelOffset(R.dimen._170sdp)*/ else w
                binding.videoView.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else h

                binding.linRoot.translationX = if (isNotFullScreen) 0f else (w - h).toFloat() / 2
                binding.linRoot.translationY = if (isNotFullScreen) 0f else (h - w).toFloat() / 2
                binding.linRoot.requestLayout()
            }
        } else {
            binding.linRoot.post {
                binding.linRoot.rotation = 0f

                binding.linRoot.layoutParams.height =
                    if (isNotFullScreen) LinearLayout.LayoutParams.WRAP_CONTENT else h
                binding.linRoot.layoutParams.width =
                    if (isNotFullScreen) LinearLayout.LayoutParams.MATCH_PARENT else w

                binding.frame.layoutParams.height =
                    if (isNotFullScreen) FrameLayout.LayoutParams.WRAP_CONTENT else h
                binding.frame.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else w

                binding.flVideo.layoutParams.height =
                    if (isNotFullScreen) initialViewHeight.roundToInt() else h
                binding.flVideo.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else w

                binding.videoView.layoutParams.height =
                    if (isNotFullScreen) initialViewHeight.roundToInt() else h
                binding.videoView.layoutParams.width =
                    if (isNotFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else w

                binding.linRoot.translationX = 0f
                binding.linRoot.translationY = 0f
                binding.linRoot.requestLayout()
            }
        }
    }

    fun goneBack(): Boolean {
        if (isFullScreen) {
            changeOrientation(isFullScreen)
            isFullScreen = !isFullScreen
            return true
        }
        return false
    }
}