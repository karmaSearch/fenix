package org.mozilla.fenix.home.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import mozilla.components.feature.top.sites.TopSite
import org.mozilla.fenix.R
import org.mozilla.fenix.home.sessioncontrol.AdapterItem
import org.mozilla.fenix.home.sessioncontrol.TopSiteInteractor
import org.mozilla.fenix.home.topsites.*

class OnBoardingPagerAdapter(
    private val onboardings: List<OnBoarding>,
    private val interactor: OnBoardingInteractor
) : RecyclerView.Adapter<OnBoardingPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingPagerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(OnBoardingPagerViewHolder.LAYOUT_ID, parent, false)
        return OnBoardingPagerViewHolder(view, interactor)
    }

    override fun onBindViewHolder(holder: OnBoardingPagerViewHolder, position: Int) {
        holder.bind(onboardings[position])

    }

    override fun getItemCount(): Int {
        return onboardings.count()
    }

}
