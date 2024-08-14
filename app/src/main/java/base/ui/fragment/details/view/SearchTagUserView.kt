package base.ui.fragment.details.view

import android.content.Context
import android.view.View
import base.R
import base.data.api.users.model.FansChatUserDetails
import base.databinding.MentionViewBinding
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.util.ConstraintLayoutWithLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SearchTagUserView(context: Context) : ConstraintLayoutWithLifecycle(context) {

    private var mentionViewBinding: MentionViewBinding? = null

    private val searchTagUserClicksSubject: PublishSubject<FansChatUserDetails> = PublishSubject.create()
    val searchTagUserClicks: Observable<FansChatUserDetails> = searchTagUserClicksSubject.hide()

    private lateinit var searchTagUserInfo: FansChatUserDetails

    init {
        inflateUi()
    }

    private fun inflateUi() {
        val view = View.inflate(context, R.layout.mention_view, this)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        mentionViewBinding = MentionViewBinding.bind(view)
        throttleClicks().subscribeAndObserveOnMainThread {
            searchTagUserClicksSubject.onNext(searchTagUserInfo)
        }
    }

    fun bind(searchTagUserInfo: FansChatUserDetails) {
        this.searchTagUserInfo = searchTagUserInfo
        mentionViewBinding?.apply {
            Glide.with(context)
                .load(searchTagUserInfo.avatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.avatar_placeholder)
                .placeholder(R.drawable.avatar_placeholder)
                .into(userAppCompatImageView)
            userNameTextView.text = searchTagUserInfo.displayName ?: ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mentionViewBinding = null
    }
}