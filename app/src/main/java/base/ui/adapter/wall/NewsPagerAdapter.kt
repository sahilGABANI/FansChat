package base.ui.adapter.wall

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import base.data.model.wall.ContentItem
import base.ui.fragment.wall.NewsWallPagerFragment
import kotlin.math.ceil

class NewsPagerAdapter(private val list: List<ContentItem>, fragmentManager: FragmentManager?) :
    FragmentStatePagerAdapter(fragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return ceil((list.size / 4.0)).toInt()
    }

    override fun getItem(position: Int): Fragment {
        return NewsWallPagerFragment.newInstance(
            ArrayList(
                list.subList(
                    position * 4,
                    if (list.size <= ((position + 1) * 4)) list.size else ((position + 1) * 4)
                )
            ), position
        )
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }
}