package base.util

import android.Manifest.permission.*
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_VIDEO_CAPTURE
import android.provider.MediaStore.EXTRA_OUTPUT
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.LayoutInflater.from
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_SEND
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TintContextWrapper
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import base.BaseActivity
import base.R
import base.application.FansChat
import base.data.api.Api
import base.data.api.authentication.LoggedInUserCache
import base.data.api.users.model.FansChatUserDetails
import base.data.cache.Cache.Companion.cache
import base.data.model.NotificationSettings
import base.data.model.User
import base.data.model.feed.FeedItem
import base.data.model.feed.FeedItem.Type.*
import base.data.model.other.Table
import base.databinding.FragmentWebBinding
import base.extension.Flavors
import base.extension.flavor
import base.extension.goBack
import base.extension.showToast
import base.socket.SocketService
import base.ui.FullScreenImageActivity
import base.ui.MainActivity
import base.ui.adapter.other.ChatsSearchAdapter
import base.ui.adapter.other.ConversationAdapter
import base.ui.adapter.other.MessagesAdapter
import base.ui.adapter.other.PublicChatAdapter
import base.ui.base.BaseFragment
import base.ui.base.goBack
import base.ui.fragment.dialog.PinDialog
import base.ui.fragment.other.StatsContainerAdapter
import base.ui.fragment.other.chat.CreateJoinChatContainerAdapter
import base.ui.fragment.other.friends.my.FriendRequestAdapter
import base.ui.fragment.other.friends.my.FriendsAdapter
import base.ui.fragment.other.profile.Language
import base.util.CommonUtils.Companion.selector
import base.util.json.ContextWrapper
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.CallbackManager
import com.google.gson.Gson
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo.single
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import com.miguelbcr.ui.rx_paparazzo2.entities.Response
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yalantis.ucrop.UCrop
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import org.json.JSONObject
import rx_activity_result2.RxActivityResult
import timber.log.Timber
import video.trimmer.TrimmerActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.collections.set
import kotlin.math.abs

const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"
const val AUTO_TRANSLATE = "AUTO_TRANSLATE"

const val AUDIO_RECORD = 234

fun <T> Observable<T>.inBackground(): Observable<T> {
    return subscribeOn(io()).observeOn(mainThread())
}

fun <T> Flowable<T>.inBackground(): Flowable<T> {
    return subscribeOn(io()).observeOn(mainThread())
}

fun <T> Single<T>.inBackground(): Single<T> {
    return subscribeOn(io()).observeOn(mainThread())
}

val FeedItem.referencesAnother
    get() = type == postShare || type == newsShare || type == rumourShare || type == socialShare || type == clubtvShared

/*
@SuppressLint("ValidFragment")
abstract class Fragment(private val layoutId: Int, private val needsLogin: Boolean = false) :
    androidx.fragment.app.Fragment() {

    val disposables by lazy { CompositeDisposable() }

    fun Disposable.autoDispose() {
        disposables.add(this)
    }

    override fun onStart() {
        super.onStart()
        if (needsLogin) askToLogin()

        setupBackButton()
        //        showGlideImages()
    }


    private fun setupBackButton() {
        view?.findViewById<View>(R.id.back)?.onClick { goBack() }
    }

    */
/*
        private fun showGlideImages() {
            when (this) {
                is RestoreFragment//,
    //            is ChatFragment,
                    *//*

    */
/*is AuthDecideFragment *//*
*/
/*
 ->
                background.src = R.drawable.background
//            is PostCreateFragment ->
//                image.src = R.drawable.header
//            is FeedFragment,
            is NewsFragment -> {
                topImage.src = R.drawable.header
            }
        }
    }
*//*


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        when (this) {
            is NewsFragment -> trackNewsSectionOpened()
            is DetailFragment -> trackPostViewed(postId.toString())
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        selectedFile = null
        hideKeyboard()
        super.onDestroyView()
    }
}
*/

val View.ctxCompat: Context
    get() {
        return if (context is ContextWrapper || context is TintContextWrapper) {
            return (context as ContextWrapper).baseContext
        } else context
    }


fun getScale(): Int {
    val widthScreen = Resources.getSystem().displayMetrics.widthPixels
    val display: Double = widthScreen / 550.0
    return (display * 100.0).toInt()

}

