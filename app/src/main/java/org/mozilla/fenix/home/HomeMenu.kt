/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home

import android.content.Context
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuHighlight
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.ext.getHighlight
import mozilla.components.browser.menu.item.BrowserMenuDivider
import mozilla.components.browser.menu.item.BrowserMenuImageSwitch
import mozilla.components.browser.menu.item.BrowserMenuImageText
import org.mozilla.fenix.FeatureFlags
import org.mozilla.fenix.R
import org.mozilla.fenix.components.accounts.AccountState
import org.mozilla.fenix.experiments.FeatureId
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.getVariables
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.theme.ThemeManager
import org.mozilla.fenix.utils.BrowsersCache

@Suppress("LargeClass", "LongMethod")
class HomeMenu(
    private val context: Context,
    private val onItemTapped: (Item) -> Unit = {},
    private val onMenuBuilderChanged: (BrowserMenuBuilder) -> Unit = {},
    private val onHighlightPresent: (BrowserMenuHighlight) -> Unit = {}
) {
    sealed class Item {
        object NewTab : Item()
        object NewPrivateTab : Item()
        object SetDefaultBrowser: Item()
        object Bookmarks : Item()
        object History : Item()
        object Downloads : Item()
        object Extensions : Item()
        data class SyncAccount(val accountState: AccountState) : Item()
        object WhatsNew : Item()
        object Help : Item()
        object CustomizeHome : Item()
        object Settings : Item()
        object Quit : Item()
        object ReconnectSync : Item()
        object Feedback : Item()

        data class DesktopMode(val checked: Boolean) : Item()
    }

    private val primaryTextColor = ThemeManager.resolveAttribute(R.attr.primaryText, context)

    private val quitItem by lazy {
        BrowserMenuImageText(
            context.getString(R.string.delete_browsing_data_on_quit_action),
            R.drawable.mozac_ic_quit,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Quit)
        }
    }

    val desktopItem = BrowserMenuImageSwitch(
        imageResource = R.drawable.ic_desktop,
        label = context.getString(R.string.browser_menu_desktop_site),
        initialState = { context.settings().openNextTabInDesktopMode }
    ) { checked ->
        onItemTapped.invoke(Item.DesktopMode(checked))
    }

    @Suppress("ComplexMethod")
    private fun coreMenuItems(): List<BrowserMenuItem> {
        val experiments = context.components.analytics.experiments
        val settings = context.components.settings

        var newTab = BrowserMenuImageText(
            context.getString(R.string.home_screen_shortcut_open_new_tab_2),
            R.drawable.ic_newtab,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.NewTab)
        }

        var newPrivateTab = BrowserMenuImageText(
            context.getString(R.string.home_screen_shortcut_open_new_private_tab_2),
            R.drawable.ic_new_private_tab,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.NewPrivateTab)
        }

        val bookmarksItem = BrowserMenuImageText(
            context.getString(R.string.library_bookmarks),
            R.drawable.ic_bookmark,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Bookmarks)
        }

        val historyItem = BrowserMenuImageText(
            context.getString(R.string.library_history),
            R.drawable.ic_history,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.History)
        }

        val downloadsItem = BrowserMenuImageText(
            context.getString(R.string.library_downloads),
            R.drawable.ic_download,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Downloads)
        }

        val extensionsItem = BrowserMenuImageText(
            context.getString(R.string.browser_menu_add_ons),
            R.drawable.ic_addons_extensions,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Extensions)
        }

        val customizeHomeItem = BrowserMenuImageText(
            context.getString(R.string.browser_menu_customize_home),
            R.drawable.ic_customize,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.CustomizeHome)
        }

        // Use nimbus to set the icon and title.
        val variables = experiments.getVariables(FeatureId.NIMBUS_VALIDATION)
        val settingsItem = BrowserMenuImageText(
            variables.getText("settings-title") ?: context.getString(R.string.browser_menu_settings),
            variables.getDrawableResource("settings-icon") ?: R.drawable.ic_settings,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Settings)
        }

        val feedbackHomeItem = BrowserMenuImageText(
            context.getString(R.string.browser_menu_feedback),
            R.drawable.ic_feedback,
            primaryTextColor
        ) {
            onItemTapped.invoke(Item.Feedback)
        }

        val menuItems = listOfNotNull(
            newTab,
            newPrivateTab,
            getSetDefaultBrowserItem(),
            BrowserMenuDivider(),
            bookmarksItem,
            historyItem,
            downloadsItem,
            extensionsItem,
            desktopItem,
            BrowserMenuDivider(),
            customizeHomeItem,
            feedbackHomeItem,
            settingsItem,
            if (settings.shouldDeleteBrowsingDataOnQuit) quitItem else null,
        ).also { items ->
            items.getHighlight()?.let { onHighlightPresent(it) }
        }

        return menuItems
    }

    init {
        val menuItems = coreMenuItems()

        // Report initial state.
        onMenuBuilderChanged(BrowserMenuBuilder(menuItems))

    }

    private fun getSetDefaultBrowserItem(): BrowserMenuImageText? {
        val browsers = BrowsersCache.all(context)

        return if (!browsers.isKARMADefaultBrowser) {
            return BrowserMenuImageText(
                label = context.getString(R.string.preferences_set_as_default_browser),
                imageResource = R.drawable.ic_globe
            ) {
                onItemTapped.invoke(Item.SetDefaultBrowser)
            }
        } else {
            null
        }
    }
}
