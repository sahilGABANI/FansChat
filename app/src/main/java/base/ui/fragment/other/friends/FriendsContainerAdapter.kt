package base.ui.fragment.other.friends


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import base.ui.fragment.other.friends.my.MyFriendsFragment
import base.ui.fragment.other.friends.requests.FriendRequestsFragment
import base.ui.fragment.other.friends.search.FriendsSearchFragment

class FriendsContainerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                MyFriendsFragment()
            }
            1 -> {
                FriendsSearchFragment()
            }
            2 -> {
                FriendRequestsFragment()
            }
            else -> {
                MyFriendsFragment()
            }
        }
    }
}