package base.ui.fragment.menu.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import base.R
import base.data.SyncedStorage.getMatches
import base.data.model.other.Match
import base.databinding.FragmentStatsFixturesBinding
import base.ui.base.BaseFragment
import base.util.onClick

open class StatsFixturesFragment : BaseFragment() {
    lateinit var binding: FragmentStatsFixturesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        binding = FragmentStatsFixturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val hardcodedIds = listOf(62, 109, 117, 79, 127, 150)

    override fun onViewCreated(view: View, state: Bundle?) {

        val it = emptyList<Match>()
        val leagues = getLeagues(it)
        loadItems(hardcodedIds[0])

        binding.leagueSelector.setItems(leagues)
        binding.leagueSelector.setOnItemSelectedListener { _, position, _, _ ->
            loadItems(hardcodedIds[position])
        }
    }

    private fun loadItems(leagueId: Int) {
        getMatches(leagueId) {
            showItems(it)
        }
    }

    open fun showItems(items: List<Match>) {
        binding.headersContainer.removeAllViews()

        val dateHeaders = getDateHeaders(items)
        dateHeaders.forEach {
            val headerLayout = LayoutInflater.from(context).inflate(R.layout.item_stats_header, binding.headersContainer, false)
            headerLayout.findViewById<TextView>(R.id.label).text = it
            headerLayout.tag = it
            binding.headersContainer.addView(headerLayout)
        }
        items.forEach {
            val itemContainer = binding.headersContainer.findViewWithTag<ViewGroup>(it.getDateTag())

            val inflater = LayoutInflater.from(context)
            val itemLayout = inflater.inflate(R.layout.item_stats_match, itemContainer, false)

            itemLayout.findViewById<TextView>(R.id.firstName).text = it.nameFirst
            itemLayout.findViewById<TextView>(R.id.secondName).text = it.nameSecond
            itemLayout.findViewById<TextView>(R.id.time).text = if (this is StatsResultsFragment) it.score else it.time

            itemLayout.onClick { showDetailView(it) }

            itemContainer.addView(itemLayout)
        }
    }

    private fun showDetailView(match: Match) {
        binding.sectionStatsDetail.detailContainer.isVisible = true
        binding.mainContainer.isVisible = false

        binding.sectionStatsDetail.firstName.text = match.nameFirst
        binding.sectionStatsDetail.secondName.text = match.nameSecond
        binding.sectionStatsDetail.extra.isVisible = false
        binding.sectionStatsDetail.score.text = match.score

        val inflater = LayoutInflater.from(context)
        match.goalscorers.forEach {
            val layoutId = if (it.isHome) R.layout.item_goal_home else R.layout.item_goal_away

            val goalItemLayout = inflater.inflate(layoutId, binding.sectionStatsDetail.detailContainer, false)
            goalItemLayout.findViewById<TextView>(R.id.info).text = "${it.name} ${it.minute}"

            if (it.isHome) binding.sectionStatsDetail.topLeftContainer.addView(goalItemLayout) else binding.sectionStatsDetail.topRightContainer.addView(
                goalItemLayout
            )
        }
    }

    // TODO: Allow to close detail view
    private fun hideDetailView() {
        binding.sectionStatsDetail.detailContainer.isVisible = false
        binding.mainContainer.isVisible = true
    }

    private fun getLeagues(items: List<Match>): List<String> {
        // Backend is not ready, data should be hardcoded for now
        if (true) return listOf(
            "English Premier League",
            "La Liga",
            "Bundesliga",
            "Serie A",
            "France Ligue 1",
            "Primeira Liga"
        )
        return items.sortedBy { it.league }.groupBy { it.league }.keys.toList()
    }

    private fun getDateHeaders(items: List<Match>): Set<String> {
        return items.sortedByDescending { it.date }.groupBy { it.getDateTag() }.keys
    }
}