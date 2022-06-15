package org.mozilla.fenix.home.intent

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.navigation.NavController
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.onboarding.WidgetNotificationWorker
import org.mozilla.gecko.search.SearchWidgetProvider

class WidgetIntentProcessor(
    private val activity: HomeActivity
) : HomeIntentProcessor {

    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return if (WidgetNotificationWorker.isWidgetNotificationIntent(intent)) {
            handleOpenRequestPinAppWidget()
            true
        } else {
            false
        }
    }

    private fun handleOpenRequestPinAppWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = activity.getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(activity, SearchWidgetProvider::class.java)

            appWidgetManager.requestPinAppWidget(myProvider,null, null)
        }
    }
}
