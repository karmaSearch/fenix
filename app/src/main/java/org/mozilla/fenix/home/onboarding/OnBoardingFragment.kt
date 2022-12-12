package org.mozilla.fenix.home.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.databinding.FragmentOnBoardingBinding

import org.mozilla.fenix.ext.settings


/**
 * A simple [Fragment] subclass.
 * Use the [OnBoardingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnBoardingFragment: Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var onBoardingSitesPagerAdapter: OnBoardingPagerAdapter
    private lateinit var pageIndicator: TabLayout
    private lateinit var viewModel: OnBoardingViewModel
    private lateinit var interactor: OnBoardingInteractor

    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        val view = binding.root

        context?.settings()?.let { settings ->
            settings.hasShownHomeOnboardingDialog = true
        }
        viewModel = OnBoardingViewModel()
        interactor = OnBoardingInteractorImpl(
            showNextPage = {

                binding.onboardingPager.doOnLayout {
                    if (currentPage < pageIndicator.tabCount-1) {
                        currentPage += 1
                        binding.onboardingPager.currentItem = currentPage
                    } else {
                        val directions = NavGraphDirections.actionStartupDefaultbrowser()
                        findNavController().navigate(directions)
                    }
                }
            }
        )
        onBoardingSitesPagerAdapter = OnBoardingPagerAdapter(viewModel.onBoardingPages, interactor)

        binding.onboardingPager.apply {
            adapter = onBoardingSitesPagerAdapter
            registerOnPageChangeCallback(onBoardingPageChangeCallback)
            offscreenPageLimit = 1
        }
        pageIndicator = binding.pageIndicator
        pageIndicator.addOnTabSelectedListener(onBoardingTabSelectedCallback)
        TabLayoutMediator(pageIndicator, binding.onboardingPager) { _, _ ->}.attach()

        binding.onboardingSkip.setOnClickListener {
            val directions = NavGraphDirections.actionStartupDefaultbrowser()
            findNavController().navigate(directions)
        }
        return view
    }

    private val onBoardingPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            currentPage = position
            binding.onboardingSkip.visibility = if (currentPage == viewModel.onBoardingPages.size -1) View.GONE else View.VISIBLE
        }
    }

    private val onBoardingTabSelectedCallback = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            currentPage = tab!!.position
            binding.onboardingSkip.visibility = if (currentPage == viewModel.onBoardingPages.size -1) View.GONE else View.VISIBLE
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }
}
