package org.mozilla.fenix.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.support.locale.LocaleManager
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.showToolbar
import org.mozilla.fenix.settings.advanced.getSelectedLocale

class FeedbackSettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.feedback_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.browser_menu_feedback))
    }

    @Suppress("ComplexMethod", "LongMethod")
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        // Hide the scrollbar so the animation looks smoother
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.isVerticalScrollBarEnabled = false

         when (preference.key) {
            resources.getString(R.string.pref_key_feedback) -> {
                (activity as HomeActivity).openToBrowserAndLoad(
                    searchTermOrURL = if (LocaleManager.getSelectedLocale((activity as HomeActivity)).language =="fr") SupportUtils.KARMA_FEEDBACK_FORMS_FR else SupportUtils.KARMA_FEEDBACK_FORMS,
                    newTab = true,
                    from = BrowserDirection.FromFeedBackSettings
                )
            }
            resources.getString(R.string.pref_key_contact) -> {
                val intent = Intent(Intent.ACTION_VIEW)
                val data = Uri.parse(
                    "mailto:"
                            + "android_app@mykarma.org"
                            + "?subject=" + "Feedback" + "&body=" + ""
                )
                intent.data = data
                (activity as HomeActivity).startActivity(intent)
            }
            resources.getString(R.string.pref_key_rate) -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SupportUtils.RATE_APP_URL)))
                } catch (e: ActivityNotFoundException) {
                    // Device without the play store installed.
                    // Opening the play store website.
                    (activity as HomeActivity).openToBrowserAndLoad(
                        searchTermOrURL = SupportUtils.FENIX_PLAY_STORE_URL,
                        newTab = true,
                        from = BrowserDirection.FromSettings
                    )
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun navigateFromSettings(directions: NavDirections) {
        view?.findNavController()?.let { navController ->
            if (navController.currentDestination?.id == R.id.settingsFragment) {
                navController.navigate(directions)
            }
        }
    }
}