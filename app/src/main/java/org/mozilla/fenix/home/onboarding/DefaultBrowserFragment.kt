package org.mozilla.fenix.home.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentDefaultBrowserBinding
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.settings

class DefaultBrowserFragment: Fragment() {
    private var _binding: FragmentDefaultBrowserBinding? = null
    private val binding get() = _binding!!
    private var originalStatusBarColor: Int? = null
    private var originalNavigationBarColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDefaultBrowserBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.onboardingDefaultButton.setOnClickListener {
            context?.settings()?.let { settings ->
                // Mark as shown when user chooses to set as default
                if (settings.shouldShowSetAsDefaultBrowserAfterOnboarding()) {
                    // This is the first time (right after onboarding)
                    settings.hasShownDefaultBrowserDialog = true
                } else if (settings.shouldShowSetAsDefaultBrowserOnBoarding()) {
                    // This is after the 7-day waiting period
                    settings.hasShownDefaultBrowserDialogAfter7Days = true
                } else {
                    // Fallback: update the general one
                    settings.hasShownDefaultBrowserDialog = true
                }
            }
            activity?.openSetDefaultBrowserOption()
        }

        binding.onboardingDefaultNotnow.setOnClickListener {
            context?.settings()?.let { settings ->
                // Determine which variable to update based on the current context
                if (settings.shouldShowSetAsDefaultBrowserAfterOnboarding()) {
                    // This is the first time (right after onboarding)
                    settings.hasShownDefaultBrowserDialog = true
                } else if (settings.shouldShowSetAsDefaultBrowserOnBoarding()) {
                    // This is after the 7-day waiting period
                    settings.hasShownDefaultBrowserDialogAfter7Days = true
                } else {
                    // Fallback: update the general one
                    settings.hasShownDefaultBrowserDialog = true
                }
            }
            val directions = NavGraphDirections.actionGlobalHome()
            findNavController().navigate(directions)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set status bar and navigation bar to black for default browser setup
        setSystemBarsToBlack(view)
    }

    override fun onResume() {
        super.onResume()
        
        // Ensure system bars are black when fragment resumes
        view?.let { setSystemBarsToBlack(it) }
    }

    private fun setSystemBarsToBlack(view: View) {
        activity?.window?.let { window ->
            // Save original colors if not already saved
            if (originalStatusBarColor == null) {
                originalStatusBarColor = window.statusBarColor
            }
            if (originalNavigationBarColor == null) {
                originalNavigationBarColor = window.navigationBarColor
            }
            
            // Set both to black
            window.statusBarColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            window.navigationBarColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            
            // Ensure icons are light (visible on black background)
            WindowCompat.getInsetsController(window, view).let { controller ->
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        
        // Restore original colors when leaving default browser setup
        restoreOriginalColors()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Ensure colors are restored
        restoreOriginalColors()
        
        _binding = null
    }

    private fun restoreOriginalColors() {
        activity?.window?.let { window ->
            originalStatusBarColor?.let { color ->
                window.statusBarColor = color
                originalStatusBarColor = null // Reset to avoid re-applying
            }
            originalNavigationBarColor?.let { color ->
                window.navigationBarColor = color
                originalNavigationBarColor = null // Reset to avoid re-applying
            }
        }
    }


}
