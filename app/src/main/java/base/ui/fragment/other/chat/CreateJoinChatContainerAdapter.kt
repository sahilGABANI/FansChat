package base.ui.fragment.other.chat

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import base.R
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.users.model.FansChatUserDetails

import base.util.getString

class CreateJoinChatContainerAdapter(
    private val ctx: Context,
    fm: FragmentManager,
    val fromEdit: Boolean?,
    private val chatToEdit: CreateChatGroupResponse?,
    private val friendUser: FansChatUserDetails?
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ChatCreateFragment(fromEdit, chatToEdit, friendUser)
            else -> ChatJoinFragment()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> if (fromEdit == true) getString(ctx, R.string.chat_edit)
            else getString(ctx, R.string.chat_create)
            else -> getString(ctx, R.string.chat_join)
        }
    }
}