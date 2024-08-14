package base.ui.fragment.other.profile

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import base.R
import base.ui.MainActivity
import base.util.*

class LanguagesAdapter(val activity: MainActivity) : Adapter<LanguagesHolder>() {

    val items = languageList
    private val names by lazy { activity.resources.getStringArray(R.array.languages) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagesHolder {
        return LanguagesHolder(parent.inflate(R.layout.item_language))
    }

    override fun onBindViewHolder(holder: LanguagesHolder, position: Int) {
        val item = items[position]
        holder.apply {
            name.text = names[item.index - 1]
            image.src = item.image

            itemView.onClick {
                activity.loggedInUserCache.setLanguage(item.shortCode)
                activity.saveString("language", item.shortCode)
                activity.restartApp()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}