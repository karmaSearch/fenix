package org.mozilla.fenix.home.intent

import android.content.Intent
import androidx.navigation.NavController
import mozilla.components.support.ktx.kotlin.toNormalizedUrl
import mozilla.components.support.locale.LocaleManager
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.components.components
import org.mozilla.fenix.ext.components

class FirebaseIntentProcessor(
    private val activity: HomeActivity
) : HomeIntentProcessor {




    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return if (isFirebaseNotificationIntentWithURL(intent)) {

            val url = intent.extras?.get(INTENT_FIREBASE_URL).toString()
            if (!url.isEmpty()) {
                activity.openToBrowserAndLoad(url,true, BrowserDirection.FromGlobal)
            } else {
                activity.openToBrowser(BrowserDirection.FromAbout)
            }
            true
        } else if (isFirebaseNotificationIntentNewTab(intent)) {
            //activity.openToBrowser(BrowserDirection.FromHome)
            //activity.openToBrowserAndLoad("about:blank",true, BrowserDirection.F)
            activity.navigateToHome()
            false
        } else {
            false
        }
    }

    companion object {
        private const val INTENT_FIREBASE_URL = "com.karmasearch.firebase.url"
        private const val INTENT_FIREBASE_NEW_TAB = "com.karmasearch.firebase.newtab"

        fun isFirebaseNotificationIntentWithURL(intent: Intent) =
            intent.extras?.containsKey(INTENT_FIREBASE_URL) ?: false
        fun isFirebaseNotificationIntentNewTab(intent: Intent) =
            intent.extras?.containsKey(INTENT_FIREBASE_NEW_TAB) ?: false
    }

}
