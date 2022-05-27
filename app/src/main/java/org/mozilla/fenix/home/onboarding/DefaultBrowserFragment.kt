package org.mozilla.fenix.home.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mozilla.fenix.databinding.FragmentDefaultBrowserBinding
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.settings

class DefaultBrowserFragment: Fragment() {
    private var _binding: FragmentDefaultBrowserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDefaultBrowserBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.onboardingDefaultButton.setOnClickListener {
            activity?.openSetDefaultBrowserOption()
        }

        binding.onboardingDefaultNotnow.setOnClickListener {
            context?.settings()?.let { settings ->
                settings.hasShownDefaultBrowserDialog = true
            }
            findNavController().navigateUp()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        context?.settings()?.let {
            if(!it.shouldShowSetAsDefaultBrowserOnBoarding()) {
                findNavController().navigateUp()
            }
        }
    }

}