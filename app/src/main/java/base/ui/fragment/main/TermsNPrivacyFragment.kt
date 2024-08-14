package base.ui.fragment.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import base.BuildConfig
import base.databinding.FragmentTermsNPrivacyBinding
import base.extension.goBack
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.ui.base.BaseFragment

class TermsNPrivacyFragment : BaseFragment() {

    private var _binding: FragmentTermsNPrivacyBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTermsNPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val privacyPolicyUrl = BuildConfig.BASE_URL + "privacy-policy"
        binding.termsNPrivacyWebView.loadUrl(privacyPolicyUrl)
        binding.termsNPrivacyWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                handleProgressVisibility(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                handleProgressVisibility(false)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                handleProgressVisibility(false)
            }
        }

//        if (requireActivity() is MainActivity)
//            (requireActivity() as MainActivity).showBadgeOnFriendRequest()

        binding.acceptButton.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()
    }

    private fun handleProgressVisibility(isVisible: Boolean) {
        try {
            binding.tnpProgressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.acceptButton.isEnabled = !isVisible
        } catch (e: Exception) {

        }
    }
}