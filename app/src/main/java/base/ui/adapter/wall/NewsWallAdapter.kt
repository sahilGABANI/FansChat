package base.ui.adapter.wall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.wall.ContentItem
import base.databinding.ItemWallNewsBinding
import base.util.Constants
import base.util.onClick
import base.util.open
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class NewsWallAdapter(
    val fragment: Fragment,
    val list: List<ContentItem>,
    val loggedInUserCache: LoggedInUserCache,
) : Adapter<NewsWallAdapter.NewsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        return NewsHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_wall_news, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        holder.apply {
            val contentItem = list[position]
            Glide.with(FansChat.context).load(contentItem.imageUrl).error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivImage)
            binding.tvWatch.text = "" + contentItem.watchCount
            binding.tvLike.text = "" + contentItem.likeCount.toString()
            binding.tvComment.text = "" + contentItem.commentsCount
            binding.tvTitle.text = "" + contentItem.title

            binding.linMain.onClick {/*if (loggedInUserCache.getLoginUserToken().isNullOrEmpty())
                    binding.linMain.open(R.id.authDecideFragment)
                else*/
                if (list.size > layoutPosition && fragment.isAdded && fragment.isVisible && !fragment.isRemoving) {
                    list[layoutPosition]._id.let {
                        binding.linMain.open(
                            R.id.details,
                            hashMapOf(
                                "postId" to it,
                                "type" to list[layoutPosition].type.ifBlank { Constants.POST_TYPE_NEWS })
                        )
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class NewsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemWallNewsBinding.bind(view)
    }
}