package base.ui.fragment.other

import android.os.Bundle

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import base.data.model.other.LiveMatch

class MatchesAdapter(val items: List<LiveMatch>, fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val params = Bundle().apply {
            putSerializable("match", items[position])
        }
        return MatchFragment().apply { arguments = params }
    }

    override fun getCount(): Int {
        return items.size
    }
}