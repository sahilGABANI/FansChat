package base.ui.adapter.wall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.application.FansChat
import base.data.model.wall.ContentItem
import base.databinding.ItemWallStreamingBinding
import base.ui.base.BaseFragment
import base.util.Constants
import base.util.onClick
import base.util.open
import com.bumptech.glide.Glide

class StreamingAdapter(
    private val fragment: BaseFragment,
    private val list: List<ContentItem>,
) : Adapter<StreamingAdapter.StreamingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamingHolder {
        return StreamingHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_wall_streaming, parent, false)
        )
    }

    override fun onBindViewHolder(holder: StreamingHolder, position: Int) {
        holder.apply {
            Glide.with(FansChat.context)
                .load(list[position].imageUrl)
                .centerCrop()
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(binding.ivImage)
            binding.tvTitle.text = "" + list[position].title
//            binding.ratingBar.rating = list[position].rating.toFloat()
            itemView.onClick {
                list[layoutPosition]._id.let {
                    itemView.open(
                        R.id.details,
                        hashMapOf(
                            "postId" to it,
                            "type" to Constants.POST_TYPE_STREAMING/*if (list[position].type.isBlank()) Constants.POST_TYPE_STREAMING else list[position].type*/
                        )
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class StreamingHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemWallStreamingBinding.bind(view)

    }
}