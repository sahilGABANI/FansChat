package base.ui.fragment.other.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.users.model.FansChatUserDetails
import base.databinding.FragmentCreateJoinChatBinding
import base.extension.goBack
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.ui.base.BaseFragment
import base.util.hideKeyboard

class CreateJoinChatContainerFragment : BaseFragment() {
    private var chatToEdit: CreateChatGroupResponse? = null

    private var _binding: FragmentCreateJoinChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentCreateJoinChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val fromCreate = arguments?.getBoolean("fromCreate")
        val fromEdit = arguments?.getBoolean("fromEdit")
        val friendUser = arguments?.getSerializable("friendUser") as FansChatUserDetails?
        if (fromEdit == true)
            chatToEdit = arguments?.getParcelable("chatToEdit") as CreateChatGroupResponse?

        binding.pager.adapter =
            CreateJoinChatContainerAdapter(
                requireContext(),
                childFragmentManager,
                fromEdit,
                chatToEdit, friendUser
            )

        if (fromCreate == true || fromEdit == true)
            binding.pager.currentItem = 0
        else
            binding.pager.currentItem = 1

        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
}