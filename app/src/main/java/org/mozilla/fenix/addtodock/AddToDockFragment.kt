/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.addtodock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import mozilla.components.support.locale.LocaleManager
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentAddToDockBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.settings.advanced.getSelectedLocale

class AddToDockFragment : DialogFragment() {

    private var _binding: FragmentAddToDockBinding? = null
    private val binding get() = _binding!!
    private var originalNavigationBarColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.HomeOnboardingDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddToDockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set navigation bar to black
        activity?.window?.let { window ->
            originalNavigationBarColor = window.navigationBarColor
            window.navigationBarColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            WindowCompat.getInsetsController(window, view).let { controller ->
                controller.isAppearanceLightNavigationBars = false
            }
        }

        binding.addToDockButton.setOnClickListener {
            openDockInstructions()
            dismiss()
        }

        binding.addToDockLater.setOnClickListener {
            context?.settings()?.userDismissedAddToDockDialog = true
            dismiss()
        }
    }

    private fun openDockInstructions() {
        val isFr = LocaleManager.getSelectedLocale((activity as HomeActivity)).language == "fr"
        val baseURL = "https://info.karmasearch.org/" + (if(isFr) "fr/" else "")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(baseURL+"android-dock?utm_source=in-app-notif"))
        startActivity(intent)
        context?.settings()?.userDismissedAddToDockDialog = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Restore original navigation bar color
        activity?.window?.let { window ->
            originalNavigationBarColor?.let { color ->
                window.navigationBarColor = color
            }
        }
        
        _binding = null
    }
}
