package org.mozilla.fenix.onboarding

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import mozilla.components.support.base.ids.SharedIdsHelper
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.utils.IntentUtils
import org.mozilla.fenix.utils.Settings
import java.util.concurrent.TimeUnit

class ShareNotificationWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        ensureChannelExists()
        applicationContext.components.notificationsDelegate
            .notify(
                NOTIFICATION_TAG,
                NOTIFICATION_ID, buildNotification())

        applicationContext.settings().shareNotificationDisplayed = true

        return Result.success()
    }

    /**
     * Build the share notification.
     */
    private fun buildNotification(): Notification {
        val channelId = ensureChannelExists()
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.putExtra(INTENT_SHARE_NOTIFICATION, true)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            SharedIdsHelper.getNextIdForTag(applicationContext,
                NOTIFICATION_PENDING_INTENT_TAG
            ),
            intent,
            IntentUtils.defaultIntentPendingFlags
        )

        with(applicationContext) {
            val message = applicationContext.getString(R.string.karma_notification_share_text)
            val title = applicationContext.getString(R.string.karma_notification_share_title)
            return NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_status_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notification_share))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title))
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
     * Make sure a notification channel for share notification exists.
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
        private const val NOTIFICATION_ID = 4
        private const val NOTIFICATION_PENDING_INTENT_TAG = "org.mozilla.share.widget"
        private const val INTENT_SHARE_NOTIFICATION = "org.mozilla.fenix.share.intent"
        private const val NOTIFICATION_TAG = "org.mozilla.fenix.share.tag"
        private const val NOTIFICATION_WORK_NAME = "org.mozilla.fenix.share.work"
        private val NOTIFICATIONS_DELAY = listOf(
            Settings.ONE_DAY_MS * 100,
        )

        fun isShareNotificationIntent(intent: Intent) =
            intent.extras?.containsKey(INTENT_SHARE_NOTIFICATION) ?: false

        fun setShareNotificationIfNeeded(context: Context) {
            if (context.settings().shareNotificationDisplayed) {
                return
            }

            for (notification_delay in NOTIFICATIONS_DELAY) {
                val instanceWorkManager = WorkManager.getInstance(context)

                val notificationWork = OneTimeWorkRequest.Builder(ShareNotificationWorker::class.java)
                    .setInitialDelay(notification_delay, TimeUnit.MILLISECONDS)
                    .build()

                instanceWorkManager.beginUniqueWork(
                    NOTIFICATION_WORK_NAME + notification_delay,
                    ExistingWorkPolicy.KEEP,
                    notificationWork
                ).enqueue()
            }
        }
    }
}
