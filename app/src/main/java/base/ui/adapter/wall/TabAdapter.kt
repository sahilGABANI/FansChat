package base.ui.adapter.wall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.databinding.LayoutTabBinding
import base.util.onClick

class TabAdapter(
    private val list: List<String>,
    private val listFiltersApplied: MutableList<String>,
    private val checkedColor: Int,
    private val unCheckedColor: Int,
    val callback: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<TabAdapter.TabHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
        return TabHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_tab, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TabHolder, position: Int) {
        holder.apply {
            binding.tvFilter.text = list[position]
            binding.tvFilter.setTextColor(if (listFiltersApplied.contains(list[position])) checkedColor else unCheckedColor)
            binding.tvFilter.isSelected = listFiltersApplied.contains(list[position])
            itemView.onClick {
                binding.tvFilter.isSelected = !binding.tvFilter.isSelected
                if (binding.tvFilter.isSelected)
                    listFiltersApplied.add(binding.tvFilter.text.toString())
                else
                    listFiltersApplied.remove(binding.tvFilter.text.toString())
                binding.tvFilter.setTextColor(if (listFiltersApplied.contains(list[position])) checkedColor else unCheckedColor)
                callback.invoke(layoutPosition, binding.tvFilter.isSelected)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class TabHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = LayoutTabBinding.bind(view)

    }
}