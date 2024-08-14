package base.ui.fragment.other.profile

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import base.R
import base.application.FansChat
import base.data.SyncedStorage.setAutoTranslateEnabled
import base.data.api.authentication.LoggedInUserCache
import base.data.api.users.model.FansChatUserDetails
import base.data.model.NotificationSettings
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentProfileBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.other.profile.viewmodel.EditProfileViewModel
import base.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.EasyWayLocation.LOCATION_SETTING_REQUEST_CODE
import com.example.easywaylocation.Listener
import com.example.easywaylocation.LocationData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ProfileFragment : BaseFragment(), LocationData.AddressCallBack, Listener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<EditProfileViewModel>
    private lateinit var editProfileViewModel: EditProfileViewModel

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private lateinit var gpsTracker: GpsTracker
    var city: String? = null
    private var country: String? = null
    private var locationFailCount: Int = 0
    private lateinit var easyWayLocation: EasyWayLocation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        editProfileViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        easyWayLocation = EasyWayLocation(requireContext(), false, false, this)

        binding.switchRumorNoti.isGone = flavor == Flavors.Sporting
        binding.tvRumorNotiLabel.isGone = flavor == Flavors.Sporting
        clearObservers()
        setClickListeners()

        listenToViewModel()
        checkPermission()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception("${task.exception?.message} Fetching FCM registration token failed"))
                return@OnCompleteListener
            }
            binding.pushToken.text = task.result.toString()
        })
        binding.pushToken.setOnClickListener {
            val clipboard: ClipboardManager? = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("Copied Token", binding.pushToken.text.toString())
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Copied Token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenToViewModel() {
        editProfileViewModel.editprofileState.subscribeAndObserveOnMainThread {
            when (it) {
                is EditProfileViewModel.EditProfileViewState.ErrorMessage -> {
                    if (it.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else showToast(it.errorMessage)
                }
                is EditProfileViewModel.EditProfileViewState.LoadingState -> {
//                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                }
                is EditProfileViewModel.EditProfileViewState.UserDetails -> {
                    editProfileViewModel.getListOfFriends(it.fansChatUser.id)
                    editProfileViewModel.getNotificationToggles()
                    loadUserDetail(it.fansChatUser)
                }
                is EditProfileViewModel.EditProfileViewState.TotalFriendsCount -> {
                    binding.profileStats.friends.text = it.count.toString()
                }
                is EditProfileViewModel.EditProfileViewState.Logout -> {
                    profileLogout()
                }
                is EditProfileViewModel.EditProfileViewState.ToggleNotificationMessage -> {
                    requireActivity().saveString(
                        Constants.SF_NOTIFICATION_SETTINGS,
                        Gson().toJson(
                            it.notificationSettings,
                            NotificationSettings::class.java
                        )
                    )
//                    /*If get api call then message will be null*/
//                    it.message?.let { message -> showToast(message) } ?: kotlin.run {
//                        editProfileViewModel.getListOfFriends(user.id)
//                    }
                    showToggles()
                }
                else -> {}
            }
        }.autoDispose()
    }

    private fun showUserInfo() {
        setToggleListeners() // as we are displaying from preference
        showToggles()
        editProfileViewModel.getUserProfile()
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.ACCESS_FINE_LOCATION).request(object : OnPermissionCallback {
            @SuppressLint("MissingPermission")
            override fun onGranted(permissions: List<String>, all: Boolean) {
                if (all) {
                    easyWayLocation.startLocation()
                }
            }

            override fun onDenied(permissions: List<String>, never: Boolean) {
                if (never) {
                    //showToast("Authorization is permanently denied, please manually grant location permissions")
                    //XXPermissions.startPermissionActivity(this@ProfileFragment, permissions)
                } else {
                    showToast("Failed to obtain location permission")
                    //goBack()
                }
            }
        })
    }

    private fun loadUserDetail(user: FansChatUserDetails) {
        binding.name.text = user.displayName
        loadUserProfile()

        val pointsText = getString(R.string.points).replace("X", user.points.toString())
//        if (user.points == 1) {
//            pointsText = pointsText.removeRange(pointsText.length - 1, pointsText.length)
//        }
        binding.points.text = pointsText
        binding.progressBar.progress = (user.points ?: 0 * 100).toInt()
        binding.capLvlTextView.text = user.capsLvL?.toString() ?: "0"
        binding.email.text = user.email
        binding.phone.text = user.phone
        if (binding.location.text.isNullOrEmpty()) {
            binding.location.text = if (user.county.isNullOrEmpty()) {
                user.city
            } else {
                user.city.plus(", ${user.county}")
            }
        }
        user.created?.let {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(user.created)
                ?: return
            val dateTagFormatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            binding.regDate.text = dateTagFormatter.format(input).toString()
        }
        val lang = loggedInUserCache.getLanguage()

        binding.languages.src = if (!lang.isNullOrEmpty()) languageList.find { it.shortCode == lang }?.image else languageIcon
        showStats(user)
    }

    private fun loadUserProfile() {
        val userProfile = loggedInUserCache.getLoggedInUserProfile()
        Glide.with(requireContext()).load(userProfile).error(R.drawable.avatar_placeholder).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate().into(binding.avatar)
    }

    private fun showStats(userData: FansChatUserDetails) {
        binding.profileStats.caps.text = userData.capsLvL.toString()
        binding.profileStats.followers.text = userData.followers.toString()
        binding.profileStats.videosWatched.text = userData.videosWatched.toString()
        binding.profileStats.videoChats.text = userData.videoChats.toString()
        binding.profileStats.matchesHome.text = userData.matches.toString()
        binding.profileStats.likes.text = userData.receivedLikes.toString()
        binding.profileStats.daysUsing.text = userData.daysUsingSSK.toString()
        binding.profileStats.following.text = userData.following.toString()
        binding.profileStats.posts.text = userData.wallPosts.toString()
        binding.profileStats.chats.text = userData.chats.toString()
        binding.profileStats.chatsPublic.text = userData.publicChats.toString()
        binding.profileStats.comments.text = userData.commentsCount.toString()
    }

    private fun showToggles() {
        val notificationSettingJson = requireActivity().getStringFromPref(Constants.SF_NOTIFICATION_SETTINGS, "")
        var notificationSettings: NotificationSettings? = null
        if (notificationSettingJson.isNotEmpty()) {
            try {
                notificationSettings = Gson().fromJson(notificationSettingJson, NotificationSettings::class.java)
            } catch (e: Exception) {
                //can remove in next build
            }
        }

        binding.switchAutoTranslate.isChecked = requireActivity().isAutoTranslateEnabled()
        binding.switchWallNoti.isChecked = notificationSettings == null || notificationSettings.wall
        binding.switchNewsNoti.isChecked = notificationSettings == null || notificationSettings.news
        binding.switchSocialNoti.isChecked = notificationSettings == null || notificationSettings.social
        binding.switchRumorNoti.isChecked = notificationSettings == null || notificationSettings.rumor
    }

    private fun setToggleListeners() {
        binding.switchAutoTranslate.onClick { setAutoTranslateEnabled(binding.switchAutoTranslate.isChecked) }

        binding.switchWallNoti.onClick {
            editProfileViewModel.updateNotificationToggles(
                if (binding.switchWallNoti.isChecked) null else Constants.NS_WALL,
                if (binding.switchWallNoti.isChecked) Constants.NS_WALL else null,
                getString(
                    if (binding.switchWallNoti.isChecked) R.string.notification_unmuted else R.string.notification_muted,
                    getString(R.string.notify_wall)
                )
            )
        }

        binding.switchRumorNoti.onClick {
            editProfileViewModel.updateNotificationToggles(
                if (binding.switchRumorNoti.isChecked) null else Constants.NS_RUMOR,
                if (binding.switchRumorNoti.isChecked) Constants.NS_RUMOR else null,
                getString(
                    if (binding.switchRumorNoti.isChecked) R.string.notification_unmuted else R.string.notification_muted,
                    getString(R.string.notify_rumours)
                )
            )
        }
        binding.switchNewsNoti.onClick {
            editProfileViewModel.updateNotificationToggles(
                if (binding.switchNewsNoti.isChecked) null else Constants.NS_NEWS,
                if (binding.switchNewsNoti.isChecked) Constants.NS_NEWS else null,
                getString(
                    if (binding.switchNewsNoti.isChecked) R.string.notification_unmuted else R.string.notification_muted,
                    getString(R.string.notify_news)
                )
            )
        }
        binding.switchSocialNoti.onClick {
            editProfileViewModel.updateNotificationToggles(
                if (binding.switchSocialNoti.isChecked) null else Constants.NS_SOCIAL,
                if (binding.switchSocialNoti.isChecked) Constants.NS_SOCIAL else null,
                getString(
                    if (binding.switchSocialNoti.isChecked) R.string.notification_unmuted else R.string.notification_muted,
                    getString(R.string.notify_social)
                )
            )
        }
    }

    private fun setClickListeners() {
        binding.editProfile.whenClicked()
        binding.languages.whenClicked()
        binding.back.throttleClicks().subscribeAndObserveOnMainThread {
            goBack()
        }
        binding.logout.onClick {
            confirm {
                editProfileViewModel.logout()
            }
        }
    }

    private fun profileLogout() {
        try {
            logout()
            socketDataManager.disconnect()
            loggedInUserCache.clearLoggedInUserLocalPrefs()
            FirebaseMessaging.getInstance().deleteToken()
        } catch (e: Exception) {
            Timber.e(e)
        }
        goBack()
    }

    private fun confirm(callback: () -> Unit) {
        showCustomDialog(getString(R.string.are_you_sure), getString(R.string.logout_from_app), true) {
            callback.invoke()
        }
    }

    override fun locationData(locationData: LocationData?) {

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
        if (!isSafe()) {
            return
        }
        val locationData = location ?: return
        val latitude = locationData.latitude
        val longitude = locationData.longitude
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                city = addresses[0].locality
                country = addresses[0].countryName
                if (binding.location.text.isNullOrEmpty()) {
                    binding.location.text = if (country.isNullOrEmpty()) {
                        city
                    } else {
                        city.plus(", $country")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun locationCancelled() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTING_REQUEST_CODE) {
            easyWayLocation.onActivityResult(resultCode)
        }
    }

    override fun onResume() {
        super.onResume()
        locationFailCount = 0
        showUserInfo()
        easyWayLocation.startLocation()
    }

    override fun onPause() {
        super.onPause()
        try {
            easyWayLocation.endUpdates()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onDestroy() {
        try {
            easyWayLocation.endUpdates()
        } catch (e: Exception) {
            Timber.e(e)
        }
        super.onDestroy()
    }
}