package org.mozilla.fenix.home.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentOnBoardingBinding
import org.mozilla.fenix.ext.settings
import java.util.*

/**
 * Simplified single-page onboarding fragment.
 */
class OnBoardingFragment: Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        
        // Mark onboarding as shown
        context?.settings()?.let { settings ->
            settings.hasShownHomeOnboardingDialog = true
        }

        setupClickListeners()
        
        return binding.root
    }

    private fun setupClickListeners() {
        // Main button - navigate to default browser setup
        binding.onboardingButton.setOnClickListener {
            val directions = NavGraphDirections.actionStartupDefaultbrowser()
            findNavController().navigate(directions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
