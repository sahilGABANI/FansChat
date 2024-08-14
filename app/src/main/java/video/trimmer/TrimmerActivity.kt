package video.trimmer

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import base.BuildConfig
import base.R
import base.util.EXTRA_VIDEO_PATH
import timber.log.Timber
import video.compressor.CompressionListener
import video.compressor.VideoCompressor.start
import video.compressor.VideoQuality
import video.compressor.config.Configuration
import video.trimmer.interfaces.OnK4LVideoListener
import video.trimmer.interfaces.OnTrimVideoListener
import java.io.File

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener, OnK4LVideoListener {
    private var mVideoTrimmer: K4LVideoTrimmer? = null
    private var mProgressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)
        val extraIntent = intent
        var path: String? = ""
        if (extraIntent != null) {
            path = extraIntent.getStringExtra(EXTRA_VIDEO_PATH)
        }

        //setting progressbar
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setMessage(getString(R.string.trimming_progress))
        mVideoTrimmer = findViewById(R.id.timeLine)
        if (mVideoTrimmer != null) {
//            mVideoTrimmer.setMaxDuration(60 * 1000);
            mVideoTrimmer!!.setOnTrimVideoListener(this)
            mVideoTrimmer!!.setOnK4LVideoListener(this)
            //mVideoTrimmer.setDestinationPath("/storage/emulated/0/DCIM/CameraCustom/");
            mVideoTrimmer!!.setVideoURI(Uri.parse(path))
            mVideoTrimmer!!.setVideoInformationVisibility(false)
        }
    }

    override fun onTrimStarted() {
        mProgressDialog!!.show()
    }

    override fun getResult(filePath: String?) {
        /*      mProgressDialog.dismiss();
        Intent intent = new Intent();
        intent.putExtra("filePath", filePath);
        setResult(RESULT_OK, intent);
        finish();*/
        if (!mProgressDialog!!.isShowing) mProgressDialog!!.show()
        compressVideo(File(filePath))
    }

    private fun compressVideo(selectedFile: File) {
        val uriList: MutableList<Uri> = ArrayList()
        uriList.add(Uri.fromFile(selectedFile))
        start(this, uriList, true, Environment.DIRECTORY_MOVIES, object : CompressionListener {
            override fun onCancelled(index: Int) {
                mProgressDialog!!.dismiss()
            }

            override fun onProgress(index: Int, percent: Float) {
                if (BuildConfig.DEBUG) Timber.tag("Compression progress: ").e("%s", percent)
            }

            override fun onFailure(index: Int, failureMessage: String) {
                mProgressDialog!!.dismiss()
                val intent = Intent()
                intent.putExtra("filePath", selectedFile.path)
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun onSuccess(index: Int, size: Long, path: String?) {
                mProgressDialog!!.dismiss()
                val intent = Intent()
                intent.putExtra("filePath", path)
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun onStart(index: Int) {
                runOnUiThread { mProgressDialog!!.setMessage(getString(R.string.compressing_video)) }
            }
        }, Configuration(VideoQuality.MEDIUM, null, false, null, false, false, null, null))
    }

    override fun cancelAction() {
        mProgressDialog!!.cancel()
        mVideoTrimmer!!.destroy()
        finish()
    }

    override fun onError(message: String?) {
        mProgressDialog!!.cancel()
        runOnUiThread { Toast.makeText(this@TrimmerActivity, message, Toast.LENGTH_SHORT).show() }
    }

    override fun onVideoPrepared() {
        Timber.tag("TrimmerActivity").e("onVideoPrepared")
    }
}