package org.mozilla.fenix.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.system.exitProcess
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.ext.showToolbar


class PushDebugSettings : PreferenceFragmentCompat() {
    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.preferences_sync_debug))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.push_debug_preferences, rootKey)

        updateMenu()
    }

    private fun updateMenu() {
        lifecycleScope.launch(Dispatchers.IO) {
            val token = FirebaseMessaging.getInstance().token.await()
            requirePreference<Preference>(R.string.pref_key_fcm_token).let {
                it.summary = token
                it.isCopyingEnabled = true
            }

        }


    }
}
