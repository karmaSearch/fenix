/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import karma.service.affiliatesites.AffiliateSite
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.home.sessioncontrol.AffiliateSiteInteractor

class AffiliateSitesAdapter(
    private val appStore: AppStore,
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: AffiliateSiteInteractor,
) : ListAdapter<AffiliateSite, AffiliateSiteItemViewHolder>(AffiliateSitesDiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffiliateSiteItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(AffiliateSiteItemViewHolder.LAYOUT_ID, parent, false)
        return AffiliateSiteItemViewHolder(view, appStore, viewLifecycleOwner, interactor)
    }

    override fun onBindViewHolder(holder: AffiliateSiteItemViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    internal object AffiliateSitesDiffCallback : DiffUtil.ItemCallback<AffiliateSite>() {
        override fun areItemsTheSame(oldItem: AffiliateSite, newItem: AffiliateSite) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AffiliateSite, newItem: AffiliateSite) = 
            oldItem == newItem
    }
}
