package base

import android.app.Activity
import android.app.Application
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import base.application.FansChat
import base.data.cache.Cache.Companion.initCache
import base.di.DaggerFansChatAppComponent
import base.di.FansChatAppComponent
import base.di.FansChatAppModule
import base.extension.subscribeOnIoAndObserveOnMainThread
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.rx.RxAmplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.analytics.FirebaseAnalytics
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import rx_activity_result2.RxActivityResult
import timber.log.Timber

class FansChatApplication : FansChat() {
    companion object {
        lateinit var firebaseAnalytics: FirebaseAnalytics

        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: StandaloneDatabaseProvider

        operator fun get(app: Application): FansChat {
            return app as FansChat
        }

        operator fun get(activity: Activity): FansChat {
            return activity.application as FansChat
        }

        lateinit var component: FansChatAppComponent
            private set
    }

    private var lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                isAppForeGrounded = false
                Timber.tag("AppLifecycleListener").e("App moved to background")
                socketDataManager.disconnect()
            }
            Lifecycle.Event.ON_START -> {
                isAppForeGrounded = true
                Timber.tag("AppLifecycleListener").e("App moved to foreground")
                socketDataManager.connect()
            }
            Lifecycle.Event.ON_DESTROY -> {
                isAppForeGrounded = false
                Timber.tag("AppLifecycleListener").e("App killed")
                socketDataManager.disconnect()
            }
            else -> {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            component = DaggerFansChatAppComponent.builder()
                .fansChatAppModule(FansChatAppModule(this))
                .build()
            component.inject(this)
            super.setAppComponent(component)
        } catch (e: Exception) {
            Timber.e(e)
        }
        observeSocket()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
        initCache(this)
        val scheduler = AndroidSchedulers.from(Looper.getMainLooper(), true)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler }
        RxPaparazzo.register(this)
        RxActivityResult.register(this)
        initLog()
        initFirebaseAnalytics()
        initAws()
        initExoPlayerCache()
    }

    private fun initExoPlayerCache() {
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)
    }

    private fun initAws() {
        try {
            RxAmplify.addPlugin(AWSCognitoAuthPlugin())
            RxAmplify.addPlugin(AWSS3StoragePlugin())
            RxAmplify.configure(applicationContext)
        } catch (error: AmplifyException) {
            Timber.e(error)
        }
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun observeSocket() {
        socketDataManager.connectionEmitter().subscribeOnIoAndObserveOnMainThread({

        }, {
            Timber.e(it)
        })
        socketDataManager.connectionError().subscribeOnIoAndObserveOnMainThread({

        }, {
            Timber.e(it)
        })
        socketDataManager.disconnectEmitter().subscribeOnIoAndObserveOnMainThread({

        }, {
            Timber.e(it)
        })
    }
}