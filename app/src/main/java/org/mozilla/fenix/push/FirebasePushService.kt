/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.push

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import mozilla.components.feature.push.AutoPushFeature
import mozilla.components.lib.push.firebase.AbstractFirebasePushService
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.perf.Performance.logger


/**
 * A singleton instance of the FirebasePushService needed for communicating between FCM and the
 * [AutoPushFeature].
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh") // Implemented internally.
class FirebasePushService : AbstractFirebasePushService()
{

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        logger.debug("onMessageReceived " + message.data)

        val notification = message.notification


        val channelId = notification?.channelId ?: NotificationManager.EXTRA_NOTIFICATION_CHANNEL_ID
        if (notification != null) {
            with(applicationContext) {

                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                for(data in message.data) {
                    logger.debug("onMessageReceived " + data.key)
                    logger.debug("onMessageReceived " + data.value)

                    intent.putExtra(data.key, data.value)
                }

                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )

                val builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_status_logo)
                    .setContentTitle(
                        notification.title,
                    )
                    .setContentText(
                        notification.body,
                    )
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setColor(ContextCompat.getColor(this, R.color.photonGreen50))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setShowWhen(false)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                NotificationManagerCompat.from(applicationContext)
                    .notify(4, builder)
            }
        }


    }
}