abstract class WebFragment(private val urlStringId: Int, private val showPinLayout: Boolean = true) : BaseFragment() {
    val homeUrl by lazy { getString(urlStringId) }
    lateinit var binding: FragmentWebBinding

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, state: Bundle?) {
        binding.toolbarWebViewLayout.linRoot.visibility = if (showPinLayout) VISIBLE else GONE
        binding.webview.webViewClient = WebViewClient()
        if (showPinLayout) {
            binding.webview.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.toolbarWebViewLayout.previous.isEnabled = binding.webview.canGoBack()
                    binding.toolbarWebViewLayout.forward.isEnabled = binding.webview.canGoForward()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }

            binding.toolbarWebViewLayout.previous.onClick {
                if (binding.webview.canGoBack()) binding.webview.goBack()
            }
            binding.toolbarWebViewLayout.forward.onClick {
                if (binding.webview.canGoForward()) binding.webview.goForward()
            }
            binding.toolbarWebViewLayout.home.isEnabled = binding.webview.url == homeUrl
            binding.toolbarWebViewLayout.home.onClick { binding.webview.loadUrl(homeUrl) }
            binding.toolbarWebViewLayout.pinStat.onClick {
                if (loggedInUserCache.getLoginUserToken().isNullOrBlank()) {
                    open(R.id.authorize)
                    return@onClick
                }
                val bitmap = getScreenShot(binding.webview) ?: return@onClick
                val hashmap: HashMap<String, Any> = HashMap()
                hashmap[PinDialog.POST_TYPE] = stats
                hashmap[PinDialog.POST_TITLE] = resources.getString(R.string.a_stats_post)
                hashmap[PinDialog.POST_IMAGE] = bitmap
                open(R.id.pinDialog, PinDialog.NEW_POST to hashmap)
            }
        }
        binding.webview.setInitialScale(getScale())
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.loadUrl(homeUrl)


        when (urlStringId) {
            R.string.url_stats -> {
                Analytics.trackStatsOpened()
            }
            R.string.url_store -> {
                Analytics.trackStoreOpened()
            }
            R.string.youtube_url -> {
                Analytics.trackTvOpened()
            }
            R.string.url_tickets -> {
                Analytics.trackTicketsOpened()
            }
            R.string.url_calendar -> {
                Analytics.trackCalendarsOpened()
            }
        }
    }

    open fun getScreenShot(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.getDrawingCache(false))
        view.isDrawingCacheEnabled = false
        return bitmap
    }
}

fun Activity.open(destination: Int, params: Pair<String, Any>? = null) {
    try {
        val navController = findNavController(this, R.id.fragmentsContainer)
        navController.navigate(destination, params.toBundle())

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun BaseActivity.open(destination: Int, params: HashMap<String, Any?>) {
    try {
        val navController = findNavController(this, R.id.fragmentsContainer)
        navController.navigate(destination, params.toBundle())

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun HashMap<String, Any?>.toBundle(): Bundle = bundleOf(*this.toList().toTypedArray())

fun Fragment.open(destination: Int, params: Pair<String, Any>? = null) {
    act.open(destination, params)
}

fun Fragment.open(destination: Int, params: HashMap<String, Any?>) {
    act.open(destination, params)
}

fun BaseFragment.open(destination: Int, params: Pair<String, Any>? = null) {
    act.open(destination, params)
}

fun BaseFragment.open(destination: Int, params: HashMap<String, Any?>) {
    act.open(destination, params)
}

fun View.open(destination: Int, params: Pair<String, Any>? = null) {
    try {
        val navController = findNavController(this)
        navController.navigate(destination, params.toBundle())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.open(destination: Int, params: HashMap<String, Any?>) {
    try {
        val navController = findNavController(this)
        navController.navigate(destination, params.toBundle())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun Pair<String, Any>?.toBundle(): Bundle? {
    if (this == null) return null
    val bundle = Bundle()

    when (second) {
        is String -> bundle.putString(first, second as String)
        is Serializable -> bundle.putSerializable(first, second as Serializable)
    }
    return bundle
}

fun Activity.goBack() {
    val navController = findNavController(this, R.id.fragmentsContainer)
    navController.popBackStack()
}

fun Fragment.goBack() {
    activity?.goBack()
}

val isLoggedIn: Boolean
    get() {
        val user = cache.daoUsers().getCurrentImmediately()
        return user != null && user.isLoggedIn
    }


val isAnonymous: Boolean
    get() = !isLoggedIn

fun RecyclerView.set(a: RecyclerView.Adapter<*>) {
    adapter = a
}

fun Observable<Response<FragmentActivity?, FileData>>.getFile(): Observable<File> {
    return filter { it.data() != null }.map { it.data().file }
}

class MessageHolder(val view: View) : ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.name)
    val avatar: ImageView = view.findViewById(R.id.avatar)
    val text: TextView = view.findViewById(R.id.text)
    val date: TextView = view.findViewById(R.id.date)
    val image: ImageView = view.findViewById(R.id.image)
    val play: ImageButton = view.findViewById(R.id.play)
    val container: View = view.findViewById(R.id.container)
    val wrapper: View = view.findViewById(R.id.wrapper)
    val progressBar: View = view.findViewById(R.id.progressBar)
}

class ProgressHolder(val view: View) : ViewHolder(view)

class ChatHolder(val view: View) : ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val name: TextView = view.findViewById(R.id.name)
    val container: View = view.findViewById(R.id.container)
    val imageBackground: View = view.findViewById(R.id.imageBackground)
    val notification: View = view.findViewById(R.id.notification)
}

class PublicChatHolder(val view: View) : ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val name: TextView = view.findViewById(R.id.name)
    val container: View = view.findViewById(R.id.container)
    val tvMemberCount: TextView = view.findViewById(R.id.tvMemberCount)
}

class ChatSearchHolder(val view: View) : ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val name: TextView = view.findViewById(R.id.name)
    val tvFrndMemberCount: TextView = view.findViewById(R.id.tvFrndMemberCount)
    val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
    val linDateTime: View = view.findViewById(R.id.linDateTime)
    val linBottom: View = view.findViewById(R.id.linBottom)
    val rvFrndList: RecyclerView = view.findViewById(R.id.rvFrndList)
    val btnJoinChat: View = view.findViewById(R.id.btnJoinChat)
    val linTop: View = view.findViewById(R.id.linTop)
}

class AddChatHolder(val view: View) : ViewHolder(view)

class FriendsHolder(val view: View) : ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.name)
    val avatar: ImageView = view.findViewById(R.id.avatar)
    val container: View = view.findViewById(R.id.container)
    val ivChecked: View = view.findViewById(R.id.ivChecked)
}

class LanguagesHolder(val view: View) : ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.name)
    val image: ImageView = view.findViewById(R.id.image)
}

