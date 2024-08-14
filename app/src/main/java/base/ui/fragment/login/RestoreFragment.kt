package base.ui.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.R
import base.application.FansChat
import base.data.api.authentication.model.RequestPasswordRequest
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentRestoreBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.login.viewmodel.ResetPasswordViewState
import base.ui.fragment.login.viewmodel.RestorePasswordViewModel
import base.util.hideKeyboard
import base.util.onClick
import javax.inject.Inject

class RestoreFragment : BaseFragment() {

    private var _binding: FragmentRestoreBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<RestorePasswordViewModel>
    private lateinit var restorePasswordViewModel: RestorePasswordViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        restorePasswordViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewEvents()
        listenToViewModel()
    }

    private fun listenToViewModel() {
        restorePasswordViewModel.resetPasswordState.subscribeAndObserveOnMainThread {
            when (it) {
                is ResetPasswordViewState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is ResetPasswordViewState.LoadingState -> {
                    binding.resetProgressBar.visibility =
                        if (it.isLoading) View.VISIBLE else View.GONE
                    binding.restore.isEnabled = !it.isLoading
                }
                is ResetPasswordViewState.SuccessMessage -> {
//                    alert(R.string.reset_check_email, R.string.password_reset).show()
                    showCustomDialog(
                        getString(R.string.password_reset),
                        getString(R.string.reset_check_email),
                        false
                    ) {
                        goBack()
                    }
                }
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()

        binding.restore.onClick {
            if (isValidate())
                restorePasswordViewModel.resetPassword(RequestPasswordRequest(binding.email.text.toString()))
        }
    }

    private fun isValidate(): Boolean {
        var isValidate = true
        when {
            binding.email.isFieldBlank() -> {
                showToast(getString(R.string.blank_email))
                isValidate = false
            }
            binding.email.isNotValidEmail() -> {
                showToast(getString(R.string.invalid_email))
                isValidate = false
            }
        }
        return isValidate
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
}