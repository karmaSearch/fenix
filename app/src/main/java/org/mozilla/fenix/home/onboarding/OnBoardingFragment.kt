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
        setupLocaleBasedImages()
        
        return binding.root
    }

    private fun setupClickListeners() {
        // Main button - navigate to default browser setup
        binding.onboardingButton.setOnClickListener {
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
        _binding = null
    }
}
