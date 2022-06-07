/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.about

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
import org.mozilla.fenix.settings.SettingsFragmentDirections
import org.mozilla.fenix.settings.advanced.getSelectedLocale

/**
 * Displays the logo and information about the app, including library versions.
 */
class KarmaSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.karma_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.isVerticalScrollBarEnabled = false
        val isFr = LocaleManager.getSelectedLocale((activity as HomeActivity)).language =="fr"
        val baseURL = "https://about.karmasearch.org/" + (if(isFr) "fr/" else "")

        when (preference.key) {
            resources.getString(R.string.pref_key_mission) -> openLinkInNormalTab(baseURL)
            resources.getString(R.string.pref_key_howitwork) -> openLinkInNormalTab(baseURL+"what")
            resources.getString(R.string.pref_key_privacy) -> openLinkInNormalTab(baseURL+"legal")
            resources.getString(R.string.pref_key_termsofservices) -> openLinkInNormalTab(if(isFr) "https://mykarma.notion.site/Conditions-d-utilisation-c5f461f440b3475cbac000dc10d8527e" else "https://mykarma.notion.site/Terms-of-service-e73514e7789b4f98b8883f88dbd11b32")
            resources.getString(R.string.pref_key_partners) -> openLinkInNormalTab(baseURL+"partners")
            resources.getString(R.string.pref_key_feedback) -> navigateFromSettings(KarmaSettingsFragmentDirections.actionGlobalFeedbackFragment())
            resources.getString(R.string.pref_key_licence) -> navigateFromSettings(KarmaSettingsFragmentDirections.actionGlobalAboutFragment())
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun navigateFromSettings(directions: NavDirections) {
        view?.findNavController()?.let { navController ->
            if (navController.currentDestination?.id == R.id.karmasettingFragment) {
                navController.navigate(directions)
            }
        }
    }

    private fun openLinkInNormalTab(url: String) {
        (activity as HomeActivity).openToBrowserAndLoad(
            searchTermOrURL = url,
            newTab = false,
            from = BrowserDirection.FromKarmaSettings
        )
    }


}
