package base.ui.fragment.login

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.authentication.model.FaceBookResponse
import base.data.api.authentication.model.FacebookLoginRequest
import base.data.api.authentication.model.RegisterRequest
import base.data.model.NotificationSettings
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentRegisterBinding
import base.extension.*
import base.ui.base.BaseFragment
import base.ui.fragment.login.viewmodel.RegisterViewModel
import base.ui.fragment.login.viewmodel.RegisterViewState
import base.util.*
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject

class RegisterFragment : BaseFragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<RegisterViewModel>
    private lateinit var registerViewModel: RegisterViewModel

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private var firebaseToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        registerViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                FirebaseCrashlytics.getInstance().recordException(Exception("${task.exception?.message} Fetching FCM registration token failed"))
                return@OnCompleteListener
            }

            // Get new FCM registration token
            firebaseToken = task.result
        })
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Timber.i("FacebookCallback onSuccess")
                    accessFacebookData(result)
                }

                override fun onCancel() {
                    binding.loadingOverlay.isVisible = false
                    Toast.makeText(requireActivity(), getString(R.string.login_cancelled), Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException) {
                    binding.loadingOverlay.isVisible = false
                    Toast.makeText(requireActivity(), error.message, Toast.LENGTH_LONG).show()
                }
            })
        binding.facebook.onClick { loginFacebooks() }
        binding.policy.text = Html.fromHtml(getString(R.string.policy_prompt))
        binding.policy.onClick { open(R.id.termsNPrivacyFragment) }
        listenToViewEvents()
        listenToViewModel()
    }

    private fun accessFacebookData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(loginResult.accessToken) { _, response ->
            val json = response?.jsonObject
            try {
                if (json != null) {
                    val faceBookResponse = Gson().fromJson(json.toString(), FaceBookResponse::class.java)

                    val email = faceBookResponse.userEmail
                    handleFacebookAccessToken(loginResult.accessToken, email)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                showLongToast(e.localizedMessage ?: "")
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,birthday,email,picture")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken, email: String? = null) {
        if (firebaseToken == null) {
            Toast.makeText(requireContext(), "Fail to get push token. Please try again", Toast.LENGTH_SHORT).show()
            return
        }
        val request = FacebookLoginRequest(accessToken.token)
        if (!email.isNullOrEmpty()) request.email = email

        registerViewModel.facebookLogin(request)
    }

    private fun loginFacebooks() {
        binding.loadingOverlay.isVisible = true
        logout()
        LoginManager.getInstance().logInWithReadPermissions(requireActivity(), listOf("public_profile", "email"))
    }

    private fun listenToViewModel() {
        registerViewModel.registerState.subscribeAndObserveOnMainThread {
            when (it) {
                is RegisterViewState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is RegisterViewState.LoadingState -> {
                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                    binding.register.visibility = if (it.hideButton) View.GONE else View.VISIBLE
                }
                is RegisterViewState.SuccessMessage -> {
                    view?.let { it1 -> hideKeyboards(it1) }
                    Constants.IS_FORCE_REFRESH = true
                    registerViewModel.getNotificationToggles()
                }
                is RegisterViewState.ToggleNotificationMessage -> {
                    if (it.notificationSettings != null) requireActivity().saveString(Constants.SF_NOTIFICATION_SETTINGS,
                        Gson().toJson(it.notificationSettings, NotificationSettings::class.java))
                    goBack()
                    open(R.id.termsNPrivacyFragment)
                }
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        binding.register.throttleClicks().subscribeAndObserveOnMainThread {
            if (firebaseToken == null) {
                Toast.makeText(requireContext(), "Fail to get push token. Please try again", Toast.LENGTH_SHORT).show()
                return@subscribeAndObserveOnMainThread
            }
            if (isValidate()) {
                val firstName = binding.firstname.text.toString()
                val lastName = binding.lastname.text.toString()
                val nickName = binding.nickname.text.toString()
                val email = binding.email.text.toString()
                val phone = binding.phone.text.toString()
                val password = binding.password.text.toString()
                registerViewModel.register(RegisterRequest(password = password,
                    email = email,
                    displayName = nickName,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    clubId = CLUB_ID.toString(),
                    fireBaseToken = firebaseToken))

                view?.let { it1 -> hideKeyboards(it1) }
            }
        }.autoDispose()
        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()
        binding.policy.throttleClicks().subscribeAndObserveOnMainThread {
            open(R.id.termsNPrivacyFragment)
        }.autoDispose()
    }

    private fun isValidate(): Boolean {
        when {
            binding.firstname.isFieldBlank() -> {
                showToast(getString(R.string.blank_first_name))
                return false
            }
            binding.firstname.isNotValidLength() -> {
                showToast(getString(R.string.invalid_first_name))
                return false
            }
            binding.lastname.isFieldBlank() -> {
                showToast(getString(R.string.blank_last_name))
                return false
            }
            binding.lastname.isNotValidLength() -> {
                showToast(getString(R.string.invalid_last_name))
                return false
            }
            binding.nickname.isFieldBlank() -> {
                showToast(getString(R.string.blank_nick_name))
                return false
            }
            binding.nickname.isNotValidLength() -> {
                showToast(getString(R.string.invalid_nick_name))
                return false
            }
            binding.email.isFieldBlank() -> {
                showToast(getString(R.string.blank_email))
                return false
            }
            binding.email.isNotValidEmail() -> {
                showToast(getString(R.string.invalid_email))
                return false
            }
            binding.phone.isFieldBlank() -> {
                showToast(getString(R.string.blank_phone))
                return false
            }
            binding.phone.isNotValidPhoneLength() -> {
                showToast(getString(R.string.invalid_phone))
                return false
            }
            binding.password.isFieldBlank() -> {
                showToast(getString(R.string.blank_password))
                return false
            }
            binding.password.isNotValidPasswordLength() -> {
                showToast(getString(R.string.invalid_password))
                return false
            }
            else -> return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
}