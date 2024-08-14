package base.ui

import android.app.Dialog
import android.content.DialogInterface
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import base.R
import base.util.onClick
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import timber.log.Timber


class AudioPlayerDialog : DialogFragment() {

    private val run = Runnable { seekUpdation() }
    private var seekHandler: Handler = Handler()
    private lateinit var playPauseSeekbar: SeekBar
    private lateinit var recordBtn: ImageView
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val mAudioManager: AudioManager? = null
    private var player: ExoPlayer? = null
    private val mAudioFocusListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying()) {
                player!!.playWhenReady = false
                updatePlayPauseButton()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
            }
            else -> {
            }
        }
    }
    private var lastSeekPos = 0L

    companion object {
        fun newInstance(
            @Nullable filepath: String?,
        ): AudioPlayerDialog {
            val args = Bundle()
            args.putString("filepath", filepath)
            val fragment = AudioPlayerDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val filepath: String = arguments?.getString("filepath")!!

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.record_dialog)
        recordBtn = dialog.findViewById(R.id.recording_button)
        playPauseSeekbar = dialog.findViewById(R.id.recording_seekbar)

        initPlayer()
        setDataSource(filepath)
        recordBtn.onClick {
            if (player != null) {
                if (!isPlaying() || (player != null && player!!.playbackState == ExoPlayer.STATE_ENDED)) {
                    //play the audio
                    if (player!!.currentPosition >= player!!.duration) {
                        player!!.seekTo(0)
                        lastSeekPos = 0
                    }
                    player!!.playWhenReady = true
                    recordBtn.setImageResource(R.drawable.ic_pause_black)
                } else {
                    //stop the audio
                    player!!.playWhenReady = false
                    recordBtn.setImageResource(R.drawable.ic_play_arrow_black)
                }
            }
        }

        playPauseSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (player != null) {
                    player!!.seekTo(seekBar.progress.toLong())
                    lastSeekPos = seekBar.progress.toLong()
                    if (!player!!.isPlaying && player!!.currentPosition != player!!.duration) {
                        player!!.playWhenReady = true
                        recordBtn.setImageResource(R.drawable.ic_pause_black)
                    }
                }
            }
        })

        return dialog
    }

    private fun initPlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(requireContext()).build()
            player!!.addListener(playbackStateListener)
            player!!.volume = 1f
        } else {
            if (isPlaying()) {
                player!!.playWhenReady = false
            }
            player!!.stop(true)
        }
    }

    private fun isPlaying(): Boolean {
        return player != null && player!!.playWhenReady
    }

    private fun setDataSource(path: String) {
        val mediaSource = buildMediaSource(path)
        player?.setMediaSource(mediaSource)
        player?.prepare()
        mAudioManager?.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        player?.playWhenReady = true
    }

    private fun buildMediaSource(path: String): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(path))
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> {
                    if (lastSeekPos > 0) {
                        player!!.seekTo(lastSeekPos)
                        lastSeekPos = 0
                    }
                    playPauseSeekbar.max = player!!.duration.toInt()
                    seekUpdation()
                    updatePlayPauseButton()
                    "ExoPlayer.STATE_READY -"
                }
                ExoPlayer.STATE_ENDED -> {
                    updatePlayPauseButton()
                    "ExoPlayer.STATE_ENDED -"
                }
                else -> "UNKNOWN_STATE -"
            }
            Timber.tag(">").d("changed state to $stateString")
        }
    }

    private fun seekUpdation() {
        if (player != null) {
            playPauseSeekbar.progress = player!!.currentPosition.toInt()
        }
        if (playPauseSeekbar.progress == playPauseSeekbar.max) {
            recordBtn.setImageResource(R.drawable.ic_play_arrow_black)
        }
        seekHandler.postDelayed(run, 500)
    }

    fun updatePlayPauseButton() {
        if (isPlaying()) {
            recordBtn.setImageResource(R.drawable.ic_pause_black)
        } else {
            recordBtn.setImageResource(R.drawable.ic_play_arrow_black)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.run {
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    override fun onPause() {
        super.onPause()
        try {
            if (player != null && isPlaying()) {
                lastSeekPos = player!!.currentPosition
                player!!.playWhenReady = false
                recordBtn.setImageResource(R.drawable.ic_play_arrow_black)
            }

        } catch (ie: java.lang.IllegalStateException) {
            ie.printStackTrace()
        }
    }

/*
    override fun onResume() {
        super.onResume()
        try {
            if (player != null && !isPlaying()) {
                player!!.playWhenReady = true
            }
        } catch (ie: java.lang.IllegalStateException) {
            ie.printStackTrace()
        }

    }
*/
}