fun ViewGroup.inflate(layoutId: Int): View {
    return from(context).inflate(layoutId, this, false)
}

fun String.fixTypeNamingInconsistency(): String {
    return replace("official", "newsOfficial").replace("webhose", "newsUnOfficial")
}

var ImageView.src: Any?
    get() {
        return ""
    }
    set(value) {
        show(value)
    }

var ImageView.srcMessage: Any?
    get() {
        return ""
    }
    set(image) {
        Glide.with(context).load(image).centerCrop().error(R.drawable.msg_placeholder) //
            .placeholder(R.drawable.msg_placeholder).into(this)
    }

var ImageView.srcRound: Any?
    get() = ""
    set(value) = showRound(value)

fun ImageView.show(image: Any?) {
    Glide.with(context).load(image).into(this)
}

fun ImageView.showRound(image: Any?) {
    Glide.with(context).load(image).apply(RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.avatar_placeholder).placeholder(R.drawable.avatar_placeholder)).diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontAnimate().into(this)
}

fun RecyclerView.show(adapter: MessagesAdapter) {
    swapAdapter(adapter, false)
}

fun RecyclerView.show(adapter: FriendsAdapter) {
    swapAdapter(adapter, false)
}

fun RecyclerView.show(adapter: FriendRequestAdapter) {
    swapAdapter(adapter, false)
}

fun RecyclerView.show(adapter: ChatsSearchAdapter) {
    swapAdapter(adapter, false)
}

fun RecyclerView.show(adapter: ConversationAdapter) {
    swapAdapter(adapter, false)
}

fun RecyclerView.show(adapter: PublicChatAdapter) {
    swapAdapter(adapter, false)
}

fun Date.toRelative(): String {
    return getRelativeTimeSpanString(time, now.time, MINUTE_IN_MILLIS).toString()
}

fun Date.toChatRelative(): String {
    return if (abs(now.time - time) >= 60 * 1000) {
        getRelativeTimeSpanString(time, now.time, MINUTE_IN_MILLIS).toString()
    } else "Just Now"
}

fun String.findSplitPosition(): Int {
    val urlPosition = indexOf("http") - 1
    val dotPosition = indexOf(".")
    val commaPosition = indexOf(",")
    val questionMarkPosition = indexOf("?")
    val exclamationPointPosition = indexOf("!")

    return listOf(urlPosition, dotPosition, commaPosition, questionMarkPosition, exclamationPointPosition).filter { it > 0 }.minOrNull()
        ?: length - 1
}


fun Context.isAutoTranslateEnabled() = getBoolean(AUTO_TRANSLATE, true)

fun NavController.onNavigate(callback: (NavDestination) -> Unit) {
    addOnDestinationChangedListener { _, destination, _ ->
        callback.invoke(destination)
    }
}

var listOfAnimators = arrayListOf<Pair<Int, ValueAnimator>>()

fun View.collapse() {
    this.post {
        val height: Int = this.height
        if (height == 0) return@post
        val valueAnimator = ObjectAnimator.ofInt(height, 0)
        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            if (value > 0) {
                val layoutParams: ViewGroup.LayoutParams = this.layoutParams
                layoutParams.height = value
                this.layoutParams = layoutParams
            } else {
                listOfAnimators.removeAll { it.second == valueAnimator } //remove current animation only
                this.visibility = GONE
            }
            animation.duration = 200
        }
        valueAnimator.start()
        listOfAnimators.add(Pair(id, valueAnimator))
    }
}

fun View.expand(targetHeight: Int) {
    this.post {
        //clear all previous animations on this view so it can get final visibility
        listOfAnimators.filter { it.first == id }.forEach {
            it.second.cancel()
            listOfAnimators.remove(it)
        }
        val layoutParams: ViewGroup.LayoutParams = this.layoutParams
        layoutParams.height = targetHeight
        this.layoutParams = layoutParams
        this.visibility = VISIBLE
    }
}

fun NavDestination.hasBottomBar(): Boolean {
    return id == R.id.feed || id == R.id.chat || id == R.id.news || id == R.id.rumours || id == R.id.social || id == R.id.tv || id == R.id.stats || id == R.id.store || id == R.id.calendar || id == R.id.tickets
}

fun NavDestination.hasToolBar(): Boolean {
    return id == R.id.feed || id == R.id.chat || id == R.id.news || id == R.id.rumours || id == R.id.social || id == R.id.tv || id == R.id.stats || id == R.id.store || id == R.id.calendar || id == R.id.tickets || id == R.id.detailFragment
}

fun EditText.get(): String {
    return text.trim().toString()
}

val Fragment.args get() = arguments!!

val Fragment.postId get() = args.getString("postId")

