package base.ui.adapter.wall

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import base.data.model.wall.ContentItem
import base.ui.fragment.wall.WatchNowWallPagerFragment

class WatchNowPagerAdapter(
    private val list: List<ContentItem>?,
    fragmentManager: FragmentManager?,
    private val groupPosition: Int/*,
    private val type: String*/
) :
    FragmentStatePagerAdapter(fragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return list?.size ?: 0
    }

    override fun getItem(position: Int): Fragment {
        return WatchNowWallPagerFragment.newInstance(
            list!![position],
            position,
            groupPosition/*,
            type*/
        )
    }
}