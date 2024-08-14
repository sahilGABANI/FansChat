package base.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.Navigation
import base.BaseActivity
import base.R
import base.data.SyncedStorage.submitRepost
import base.data.api.Api
import base.data.model.CommonFeedItem
import base.data.model.PostShare
import base.data.model.feed.FeedItem
import base.databinding.PinDialogBinding
import base.extension.showToast
import base.ui.fragment.dialog.PinDialog
import base.ui.fragment.main.NewsFragment
import base.util.*
import base.views.CustomProgressDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.File

@SuppressLint("ValidFragment")
abstract class BaseBottomSheetBaseFragment(
    private val needsLogin: Boolean = false
) : BottomSheetDialogFragment() {

    lateinit var binding: PinDialogBinding
    val disposables by lazy { CompositeDisposable() }

    fun Disposable.autoDispose() {
        disposables.add(this)
    }

    /* override fun onStart() {
         super.onStart()
         setupBackButton()
     }

     private fun setupBackButton() {
         findOptional<View>(R.id.back)?.onClick { goBack() }
     }
 */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        if (!::binding.isInitialized) binding = PinDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        when (this) {
            is NewsFragment -> Analytics.trackNewsSectionOpened()
//            is DetailFragment -> Analytics.trackPostViewed(postId ?: "")
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        selectedFile = null
        super.onDestroyView()
    }
}


fun android.app.Activity.goBack() {
    val navController = Navigation.findNavController(this, R.id.fragmentsContainer)
    navController.popBackStack()
}

fun BaseBottomSheetBaseFragment.goBack() {
    activity?.goBack()
}

fun PinDialog.showData(post: CommonFeedItem) {
//    val post = if (item is PostShare) item.referencedItem!! else item
    binding.title.text = post.title
//    imageAppCompatImageView.scaleType = post.scaleType
    Glide.with(requireContext()).load(post.imageUrl).apply(
        RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.placeholder).error(R.drawable.placeholder)
    ).into(binding.imageAppCompatImageView)

    binding.likesCount.text = post.likeCount.toString()
    binding.commentsCount.text = post.commentsCount.toString()
    binding.date.text = post.created?.toRelative() ?: ""
}

val BaseBottomSheetBaseFragment.act get() = activity as BaseActivity

fun PinDialog.pinPost(post: FeedItem) {
    if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
        open(R.id.authorize); return
    }
    val item = PostShare()
    item.type = post.shareType
    item.referencedItem = if (post.title.isEmpty() && post.body.isEmpty()) {
        post.let { it.referencedItem }
    } else post
    item.referencedItemId = post.id
    item.sharedMessage = post.sharedMessage
    item.author = user

    submitRepost(item) { goBack(); open(R.id.feed); }
}

fun BaseBottomSheetBaseFragment.open(destination: Int, params: Pair<String, Any>? = null) {
    act.open(destination, params)
}

fun BaseBottomSheetBaseFragment.upload(file: File?, callback: (String?) -> Unit) {
    if (file != null) {
        val dialog = CustomProgressDialog()
        dialog.show(requireContext())
        Api.upload(requireContext(), file).subscribe({
            dialog.dialog.dismiss()
            callback.invoke(it); selectedFile = null
        }, {
            dialog.dialog.dismiss()
            showToast(getString(R.string.failed_to_upload))
        }).autoDispose()
    } else {
        callback.invoke(null)
    }
}

fun BaseBottomSheetBaseFragment.hideKeyboards() {
    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
}