val deviceId get() = UUID.randomUUID().toString()

val userId get() = cache.daoUsers().getCurrentImmediately()!!.id

val user get() = cache.daoUsers().getCurrentImmediately() ?: emptyUser

val emptyUser = User()

fun Context.isNotificationOn(eventName: String): Boolean {
    val notificationSettings: NotificationSettings? = Gson().fromJson(getStringFromPref(Constants.SF_NOTIFICATION_SETTINGS, ""), NotificationSettings::class.java)
    //notification is regarding posts? is it muted?
    when {
        notificationSettings == null -> return true
        eventName in wallEventList() && !notificationSettings.wall -> {
            return false
        }
        eventName in newsEventList() && !notificationSettings.news -> {
            return false
        }
        eventName in rumorEventList() && !notificationSettings.rumor -> {
            return false
        }
        eventName in socialEventList() && !notificationSettings.social -> {
            return false
        }
        eventName in videoEventList() && !notificationSettings.video -> {
            return false
        }
        else -> return true
    }
}
/*eventName == Constants.NS_IMS && notificationSettings.ims -> {
    return true
}*/

val BaseFragment.prefs get() = requireContext().prefs
val Fragment.prefs get() = requireContext().prefs

val Context.prefs get() = getDefaultSharedPreferences(this)

fun Context.getStringFromPref(key: String, defValue: String = ""): String {
    return prefs.getString(key, defValue).toString()
}

fun Fragment.getString(key: String, defValue: String = ""): String {
    return prefs.getString(key, defValue).toString()
}

fun Fragment.getInt(key: String): Int {
    return prefs.getInt(key, 0)
}

fun Context.getInt(key: String): Int {
    return prefs.getInt(key, 0)
}

fun Fragment.getBoolean(key: String, defValue: Boolean = false): Boolean {
    return prefs.getBoolean(key, defValue)
}

fun BaseFragment.getBoolean(key: String, defValue: Boolean = false): Boolean {
    return prefs.getBoolean(key, defValue)
}

fun Context.getBoolean(key: String, defValue: Boolean = false): Boolean {
    return prefs.getBoolean(key, defValue)
}

fun Context.saveString(key: String, value: String) {
    prefs.edit().putString(key, value).apply()
}

fun Context.saveInt(key: String, value: Int) {
    prefs.edit().putInt(key, value).apply()
}

fun Fragment.saveBoolean(key: String, value: Boolean) {
    prefs.edit().putBoolean(key, value).apply()
}

fun BaseFragment.saveBoolean(key: String, value: Boolean) {
    prefs.edit().putBoolean(key, value).apply()
}

fun View.whenClicked() {
    onClick { open(id) }
}

fun EditText.onSubmit(callback: (String) -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        return@setOnEditorActionListener if (actionId == IME_ACTION_SEND) {
            callback.invoke(get())
            true
        } else false
    }
}

fun EditText.onTextChange(callback: (CharSequence?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            callback.invoke(p0)
        }

        override fun afterTextChanged(p0: Editable?) {}
    })
}

fun randomId(): String {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 23)
}

fun EditText.clear() {
    text.clear()
}

val now get() = Date()

val callbackManager by lazy { CallbackManager.Factory.create() }

val JSONObject.avatar: String?
    get() {
        return if (has("picture")) {
            getJSONObject("picture").getJSONObject("data").getString("url")
        } else null
    }

fun Fragment.askToLogin() {
    if (isAnonymous) {
        goBack()
        open(R.id.authorize)
    }
}


inline fun <reified T> Fragment.getArgument(): T {
    val className = T::class.java.simpleName.lowercase(Locale.getDefault())
    return arguments!!.getSerializable(className) as T
}

fun CreateJoinChatContainerAdapter.getString(ctx: Context, id: Int): String {
    return ctx.getString(id)
}

fun StatsContainerAdapter.getString(id: Int): String {
    val ctx = fm.fragments.first().requireContext()
    return ctx.getString(id)
}

fun logout() {
    Constants.SHOW_REFRESH_INDICATOR = false
    Constants.IS_FORCE_REFRESH = true
    cache.daoConversationList().deleteAll()
    cache.daoConversation().deleteAll()
    cache.daoTranslation().deleteAll()
    cache.daoUsers().deleteAll()
    cache.daoNotifications().deleteAll()
    //    Api.unRegisterForPushNotifications()
}

var selectedFile: File? = null

fun BaseFragment.takePhotoOrVideo(callback: () -> Unit) {
    val title = getString(R.string.add_attachment)
    selector(title, arrayOf(getString(R.string.take_photo), getString(R.string.record_video))) { _, i ->
        when (i) {
            0 -> takePhotos {
                callback.invoke()
            }
            1 -> recordVideos {
                callback.invoke()
            }
        }
    }
}

fun BaseFragment.selectImageOrVideo(callback: () -> Unit) {
    val title = getString(R.string.add_attachment)
    selector(title, arrayOf(getString(R.string.pick_photo), getString(R.string.pick_video))) { _, i ->
        when (i) {
            0 -> selectImages {
                callback.invoke()
            }
            1 -> selectVideos {
                callback.invoke()
            }
        }
    }
}

