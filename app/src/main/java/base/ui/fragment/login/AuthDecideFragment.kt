package base.ui.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.databinding.FragmentAuthDecideBinding
import base.extension.goBack
import base.ui.MainActivity
import base.ui.base.BaseFragment
import base.util.whenClicked

class AuthDecideFragment : BaseFragment() {

    private var _binding: FragmentAuthDecideBinding? = null
    private val binding get() = _binding!!

    private var visibleSecondTime = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthDecideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {

        if (visibleSecondTime) {
            goBack(); return
        }
        visibleSecondTime = !visibleSecondTime

        binding.login.whenClicked()
        binding.register.whenClicked()

        //if (requireActivity() is MainActivity) (requireActivity() as MainActivity).showBadge(false)
    }
}