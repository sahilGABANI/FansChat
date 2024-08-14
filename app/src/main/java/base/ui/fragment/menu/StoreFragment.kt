package base.ui.fragment.menu

import android.os.Bundle
import android.view.View
import base.R
import base.util.WebFragment

open class StoreFragment : WebFragment(R.string.url_store) {

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state) // required

        binding.toolbarWebViewLayout.pinStat.isEnabled = binding.webview.url != homeUrl
    }
}