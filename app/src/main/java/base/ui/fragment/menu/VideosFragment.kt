package base.ui.fragment.menu

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import base.R
import base.databinding.FragmentWebBinding
import base.ui.base.BaseFragment
import base.util.Analytics
import base.util.getScale

class VideosFragment : BaseFragment() {
    private val urlStringId = R.string.youtube_url
    lateinit var binding: FragmentWebBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.toolbarWebViewLayout.linRoot.visibility = View.GONE

        binding.webview.setInitialScale(getScale())
        Analytics.trackTvOpened()

        binding.webview.webViewClient = Browser_Home()
        binding.webview.webChromeClient = ChromeClient(requireActivity())

        val webSettings: WebSettings = binding.webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)

        loadWebSite()
    }

    private fun loadWebSite() {
        binding.webview.loadUrl(getString(urlStringId))
    }

    private class Browser_Home : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }
    }

    private open class ChromeClient(val context: Activity) : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(context.resources, 2130837573)
        }

        override fun onHideCustomView() {
            (context.window.decorView as FrameLayout).removeView(
                mCustomView
            )
            mCustomView = null
            context.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            context.requestedOrientation = mOriginalOrientation
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View,
            paramCustomViewCallback: CustomViewCallback
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility =
                context.window.decorView.systemUiVisibility
            mOriginalOrientation = context.requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            (context.window.decorView as FrameLayout).addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            context.window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}