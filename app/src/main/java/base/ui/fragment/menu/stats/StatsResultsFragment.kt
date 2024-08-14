package base.ui.fragment.menu.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.R

class StatsResultsFragment : StatsFixturesFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats_results, container, false)
    }
}