package base.application

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDex
import base.di.BaseAppComponent
import base.di.BaseUiApp
import base.socket.SocketDataManager
import javax.inject.Inject

open class FansChat : BaseUiApp() {
    @Inject
    lateinit var socketDataManager: SocketDataManager
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun getAppComponent(): BaseAppComponent {
        return component
    }

    override fun setAppComponent(baseAppComponent: BaseAppComponent) {
        component = baseAppComponent
    }

    companion object {
        lateinit var component: BaseAppComponent
        var isAppForeGrounded: Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}