fun BaseFragment.takePhotos(callback: () -> Unit) {
    val options = UCrop.Options()
    options.setToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
    options.setActiveWidgetColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
    options.setStatusBarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))

    compositeDisposable.add(single(activity).crop(options).useInternalStorage().usingCamera().getFile().inBackground().subscribe { selectedFile = it; callback.invoke() })
}

fun BaseFragment.selectImages(callback: () -> Unit) {
    val options = UCrop.Options()
    options.setToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
    options.setActiveWidgetColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
    options.setStatusBarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))

    compositeDisposable.add(single(activity).crop(options).useInternalStorage().usingGallery().getFile().inBackground().subscribe { selectedFile = it; callback.invoke() })
}

fun BaseFragment.selectVideos(callback: () -> Unit) {

    //    val intent = Intent(ACTION_PICK, EXTERNAL_CONTENT_URI)
    val intent = Intent()
    intent.type = "video/*"
    intent.action = ACTION_GET_CONTENT

    compositeDisposable.add(RxPermissions(act).request(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE).flatMap { RxActivityResult.on(this).startIntent(intent) }.subscribe({ result ->

            if (result.resultCode() == RESULT_OK) {

                //                val uri = Uri.parse(result.data().toUri(0))
                //                selectedFile = File(getRealPathFromUri(ctx, uri))
                /*selectedFile = File(FileUtils.getPath(requireContext(), result.data()?.data!!))

                callback.invoke()*/

                val file = File(FileUtils.getPath(requireContext(), result.data()?.data!!))
                if (file.exists()) {
                    startTrimActivity(file, callback)
                }
            }
        }, { showToast(it.msg) }))
}

lateinit var audioCallback: () -> Unit
val audioPath = "${Environment.getExternalStorageDirectory()}/RxPaparazzo/audio_" + System.currentTimeMillis() + ".wav"

fun BaseFragment.recordAudio() {

    val color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
    val requestCode = AUDIO_RECORD

    val root = Environment.getExternalStorageDirectory().toString()
    val myDir = File("$root/RxPaparazzo")
    myDir.mkdirs()

    RxPermissions(act).request(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE).subscribe {
        AndroidAudioRecorder.with(act).setFilePath(audioPath).setColor(color).setRequestCode(requestCode).setAutoStart(true).record()
    }
}

fun BaseFragment.recordVideos(callback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= 24) {
        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val intent = Intent(ACTION_VIDEO_CAPTURE)
    val cachePath = File(activity!!.applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "/RxPaparazzo/")

    if (!cachePath.exists()) {
        if (!cachePath.mkdirs()) {
            Timber.d(resources.getString(R.string.failed_to_create_directory))
        }
    }
    val videoPath = File(cachePath, "video_" + System.currentTimeMillis() + ".mp4")
    val videoUri = Uri.fromFile(videoPath)
    if (Build.VERSION.SDK_INT < 29) {
        intent.putExtra(EXTRA_OUTPUT, videoUri)
    }

    compositeDisposable.add(RxPermissions(act).request(READ_EXTERNAL_STORAGE).flatMap { RxActivityResult.on(this).startIntent(intent) }.subscribe({ result ->
            if (result.resultCode() == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= 29 && result.data()?.data != null) {
                    /*val videoFilePath = FileUtils.getPath(requireContext(), result.data()?.data!!)
                    selectedFile = File(videoFilePath)
                    callback.invoke()*/
                    val file = File(FileUtils.getPath(requireContext(), result.data()?.data!!))
                    if (file.exists()) {
                        startTrimActivity(file, callback)
                    }
                } else {
                    /*selectedFile = File(videoUri.path)
                    callback.invoke()*/
                    val file = File(videoUri.path)
                    if (file.exists()) {
                        startTrimActivity(file, callback)
                    }
                }
            }
        }, { showToast(it.msg) }))
}

private fun BaseFragment.startTrimActivity(@NonNull file: File, callback: () -> Unit) {
    val intent = Intent(requireContext(), TrimmerActivity::class.java)
    intent.putExtra(EXTRA_VIDEO_PATH, file.absolutePath)

    compositeDisposable.add(RxPermissions(act).request(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE).flatMap { RxActivityResult.on(this).startIntent(intent) }.subscribe({ result ->

            if (result.resultCode() == RESULT_OK) {
                val file = File(result.data()?.getStringExtra("filePath"))
                if (file.exists()) {
                    selectedFile = file
                    callback.invoke()
                }
            }
        }, { showToast(it.msg) }))
}

fun BaseFragment.upload(file: File?, callback: (String?) -> Unit) {
    if (file != null) {
        /* val dialog = CustomProgressDialog()
         dialog.show(requireContext())*/
        Api.upload(requireContext(), file).subscribe({
            //                    dialog.dialog.dismiss()
            callback.invoke(it); selectedFile = null
        }, {
            //                    dialog.dialog.dismiss()
            callback.invoke(null)
            showToast(getString(R.string.failed_to_upload))
        }).autoDispose()
    } else {
        callback.invoke(null)
    }
}

val String.extension: String
    get() {
        return substring(lastIndexOf(".") + 1, length)
    }

val String.isImage: Boolean
    get() {
        return this == "jpg" || this == "jpeg" || this == "png" || this == "tiff"
    }

