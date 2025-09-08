/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import karma.service.affiliatesites.AffiliateSite
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.databinding.ComponentAffiliateSitesBinding
import org.mozilla.fenix.home.sessioncontrol.AffiliateSiteInteractor
import org.mozilla.fenix.utils.AccessibilityGridLayoutManager

class AffiliateSiteViewHolder(
    view: View,
    appStore: AppStore,
    viewLifecycleOwner: LifecycleOwner,
    interactor: AffiliateSiteInteractor,
) : RecyclerView.ViewHolder(view) {

    private val affiliateSitesAdapter = AffiliateSitesAdapter(appStore, viewLifecycleOwner, interactor)
    val binding = ComponentAffiliateSitesBinding.bind(view)

    init {
        val gridLayoutManager =
            AccessibilityGridLayoutManager(view.context, SPAN_COUNT)

        binding.affiliateSitesList.apply {
            adapter = affiliateSitesAdapter
            layoutManager = gridLayoutManager
        }
    }

    fun bind(affiliateSites: List<AffiliateSite>) {
        affiliateSitesAdapter.submitList(affiliateSites)
    }

    companion object {
        const val LAYOUT_ID = R.layout.component_affiliate_sites
        const val SPAN_COUNT = 4
    }
}
