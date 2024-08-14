package base.ui.fragment.menu.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import base.R
import base.data.SyncedStorage.getTables
import base.data.model.other.Table
import base.databinding.FragmentStatsTablesBinding
import base.ui.base.BaseFragment
import base.util.GD

class StatsTablesFragment : BaseFragment() {
    lateinit var binding: FragmentStatsTablesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatsTablesBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val hardcodedIds = listOf(62, 109, 117, 79, 127, 150)

    override fun onViewCreated(view: View, state: Bundle?) {

        val leagues = getLeagues()
        showItems(hardcodedIds[0])

        binding.leagueSelector.setItems(leagues)
        binding.leagueSelector.setOnItemSelectedListener { _, position, _, _ ->
            showItems(hardcodedIds[position])
        }
    }

    private fun getLeagues(): List<String> {
        // Backend is not ready, data should be hardcoded for now
        return listOf(
            "English Premier League",
            "La Liga",
            "Bundesliga",
            "Serie A",
            "France Ligue 1",
            "Primeira Liga"
        )
    }

    private fun showItems(leagueId: Int) {
        binding.tableContainer.removeAllViews()

        getTables(leagueId) {
            it.sortedBy { it.position }.distinctBy { it.teamName }.forEach {
                inflateTableItemLayout(it)
            }
        }
    }

    private fun inflateTableItemLayout(table: Table) {

        val inflater = LayoutInflater.from(context)
        val itemLayout = inflater.inflate(
            R.layout.item_table_entry, binding.tableContainer, false
        )
        itemLayout.findViewById<TextView>(R.id.position).text = table.position.toString()
        itemLayout.findViewById<TextView>(R.id.teamName).text = table.teamName
        itemLayout.findViewById<TextView>(R.id.Pl).text = table.Pl.toString()
        itemLayout.findViewById<TextView>(R.id.W).text = table.W.toString()
        itemLayout.findViewById<TextView>(R.id.D).text = table.D.toString()
        itemLayout.findViewById<TextView>(R.id.L).text = table.L.toString()
        itemLayout.findViewById<TextView>(R.id.GD).text = table.GD
        itemLayout.findViewById<TextView>(R.id.points).text = table.points.toString()
        binding.tableContainer.addView(itemLayout)
    }
}