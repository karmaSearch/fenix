package org.mozilla.fenix.home.intent

import android.content.Intent
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.NavController
import mozilla.components.support.locale.LocaleManager
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.onboarding.DockNotificationWorker

class DockIntentProcessor(
    private val activity: HomeActivity
) : HomeIntentProcessor {

    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return if (DockNotificationWorker.isDockNotificationIntent(intent)) {

            val url = when(LocaleManager.getSystemDefault().country) {
                "FR" -> "https://info.karmasearch.org/fr/android-dock?utm_source=push-notif"
                else -> "https://info.karmasearch.org/android-dock?utm_source=push-notif"
            }
            activity.openToBrowserAndLoad(url,true,BrowserDirection.FromGlobal)
            true
        } else {
            false
        }
    }

}
