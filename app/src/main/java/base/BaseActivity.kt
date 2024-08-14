package base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import base.application.FansChat.Companion.isAppForeGrounded
import base.util.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.File

abstract class BaseActivity : AppCompatActivity() {

    val disposables = CompositeDisposable()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.forceSelectedLanguage())
    }

    override fun onDestroy() {
        disposables.clear()
        isAppForeGrounded = false
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == AUDIO_RECORD) {
            selectedFile = File(audioPath)
            audioCallback.invoke()
        }
    }

    fun Disposable.autoDispose() {
        disposables.add(this)
    }

    override fun onStart() {
        super.onStart()
        isAppForeGrounded = true
    }

    override fun onStop() {
        super.onStop()
        isAppForeGrounded = false
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
}
