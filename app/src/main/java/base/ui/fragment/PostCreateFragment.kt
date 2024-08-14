package base.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.file_upload.UploadFile
import base.data.api.wall.model.WallPostRequest
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentPostCreateBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.MainActivity
import base.ui.base.BaseFragment
import base.ui.fragment.wall.viewmodel.WallPostState
import base.ui.fragment.wall.viewmodel.WallViewModel
import base.util.*
import base.util.Analytics.trackPostComposing
import base.util.CommonUtils.Companion.selector
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import javax.inject.Inject
import kotlin.math.roundToInt

class PostCreateFragment : BaseFragment() {

    fun onBackPress() {
        if (binding.title.get().isNotEmpty() || selectedFile != null) {
            showCustomDialog(getString(R.string.cancel_post), "", true) {
                goBack()
            }
        } else {
            goBack()
        }
    }

    private var _binding: FragmentPostCreateBinding? = null
    private val binding get() = _binding!!
    private val width by lazy { resources.displayMetrics.widthPixels }
    private val height by lazy {
        (if (MainActivity.height == 0) resources.displayMetrics.heightPixels else MainActivity.height) - (CommonUtils.dpToPx(
            requireContext(),
            48f
        ) + resources.getDimensionPixelOffset(R.dimen._90sdp))//70
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<WallViewModel>
    private lateinit var feedsViewModel: WallViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        feedsViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPostCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.avatar.srcRound = loggedInUserCache.getLoggedInUserProfile()
        binding.name.text = loggedInUserCache.getUserName()

        trackPostComposing()
        listenToViewModel()

        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }

        binding.btnPost.throttleClicks().subscribeAndObserveOnMainThread {
            if (binding.title.get().isEmpty()) {
                showToast(getString(R.string.enter_title))
            } else {
                requireActivity().hideKeyboard()
                selectedFile?.let { uploadingFile ->
                    if (uploadingFile.extension.isVideo) {
                        binding.loadingOverlay.visibility = View.VISIBLE
                        val videoThumbnail = uploadingFile.createVideoThumb(requireActivity())
                        UploadFile.upload(videoThumbnail).subscribe({ videoThumbnailUrl ->
                            selectedFile?.let { selected ->
                                UploadFile.upload(selected).subscribe({ videoFileUrl ->
                                    binding.loadingOverlay.visibility = View.GONE
                                    postFeed(videoThumbnailUrl, videoFileUrl, videoThumbnail.calculateAspectRatio())
                                }, {
                                    binding.loadingOverlay.visibility = View.GONE
                                    showToast(getString(R.string.failed_to_upload))
                                })
                            }
                        }, {
                            binding.loadingOverlay.visibility = View.GONE
                            showToast(getString(R.string.failed_to_upload))
                        })
                    }
//
                    if (uploadingFile.extension.isImage) {
                        binding.loadingOverlay.visibility = View.VISIBLE
                        UploadFile.upload(selectedFile!!).subscribe({
                            binding.loadingOverlay.visibility = View.GONE
                            postFeed(it, null, selectedFile.calculateAspectRatio())
                        }, {
                            binding.loadingOverlay.visibility = View.GONE
                            showToast(getString(R.string.failed_to_upload))
                        })
                    }
                } ?: run {
                    postFeed(null, null)
                }

//                if(selectedFile == null) {
//                    postFeed(null, null)
//                }
//                postFeed(null, null)
//                postFeed(null, null)
//                socketDataManager.connectionEmitter().subscribeAndObserveOnMainThread {
//                    postFeed(null, null)
//                }
            }
        }

        binding.imageButton.throttleClicks().subscribeAndObserveOnMainThread {
            requireActivity().hideKeyboard()
            selector(items = arrayOf(getString(R.string.from_gallery), getString(R.string.from_camera)), onClick = { _, i ->
                when (i) {
                    0 -> selectImages { loadImageVideo() }
                    1 -> takePhotos { loadImageVideo() }
                }
            })
        }
//
        binding.videoButton.onClick {
            requireActivity().hideKeyboard()
            selector(items = arrayOf(getString(R.string.pick_video), getString(R.string.record_video)), onClick = { _, i ->
                when (i) {
                    0 -> selectVideos { loadImageVideo() }
                    1 -> recordVideos { loadImageVideo() }
                }
            })
        }
    }

    private fun loadImageVideo() {
        var initialViewHeight =
            width / if (selectedFile?.extension?.isVideo == true) selectedFile.calculateAspectRatioForVideo() else selectedFile.calculateAspectRatio()

        if (initialViewHeight > height) {
            initialViewHeight = height.toDouble()
        } else if (initialViewHeight < resources.getDimensionPixelOffset(R.dimen._170sdp)) {
            initialViewHeight = resources.getDimensionPixelOffset(R.dimen._170sdp).toDouble()
        }

        binding.image.layoutParams.height = initialViewHeight.roundToInt()
        Glide.with(requireContext()).load(selectedFile).override(width, initialViewHeight.roundToInt())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.image.setBackgroundResource(if (selectedFile?.extension?.isVideo == true) R.color.colorPrimary else R.drawable.placeholder_flex)
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
    }

    private fun listenToViewModel() {
        feedsViewModel.wallPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is WallPostState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is WallPostState.LoadingState -> {
                    binding.loadingOverlay.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                }
                is WallPostState.PostNewWall -> {
                    selectedFile = null
                    view?.let { it1 -> hideKeyboards(it1) }
                    goBack()
                }
                else -> {}
            }
        }.autoDispose()
    }

    private fun postFeed(contentImageUrl: String?, contentVideoUrl: String?, coverAspectRatio: Double = 0.0) {
        val postWall = WallPostRequest(Constants.POST_ORIGINAL_TYPE_WALL, binding.title.text.trim().toString())

        if (binding.body.text.trim().toString().isNotEmpty()) {
            postWall.bodyText = binding.body.text.trim().toString()
        }

        if (contentImageUrl != null) {
            postWall.imageUrl = contentImageUrl
            postWall.coverAspectRatio = coverAspectRatio
        }
        if (contentVideoUrl != null) {
            postWall.videoUrl = contentVideoUrl
            postWall.thumbnailUrl = contentImageUrl
        }

        when {
            contentVideoUrl != null -> {
                postWall.contentType = Constants.POST_TYPE_VIDEO
            }
            contentImageUrl != null -> {
                postWall.contentType = Constants.POST_TYPE_IMAGE
            }
            else -> postWall.contentType = Constants.POST_TYPE_TEXT
        }

        feedsViewModel.createWallPost(wallPostRequest = postWall)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().hideKeyboard()
    }
}