/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol

import androidx.recyclerview.widget.RecyclerView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import karma.service.learnandact.LearnAndAct
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.service.pocket.PocketRecommendedStory
import mozilla.components.support.test.robolectric.testContext
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.helpers.FenixRobolectricTestRunner
import org.mozilla.fenix.historymetadata.HistoryMetadataGroup
import org.mozilla.fenix.home.HomeFragmentState
import org.mozilla.fenix.home.recenttabs.RecentTab
import org.mozilla.fenix.utils.Settings

@RunWith(FenixRobolectricTestRunner::class)
class SessionControlViewTest {

    @Test
    fun `GIVEN recent Bookmarks WHEN calling shouldShowHomeOnboardingDialog THEN show the dialog `() {
        val recentBookmarks =
            listOf(BookmarkNode(BookmarkNodeType.ITEM, "guid", null, null, null, null, 0, null))
        val settings: Settings = mockk()

        every { settings.hasShownHomeOnboardingDialog } returns false

        val state = HomeFragmentState(recentBookmarks = recentBookmarks)

        assertTrue(state.shouldShowHomeOnboardingDialog(settings))
    }

    @Test
    fun `GIVEN recentTabs WHEN calling shouldShowHomeOnboardingDialog THEN show the dialog `() {
        val recentTabs = listOf<RecentTab>(mockk())
        val settings: Settings = mockk()

        every { settings.hasShownHomeOnboardingDialog } returns false

        val state = HomeFragmentState(recentTabs = recentTabs)

        assertTrue(state.shouldShowHomeOnboardingDialog(settings))
    }

    @Test
    fun `GIVEN historyMetadata WHEN calling shouldShowHomeOnboardingDialog THEN show the dialog `() {
        val historyMetadata = listOf(HistoryMetadataGroup("title", emptyList()))
        val settings: Settings = mockk()

        every { settings.hasShownHomeOnboardingDialog } returns false

        val state = HomeFragmentState(historyMetadata = historyMetadata)

        assertTrue(state.shouldShowHomeOnboardingDialog(settings))
    }

    @Test
    fun `GIVEN pocketArticles WHEN calling shouldShowHomeOnboardingDialog THEN show the dialog `() {
        val pocketArticles = listOf(PocketRecommendedStory("", "", "", "", "", 0, 0))
        val settings: Settings = mockk()

        every { settings.hasShownHomeOnboardingDialog } returns false

        val state = HomeFragmentState(pocketStories = pocketArticles)

        assertTrue(state.shouldShowHomeOnboardingDialog(settings))
    }

    @Test
    fun `GIVEN the home onboading dialog has been shown before WHEN calling shouldShowHomeOnboardingDialog THEN DO NOT showthe dialog `() {
        val pocketArticles = listOf(PocketRecommendedStory("", "", "", "", "", 0, 0))
        val settings: Settings = mockk()

        every { settings.hasShownHomeOnboardingDialog } returns true

        val state = HomeFragmentState(pocketStories = pocketArticles)

        assertFalse(state.shouldShowHomeOnboardingDialog(settings))
    }

    @Test
    fun `GIVENs updates WHEN sections recentTabs, recentBookmarks, historyMetadata or pocketArticles are available THEN show the dialog`() {
        val interactor = mockk<SessionControlInteractor>(relaxed = true)
        val view = RecyclerView(testContext)
        val controller = SessionControlView(
            mockk(relaxed = true),
            view,
            mockk(relaxed = true),
            interactor,
            mockk(relaxed = true)
        )
        val recentTabs = listOf<RecentTab>(mockk(relaxed = true))

        val state = HomeFragmentState(recentTabs = recentTabs)

        controller.update(state)

        verify {
            interactor.showOnboardingDialog()
        }
    }

    @Test
    fun `GIVENs updates WHEN sections recentTabs, recentBookmarks, historyMetadata or pocketArticles are NOT available THEN DO NOT show the dialog`() {
        val interactor = mockk<SessionControlInteractor>(relaxed = true)
        val view = RecyclerView(testContext)
        val controller = SessionControlView(
            mockk(relaxed = true),
            view,
            mockk(relaxed = true),
            interactor,
            mockk(relaxed = true)
        )

        val state = HomeFragmentState()

        controller.update(state)

        verify(exactly = 0) {
            interactor.showOnboardingDialog()
        }
    }

