/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import karma.service.affiliatesites.AffiliateSite
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.databinding.ComponentAffiliateSitesPagerBinding
import org.mozilla.fenix.home.sessioncontrol.AdapterItem
import org.mozilla.fenix.home.sessioncontrol.AffiliateSiteInteractor

class AffiliateSitesPagerViewHolder(
    view: View,
    appStore: AppStore,
    viewLifecycleOwner: LifecycleOwner,
    interactor: AffiliateSiteInteractor,
) : RecyclerView.ViewHolder(view) {

    private val binding = ComponentAffiliateSitesPagerBinding.bind(view)
    private val affiliateSitesPagerAdapter = AffiliateSitesPagerAdapter(appStore, viewLifecycleOwner, interactor)
    private val pageIndicator = binding.pageIndicator
    private var currentPage = 0

    private val affiliateSitesPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageIndicator.setSelection(position)
            currentPage = position
        }
    }

    init {
        binding.affiliateSitesPager.apply {
            adapter = affiliateSitesPagerAdapter
            registerOnPageChangeCallback(affiliateSitesPageChangeCallback)
        }
    }

    fun bind(affiliateSites: List<AffiliateSite>) {
        val chunkedAffiliateSites = affiliateSites.chunked(AFFILIATE_SITES_PER_PAGE)
        
        binding.affiliateSitesPager.isVisible = affiliateSites.isNotEmpty()
        binding.pageIndicator.isVisible = chunkedAffiliateSites.size > 1

        affiliateSitesPagerAdapter.submitList(chunkedAffiliateSites)

        // Update page indicators
        if (chunkedAffiliateSites.size > 1) {
            pageIndicator.setSize(chunkedAffiliateSites.size)
            pageIndicator.setSelection(currentPage)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun update(payload: AdapterItem.AffiliateSitesPagerPayload) {
        // For now, just rebind with empty list - could be optimized later
        bind(emptyList())
    }

    companion object {
        const val LAYOUT_ID = R.layout.component_affiliate_sites_pager
        const val AFFILIATE_SITES_PER_PAGE = 4
    }
}
