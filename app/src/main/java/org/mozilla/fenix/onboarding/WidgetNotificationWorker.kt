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

class WidgetNotificationWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        if (!context.settings().shouldShowNotificationWidget()) {
            return Result.success()
        }
        ensureChannelExists()
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_TAG, NOTIFICATION_ID, buildNotification())

        applicationContext.settings().widgetNotificationDisplayed = true

        return Result.success()
    }

    /**
     * Build the default browser notification.
     */
    private fun buildNotification(): Notification {
        val channelId = ensureChannelExists()
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.putExtra(INTENT_WIDGET_NOTIFICATION, true)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            SharedIdsHelper.getNextIdForTag(applicationContext, NOTIFICATION_PENDING_INTENT_TAG),
            intent,
            IntentUtils.defaultIntentPendingFlags
        )

        with(applicationContext) {
            val message = applicationContext.getString(R.string.karma_notification_widget_text)

            return NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_status_logo)
                .setContentTitle(
                    applicationContext.getString(R.string.karma_notification_widget_title)
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
                applicationContext.getString(R.string.channel_karma_update),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        return NOTIFICATION_CHANNEL_ID
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "com.karmasearch.channel.update"
        private const val NOTIFICATION_ID = 2
        private const val NOTIFICATION_PENDING_INTENT_TAG = "org.mozilla.fenix.widget"
        private const val INTENT_WIDGET_NOTIFICATION = "org.mozilla.fenix.widget.intent"
        private const val NOTIFICATION_TAG = "org.mozilla.fenix.widget.tag"
        private const val NOTIFICATION_WORK_NAME = "org.mozilla.fenix.widget.work"
        private const val NOTIFICATION_DELAY = Settings.ONE_WEEK_MS

        fun isWidgetNotificationIntent(intent: Intent) =
            intent.extras?.containsKey(INTENT_WIDGET_NOTIFICATION) ?: false

        fun setWidgetNotificationIfNeeded(context: Context) {
            val instanceWorkManager = WorkManager.getInstance(context)

            if (!context.settings().shouldShowNotificationWidget()) {
                return
            }

            val notificationWork = OneTimeWorkRequest.Builder(WidgetNotificationWorker::class.java)
                .setInitialDelay(NOTIFICATION_DELAY, TimeUnit.MILLISECONDS)
                .build()

            instanceWorkManager.beginUniqueWork(
                NOTIFICATION_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                notificationWork
            ).enqueue()

        }
    }
}
