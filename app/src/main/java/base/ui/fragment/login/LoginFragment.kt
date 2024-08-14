package base.ui.fragment.login

import android.os.Bundle
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
import base.data.api.authentication.model.LoginRequest
import base.data.model.NotificationSettings
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentLoginBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.login.viewmodel.LoginViewModel
import base.ui.fragment.login.viewmodel.LoginViewState
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


class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<LoginViewModel>
    private lateinit var loginViewModel: LoginViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private var firebaseToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        loginViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
            override fun onSuccess(loginResult: LoginResult) {
                Timber.i("FacebookCallback onSuccess")
                accessFacebookData(loginResult)
            }

            override fun onCancel() {
                binding.loadingOverlay.isVisible = false
                Toast.makeText(requireActivity(), getString(R.string.login_cancelled), Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                binding.loadingOverlay.isVisible = false
                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_LONG).show()
            }
        })
        binding.facebook.onClick { loginFaceBooks() }
        binding.restore.whenClicked()
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
        parameters.putString("fields", "id,name,email")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken, email: String? = null) {
        if (firebaseToken == null) {
            Toast.makeText(requireContext(), "Fail to get push token. Please try again", Toast.LENGTH_SHORT).show()
            return
        }
        val request = FacebookLoginRequest(accessToken.token, fireBaseToken = firebaseToken)
        if (!email.isNullOrEmpty()) request.email = email
        loginViewModel.facebookLogin(request)
    }

    private fun loginFaceBooks() {
        binding.loadingOverlay.isVisible = true
        logout()
        LoginManager.getInstance().logInWithReadPermissions(requireActivity(), listOf("email", "public_profile"))
    }

    private fun listenToViewModel() {
        loginViewModel.loginState.subscribeAndObserveOnMainThread {
            when (it) {
                is LoginViewState.ErrorMessage -> {
                    showToast(it.errorMessage.ifBlank { resources.getString(R.string.wrong_email_or_password_please_try_again) })
                }
                is LoginViewState.LoadingState -> {
                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                    binding.loginButton.visibility = if (it.hideButton) View.GONE else View.VISIBLE
                }
                is LoginViewState.SuccessMessage -> {
                    view?.let { it1 -> hideKeyboards(it1) }
                    Constants.IS_FORCE_REFRESH = true
                    // goBack()
                    loginViewModel.getNotificationToggles()
                }
                is LoginViewState.ToggleNotificationMessage -> {
                    if (it.notificationSettings != null) requireActivity().saveString(Constants.SF_NOTIFICATION_SETTINGS,
                        Gson().toJson(it.notificationSettings, NotificationSettings::class.java))
                    goBack()
                }
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {

        binding.signUpTextView.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
            open(R.id.registerFragment)
        }.autoDispose()

        /*Glide.with(requireContext())
            .load(R.drawable.background_clear)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.background)*/

        binding.loginButton.throttleClicks().subscribeAndObserveOnMainThread {
            if (firebaseToken == null) {
                Toast.makeText(requireContext(), "Fail to get push token. Please try again", Toast.LENGTH_SHORT).show()
                return@subscribeAndObserveOnMainThread
            }
            if (isValidate()) {
                loginViewModel.login(LoginRequest(binding.emailIdEditTextView.text.toString(),
                    binding.passwordEditTextView.text.toString(),
                    firebaseToken))
                view?.let { it1 -> hideKeyboards(it1) }
            }
        }.autoDispose()

        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }.autoDispose()

        /*if (flavor == Flavors.MTN) {
            binding.signUpTextView.throttleClicks().subscribeAndObserveOnMainThread { open(R.id.registerFragment) }.autoDispose()
        }*/
    }

    private fun isValidate(): Boolean {
        var isValidate = true
        when {
            binding.emailIdEditTextView.isFieldBlank() -> {
                showToast(getString(R.string.blank_email))
                isValidate = false
            }
            binding.emailIdEditTextView.isNotValidEmail() -> {
                showToast(getString(R.string.invalid_email))
                isValidate = false
            }
            binding.passwordEditTextView.isFieldBlank() -> {
                showToast(getString(R.string.blank_password))
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