val String.isVideo: Boolean
    get() {
        return this == "mp4" || this == "avi" || this == ".webm" || this == ".flv" || this == ".3gp" || this == ".mpeg-4" || this == ".gif"
    }

val String.isAudio: Boolean
    get() {
        return this == "wav" || this == ".mp3" || this == ".ac3" || this == ".aac" || this == ".aax" || this == ".aiff" || this == ".amr" || this == ".flac" || this == ".ogg" || this == ".voc" || this == ".wma" || this == ".m4a" || this == ".caf"
    }

fun Fragment.viewImage(image: String?) {
    if (image == null) return
//    ImageViewer.Builder(context, listOf(image)).show()
    startActivity(Intent(activity, FullScreenImageActivity::class.java).apply {
        putExtra("imageToOpen", image)
    })
}

fun MessageHolder.viewImage(image: String?) {
    if (image == null) return
//    ImageViewer.Builder(itemView.context, listOf(image)).show()
    view.context.startActivity(Intent(view.context, FullScreenImageActivity::class.java).apply {
        putExtra("imageToOpen", image)
    })
}

fun Context.viewVideo(video: String?) {
    try {
        val intent = Intent(ACTION_VIEW)
        intent.setDataAndType(Uri.parse(video), "video/mp4")
        startActivity(intent)
    } catch (e: Exception) {
        showToast(getString(R.string.cant_play_video))
    }
}

fun Fragment.viewAudio(video: String?) {
    AudioPlayer(requireActivity(), requireActivity().isFinishing, childFragmentManager, video).playAudio()
}
/*
fun DetailFragment.populateSharedPostBar(item: FeedItem) {
    val author = item.author.fillDataIfOfficialAccount()

    sharedPostBar.isVisible = true
    sharedMessage.setText(item.sharedMessage.ifBlank { "" })
    sharedMessageAvatar.srcRound = author.avatar
    sharedMessageAvatar.visibility = GONE

    sharedMessageMoreButton.isVisible = author == user
    sharedMessageMoreButton.onClick {
        val actions = listOf(getString(R.string.edit), getString(R.string.delete))
        selector(items = actions, onClick = { _, i ->
            when (i) {
                0 -> editSharedPost(item)
                //1 -> /*deletePost(item) { goBack() }*/
            }
        })
    }
}
*/
/*fun DetailFragment.editSharedPost(item: FeedItem) {

    sharedMessage.isEnabled = true
    sharedMessage.selectAll()
    sharedMessage.requestFocus()

    //    showKeyboard()

    sharedMessage.onSubmit {
        item.sharedMessage = it
        //        update(item) {
        //            sharedMessage.isEnabled = false
        //        }
    }
}*/

fun Context.forceSelectedLanguage(): Context {
    return ContextWrapper.wrap(this, Locale(language))
}

fun AppCompatActivity.restartApp() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK)
    finish()
    startActivity(intent)
}

val Throwable.msg: String
    get() {
        var result = localizedMessage
        if (result.contains("unrecognized", true)) {
            result = "Invalid email or password, please try again"
        }
        if (result.contains("timeout", true)) {
            result = "Failed to execute request (timeout), please check your network connection"
        }
        if (result.contains("taken", true)) {
            result = "This email is already registered, please log in instead"
        }
        if (result.contains("unrecognised", true)) {
            result = "Invalid email or password, please try again"
        }
        return result
    }

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(window!!.decorView.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(act.window!!.decorView.windowToken, 0)
}

fun hideKeyboards(con: View) {
    val imm = con.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(con.windowToken, 0)
}

fun showKeyboards(act: FragmentActivity) {
    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
}

val FeedItem.shareType: FeedItem.Type
    get() {
        return when (type) {
            newsOfficial -> newsShare
            newsUnOfficial -> rumourShare
            social -> socialShare
            clubtv -> clubtvShared
            else -> postShare
        }
    }

val officialIds = listOf("", "5901c9af83a527355ee069e0")

class Ignorable

val Fragment.act get() = activity as BaseActivity

fun MutableList<User>.toggle(user: User) {
    if (contains(user)) remove(user) else add(user)
}

fun MutableList<FansChatUserDetails>.toggle(user: FansChatUserDetails) {
    if (contains(user)) remove(user) else add(user)
}


val selectedFriends = ArrayList<User>()
val selectedFriend = ArrayList<FansChatUserDetails>()

fun User.fillDataIfOfficialAccount(): User {
    if (id in officialIds) {

        when (flavor) {
            Flavors.Sokkaa -> {
                name = "Sokkaa"
                avatar = "https://i.ibb.co/wQq1Sc6/sokkaa-avatar.png"
            }
            Flavors.Sporting -> {
                name = "SCP Sporting"
                avatar = "https://image.ibb.co/c2Qxqe/logo_toolbar.png"
            }
            Flavors.Bal -> {
                name = "Basketball Africa League"
                avatar = "https://airmap.s3.us-east-2.amazonaws.com/BAL-avatar.png"
            }
            Flavors.MTN -> {
                // TODO need to update it
                name = "Basketball Africa League"
                avatar = "https://airmap.s3.us-east-2.amazonaws.com/BAL-avatar.png"
            }
        }
    }
    avatar = avatar?.replace("https://s3-eu-west-1.amazonaws.com/sskirbucket/55895716719781803LY1W6K72QzuhBP45HLH.png", "https://i.ibb.co/wQq1Sc6/sokkaa-avatar.png")
    return this
}

