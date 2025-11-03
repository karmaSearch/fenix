/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.writereview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentEnjoyingKarmaBinding
import org.mozilla.fenix.ext.settings

class EnjoyingKarmaFragment : DialogFragment() {

    private var _binding: FragmentEnjoyingKarmaBinding? = null
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
        _binding = FragmentEnjoyingKarmaBinding.inflate(inflater, container, false)
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

        binding.enjoyingKarmaNoButton.setOnClickListener {
            context?.settings()?.userDismissedWriteReviewDialog = true
            dismiss()
        }

        binding.enjoyingKarmaYesButton.setOnClickListener {
            dismiss()
            // Show WriteReviewFragment
            WriteReviewFragment().show(parentFragmentManager, "WriteReviewFragment")
        }
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