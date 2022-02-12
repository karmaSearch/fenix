/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.share

import org.mozilla.fenix.share.listadapters.AppShareOption

/**
 * Interactor for the share screen.
 */
class ShareInteractor(
    private val controller: ShareController
) : ShareCloseInteractor, ShareToAppsInteractor {

    override fun onShareClosed() {
        controller.handleShareClosed()
    }

    override fun onShareToApp(appToShareTo: AppShareOption) {
        controller.handleShareToApp(appToShareTo)
    }
}
