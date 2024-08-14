package base.ui.fragment.other.profile

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.authentication.model.UpdateAvatarRequest
import base.data.api.authentication.model.UpdateProfileRequest
import base.data.api.file_upload.UploadFile
import base.data.api.users.model.FansChatUserDetails
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentProfileEditBinding
import base.extension.*
import base.ui.base.BaseFragment
import base.ui.fragment.other.profile.viewmodel.EditProfileViewModel
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.IOException
import java.util.*
import javax.inject.Inject


class ProfileEditFragment : BaseFragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<EditProfileViewModel>
    private lateinit var editProfileViewModel: EditProfileViewModel

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private lateinit var gpsTracker: GpsTracker
    var city: String? = null
    private var country: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        editProfileViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewEvents()
        listenToViewModel()
        checkPermission()
        editProfileViewModel.getUserProfile()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.camera.setOnClickListener {
            this.takePhotos { binding.avatar.srcRound = selectedFile }
        }
        binding.gallery.onClick { selectImages { binding.avatar.srcRound = selectedFile } }
    }

    private fun listenToViewModel() {
        editProfileViewModel.editprofileState.subscribeAndObserveOnMainThread { viewState ->
            when (viewState) {
                is EditProfileViewModel.EditProfileViewState.ErrorMessage -> {
                    showToast(viewState.errorMessage)
                }
                is EditProfileViewModel.EditProfileViewState.ErrorMessages -> {
                    showToast(viewState.errorMessage)
                }
                is EditProfileViewModel.EditProfileViewState.LoadingState -> {
                    binding.editProfileProgressBar.visibility =
                        if (viewState.isLoading) View.VISIBLE else View.GONE

                    if (viewState.hideButton)
                        binding.save.isEnabled = !viewState.isLoading
                }
                is EditProfileViewModel.EditProfileViewState.UpdateUserDetails -> {

                    selectedFile?.let { file ->
                        binding.editProfileProgressBar.isVisible = true
                        UploadFile.upload(file).subscribe({
                            editProfileViewModel.updateAvatar(UpdateAvatarRequest(it))
                            selectedFile = null
                        }, {
                            binding.editProfileProgressBar.isVisible = false
                            showToast(getString(R.string.failed_to_upload))
                        })
                    }

                    if (selectedFile == null) {
                        binding.editProfileProgressBar.isVisible = false
                        loggedInUserCache.setLoggedInUserProfile(viewState.fansChatUser.avatarUrl)

                        goBack()
                    }
                }
                is EditProfileViewModel.EditProfileViewState.UserDetails -> {
                    binding.editProfileProgressBar.visibility = View.GONE
                    showUserInfo(viewState.fansChatUser)
                }
                else -> {}
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        binding.save.throttleClicks().subscribeAndObserveOnMainThread {
            hideKeyBoard()
            if (isValidate()) {
                val updateProfileRequest = UpdateProfileRequest(
                    displayName = binding.nickname.text.toString(),
                    firstName = binding.firstname.text.toString(),
                    lastName = binding.lastname.text.toString(),
                    email = binding.email.text.toString(),
                    phone = binding.phone.text.toString(),
                    passwordConfirm = loggedInUserCache.getLoggedInPassword()
                )
                if (city.isNullOrEmpty()) {
                    updateProfileRequest.city = city
                }
                if (country.isNullOrEmpty()) {
                    updateProfileRequest.country = country
                }
                editProfileViewModel.updateUserProfile(updateProfileRequest)
            }
        }.autoDispose()

        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
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
            binding.email.isNotValidEmail() -> {
                showToast(getString(R.string.blank_email))
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
        }
        return true
    }

    private fun showUserInfo(user: FansChatUserDetails) {
        //binding.avatar.srcRound = user.avatarUrl
        loadUserProfile()
        binding.firstname.setText(user.firstName)
        binding.lastname.setText(user.lastName)
        binding.nickname.setText(user.displayName)
        binding.email.setText(user.email)
        binding.phone.setText(user.phone)
    }

    private fun loadUserProfile() {
        val userProfile = loggedInUserCache.getLoggedInUserProfile()
        Glide.with(requireContext())
            .load(userProfile)
            .error(R.drawable.avatar_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .into(binding.avatar)
    }

    private fun hideKeyBoard() {
        try {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.ACCESS_FINE_LOCATION)
            .request(object : OnPermissionCallback {
                @SuppressLint("MissingPermission")
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        getLocation()
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        openSnackBar(permissions)
                    } else {
                        //showToast("Failed to obtain location permission")
                        //goBack()
                    }
                }
            })
    }

    private fun openSnackBar(permissions: List<String>) {
        Snackbar.make(binding.root, "Location permission is permanently denied, Please manually grant location permissions", Snackbar.LENGTH_LONG)
            .setAction("Setting") {
                XXPermissions.startPermissionActivity(this@ProfileEditFragment, permissions)
            }.show()
    }

    fun getLocation() {
        gpsTracker = GpsTracker(requireActivity())
        if (gpsTracker.canGetLocation()) {
            val latitude = gpsTracker.getLatitude()
            val longitude = gpsTracker.getLongitude()

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses != null && addresses.isNotEmpty()) {
                    city = addresses[0].locality
                    country = addresses[0].countryName
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            gpsTracker.showSettingsAlert()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().hideKeyboard()
    }
}