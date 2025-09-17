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

class AffiliateSitesPagerAdapter(
    private val appStore: AppStore,
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: AffiliateSiteInteractor,
) : ListAdapter<List<AffiliateSite>, AffiliateSiteViewHolder>(AffiliateSiteListDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffiliateSiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(AffiliateSiteViewHolder.LAYOUT_ID, parent, false)
        return AffiliateSiteViewHolder(view, appStore, viewLifecycleOwner, interactor)
    }

    override fun onBindViewHolder(holder: AffiliateSiteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object AffiliateSiteListDiffCallback : DiffUtil.ItemCallback<List<AffiliateSite>>() {
    override fun areItemsTheSame(
        oldItem: List<AffiliateSite>,
        newItem: List<AffiliateSite>,
    ): Boolean {
        return oldItem.size == newItem.size &&
            oldItem.zip(newItem).all { (old, new) -> old.id == new.id }
    }

    override fun areContentsTheSame(
        oldItem: List<AffiliateSite>,
        newItem: List<AffiliateSite>,
    ): Boolean {
        return oldItem.zip(newItem).all { (old, new) ->
            old.siteName == new.siteName && old.url == new.url && old.imageUrl == new.imageUrl
        }
    }
}
