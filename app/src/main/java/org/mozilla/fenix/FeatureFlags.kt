/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix

import android.content.Context
import mozilla.components.support.locale.LocaleManager
import mozilla.components.support.locale.LocaleManager.getSystemDefault

/**
 * A single source for setting feature flags that are mostly based on build type.
 */
object FeatureFlags {

    /**
     * Enables custom extension collection feature,
     * This feature does not only depend on this flag. It requires the AMO collection override to
     * be enabled which is behind the Secret Settings.
     * */
    val customExtensionCollectionFeature = Config.channel.isNightlyOrDebug || Config.channel.isBeta

    /**
     * Pull-to-refresh allows you to pull the web content down far enough to have the page to
     * reload.
     */
    const val pullToRefreshEnabled = true

    /**
     * Enables the Sync Addresses feature.
     */
    const val syncAddressesFeature = false

    /**
     * Enables the onboarding sync CFR on the home screen.
     */
    const val showSynCFR = true

    /**
     * Enables the onboarding jump back in CFR on the home screen.
     */
    const val showJumpBackInCFR = true

    /**
     * Enables the first run onboarding updates.
     */
    const val showFirstRunOnboardingUpdates = false

    /**
     * Enables the "recent" tabs feature in the home screen.
     */
    const val showRecentTabsFeature = true

    /**
     * Enables UI features based on history metadata.
     */
    const val historyMetadataUIFeature = true

    /**
     * Enables the recently saved bookmarks feature in the home screen.
     */
    const val recentBookmarksFeature = true

    /**
     * Identifies and separates the tabs list with a secondary section containing least used tabs.
     */
    const val inactiveTabs = true

    /**
     * Allows tabs to be dragged around as long as tab groups are disabled
     */
    val tabReorderingFeature = Config.channel.isNightlyOrDebug

    /**
     * Show Pocket recommended stories on home.
     */
    fun isPocketRecommendationsFeatureEnabled() = false

    /**
     * Show Pocket sponsored stories in between Pocket recommended stories on home.
     */
    fun isPocketSponsoredStoriesFeatureEnabled(): Boolean {
        return isPocketRecommendationsFeatureEnabled() && Config.channel.isDebug
    }

    /**
     * Enables showing the homescreen onboarding card.
     */
    const val showHomeOnboarding = false

    /**
     * Enables history improvement features.
     */
    const val historyImprovementFeatures = true

    /**
     * Enables the Task Continuity enhancements.
     */
    const val taskContinuityFeature = true

    /**
     * Enables the Unified Search feature.
     */
    val unifiedSearchFeature = Config.channel.isNightlyOrDebug

    /**
     * Enables receiving from the messaging framework.
     */
    const val messagingFeature = false

    /**
     * Enables compose on the tabs tray items.
     */
    val composeTabsTray = Config.channel.isDebug

    /**
     * Enables the wallpaper onboarding.
     */
    const val wallpaperOnboardingEnabled = false

    /**
     * Enables the wallpaper v2 enhancements.
     */
    const val wallpaperV2Enabled = false

    /**
     * Enables the save to PDF feature.
     */
    val saveToPDF = Config.channel.isNightlyOrDebug
}
