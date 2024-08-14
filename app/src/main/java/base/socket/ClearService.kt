package base.socket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import base.application.FansChat
import javax.inject.Inject

class ClearService : Service() {

    @Inject
    lateinit var socketService: SocketService

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        FansChat.component.inject(this)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        socketService.disconnect()
        //Toast.makeText(this, "App killed", Toast.LENGTH_SHORT).show();
        stopSelf()
    }
}