fun FansChatUserDetails.fillDataIfOfficialAccounts(): FansChatUserDetails {
    if (id in officialIds) {

        when (flavor) {
            Flavors.Sokkaa -> {
                displayName = "Sokkaa"
                avatarUrl = "https://i.ibb.co/wQq1Sc6/sokkaa-avatar.png"
            }
            Flavors.Sporting -> {
                displayName = "SCP Sporting"
                avatarUrl = "https://image.ibb.co/c2Qxqe/logo_toolbar.png"
            }
            Flavors.Bal -> {
                displayName = "Basketball Africa League"
                avatarUrl = "https://airmap.s3.us-east-2.amazonaws.com/BAL-avatar.png"
            }
            Flavors.MTN -> {
                // TODO need to update it
                displayName = "Basketball Africa League"
                avatarUrl = "https://airmap.s3.us-east-2.amazonaws.com/BAL-avatar.png"
            }
        }
    }
    avatarUrl = avatarUrl?.replace("https://s3-eu-west-1.amazonaws.com/sskirbucket/55895716719781803LY1W6K72QzuhBP45HLH.png", "https://i.ibb.co/wQq1Sc6/sokkaa-avatar.png")
    return this
}

val languageList by lazy {
    listOf(Language(1, "bn", R.drawable.bengali), Language(2, "zh", R.drawable.chinese), Language(3, "en", R.drawable.english), Language(4, "fr", R.drawable.french), Language(5, "de", R.drawable.german), Language(6, "in", R.drawable.indonesian), Language(7, "it", R.drawable.italian), Language(8, "pl", R.drawable.polish), Language(9, "pt", R.drawable.portuguese), Language(10, "ru", R.drawable.russian), Language(11, "es", R.drawable.spanish), Language(12, "ar", R.drawable.arabic))
}

