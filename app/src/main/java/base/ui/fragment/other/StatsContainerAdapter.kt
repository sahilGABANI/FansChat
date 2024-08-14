package base.ui.fragment.other

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import base.R
import base.ui.fragment.menu.stats.StatsFixturesFragment
import base.ui.fragment.menu.stats.StatsResultsFragment
import base.ui.fragment.menu.stats.StatsTablesFragment
import base.util.getString

class StatsContainerAdapter(val fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> StatsFixturesFragment()
            1 -> StatsResultsFragment()
            else -> StatsTablesFragment()
        }
    }

    override fun getCount() = 3

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> getString(R.string.fixtures)
            1 -> getString(R.string.results)
            else -> getString(R.string.tables)
        }
    }
}