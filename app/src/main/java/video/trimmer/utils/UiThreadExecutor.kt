package video.trimmer.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock

/**
 * This class provide operations for
 * UiThread tasks.
 */
object UiThreadExecutor {
    private val HANDLER: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val callback = msg.callback
            if (callback != null) {
                callback.run()
                decrementToken(msg.obj as Token)
            } else {
                super.handleMessage(msg)
            }
        }
    }
    private val TOKENS: MutableMap<String, Token?> = HashMap()

    /**
     * Store a new task in the map for providing cancellation. This method is
     * used by AndroidAnnotations and not intended to be called by clients.
     *
     * @param id    the identifier of the task
     * @param task  the task itself
     * @param delay the delay or zero to run immediately
     */
    fun runTask(id: String, task: Runnable?, delay: Long) {
        if ("" == id) {
            HANDLER.postDelayed(task!!, delay)
            return
        }
        val time = SystemClock.uptimeMillis() + delay
        HANDLER.postAtTime(task!!, nextToken(id), time)
    }

    private fun nextToken(id: String): Token {
        synchronized(TOKENS) {
            var token = TOKENS[id]
            if (token == null) {
                token = Token(id)
                TOKENS[id] = token
            }
            token.runnablesCount++
            return token
        }
    }

    private fun decrementToken(token: Token) {
        synchronized(TOKENS) {
            if (--token.runnablesCount == 0) {
                val id = token.id
                val old = TOKENS.remove(id)
                if (old != token) {
                    // a runnable finished after cancelling, we just removed a
                    // wrong token, lets put it back
                    TOKENS[id] = old
                }
            }
        }
    }

    /**
     * Cancel all tasks having the specified `id`.
     *
     * @param id the cancellation identifier
     */
    fun cancelAll(id: String) {
        var token: Token?
        synchronized(TOKENS) { token = TOKENS.remove(id) }
        if (token == null) {
            // nothing to cancel
            return
        }
        HANDLER.removeCallbacksAndMessages(token)
    }

    private class Token(val id: String) {
        var runnablesCount = 0
    }
}