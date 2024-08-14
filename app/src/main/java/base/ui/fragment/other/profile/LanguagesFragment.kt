package base.ui.fragment.other.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.databinding.FragmentLanguagesBinding
import base.extension.goBack
import base.extension.subscribeAndObserveOnMainThread
import base.extension.throttleClicks
import base.ui.MainActivity
import base.ui.base.BaseFragment

class LanguagesFragment : BaseFragment() {
    private var _binding: FragmentLanguagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLanguagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.toolbarLanguages.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()
        binding.list.adapter = LanguagesAdapter(requireActivity() as MainActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}