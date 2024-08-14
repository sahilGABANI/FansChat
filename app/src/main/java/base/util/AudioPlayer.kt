package base.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import base.ui.AudioPlayerDialog


class AudioPlayer(
    private val activity: Activity,
    private val isFinishing: Boolean,
    private val fragmentManager: FragmentManager,
    private val path: String?,
) {

    fun playAudio() {
        //Permission to read and write file on to storage
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1208
            )
        } else {
            if (!isFinishing && fragmentManager.findFragmentByTag("play_pause_dialog") == null) {
                if (path != "" && path!!.isNotEmpty()) {
                    AudioPlayerDialog.newInstance(path).show(fragmentManager, "play_pause_dialog")
                } else {
                    Toast.makeText(
                        activity,
                        "Empty file path",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}