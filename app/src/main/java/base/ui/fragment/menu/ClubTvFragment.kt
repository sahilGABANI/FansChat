package base.ui.fragment.menu

import android.os.Bundle
import android.view.View
import base.R
import base.ui.fragment.main.NewsFragment
import base.util.Constants

class ClubTvFragment : NewsFragment() {

    override val type = Constants.POST_TYPE_CLUB_TV

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)

        binding.headerNews.topCaption.text = getString(R.string.club_tv)
    }
}