/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mozilla.components.support.base.feature.LifecycleAwareFeature
import karma.service.affiliatesites.AffiliateSitesService

/**
 * Feature for managing affiliate sites background refresh.
 * Data loading is handled directly in HomeFragment following the LearnAndAct pattern.
 */
class AffiliateSitesFeature(
    private val affiliateSitesService: AffiliateSitesService
) : LifecycleAwareFeature {

    private var scope: CoroutineScope? = null

    override fun start() {
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        
        // Start the affiliate sites service background refresh only
        // Data loading is now handled directly in HomeFragment like LearnAndAct
        affiliateSitesService.startAffiliateSitesRefresh()
    }

    override fun stop() {
        scope?.cancel()
        scope = null
        
        // Stop the affiliate sites service background refresh
        affiliateSitesService.stopPeriodicAffiliateSitesRefresh()
    }

}