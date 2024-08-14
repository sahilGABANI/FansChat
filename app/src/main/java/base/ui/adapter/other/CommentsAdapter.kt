package base.ui.adapter.other

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.data.api.authentication.LoggedInUserCache
import base.data.api.wall.WallCacheRepository
import base.data.api.wall.model.Comment
import base.data.model.other.Translation
import base.databinding.ItemCommentBinding
import base.ui.fragment.details.DetailFragment
import base.util.onClick
import base.util.open
import base.util.toChatRelative
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

const val MY_MESSAGES = 0
const val OTHER_MESSAGES = 1

class CommentsAdapter(
    val context: Context,
    val items: List<Comment>,
    val fragment: DetailFragment,
    val loggedInUserCache: LoggedInUserCache,
    val wallCacheRepository: WallCacheRepository
) :
    Adapter<CommentsAdapter.CommentsHolder>() {

    private val requestOption: RequestOptions by lazy {
        return@lazy RequestOptions().override(100, 100).circleCrop()
            .placeholder(R.drawable.avatar_placeholder).error(R.drawable.avatar_placeholder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsHolder {
        return CommentsHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CommentsHolder, position: Int) {
        val comment = items[position]

        holder.apply {
            binding.translate.isVisible = loggedInUserCache.getLoggedInUserId()
                .isNullOrBlank() || comment.authorId != loggedInUserCache.getLoggedInUserId()
            //compare by ID and original comment[en] so edited comments can be ignored
            //fragment.listCommentTranslation.find { it.translations != null && it.translations.title == comment.comment && it._id == comment.id }
            val translation: Translation? =
                wallCacheRepository.getCommentTranslation(Translation().apply {
                    this.postId = comment.postId!!;this.commentId = comment.id!!; this.oriTitle =
                    comment.comment!!
                })

            if (translation != null) {
                binding.message.text = translation.comment
                //Undo
                binding.translate.text = fragment.getString(R.string.undo_translate)
                binding.translate.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.red))
            } else {
                binding.message.text = comment.comment
                //Translate
                binding.translate.text = fragment.getString(R.string.translate)
                binding.translate.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorPrimary))
            }


            comment.author?.let {
                binding.commentAuthor.text = comment.author.displayName + "   |"
                comment.author.avatarUrl?.let {
                    Glide.with(context).load(comment.author.avatarUrl).apply(requestOption)
                        .into(binding.avatar)
                } ?: Glide.with(context).load(R.drawable.avatar_placeholder).apply(requestOption)
                    .into(binding.avatar)

            }

            binding.date.text = comment.created?.toChatRelative()

            binding.translate.onClick {
                //compare by ID and original comment[en] so edited comments can be ignored
//                val tmpTranslation: TranslateResponse? = fragment.listCommentTranslation.find { it.translations != null && it.translations.title == comment.comment && it._id == comment.id }

                val tmpTranslation: Translation? =
                    wallCacheRepository.getCommentTranslation(Translation().apply {
                        this.postId = comment.postId!!;this.commentId =
                        comment.id!!; this.oriTitle = comment.comment!!
                    })

                if (tmpTranslation != null) {
                    //Just Undo it
                    wallCacheRepository.deleteTranslation(tmpTranslation)
                    binding.message.text = comment.comment
                    binding.translate.text = fragment.getString(R.string.translate)
                    binding.translate.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorPrimary))
                } else {
                    //Just translate it
                    fragment.dialogTranslate(binding.avatar, comment.id!!)
                }
            }
            val isMyComment =
                loggedInUserCache.getLoggedInUserId() != null && comment.authorId == loggedInUserCache.getLoggedInUserId()
            binding.delete.isVisible = isMyComment
            binding.edit.isVisible = isMyComment

            binding.delete.onClick { fragment.deleteCommentData(comment) }
            binding.edit.onClick { fragment.editCommentData(comment) }
            binding.avatar.onClick {
                when {
                    loggedInUserCache.getLoginUserToken().isNullOrEmpty() -> {
                        itemView.open(R.id.authDecideFragment)
                    }
                    comment.author?.id != loggedInUserCache.getLoggedInUserId() -> {
                        comment.author?.let {
                            view.open(R.id.friendDetails, "userId" to comment.author.id)
                        }
                    }
                    else -> {
                        itemView.open(R.id.profile)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CommentsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemCommentBinding.bind(view)
    }


}