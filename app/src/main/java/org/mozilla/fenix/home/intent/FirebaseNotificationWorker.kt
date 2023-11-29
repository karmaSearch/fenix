package org.mozilla.fenix.home.intent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.mozilla.fenix.R

class FirebaseNotificationWorker  {


    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "com.karmasearch.channel.learnandact"
        private const val NOTIFICATION_CHANNEL_UPDATE_ID = "com.karmasearch.channel.update"

        fun ensureChannelExists(context: Context): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channelLearnAndAct = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.channel_learnandact),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val channelUpdate = NotificationChannel(
                    NOTIFICATION_CHANNEL_UPDATE_ID,
                    context.getString(R.string.channel_karma_update),
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                notificationManager.createNotificationChannel(channelUpdate)

                notificationManager.createNotificationChannel(channelLearnAndAct)
                notificationManager.deleteNotificationChannel("org.mozilla.fenix.widget.channel")
                notificationManager.deleteNotificationChannel("org.mozilla.fenix.dock.channel")
                notificationManager.deleteNotificationChannel("org.mozilla.fenix.default.browser.channel")

            }

            return NOTIFICATION_CHANNEL_ID
        }
    }

}
