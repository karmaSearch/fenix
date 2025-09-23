package org.mozilla.fenix.home.intent

import android.content.Intent
import androidx.navigation.NavController
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.onboarding.ShareNotificationWorker

class ShareIntentProcessor(
    private val activity: HomeActivity
) : HomeIntentProcessor {

    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return if (ShareNotificationWorker.isShareNotificationIntent(intent)) {
            val url = "https://info.karmasearch.org/share?utm_source=push-notif"
            activity.openToBrowserAndLoad(url, true, BrowserDirection.FromGlobal)
            true
        } else {
            false
        }
    }
}