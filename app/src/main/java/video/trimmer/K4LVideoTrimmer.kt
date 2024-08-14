/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package video.trimmer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.format.Formatter
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import base.R
import timber.log.Timber
import video.trimmer.interfaces.OnK4LVideoListener
import video.trimmer.interfaces.OnProgressVideoListener
import video.trimmer.interfaces.OnRangeSeekBarListener
import video.trimmer.interfaces.OnTrimVideoListener
import video.trimmer.utils.BackgroundExecutor
import video.trimmer.utils.TrimVideoUtils
import video.trimmer.utils.UiThreadExecutor
import video.trimmer.view.ProgressBarView
import video.trimmer.view.RangeSeekBarView
import video.trimmer.view.Thumb
import video.trimmer.view.TimeLineView
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class K4LVideoTrimmer @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var mHolderTopView: SeekBar? = null
    private var mRangeSeekBarView: RangeSeekBarView? = null
    private var mLinearVideo: RelativeLayout? = null
    private var mTimeInfoContainer: View? = null
    private var mVideoView: VideoView? = null
    private var mPlayView: ImageView? = null
    private var mTextSize: TextView? = null
    private var mTextTimeFrame: TextView? = null
    private var mTextTime: TextView? = null
    private var mTimeLineView: TimeLineView? = null
    private var mVideoProgressIndicator: ProgressBarView? = null
    private var mSrc: Uri? = null
    private var mFinalPath: String? = null
    private var mMaxDuration = 0
    private lateinit var mListeners: ArrayList<OnProgressVideoListener>
    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnK4LVideoListener: OnK4LVideoListener? = null
    private var mDuration = 0
    private var mTimeVideo = 0
    private var mStartPosition = 0
    private var mEndPosition = 0
    private var mOriginSizeFile: Long = 0
    private var mResetSeekBar = true
    private val mMessageHandler = MessageHandler(this)
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_time_line, this, true)
        mHolderTopView = findViewById(R.id.handlerTop)
        mVideoProgressIndicator = findViewById(R.id.timeVideoView)
        mRangeSeekBarView = findViewById(R.id.timeLineBar)
        mLinearVideo = findViewById(R.id.layout_surface_view)
        mVideoView = findViewById(R.id.video_loader)
        mPlayView = findViewById(R.id.icon_video_play)
        mTimeInfoContainer = findViewById(R.id.timeText)
        mTextSize = findViewById(R.id.textSize)
        mTextTimeFrame = findViewById(R.id.textTimeSelection)
        mTextTime = findViewById(R.id.textTime)
        mTimeLineView = findViewById(R.id.timeLineView)
        setUpListeners()
        setUpMargins()
    }

    private fun setUpListeners() {
        mListeners = arrayListOf()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Int, max: Int, scale: Float) {
                updateVideoProgress(time)
            }
        })
        mListeners.add(mVideoProgressIndicator as OnProgressVideoListener)
        findViewById<View>(R.id.btCancel).setOnClickListener { view: View? -> onCancelClicked() }
        findViewById<View>(R.id.btSave).setOnClickListener { view: View? -> onSaveClicked() }
        val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onClickVideoPlayPause()
                return true
            }
        })
        mVideoView!!.setOnErrorListener { mediaPlayer: MediaPlayer?, what: Int, extra: Int ->
            if (mOnTrimVideoListener != null) mOnTrimVideoListener!!.onError("Something went wrong reason : $what")
            false
        }
        mVideoView!!.setOnTouchListener { v: View?, event: MotionEvent? ->
            gestureDetector.onTouchEvent(event)
            true
        }
        mRangeSeekBarView!!.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
                onStopSeekThumbs()
            }
        })
        mRangeSeekBarView!!.addOnRangeSeekBarListener(mVideoProgressIndicator!!)
        mHolderTopView!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })
        mVideoView!!.setOnPreparedListener { mp: MediaPlayer -> onVideoPrepared(mp) }
        mVideoView!!.setOnCompletionListener { mp: MediaPlayer? -> onVideoCompleted() }
    }

    private fun setUpMargins() {
        val marge = mRangeSeekBarView!!.thumbs!![0].widthBitmap
        val widthSeek = mHolderTopView!!.thumb.minimumWidth / 2
        var lp = mHolderTopView!!.layoutParams as RelativeLayout.LayoutParams
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0)
        mHolderTopView!!.layoutParams = lp
        lp = mTimeLineView!!.layoutParams as RelativeLayout.LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        mTimeLineView!!.layoutParams = lp
        lp = mVideoProgressIndicator!!.layoutParams as RelativeLayout.LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        mVideoProgressIndicator!!.layoutParams = lp
    }

    private fun onSaveClicked() {
        if (mStartPosition <= 0 && mEndPosition >= mDuration) {
            if (mOnTrimVideoListener != null) mOnTrimVideoListener!!.getResult(File(mSrc.toString()).absolutePath)
        } else {
            mPlayView!!.visibility = VISIBLE
            mVideoView!!.pause()
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, mSrc)
            val METADATA_KEY_DURATION = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            val file = File(mSrc!!.path.toString())
            if (mTimeVideo < MIN_TIME_FRAME) {
                if (METADATA_KEY_DURATION - mEndPosition > MIN_TIME_FRAME - mTimeVideo) {
                    mEndPosition += MIN_TIME_FRAME - mTimeVideo
                } else if (mStartPosition > MIN_TIME_FRAME - mTimeVideo) {
                    mStartPosition -= MIN_TIME_FRAME - mTimeVideo
                }
            }

            //notify that video trimming started
            if (mOnTrimVideoListener != null) mOnTrimVideoListener!!.onTrimStarted()
            BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
                override fun execute() {
                    try {
                        TrimVideoUtils.startTrim(file, destinationPath!!, mStartPosition.toLong(), mEndPosition.toLong(), mOnTrimVideoListener!!)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(Thread.currentThread(), e)
                    }
                }
            })
        }
    }

    private fun onClickVideoPlayPause() {
        if (mVideoView!!.isPlaying) {
            mPlayView!!.visibility = VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mVideoView!!.pause()
        } else {
            mPlayView!!.visibility = GONE
            if (mResetSeekBar) {
                mResetSeekBar = false
                mVideoView!!.seekTo(mStartPosition)
            }
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mVideoView!!.start()
        }
    }

    private fun onCancelClicked() {
        mVideoView!!.stopPlayback()
        if (mOnTrimVideoListener != null) {
            mOnTrimVideoListener!!.cancelAction()
        }
    }

    /**
     * Sets the path where the trimmed video will be saved
     * Ex: /storage/emulated/0/MyAppFolder/
     *
     * @param finalPath the full path
     */
    private var destinationPath: String?
        get() {
            if (mFinalPath == null) {
                val folder = Environment.getExternalStorageDirectory()
                mFinalPath = folder.path + File.separator
                Timber.tag(TAG).d("Using default path %s", mFinalPath)
            }
            return mFinalPath
        }
        set(finalPath) {
            mFinalPath = finalPath
            Timber.tag(TAG).d("Setting custom path %s", mFinalPath)
        }

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        var duration = (mDuration * progress / 1000L).toInt()
        if (fromUser) {
            if (duration < mStartPosition) {
                setProgressBarPosition(mStartPosition)
                duration = mStartPosition
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition)
                duration = mEndPosition
            }
            setTimeVideo(duration)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView!!.pause()
        mPlayView!!.visibility = VISIBLE
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView!!.pause()
        mPlayView!!.visibility = VISIBLE
        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        mVideoView!!.seekTo(duration)
        setTimeVideo(duration)
        notifyProgressUpdate(false)
    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        // Adjust the size of the video
        // so it fits on the screen
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = mLinearVideo!!.width
        val screenHeight = mLinearVideo!!.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = mVideoView!!.layoutParams
        if (videoProportion > screenProportion) {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            lp.width = (videoProportion * screenHeight.toFloat()).toInt()
            lp.height = screenHeight
        }
        mVideoView!!.layoutParams = lp
        mPlayView!!.visibility = VISIBLE
        mDuration = mVideoView!!.duration
        setSeekBarPosition()
        setTimeFrames()
        setTimeVideo(0)
        if (mOnK4LVideoListener != null) {
            mOnK4LVideoListener!!.onVideoPrepared()
        }
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = mDuration / 2 - mMaxDuration / 2
            mEndPosition = (mDuration / 2.0 + mMaxDuration / 2.0).toInt()
            mRangeSeekBarView!!.setThumbValue(0, (mStartPosition * 100 / mDuration).toFloat())
            mRangeSeekBarView!!.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
        }
        setProgressBarPosition(mStartPosition)
        mVideoView!!.seekTo(mStartPosition)
        mTimeVideo = mDuration
        mRangeSeekBarView!!.initMaxWidth()
    }

    private fun setTimeFrames() {
        val seconds = context.getString(R.string.short_seconds)
        mTextTimeFrame!!.text = String.format("%s %s - %s %s", TrimVideoUtils.stringForTime(mStartPosition), seconds, TrimVideoUtils.stringForTime(mEndPosition), seconds)
    }

    private fun setTimeVideo(position: Int) {
        val seconds = context.getString(R.string.short_seconds)
        mTextTime!!.text = String.format("%s %s", TrimVideoUtils.stringForTime(position), seconds)
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L).toInt()
                mVideoView!!.seekTo(mStartPosition)
            }
            Thumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L).toInt()
            }
        }
        setProgressBarPosition(mStartPosition)
        setTimeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mVideoView!!.pause()
        mPlayView!!.visibility = VISIBLE
    }

    private fun onVideoCompleted() {
        mVideoView!!.seekTo(mStartPosition)
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0) return
        val position = mVideoView!!.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position, mDuration, (position * 100 / mDuration).toFloat())
            }
        } else {
            mListeners[1].updateProgress(position, mDuration, (position * 100 / mDuration).toFloat())
        }
    }

    private fun updateVideoProgress(time: Int) {
        if (mVideoView == null) {
            return
        }
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mVideoView!!.pause()
            mPlayView!!.visibility = VISIBLE
            mResetSeekBar = true
            return
        }
        if (mHolderTopView != null) {
            // use long to avoid overflow
            setProgressBarPosition(time)
        }
        setTimeVideo(time)
    }

    private fun setProgressBarPosition(position: Int) {
        if (mDuration > 0) {
            val pos = 1000L * position / mDuration
            mHolderTopView!!.progress = pos.toInt()
        }
    }

    /**
     * Set video information visibility.
     * For now this is for debugging
     *
     * @param visible whether or not the videoInformation will be visible
     */
    fun setVideoInformationVisibility(visible: Boolean) {
        mTimeInfoContainer!!.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * Listener for events such as trimming operation success and cancel
     *
     * @param onTrimVideoListener interface for events
     */
    fun setOnTrimVideoListener(onTrimVideoListener: OnTrimVideoListener?) {
        mOnTrimVideoListener = onTrimVideoListener
    }

    /**
     * Listener for some [VideoView] events
     *
     * @param onK4LVideoListener interface for events
     */
    fun setOnK4LVideoListener(onK4LVideoListener: OnK4LVideoListener?) {
        mOnK4LVideoListener = onK4LVideoListener
    }

    /**
     * Cancel all current operations
     */
    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    /**
     * Set the maximum duration of the trimmed video.
     * The video.trimmer interface wont allow the user to set duration longer than maxDuration
     *
     * @param maxDuration the maximum duration of the trimmed video in seconds
     */
    fun setMaxDuration(maxDuration: Int) {
        mMaxDuration = maxDuration /* * 1000*/
    }

    /**
     * Sets the uri of the video to be video.trimmer
     *
     * @param videoURI Uri of the video
     */
    fun setVideoURI(videoURI: Uri?) {
        mSrc = videoURI
        if (mOriginSizeFile == 0L) {
            val file = File(mSrc!!.path.toString())
            mOriginSizeFile = file.length()
            val fileSizeInKB = mOriginSizeFile / 1024
            mTextSize!!.text = Formatter.formatShortFileSize(context, mOriginSizeFile)
        }
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
        setMaxDuration(if (duration > 0) duration.toInt() else 60 * 1000)
        mVideoView!!.setVideoURI(mSrc)
        mVideoView!!.requestFocus()
        mTimeLineView!!.setVideo(mSrc!!)
    }

    private class MessageHandler(view: K4LVideoTrimmer) : Handler() {
        private val mView: WeakReference<K4LVideoTrimmer>
        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view?.mVideoView == null) {
                return
            }
            view.notifyProgressUpdate(true)
            if (view.mVideoView!!.isPlaying) {
                sendEmptyMessageDelayed(0, 10)
            }
        }

        init {
            mView = WeakReference(view)
        }
    }

    companion object {
        private val TAG = K4LVideoTrimmer::class.java.simpleName
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
    }

    init {
        init(context)
    }
}