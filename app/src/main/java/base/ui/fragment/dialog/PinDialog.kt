package base.ui.fragment.dialog

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import base.R
import base.application.FansChat
import base.data.SyncedStorage.submitPost
import base.data.api.authentication.LoggedInUserCache
import base.data.model.feed.FeedItem
import base.data.viewmodelmodule.ViewModelFactory
import base.extension.getViewModelFromFactory
import base.extension.goBack
import base.extension.showToast
import base.extension.subscribeAndObserveOnMainThread
import base.ui.base.*
import base.ui.fragment.dialog.viewmodel.PinDialogViewModel
import base.ui.fragment.dialog.viewmodel.PinDialogViewState
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject

open class PinDialog : BaseBottomSheetBaseFragment() {

    companion object {
        const val POST_ID: String = "postId"
        const val NEW_POST: String = "newPost"
        const val POST_TITLE: String = "postTitle"
        const val POST_IMAGE: String = "imageBitmap"
        const val POST_TYPE: String = "postType"
    }

    private var postId: String? = null
    private var postType: String = Constants.POST_TYPE_WALL

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<PinDialogViewModel>
    private lateinit var pinDialogViewModel: PinDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        pinDialogViewModel = getViewModelFromFactory(viewModelFactory)
        postId = arguments?.getString(POST_ID) ?: return
        postType = arguments?.getString(POST_TYPE) ?: return
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        listenToViewModel()
        postId?.let {
            pinDialogViewModel.getPostDetail(it, postType)
/*
            getPost(postID) {
                title.visibility = View.VISIBLE
                titleEditText.visibility = View.GONE
                showData(it)
                publishAppCompatTextView.onClick {
                    it.sharedMessage =
                            if (input.text.toString().isEmpty()) "" else input.text.toString()
                    pinPost(it)
                }
            }
*/
        } ?: run {
            val hashMap = arguments?.get(NEW_POST) as HashMap<String, Any>
            val bitmap = hashMap[POST_IMAGE] as Bitmap
            val postTitle = hashMap[POST_TITLE].toString()
            binding.title.visibility = View.GONE
            binding.input.hint = resources.getString(R.string.post_body_hint)
            binding.titleEditText.visibility = View.VISIBLE
            binding.title.text = postTitle
            binding.imageAppCompatImageView.scaleType = ImageView.ScaleType.FIT_XY
            Glide.with(FansChat.context)
                .load(bitmap)
                .apply(
                    RequestOptions()
                        .override(250, 200)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                ).into(binding.imageAppCompatImageView)

            binding.likesCount.text = "0"
            binding.commentsCount.text = "0"
            binding.date.text = Date().toRelative()
            val file = saveBitmap(bitmap)
            binding.publishAppCompatTextView.onClick {
                /*  if (isAnonymous) {
                      goBack()
                      open(R.id.authorize)
                      return@onClick
                  }*/
                if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
                    goBack()
                    open(R.id.authDecideFragment)
                } else
                    if (file != null) {
                        buttonVisibility(false)
                        upload(file) {
                            val post = FeedItem()
                            post.title = binding.titleEditText.text.toString().ifEmpty { "" }
                            post.body = binding.input.text.toString().ifEmpty { "" }
                            post.sharedMessage = binding.input.get()
                            post.authorId = user.id
                            post.author = user

                            if (it != null) {
                                if (it.extension.isImage) {
                                    post.image = it
                                }
                            }
                            submitPost(post) {
                                buttonVisibility(true)
                                hideKeyboards()
                                goBack()
                                open(R.id.feed)
                            }
                        }
                    }
            }
        }

        binding.backImageButton.onClick {
            dismiss()
        }
    }

    private fun listenToViewModel() {
        pinDialogViewModel.feedPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is PinDialogViewState.ErrorMessage -> {
                    if (it.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else
                        showToast(it.errorMessage)
                }
                is PinDialogViewState.WallPostDetails -> {
                    binding.title.visibility = View.VISIBLE
                    binding.titleEditText.visibility = View.GONE
                    showData(it.wallPost)
                    binding.publishAppCompatTextView.onClick {
                        /*it.sharedMessage =
                                if (input.text.toString().isEmpty()) "" else input.text.toString()
                        pinPost(it)*/
                    }
                }
                is PinDialogViewState.LoadingState -> {
                }
            }
        }.autoDispose()
    }

    private fun saveBitmap(bitmap: Bitmap): File? {
        var imagePath: File? =
            File(requireContext().cacheDir, "screenshot.png")
        val fos: FileOutputStream
        try {
            fos = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            imagePath = null
            Timber.e(e)
        } catch (e: IOException) {
            imagePath = null
            Timber.e(e)
        }
        return imagePath
    }

    private fun buttonVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.postProgressBar.visibility = View.GONE
            binding.publishAppCompatTextView.visibility = View.VISIBLE
        } else {
            binding.postProgressBar.visibility = View.VISIBLE
            binding.publishAppCompatTextView.visibility = View.GONE
        }
    }
}