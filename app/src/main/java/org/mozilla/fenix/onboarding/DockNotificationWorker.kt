package org.mozilla.fenix.onboarding

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import mozilla.components.support.base.ids.SharedIdsHelper
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.utils.IntentUtils
import org.mozilla.fenix.utils.Settings
import java.util.concurrent.TimeUnit

class DockNotificationWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        ensureChannelExists()
        NotificationManagerCompat.from(applicationContext)
            .notify(
                NOTIFICATION_TAG,
                NOTIFICATION_ID, buildNotification())

        applicationContext.settings().dockNotificationDisplayed = true

        return Result.success()
    }

    /**
     * Build the default browser notification.
     */
    private fun buildNotification(): Notification {
        val channelId = ensureChannelExists()
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.putExtra(INTENT_DOCK_NOTIFICATION, true)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            SharedIdsHelper.getNextIdForTag(applicationContext,
                NOTIFICATION_PENDING_INTENT_TAG
            ),
            intent,
            IntentUtils.defaultIntentPendingFlags
        )

        with(applicationContext) {
            val message = applicationContext.getString(R.string.karma_notification_dock_text)
            return NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_status_logo)
                .setContentTitle(
                    applicationContext.getString(R.string.karma_notification_dock_title)
                )
                .setContentText(
                    message
                )
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setColor(ContextCompat.getColor(this, R.color.photonGreen50))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }
    }

    /**
     * Make sure a notification channel for default browser notification exists.
     *
     * Returns the channel id to be used for notifications.
     */
    private fun ensureChannelExists(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.notification_marketing_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        return NOTIFICATION_CHANNEL_ID
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "org.mozilla.fenix.dock.channel"
        private const val NOTIFICATION_ID = 3
        private const val NOTIFICATION_PENDING_INTENT_TAG = "org.mozilla.dock.widget"
        private const val INTENT_DOCK_NOTIFICATION = "org.mozilla.fenix.dock.intent"
        private const val NOTIFICATION_TAG = "org.mozilla.fenix.dock.tag"
        private const val NOTIFICATION_WORK_NAME = "org.mozilla.fenix.dock.work"
        private val NOTIFICATIONS_DELAY = listOf(
            Settings.HOURS_MS,
            Settings.ONE_DAY_MS*2,
        )

        fun isDockNotificationIntent(intent: Intent) =
            intent.extras?.containsKey(INTENT_DOCK_NOTIFICATION) ?: false

        fun setDockNotificationIfNeeded(context: Context) {

            if (context.settings().dockNotificationDisplayed) {
                return
            }

            for (notification_delay in NOTIFICATIONS_DELAY) {
                val instanceWorkManager = WorkManager.getInstance(context)

                val notificationWork = OneTimeWorkRequest.Builder(DockNotificationWorker::class.java)
                    .setInitialDelay(notification_delay, TimeUnit.MILLISECONDS)
                    .build()

                instanceWorkManager.beginUniqueWork(
                    NOTIFICATION_WORK_NAME +notification_delay,
                    ExistingWorkPolicy.KEEP,
                    notificationWork
                ).enqueue()
            }

        }
    }
}