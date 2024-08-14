package base.ui.adapter.wall

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import base.data.model.wall.ContentItem
import base.ui.fragment.wall.BannerPagerFragment

class BannerPagerAdapter(
    private val list: List<ContentItem>?,
    fragmentManager: FragmentManager?,
    private val groupPosition: Int,
    private val switchPager: WallAdapter.SwitchPager,
) :
    FragmentStatePagerAdapter(fragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return list?.size ?: 0
    }

    override fun getItem(position: Int): Fragment {
        return BannerPagerFragment.newInstance(
            list!![position],
            position,
            groupPosition,
            switchPager
        )
    }
}