    @Test
    fun `GIVEN recent Bookmarks WHEN normalModeAdapterItems is called THEN add a customize home button`() {
        val topSites = emptyList<TopSite>()
        val collections = emptyList<TabCollection>()
        val expandedCollections = emptySet<Long>()
        val recentBookmarks =
            listOf(BookmarkNode(BookmarkNodeType.ITEM, "guid", null, null, null, null, 0, null))
        val recentTabs = emptyList<RecentTab.Tab>()
        val historyMetadata = emptyList<HistoryMetadataGroup>()
        val pocketArticles = emptyList<PocketRecommendedStory>()
        val learnAndAct = emptyList<LearnAndAct>()
        val results = normalModeAdapterItems(
            topSites,
            collections,
            expandedCollections,
            null,
            recentBookmarks,
            false,
            false,
            recentTabs,
            historyMetadata,
            pocketArticles,
            learnAndAct
        )

        assertTrue(results[0] is AdapterItem.RecentBookmarks)
        assertTrue(results[1] is AdapterItem.CustomizeHomeButton)
    }

    @Test
    fun `GIVEN recent tabs WHEN normalModeAdapterItems is called THEN add a customize home button`() {
        val topSites = emptyList<TopSite>()
        val collections = emptyList<TabCollection>()
        val expandedCollections = emptySet<Long>()
        val recentBookmarks = listOf<BookmarkNode>()
        val recentTabs = listOf<RecentTab.Tab>(mockk())
        val historyMetadata = emptyList<HistoryMetadataGroup>()
        val pocketArticles = emptyList<PocketRecommendedStory>()
        val learnAndAct = emptyList<LearnAndAct>()

        val results = normalModeAdapterItems(
            topSites,
            collections,
            expandedCollections,
            null,
            recentBookmarks,
            false,
            false,
            recentTabs,
            historyMetadata,
            pocketArticles,
            learnAndAct
        )

        assertTrue(results[0] is AdapterItem.RecentTabsHeader)
        assertTrue(results[1] is AdapterItem.RecentTabItem)
        assertTrue(results[2] is AdapterItem.CustomizeHomeButton)
    }

    @Test
    fun `GIVEN history metadata WHEN normalModeAdapterItems is called THEN add a customize home button`() {
        val topSites = emptyList<TopSite>()
        val collections = emptyList<TabCollection>()
        val expandedCollections = emptySet<Long>()
        val recentBookmarks = listOf<BookmarkNode>()
        val recentTabs = emptyList<RecentTab.Tab>()
        val historyMetadata = listOf(HistoryMetadataGroup("title", emptyList()))
        val pocketArticles = emptyList<PocketRecommendedStory>()
        val learnAndAct = emptyList<LearnAndAct>()

        val results = normalModeAdapterItems(
            topSites,
            collections,
            expandedCollections,
            null,
            recentBookmarks,
            false,
            false,
            recentTabs,
            historyMetadata,
            pocketArticles,
            learnAndAct
        )

        assertTrue(results[0] is AdapterItem.HistoryMetadataHeader)
        assertTrue(results[1] is AdapterItem.HistoryMetadataGroup)
        assertTrue(results[2] is AdapterItem.CustomizeHomeButton)
    }

    @Test
    fun `GIVEN pocket articles WHEN normalModeAdapterItems is called THEN add a customize home button`() {
        val topSites = emptyList<TopSite>()
        val collections = emptyList<TabCollection>()
        val expandedCollections = emptySet<Long>()
        val recentBookmarks = listOf<BookmarkNode>()
        val recentTabs = emptyList<RecentTab.Tab>()
        val historyMetadata = emptyList<HistoryMetadataGroup>()
        val pocketArticles = listOf(PocketRecommendedStory("", "", "", "", "", 1, 1))
        val learnAndAct = emptyList<LearnAndAct>()

        val results = normalModeAdapterItems(
            topSites,
            collections,
            expandedCollections,
            null,
            recentBookmarks,
            false,
            false,
            recentTabs,
            historyMetadata,
            pocketArticles,
            learnAndAct
        )

        assertTrue(results[0] is AdapterItem.PocketStoriesItem)
        assertTrue(results[1] is AdapterItem.CustomizeHomeButton)
    }

    @Test
    fun `GIVEN none recentBookmarks,recentTabs, historyMetadata or pocketArticles WHEN normalModeAdapterItems is called THEN the customize home button is not added`() {
        val topSites = emptyList<TopSite>()
        val collections = emptyList<TabCollection>()
        val expandedCollections = emptySet<Long>()
        val recentBookmarks = listOf<BookmarkNode>()
        val recentTabs = emptyList<RecentTab.Tab>()
        val historyMetadata = emptyList<HistoryMetadataGroup>()
        val pocketArticles = emptyList<PocketRecommendedStory>()
        val learnAndAct = emptyList<LearnAndAct>()

        val results = normalModeAdapterItems(
            topSites,
            collections,
            expandedCollections,
            null,
            recentBookmarks,
            false,
            false,
            recentTabs,
            historyMetadata,
            pocketArticles,
            learnAndAct
        )
        assertTrue(results.isEmpty())
    }
}
