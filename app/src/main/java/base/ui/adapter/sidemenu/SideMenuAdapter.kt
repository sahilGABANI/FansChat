package base.ui.adapter.sidemenu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.data.SideMenuOption
import base.databinding.ItemSideMenuBinding
import base.extension.Flavors
import base.extension.flavor
import base.util.CommonUtils
import base.util.onClick
import kotlin.math.ceil

class SideMenuAdapter(val context: Context, val callback: (option: SideMenuOption) -> Unit) : RecyclerView.Adapter<SideMenuAdapter.MyViewHolder>() {
    val list = arrayListOf<SideMenuOption>()
    var currentDest: Int = 0
    var deviceSizeInch = 0.0
    var optionalIconSize = 0
    private var titleTextColor = 0

    init {
        currentDest = R.id.feed
        prepareSideMenuOptions()
        deviceSizeInch = CommonUtils.getDeviceSizeInInch(context)
        optionalIconSize = context.resources.getDimensionPixelSize(R.dimen._32sdp)
    }

    private fun prepareSideMenuOptions() {
        when (flavor) {
            Flavors.MTN -> {
                titleTextColor = ContextCompat.getColor(context, R.color.colorPrimary)
                list.add(SideMenuOption(R.id.feed, R.string.wall, R.drawable.menu_fc))
                list.add(SideMenuOption(R.id.chat, R.string.chat, R.drawable.menu_chat))
                list.add(SideMenuOption(R.id.news, R.string.news, R.drawable.menu_news))
                list.add(SideMenuOption(R.id.social, R.string.social, R.drawable.menu_social))
                list.add(SideMenuOption(R.id.rumours, R.string.rumours, R.drawable.menu_rumors))
                list.add(SideMenuOption(R.id.tv, R.string.tv, R.drawable.menu_club_tv))
                //list.add(SideMenuOption(R.id.stats, R.string.stats, R.drawable.icon_menu_stats_big))
                //list.add(SideMenuOption(R.id.store, R.string.store, R.drawable.store))
                //list.add(SideMenuOption(R.id.tickets, R.string.tickets, R.drawable.icon_menu_tickets_big))
                //list.add(SideMenuOption(R.id.calendar, R.string.calendar, R.drawable.icon_menu_calendar_big))
            }
            Flavors.Sporting -> {
                titleTextColor = ContextCompat.getColor(context, R.color.bg_wall_start)
                list.add(SideMenuOption(R.id.feed, R.string.wall, R.drawable.menu_fc))
                list.add(SideMenuOption(R.id.chat, R.string.chat, R.drawable.menu_chat))
                list.add(SideMenuOption(R.id.news, R.string.news, R.drawable.menu_news))
                list.add(SideMenuOption(R.id.social, R.string.social, R.drawable.menu_social))
                list.add(SideMenuOption(R.id.stats, R.string.stats, R.drawable.icon_menu_stats_big))
                list.add(SideMenuOption(R.id.tickets, R.string.tickets, R.drawable.icon_menu_tickets_big))
                list.add(SideMenuOption(R.id.store, null, R.drawable.tutorialicon_store))
                list.add(SideMenuOption(R.id.tv, null, R.drawable.tutorialicon_tv))
                list.add(SideMenuOption(R.id.calendar, R.string.calendar, R.drawable.icon_menu_calendar_big))
            }
            Flavors.Bal -> {
                titleTextColor = ContextCompat.getColor(context, R.color.colorPrimary)
                list.add(SideMenuOption(R.id.feed, R.string.wall, R.drawable.menu_fc))
                list.add(SideMenuOption(R.id.chat, R.string.chat, R.drawable.menu_chat))
                list.add(SideMenuOption(R.id.news, R.string.news, R.drawable.menu_news))
                list.add(SideMenuOption(R.id.stats, R.string.stats, R.drawable.icon_menu_stats_big))
                list.add(SideMenuOption(R.id.rumours, R.string.rumours, R.drawable.menu_rumors))
                list.add(SideMenuOption(R.id.store, R.string.store, R.drawable.store))
                list.add(SideMenuOption(R.id.tv, R.string.tv, R.drawable.menu_club_tv))
                list.add(SideMenuOption(R.id.social, R.string.social, R.drawable.menu_social))
                list.add(SideMenuOption(R.id.tickets, R.string.tickets, R.drawable.icon_menu_tickets_big))
                list.add(SideMenuOption(R.id.calendar, R.string.calendar, R.drawable.icon_menu_calendar_big))
            }
            Flavors.Sokkaa -> {
                titleTextColor = ContextCompat.getColor(context, R.color.colorPrimary)
                list.add(SideMenuOption(R.id.feed, R.string.wall, R.drawable.menu_fc))
                list.add(SideMenuOption(R.id.chat, R.string.chat, R.drawable.menu_chat))
                list.add(SideMenuOption(R.id.news, R.string.news, R.drawable.menu_news))
                list.add(SideMenuOption(R.id.rumours, R.string.rumours, R.drawable.menu_rumors))
                list.add(SideMenuOption(R.id.social, R.string.social, R.drawable.menu_social))
                list.add(SideMenuOption(R.id.stats, R.string.stats, R.drawable.icon_menu_stats_big))
                list.add(SideMenuOption(R.id.fantasy, R.string.fantasy, R.drawable.icon_menu_fantasy_big))
                list.add(SideMenuOption(R.id.tv, null, R.drawable.tutorialicon_tv))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_side_menu, parent, false)
        view.layoutParams.height = ceil(parent.height / ceil(itemCount / 2.0)).toInt()
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            binding.tvOption.setTextColor(titleTextColor)
            if (position == list.size) {
                binding.rlMain.isActivated = true
                binding.ivOption.setImageResource(R.drawable.logo_fanschat)
                binding.ivOption.isActivated = true
                binding.tvOption.isVisible = false
            } else {
                list[position].optionName?.let {
                    binding.ivOption.setImageResource(list[position].optionIcon)
                    binding.tvOption.text = context.resources.getString(list[position].optionName!!)
                    binding.tvOption.isVisible = true
                    binding.ivBigOption.isVisible = false
                } ?: kotlin.run {
                    binding.ivBigOption.setImageResource(list[position].optionIcon)
                    binding.ivOption.setImageResource(0)
                    binding.tvOption.isVisible = false
                    binding.ivBigOption.isVisible = true
                }

                binding.rlMain.isActivated = false
                binding.ivOption.isActivated = false

                binding.rlMain.isSelected = list[position].id == currentDest
                binding.ivOption.isSelected = list[position].id == currentDest

                itemView.onClick { callback.invoke(list[position]) }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemSideMenuBinding.bind(view)

        init {
            if (deviceSizeInch <= 5.5) {
                binding.ivOption.layoutParams.height = optionalIconSize
                binding.ivOption.layoutParams.width = optionalIconSize
            }
        }
    }
}