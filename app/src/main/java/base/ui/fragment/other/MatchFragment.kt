package base.ui.fragment.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import base.R
import base.data.model.other.LiveMatch
import base.databinding.PageNextMatchesBinding
import base.ui.base.BaseFragment
import base.util.*

class MatchFragment : BaseFragment() {
    lateinit var binding: PageNextMatchesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        binding = PageNextMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val match = getArgument<LiveMatch>()

        binding.nameFirst.text = match.nameFirst
        binding.nameSecond.text = match.nameSecond

        binding.imageFirst.src = match.imageFirst
        binding.imageSecond.src = match.imageSecond

        binding.tickets.isVisible = match.ticketsUrl != null
        binding.tickets.whenClicked()
    }
}