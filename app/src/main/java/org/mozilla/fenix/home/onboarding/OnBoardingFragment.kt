package org.mozilla.fenix.home.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    private var originalNavigationBarColor: Int? = null
    private var originalStatusBarColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        
        setupClickListeners()
        setupLocaleBasedImages()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set status bar and navigation bar to black for onboarding
        activity?.window?.let { window ->
            // Save original colors
            originalStatusBarColor = window.statusBarColor
            originalNavigationBarColor = window.navigationBarColor
            
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

    private fun setupClickListeners() {
        // Main button - navigate to default browser setup
        binding.onboardingButton.setOnClickListener {
            // Mark onboarding as completed when user clicks the button
            context?.settings()?.let { settings ->
                settings.hasShownHomeOnboardingDialog = true
            }
            
            val directions = NavGraphDirections.actionStartupDefaultbrowser()
            findNavController().navigate(directions)
        }
    }

    private fun setupLocaleBasedImages() {
        val locale = Locale.getDefault().language
        
        when (locale) {
            "en" -> {
                // Afficher les versions par défaut (PNG) pour l'anglais
                binding.orgaImage1.setImageResource(R.drawable.ic_orga_1)
                binding.orgaImage2.setImageResource(R.drawable.ic_orga_2)
                binding.orgaImage3.setImageResource(R.drawable.ic_orga_3)
                
                binding.orgaImage1.visibility = View.VISIBLE
                binding.orgaImage2.visibility = View.VISIBLE
                binding.orgaImage3.visibility = View.VISIBLE
            }
            "fr" -> {
                // Utiliser les versions françaises avec nouveaux noms
                binding.orgaImage1.setImageResource(R.drawable.ic_orga_1_fr)
                binding.orgaImage2.setImageResource(R.drawable.ic_orga_2_fr)
                binding.orgaImage3.setImageResource(R.drawable.ic_orga_3_fr)
                
                binding.orgaImage1.visibility = View.VISIBLE
                binding.orgaImage2.visibility = View.VISIBLE
                binding.orgaImage3.visibility = View.VISIBLE
            }
            else -> {
                // Cacher les images pour toutes les autres langues
                binding.orgaImage1.visibility = View.GONE
                binding.orgaImage2.visibility = View.GONE
                binding.orgaImage3.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Restore original colors when leaving onboarding
        activity?.window?.let { window ->
            originalStatusBarColor?.let { color ->
                window.statusBarColor = color
            }
            originalNavigationBarColor?.let { color ->
                window.navigationBarColor = color
            }
        }
        
        _binding = null
    }
}
