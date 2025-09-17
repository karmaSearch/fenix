/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import karma.service.affiliatesites.AffiliateSite
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.utils.Settings

/**
 * Interface for a view that can display a list of affiliate sites.
 */
interface AffiliateSitesView {
    fun displayAffiliateSites(affiliateSites: List<AffiliateSite>)
}

/**
 * View implementation for displaying affiliate sites.
 * This follows the same pattern as DefaultTopSitesView.
 */
class DefaultAffiliateSitesView(
    val store: AppStore,
    val settings: Settings,
) : AffiliateSitesView {

    override fun displayAffiliateSites(affiliateSites: List<AffiliateSite>) {
        store.dispatch(
            AppAction.AffiliateSitesChange(affiliateSites)
        )
    }
}