val languageNames = listOf("Afrikaans", "Albanian", "Amharic", "Arabic", "Armenian", "Azerbaijani", "Basque", "Belarusian", "Bengali", "Bosnian", "Bulgarian", "Catalan", "Cebuano", "Chichewa", "Chinese (Simplified)", "Chinese (Traditional)", "Corsican", "Croatian", "Czech", "Danish", "Dutch", "English", "Esperanto", "Estonian", "Filipino", "Finnish", "French", "Frisian", "Galician", "Georgian", "German", "Greek", "Gujarati", "Haitian Creole", "Hausa", "Hawaiian", "Hebrew", "Hindi", "Hmong", "Hungarian", "Icelandic", "Igbo", "Indonesian", "Irish", "Italian", "Japanese", "Javanese", "Kannada", "Kazakh", "Khmer", "Korean", "Kurdish (Kurmanji)", "Kyrgyz", "Lao", "Latin", "Latvian", "Lithuanian", "Luxembourgish", "Macedonian", "Malagasy", "Malay", "Malayalam", "Maltese", "Maori", "Marathi", "Mongolian", "Myanmar (Burmese)", "Nepali", "Norwegian", "Pashto", "Persian", "Polish", "Portuguese", "Punjabi", "Romanian", "Russian", "Samoan", "Scots Gaelic", "Serbian", "Sesotho", "Shona", "Sindhi", "Sinhala", "Slovak", "Slovenian", "Somali", "Spanish", "Sundanese", "Swahili", "Swedish", "Tajik", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Uzbek", "Vietnamese", "Welsh", "Xhosa", "Yiddish", "Yoruba", "Zulu")
val languageCodes = listOf("af", "sq", "am", "ar", "hy", "az", "eu", "be", "bn", "bs", "bg", "ca", "ceb", "ny", "zh", "zh-TW", "co", "hr", "cs", "da", "nl", "en", "eo", "et", "tl", "fi", "fr", "fy", "gl", "ka", "de", "el", "gu", "ht", "ha", "haw", "iw", "hi", "hmn", "hu", "is", "ig", "in", "ga", "it", "ja", "jw", "kn", "kk", "km", "ko", "ku", "ky", "lo", "la", "lv", "lt", "lb", "mk", "mg", "ms", "ml", "mt", "mi", "mr", "mn", "my", "ne", "no", "ps", "fa", "pl", "pt", "pa", "ro", "ru", "sm", "gd", "sr", "st", "sn", "sd", "si", "sk", "sl", "so", "es", "su", "sw", "sv", "tg", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "cy", "xh", "yi", "yo", "zu")

val Context.languageForTranslation
    get() = if (isAutoTranslateEnabled()) getStringFromPref("language", "en")
    else "en"

val Context.language get() = getStringFromPref("language", "en")
val Fragment.language get() = getString("language", "en")

val Fragment.languageIcon get() = languageList.find { it.shortCode == language }!!.image
val BaseFragment.languageIcon get() = languageList.find { it.shortCode == "en" }!!.image

fun View.onClick(callback: () -> Unit) {
    setOnClickListener { callback.invoke() }
}

fun View.onLongClick(callback: () -> Unit) {
    setOnLongClickListener { callback.invoke(); true }
}

// Goal difference = goals for - goals against
val Table.GD: String
    get() {
        val value = GF - GA
        return if (value > 0) {
            "+$value"
        } else {
            value.toString()
        }
    }

fun Fragment.email(receiver: String, body: String) {
    val emailIntent = Intent(ACTION_SENDTO, Uri.fromParts("mailto", receiver, null))
    emailIntent.putExtra(Intent.EXTRA_TEXT, body)
    startActivity(createChooser(emailIntent, "Report via email..."))
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun BaseFragment.openUrl(url: String) {
    try {
        val intent = Intent(ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.dp2px(dipValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics).toInt()

fun File.createVideoThumb(context: Context): File {
    val out: FileOutputStream
    val cachePath = File(context.cacheDir, "/VideoThumb/")
    // Create the storage directory if it does not exist
    if (!cachePath.exists()) {
        if (!cachePath.mkdirs()) {
            Timber.d("Failed to create directory")
        }
    }
    val imagePathFile = File(cachePath, "fanschat_${System.currentTimeMillis()}.jpeg")
    val bitmap = ThumbnailUtils.createVideoThumbnail(this.absolutePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND) //filePath is your video file path.
    val stream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray: ByteArray = stream.toByteArray()
    out = FileOutputStream(imagePathFile)
    out.write(byteArray)
    out.close()
    return imagePathFile
}

fun File?.calculateAspectRatio(): Double {
    this?.let {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(absolutePath, options)
        val angle = ExifInterface(absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return if (angle == ExifInterface.ORIENTATION_ROTATE_90 || angle == ExifInterface.ORIENTATION_ROTATE_270) (imageHeight / imageWidth.toDouble()) else (imageWidth / imageHeight.toDouble())
    } ?: return 0.0
}

fun File?.calculateAspectRatioForVideo(): Double {
    this?.let {
        val bitmap = ThumbnailUtils.createVideoThumbnail(absolutePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND) //filePath is your video file path.
        return bitmap?.let { bitmap.width / bitmap.height.toDouble() } ?: 0.0
    } ?: return 0.0
}

fun wallEventList(): List<String> =
    listOf(SocketService.EVENT_NEW_WALL_POST, SocketService.EVENT_UPDATE_WALL_POST, SocketService.EVENT_NEW_POST_COMMENT, SocketService.EVENT_UPDATE_POST_COMMENT, SocketService.EVENT_UPDATE_POST_LIKE, SocketService.EVENT_DELETE_WALL_POST, SocketService.EVENT_DELETE_POST_COMMENT)

fun newsEventList(): List<String> =
    listOf(SocketService.EVENT_UPDATE_NEWS_LIKE, SocketService.EVENT_NEW_NEWS, SocketService.EVENT_UPDATE_NEWS, SocketService.EVENT_NEW_NEWS_COMMENT, SocketService.EVENT_UPDATE_NEWS_COMMENT, SocketService.EVENT_DELETE_NEWS, SocketService.EVENT_DELETE_NEWS_COMMENT)

fun socialEventList(): List<String> =
    listOf(SocketService.EVENT_UPDATE_SOCIAL_LIKE, SocketService.EVENT_NEW_SOCIAL, SocketService.EVENT_UPDATE_SOCIAL, SocketService.EVENT_NEW_SOCIAL_COMMENT, SocketService.EVENT_UPDATE_SOCIAL_COMMENT, SocketService.EVENT_DELETE_SOCIAL, SocketService.EVENT_DELETE_SOCIAL_COMMENT)

fun videoEventList(): List<String> =
    listOf(SocketService.EVENT_UPDATE_VIDEO_LIKE, SocketService.EVENT_NEW_VIDEO, SocketService.EVENT_UPDATE_VIDEO, SocketService.EVENT_NEW_VIDEO_COMMENT, SocketService.EVENT_UPDATE_VIDEO_COMMENT, SocketService.EVENT_DELETE_VIDEO, SocketService.EVENT_DELETE_VIDEO_COMMENT)

fun rumorEventList(): List<String> =
    listOf(SocketService.EVENT_UPDATE_RUMOR_LIKE, SocketService.EVENT_NEW_RUMOR, SocketService.EVENT_UPDATE_RUMOR, SocketService.EVENT_NEW_RUMOR_COMMENT, SocketService.EVENT_UPDATE_RUMOR_COMMENT, SocketService.EVENT_DELETE_RUMOR, SocketService.EVENT_DELETE_RUMOR_COMMENT)

fun String.getPostTypeByEvent(): String {
    return when (this) {
        in wallEventList() -> Constants.POST_TYPE_WALL
        in newsEventList() -> Constants.POST_TYPE_NEWS
        in socialEventList() -> Constants.POST_TYPE_SOCIAL
        in rumorEventList() -> Constants.POST_TYPE_RUMOURS
        in videoEventList() -> Constants.POST_TYPE_CLUB_TV
        else -> Constants.POST_TYPE_WALL
    }
}

fun Context.getSectionType(eventName: String): String {
    return when (eventName) {
        in newsEventList() -> getString(R.string.news)
        in socialEventList() -> getString(R.string.social)
        in rumorEventList() -> getString(R.string.rumours)
        in videoEventList() -> getString(R.string.club_tv)
        else -> getString(R.string.news)
    }
}