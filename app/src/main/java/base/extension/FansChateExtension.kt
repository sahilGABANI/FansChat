package base.extension

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import base.BuildConfig
import base.R
import java.util.*

enum class Flavors {
    Sporting, Sokkaa, Bal, MTN
}

val flavor
    get() = when (BuildConfig.FLAVOR) {
        "sporting" -> Flavors.Sporting
        "sokkaa" -> Flavors.Sokkaa
        "bal" -> Flavors.Bal
        "mtn" -> Flavors.MTN
        else -> {
            throw(RuntimeException("Cannot find the specified flavor: ${BuildConfig.FLAVOR}"))
        }
    }

val CLUB_ID = when (flavor) {
    Flavors.Sokkaa -> {
        1686
    }
    Flavors.Sporting -> {
        1680
    }
    Flavors.Bal -> {
        1680 //Todo: Replace with 2000
    }
    Flavors.MTN -> {
        2001
    }
}

fun NotificationManager.displayNotification(
    context: Context, textTitle: String, textContent: String, CHANNEL_ID: String, pendingIntent: PendingIntent
) {
    val notificationId = (Date().time / 1000L % Int.MAX_VALUE).toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(CHANNEL_ID, "notification", NotificationManager.IMPORTANCE_HIGH)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.logo_fanschat).setContentTitle(textTitle)
            .setContentText(textContent).setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
        this.createNotificationChannel(notificationChannel)
        this.notify(notificationId, builder.build())
    } else {
        val builder =
            NotificationCompat.Builder(context).setSmallIcon(R.drawable.logo_fanschat).setContentTitle(textTitle).setContentText(textContent)
                .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
        this.notify(notificationId, builder.build())
    }
}