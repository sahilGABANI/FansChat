package base.ui.fragment.other.friends

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.databinding.FragmentFriendsContainerBinding
import base.extension.goBack
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.socket.SocketDataManager
import base.socket.model.InAppNotificationType
import base.ui.base.BaseFragment
import base.util.hideKeyboard
import base.util.open
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class FriendsContainerFragment : BaseFragment() {

    private var _binding: FragmentFriendsContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendsContainerAdapter: FriendsContainerAdapter

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private var viewPagerChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (position == 2) binding.friendsBadge.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentFriendsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewEvents()
    }

    override fun onDestroy() {
        binding.viewpager.unregisterOnPageChangeCallback(viewPagerChangeCallback)
        _binding = null
        super.onDestroy()
    }

    private fun listenToViewEvents() {
        binding.viewpager.registerOnPageChangeCallback(viewPagerChangeCallback)
        friendsContainerAdapter = FriendsContainerAdapter(requireActivity())
        binding.viewpager.apply {
            isUserInputEnabled = false
            adapter = friendsContainerAdapter
        }
        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.friends)
                }
                1 -> {
                    tab.text = getString(R.string.find)
                }
                2 -> {
                    tab.text = getString(R.string.requests)
                }
            }
        }.attach()

        if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            goBack()
            open(R.id.authorize)
        }

        arguments?.let {
            var friendRequest = it.getBoolean("friendRequest")
            Handler().postDelayed({
                if (friendRequest) {
                    binding.viewpager.currentItem = 2
                } else {
                    binding.viewpager.currentItem = 1
                }
            }, 600)
        }

        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()

        socketDataManager.notificationData().subscribeAndObserveOnMainThread {
            when (it.event) {
                InAppNotificationType.friendsRequestReceived -> {
                    binding.friendsBadge.visibility = View.VISIBLE
                }
                else -> {

                }
            }
        }.autoDispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }
}