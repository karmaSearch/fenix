/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.search.ext.buildSearchUrl
import mozilla.components.feature.search.ext.waitForSelectedOrDefaultSearchEngine
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.ui.tabcounter.TabCounterMenu
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.Config
import org.mozilla.fenix.FeatureFlags
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.BrowserAnimator.Companion.getToolbarNavOptions
import org.mozilla.fenix.browser.BrowserFragmentDirections
import org.mozilla.fenix.browser.browsingmode.BrowsingMode
import org.mozilla.fenix.components.Components
import org.mozilla.fenix.components.FenixSnackbar
import org.mozilla.fenix.components.PrivateShortcutCreateManager
import org.mozilla.fenix.components.StoreProvider
import org.mozilla.fenix.components.TabCollectionStorage
import org.mozilla.fenix.components.accounts.AccountState
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.tips.FenixTipManager
import org.mozilla.fenix.components.tips.Tip
import org.mozilla.fenix.components.tips.providers.MasterPasswordTipProvider
import org.mozilla.fenix.components.toolbar.FenixTabCounterMenu
import org.mozilla.fenix.components.toolbar.ToolbarPosition
import org.mozilla.fenix.databinding.FragmentHomeBinding
import org.mozilla.fenix.datastore.pocketStoriesSelectedCategoriesDataStore
import org.mozilla.fenix.experiments.FeatureId
import org.mozilla.fenix.ext.*
import org.mozilla.fenix.home.learnandact.DefaultLearnAndActController
import org.mozilla.fenix.home.mozonline.showPrivacyPopWindow
import org.mozilla.fenix.home.pocket.DefaultPocketStoriesController
import org.mozilla.fenix.home.pocket.PocketRecommendedStoriesCategory
import org.mozilla.fenix.home.recentbookmarks.RecentBookmarksFeature
import org.mozilla.fenix.home.recentbookmarks.controller.DefaultRecentBookmarksController
import org.mozilla.fenix.home.recenttabs.RecentTab
import org.mozilla.fenix.home.recenttabs.RecentTabsListFeature
import org.mozilla.fenix.home.recenttabs.controller.DefaultRecentTabsController
import org.mozilla.fenix.home.recentvisits.RecentVisitsFeature
import org.mozilla.fenix.home.recentvisits.controller.DefaultRecentVisitsController
import org.mozilla.fenix.home.sessioncontrol.DefaultSessionControlController
import org.mozilla.fenix.home.sessioncontrol.SessionControlInteractor
import org.mozilla.fenix.home.sessioncontrol.SessionControlView
import org.mozilla.fenix.home.sessioncontrol.viewholders.CollectionViewHolder
import org.mozilla.fenix.home.topsites.DefaultTopSitesView
import org.mozilla.fenix.onboarding.FenixOnboarding
import org.mozilla.fenix.perf.MarkersFragmentLifecycleCallbacks
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.settings.SupportUtils.SumoTopic.HELP
import org.mozilla.fenix.settings.deletebrowsingdata.deleteAndQuit
import org.mozilla.fenix.theme.ThemeManager
import org.mozilla.fenix.utils.ToolbarPopupWindow
import org.mozilla.fenix.utils.allowUndo
import org.mozilla.fenix.whatsnew.WhatsNew
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.min

@Suppress("TooManyFunctions", "LargeClass")
class HomeFragment : Fragment() {
    private val args by navArgs<HomeFragmentArgs>()
    private lateinit var bundleArgs: Bundle

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeScreenViewModel by activityViewModels()

    private val snackbarAnchorView: View?
        get() = when (requireContext().settings().toolbarPosition) {
            ToolbarPosition.BOTTOM -> binding.toolbarLayout
            ToolbarPosition.TOP -> null
        }

    private val browsingModeManager get() = (activity as HomeActivity).browsingModeManager

    private val collectionStorageObserver = object : TabCollectionStorage.Observer {
        @SuppressLint("NotifyDataSetChanged")
        override fun onCollectionRenamed(tabCollection: TabCollection, title: String) {
            lifecycleScope.launch(Main) {
                binding.sessionControlRecyclerView.adapter?.notifyDataSetChanged()
            }
            showRenamedSnackbar()
        }
    }

    private val store: BrowserStore
        get() = requireComponents.core.store

    private val onboarding by lazy {
        requireComponents.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
            FenixOnboarding(requireContext())
        }
    }

    private lateinit var homeFragmentStore: HomeFragmentStore
    private var _sessionControlInteractor: SessionControlInteractor? = null
    private val sessionControlInteractor: SessionControlInteractor
        get() = _sessionControlInteractor!!

    private var sessionControlView: SessionControlView? = null
    private var appBarLayout: AppBarLayout? = null
    private lateinit var currentMode: CurrentMode

    private val topSitesFeature = ViewBoundFeatureWrapper<TopSitesFeature>()
    private val recentTabsListFeature = ViewBoundFeatureWrapper<RecentTabsListFeature>()
    private val recentBookmarksFeature = ViewBoundFeatureWrapper<RecentBookmarksFeature>()
    private val historyMetadataFeature = ViewBoundFeatureWrapper<RecentVisitsFeature>()

    @VisibleForTesting
    internal var getMenuButton: () -> MenuButton? = { binding.menuButton }

    override fun onCreate(savedInstanceState: Bundle?) {
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        val profilerStartTime = requireComponents.core.engine.profiler?.getProfilerTime()

        super.onCreate(savedInstanceState)

        bundleArgs = args.toBundle()
        lifecycleScope.launch(IO) {
            if (!onboarding.userHasBeenOnboarded()) {
                requireComponents.analytics.metrics.track(Event.OpenedAppFirstRun)
            }
        }

        if (!onboarding.userHasBeenOnboarded() &&
            requireContext().settings().shouldShowPrivacyPopWindow &&
            Config.channel.isMozillaOnline
        ) {
            showPrivacyPopWindow(requireContext(), requireActivity())
        }

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        requireComponents.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME, profilerStartTime, "HomeFragment.onCreate",
        )
    }

    @Suppress("LongMethod")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        val profilerStartTime = requireComponents.core.engine.profiler?.getProfilerTime()

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val activity = activity as HomeActivity
        val components = requireComponents

        currentMode = CurrentMode(
            requireContext(),
            onboarding,
            browsingModeManager,
            ::dispatchModeChanges
        )

        homeFragmentStore = StoreProvider.get(this) {
            HomeFragmentStore(
                HomeFragmentState(
                    collections = components.core.tabCollectionStorage.cachedTabCollections,
                    expandedCollections = emptySet(),
                    mode = currentMode.getCurrentMode(),
                    topSites = components.core.topSitesStorage.cachedTopSites,
                    tip = components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
                        FenixTipManager(
                            listOf(
                                MasterPasswordTipProvider(
                                    requireContext(),
                                    ::navToSavedLogins,
                                    ::dismissTip
                                )
                            )
                        ).getTip()
                    },
                    recentBookmarks = emptyList(),
                    showCollectionPlaceholder = components.settings.showCollectionsPlaceholderOnHome,
                    showSetAsDefaultBrowserCard = components.settings.shouldShowSetAsDefaultBrowserCard(),
                    // Provide an initial state for recent tabs to prevent re-rendering on the home screen.
                    //  This will otherwise cause a visual jump as the section gets rendered from no state
                    //  to some state.
                    recentTabs = getRecentTabs(components),
                    recentHistory = emptyList()
                ),
                listOf(
                    PocketUpdatesMiddleware(
                        lifecycleScope,
                        requireComponents.core.pocketStoriesService,
                        requireContext().pocketStoriesSelectedCategoriesDataStore
                    )
                )
            )
        }

        lifecycleScope.launch(IO) {
            if (requireContext().settings().showPocketRecommendationsFeature) {
                val categories = components.core.pocketStoriesService.getStories()
                    .groupBy { story -> story.category }
                    .map { (category, stories) -> PocketRecommendedStoriesCategory(category, stories) }

                homeFragmentStore.dispatch(HomeFragmentAction.PocketStoriesCategoriesChange(categories))
            } else {
                homeFragmentStore.dispatch(HomeFragmentAction.PocketStoriesChange(emptyList()))
            }
        }

        lifecycleScope.launch(IO) {
            if (requireContext().settings().showLearnAndAct) {
                val blocs = components.core.learnAndActService.getLearnAndAct()

                homeFragmentStore.dispatch(HomeFragmentAction.LearnAndActShown(blocs))
            } else {
                homeFragmentStore.dispatch(HomeFragmentAction.LearnAndActShown(kotlin.collections.emptyList()))
            }

        }

        components.core.store.waitForSelectedOrDefaultSearchEngine {
            topSitesFeature.set(
                feature = TopSitesFeature(
                    view = DefaultTopSitesView(homeFragmentStore),
                    storage = components.core.topSitesStorage,
                    config = ::getTopSitesConfig,
                    searchEngineStartURL = it?.let { it.buildSearchUrl("").split("&")[0] }
                ),
                owner = viewLifecycleOwner,
                view = binding.root
            )
        }

        if (requireContext().settings().showRecentTabsFeature) {
            recentTabsListFeature.set(
                feature = RecentTabsListFeature(
                    browserStore = components.core.store,
                    homeStore = homeFragmentStore
                ),
                owner = viewLifecycleOwner,
                view = binding.root
            )
        }

        if (requireContext().settings().showRecentBookmarksFeature) {
            recentBookmarksFeature.set(
                feature = RecentBookmarksFeature(
                    homeStore = homeFragmentStore,
                    bookmarksUseCase = run {
                        requireContext().components.useCases.bookmarksUseCases
                    },
                    scope = viewLifecycleOwner.lifecycleScope
                ),
                owner = viewLifecycleOwner,
                view = binding.root
            )
        }

        if (requireContext().settings().historyMetadataUIFeature) {
            historyMetadataFeature.set(
                feature = RecentVisitsFeature(
                    homeStore = homeFragmentStore,
                    historyMetadataStorage = components.core.historyStorage,
                    historyHighlightsStorage = components.core.lazyHistoryStorage,
                    scope = viewLifecycleOwner.lifecycleScope
                ),
                owner = viewLifecycleOwner,
                view = binding.root
            )
        }

        _sessionControlInteractor = SessionControlInteractor(
            controller = DefaultSessionControlController(
                activity = activity,
                settings = components.settings,
                engine = components.core.engine,
                metrics = components.analytics.metrics,
                store = store,
                tabCollectionStorage = components.core.tabCollectionStorage,
                addTabUseCase = components.useCases.tabsUseCases.addTab,
                restoreUseCase = components.useCases.tabsUseCases.restore,
                reloadUrlUseCase = components.useCases.sessionUseCases.reload,
                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
                fragmentStore = homeFragmentStore,
                navController = findNavController(),
                viewLifecycleScope = viewLifecycleOwner.lifecycleScope,
                hideOnboarding = ::hideOnboardingAndOpenSearch,
                registerCollectionStorageObserver = ::registerCollectionStorageObserver,
                removeCollectionWithUndo = ::removeCollectionWithUndo,
                showTabTray = ::openTabsTray
            ),
            recentTabController = DefaultRecentTabsController(
                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
                navController = findNavController(),
                metrics = requireComponents.analytics.metrics,
                store = components.core.store
            ),
            recentBookmarksController = DefaultRecentBookmarksController(
                activity = activity,
                navController = findNavController()
            ),
            recentVisitsController = DefaultRecentVisitsController(
                navController = findNavController(),
                homeStore = homeFragmentStore,
                selectOrAddTabUseCase = components.useCases.tabsUseCases.selectOrAddTab,
                storage = components.core.historyStorage,
                scope = viewLifecycleOwner.lifecycleScope,
                store = components.core.store,
                metrics = requireComponents.analytics.metrics
            ),
            pocketStoriesController = DefaultPocketStoriesController(
                homeActivity = activity,
                homeStore = homeFragmentStore,
                navController = findNavController(),
                metrics = requireComponents.analytics.metrics
            ),
            learnAndActController = DefaultLearnAndActController(
                homeActivity = activity,
                homeStore = homeFragmentStore,
                navController = findNavController()
            )
        )

        updateLayout()
        sessionControlView = SessionControlView(
            homeFragmentStore,
            binding.sessionControlRecyclerView,
            binding.toolbarWrapper,
            viewLifecycleOwner,
            sessionControlInteractor
        )

        updateSessionControlView()

        appBarLayout = binding.homeAppBar
        val appBarOffsetChangedListener = object : AppBarLayout.OnOffsetChangedListener {

            override fun onOffsetChanged(
                appBarLayout: com.google.android.material.appbar.AppBarLayout,
                verticalOffset: kotlin.Int
            ) {
                if (_binding != null) {
                    val toolBarHeight = binding.toolbarWrapper.height / 2

                    if (abs(verticalOffset - toolBarHeight) >= appBarLayout.height) {
                        binding.toolbarWrapper2.visibility = View.VISIBLE
                        binding.toolbarWrapper.visibility = View.INVISIBLE
                    } else {
                        binding.toolbarWrapper2.visibility = View.INVISIBLE
                        binding.toolbarWrapper.visibility = View.VISIBLE
                    }
                    if (requireContext().settings().showKARMAPicture) {
                        when (requireContext().settings().toolbarPosition) {
                            ToolbarPosition.TOP -> {
                                binding.karmaLogo.visibility =
                                    if (abs(verticalOffset - binding.toolbarWrapper.height) >= appBarLayout.height - binding.randomAnimalsImage.height - binding.karmaLogo.height / 2) View.INVISIBLE else View.VISIBLE
                            }
                            ToolbarPosition.BOTTOM -> {
                                binding.karmaLogo.visibility = binding.toolbarWrapper.visibility
                            }
                        }
                    }

                }
            }
        }

        binding.homeAppBar.addOnOffsetChangedListener(appBarOffsetChangedListener)

        activity.themeManager.applyStatusBarTheme(activity)

        requireContext().components.analytics.experiments.recordExposureEvent(FeatureId.HOME_PAGE)

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        requireComponents.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME, profilerStartTime, "HomeFragment.onCreateView",
        )

        if (shouldEnableWallpaper()) {
            val wallpaperManger = requireComponents.wallpaperManager
            wallpaperManger.updateWallpaper(binding.homeLayout, wallpaperManger.currentWallpaper)
        }

        if (requireContext().settings().shouldShowAddWidgetCard()) {
            nav(
                R.id.homeFragment,
                HomeFragmentDirections.actionGlobalHomeAddWidget()
            )
        }
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        getMenuButton()?.dismissMenu()
    }

    private fun dismissTip(tip: Tip) {
        sessionControlInteractor.onCloseTip(tip)
    }

    /**
     * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
     * not frequently visited sites should be displayed.
     */
    @VisibleForTesting
    internal fun getTopSitesConfig(): TopSitesConfig {
        val settings = requireContext().settings()
        return TopSitesConfig(
            settings.topSitesMaxLimit,
            if (settings.showTopFrecentSites) FrecencyThresholdOption.SKIP_ONE_TIME_PAGES else null
        )
    }

    /**
     * The [SessionControlView] is forced to update with our current state when we call
     * [HomeFragment.onCreateView] in order to be able to draw everything at once with the current
     * data in our store. The [View.consumeFrom] coroutine dispatch
     * doesn't get run right away which means that we won't draw on the first layout pass.
     */
    private fun updateSessionControlView() {
        if (browsingModeManager.mode == BrowsingMode.Private) {
            binding.root.consumeFrom(homeFragmentStore, viewLifecycleOwner) {
                sessionControlView?.update(it)
            }
        } else {
            sessionControlView?.update(homeFragmentStore.state)

            binding.root.consumeFrom(homeFragmentStore, viewLifecycleOwner) {
                sessionControlView?.update(it, shouldReportMetrics = true)
            }
        }
    }

    private fun updateLayout() {
        when (requireContext().settings().toolbarPosition) {
            ToolbarPosition.TOP -> {
                binding.toolbarLayout.layoutParams = CoordinatorLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.TOP
                }

                ConstraintSet().apply {
                    clone(binding.toolbarLayout)
                    clear(binding.bottomBar.id, BOTTOM)
                    //clear(binding.bottomBarShadow.id, BOTTOM)
                    connect(binding.bottomBar.id, TOP, PARENT_ID, TOP)
                    //connect(binding.bottomBarShadow.id, TOP, binding.bottomBar.id, BOTTOM)
                    //connect(binding.bottomBarShadow.id, BOTTOM, PARENT_ID, BOTTOM)
                    applyTo(binding.toolbarLayout)
                }

                binding.homeAppBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin =
                        resources.getDimensionPixelSize(R.dimen.home_fragment_top_toolbar_header_margin)
                }
                binding.toolbarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
            ToolbarPosition.BOTTOM -> {
                binding.toolbarLayout.setBackgroundColor(requireContext().getColor(R.color.fx_mobile_layer_color_1))
            }
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        val profilerStartTime = requireComponents.core.engine.profiler?.getProfilerTime()

        super.onViewCreated(view, savedInstanceState)
        context?.metrics?.apply {
            track(Event.HomeScreenDisplayed)
            track(Event.HomeScreenViewCount)
        }

        createHomeMenu(requireContext(), WeakReference(binding.menuButton))
        createTabCounterMenu()

        binding.menuButton.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                ThemeManager.resolveAttribute(R.attr.primaryText, requireContext())
            )
        )

        binding.toolbar.compoundDrawablePadding =
            view.resources.getDimensionPixelSize(R.dimen.search_bar_search_engine_icon_padding)

        val toolbarWrapperOnClickListener = View.OnClickListener {
            navigateToSearch()
            requireComponents.analytics.metrics.track(Event.SearchBarTapped(Event.SearchBarTapped.Source.HOME))
        }
        val toolbarWrapperOnLongClickListener = View.OnLongClickListener {
            ToolbarPopupWindow.show(
                WeakReference(it),
                handlePasteAndGo = sessionControlInteractor::onPasteAndGo,
                handlePaste = sessionControlInteractor::onPaste,
                copyVisible = false
            )
            true
        }
        binding.toolbarWrapper.setOnClickListener(toolbarWrapperOnClickListener)
        binding.toolbarWrapper2.setOnClickListener(toolbarWrapperOnClickListener)

        binding.toolbarWrapper.setOnLongClickListener(toolbarWrapperOnLongClickListener)
        binding.toolbarWrapper2.setOnLongClickListener(toolbarWrapperOnLongClickListener)

        binding.tabButton.setOnClickListener {
            if (FeatureFlags.showStartOnHomeSettings) {
                requireComponents.analytics.metrics.track(Event.StartOnHomeOpenTabsTray)
            }
            openTabsTray()
        }

        consumeFrom(requireComponents.core.store) {
            updateTabCounter(it)
        }

        homeViewModel.sessionToDelete?.also {
            if (it == ALL_NORMAL_TABS || it == ALL_PRIVATE_TABS) {
                removeAllTabsAndShowSnackbar(it)
            } else {
                removeTabAndShowSnackbar(it)
            }
        }

        homeViewModel.sessionToDelete = null

        updateTabCounter(requireComponents.core.store.state)

        if (bundleArgs.getBoolean(FOCUS_ON_ADDRESS_BAR)) {
            navigateToSearch()
        } else if (bundleArgs.getLong(FOCUS_ON_COLLECTION, -1) >= 0) {
            /* Triggered when the user has added a tab to a collection and has tapped
            * the View action on the [TabsTrayDialogFragment] snackbar.*/
            scrollAndAnimateCollection(bundleArgs.getLong(FOCUS_ON_COLLECTION, -1))
        }

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        requireComponents.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME, profilerStartTime, "HomeFragment.onViewCreated",
        )
    }

    fun updatePosition(expanded: Boolean) {
        val recyclerView = sessionControlView!!.view

        if (!recyclerView.hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
            recyclerView.startNestedScroll(View.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
        }
        binding.homeAppBar.setExpanded(expanded)
    }

    private fun createTabCounterMenu() {
        val browsingModeManager = (activity as HomeActivity).browsingModeManager
        val mode = browsingModeManager.mode

        val onItemTapped: (TabCounterMenu.Item) -> Unit = {
            if (it is TabCounterMenu.Item.NewTab) {
                browsingModeManager.mode = BrowsingMode.Normal
            } else if (it is TabCounterMenu.Item.NewPrivateTab) {
                browsingModeManager.mode = BrowsingMode.Private
            }
        }

        val tabCounterMenu = FenixTabCounterMenu(
            requireContext(),
            onItemTapped,
            iconColor = if (mode == BrowsingMode.Private) {
                ContextCompat.getColor(requireContext(), R.color.fx_mobile_private_text_color_primary)
            } else {
                null
            }
        )

        val inverseBrowsingMode = when (mode) {
            BrowsingMode.Normal -> BrowsingMode.Private
            BrowsingMode.Private -> BrowsingMode.Normal
        }

        tabCounterMenu.updateMenu(showOnly = inverseBrowsingMode)
        binding.tabButton.setOnLongClickListener {
            tabCounterMenu.menuController.show(anchor = it)
            true
        }
    }

    private fun removeAllTabsAndShowSnackbar(sessionCode: String) {
        if (sessionCode == ALL_PRIVATE_TABS) {
            requireComponents.useCases.tabsUseCases.removePrivateTabs()
        } else {
            requireComponents.useCases.tabsUseCases.removeNormalTabs()
        }

        val snackbarMessage = if (sessionCode == ALL_PRIVATE_TABS) {
            getString(R.string.snackbar_private_tabs_closed)
        } else {
            getString(R.string.snackbar_tabs_closed)
        }

        viewLifecycleOwner.lifecycleScope.allowUndo(
            requireView(),
            snackbarMessage,
            requireContext().getString(R.string.snackbar_deleted_undo),
            {
                requireComponents.useCases.tabsUseCases.undo.invoke()
            },
            operation = { },
            anchorView = snackbarAnchorView
        )
    }

    private fun removeTabAndShowSnackbar(sessionId: String) {
        val tab = store.state.findTab(sessionId) ?: return

        requireComponents.useCases.tabsUseCases.removeTab(sessionId)

        val snackbarMessage = if (tab.content.private) {
            requireContext().getString(R.string.snackbar_private_tab_closed)
        } else {
            requireContext().getString(R.string.snackbar_tab_closed)
        }

        viewLifecycleOwner.lifecycleScope.allowUndo(
            requireView(),
            snackbarMessage,
            requireContext().getString(R.string.snackbar_deleted_undo),
            {
                requireComponents.useCases.tabsUseCases.undo.invoke()
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalBrowser(null)
                )
            },
            operation = { },
            anchorView = snackbarAnchorView
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _sessionControlInteractor = null
        sessionControlView = null
        appBarLayout = null
        _binding = null
        bundleArgs.clear()
    }

    override fun onStart() {
        super.onStart()

        subscribeToRandomAnimalBackground()
        subscribeToTabCollections()

        val context = requireContext()
        val components = context.components

        homeFragmentStore.dispatch(
            HomeFragmentAction.Change(
                collections = components.core.tabCollectionStorage.cachedTabCollections,
                mode = currentMode.getCurrentMode(),
                topSites = components.core.topSitesStorage.cachedTopSites,
                tip = components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
                    FenixTipManager(
                        listOf(
                            MasterPasswordTipProvider(
                                requireContext(),
                                ::navToSavedLogins,
                                ::dismissTip
                            )
                        )
                    ).getTip()
                },
                showCollectionPlaceholder = components.settings.showCollectionsPlaceholderOnHome,
                // Provide an initial state for recent tabs to prevent re-rendering on the home screen.
                //  This will otherwise cause a visual jump as the section gets rendered from no state
                //  to some state.
                recentTabs = getRecentTabs(components),
                recentBookmarks = emptyList(),
                recentHistory = emptyList()
            )
        )

        requireComponents.backgroundServices.accountManagerAvailableQueue.runIfReadyOrQueue {
            // By the time this code runs, we may not be attached to a context or have a view lifecycle owner.
            if ((this@HomeFragment).view?.context == null) {
                return@runIfReadyOrQueue
            }

            requireComponents.backgroundServices.accountManager.register(
                currentMode,
                owner = this@HomeFragment.viewLifecycleOwner
            )
            requireComponents.backgroundServices.accountManager.register(
                object : AccountObserver {
                    override fun onAuthenticated(account: OAuthAccount, authType: AuthType) {
                        if (authType != AuthType.Existing) {
                            view?.let {
                                FenixSnackbar.make(
                                    view = it,
                                    duration = Snackbar.LENGTH_SHORT,
                                    isDisplayedWithBrowserToolbar = false
                                )
                                    .setText(it.context.getString(R.string.onboarding_firefox_account_sync_is_on))
                                    .setAnchorView(binding.toolbarLayout)
                                    .show()
                            }
                        }
                    }
                },
                owner = this@HomeFragment.viewLifecycleOwner
            )
        }

        if (browsingModeManager.mode.isPrivate &&
            // We will be showing the search dialog and don't want to show the CFR while the dialog shows
            !bundleArgs.getBoolean(FOCUS_ON_ADDRESS_BAR) &&
            context.settings().shouldShowPrivateModeCfr
        ) {
            recommendPrivateBrowsingShortcut()
        }

        // We only want this observer live just before we navigate away to the collection creation screen
        requireComponents.core.tabCollectionStorage.unregister(collectionStorageObserver)

        lifecycleScope.launch(IO) {
            requireComponents.reviewPromptController.promptReview(requireActivity())
        }

        if (shouldEnableWallpaper()) {
            binding.wordmark.setOnClickListener {
                val manager = requireComponents.wallpaperManager
                manager.updateWallpaper(
                    wallpaperContainer = binding.homeLayout,
                    newWallpaper = manager.switchToNextWallpaper()
                )
            }
        }

    }

    private fun navToSavedLogins() {
        findNavController().navigate(
            HomeFragmentDirections.actionGlobalSavedLoginsAuthFragment()
        )
    }

    private fun dispatchModeChanges(mode: Mode) {
        if (mode != Mode.fromBrowsingMode(browsingModeManager.mode)) {
            homeFragmentStore.dispatch(HomeFragmentAction.ModeChange(mode))
        }
    }

    @VisibleForTesting
    internal fun removeCollectionWithUndo(tabCollection: TabCollection) {
        val snackbarMessage = getString(R.string.snackbar_collection_deleted)

        lifecycleScope.allowUndo(
            requireView(),
            snackbarMessage,
            getString(R.string.snackbar_deleted_undo),
            {
                requireComponents.core.tabCollectionStorage.createCollection(tabCollection)
            },
            operation = { },
            elevation = TOAST_ELEVATION,
            anchorView = null
        )

        lifecycleScope.launch(IO) {
            requireComponents.core.tabCollectionStorage.removeCollection(tabCollection)
        }
    }

    override fun onResume() {
        super.onResume()
        if (browsingModeManager.mode == BrowsingMode.Private) {
            activity?.window?.setBackgroundDrawableResource(R.drawable.private_home_background_gradient)
        }

        hideToolbar()

        // Whenever a tab is selected its last access timestamp is automatically updated by A-C.
        // However, in the case of resuming the app to the home fragment, we already have an
        // existing selected tab, but its last access timestamp is outdated. No action is
        // triggered to cause an automatic update on warm start (no tab selection occurs). So we
        // update it manually here.
        requireComponents.useCases.sessionUseCases.updateLastAccess()
    }

    override fun onPause() {
        super.onPause()
        if (browsingModeManager.mode == BrowsingMode.Private) {
            activity?.window?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.foundation_private_theme
                    )
                )
            )
        }

        // Counterpart to the update in onResume to keep the last access timestamp of the selected
        // tab up-to-date.
        requireComponents.useCases.sessionUseCases.updateLastAccess()
    }

    private fun recommendPrivateBrowsingShortcut() {
        context?.let { context ->
            val layout = LayoutInflater.from(context)
                .inflate(R.layout.pbm_shortcut_popup, null)
            val privateBrowsingRecommend =
                PopupWindow(
                    layout,
                    min(
                        (resources.displayMetrics.widthPixels / CFR_WIDTH_DIVIDER).toInt(),
                        (resources.displayMetrics.heightPixels / CFR_WIDTH_DIVIDER).toInt()
                    ),
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                )
            layout.findViewById<Button>(R.id.cfr_pos_button).apply {
                setOnClickListener {
                    PrivateShortcutCreateManager.createPrivateShortcut(context)
                    privateBrowsingRecommend.dismiss()
                }
            }
            layout.findViewById<Button>(R.id.cfr_neg_button).apply {
                setOnClickListener {
                    privateBrowsingRecommend.dismiss()
                }
            }

        }
    }

    private fun hideOnboardingIfNeeded() {
        if (!onboarding.userHasBeenOnboarded()) {
            onboarding.finish()
            homeFragmentStore.dispatch(
                HomeFragmentAction.ModeChange(
                    mode = currentMode.getCurrentMode()
                )
            )
        }
    }

    private fun hideOnboardingAndOpenSearch() {
        hideOnboardingIfNeeded()
        appBarLayout?.setExpanded(true, true)
        navigateToSearch()
    }

    private fun navigateToSearch() {
        // Dismisses the search dialog when the home content is scrolled
        val recyclerView = sessionControlView!!.view
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    findNavController().navigateUp()
                    recyclerView.removeOnScrollListener(this)
                    if (!recyclerView.hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
                        recyclerView.startNestedScroll(View.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
                    }
                    binding.homeAppBar.setExpanded(true)
                }
            }
        }
        if (!recyclerView.hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
            recyclerView.startNestedScroll(View.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
        }
        binding.homeAppBar.setExpanded(false)

        recyclerView.addOnScrollListener(listener)

        val directions =
            HomeFragmentDirections.actionGlobalSearchDialog(
                sessionId = null
            )

        nav(R.id.homeFragment, directions, getToolbarNavOptions(requireContext()))
    }

    @SuppressWarnings("ComplexMethod", "LongMethod")
    private fun createHomeMenu(context: Context, menuButtonView: WeakReference<MenuButton>) =
        HomeMenu(
            context,
            onItemTapped = {
                if (it !is HomeMenu.Item.DesktopMode) {
                    hideOnboardingIfNeeded()
                }

                when (it) {
                    HomeMenu.Item.NewTab -> {
                        hideOnboardingIfNeeded()
                        browsingModeManager.mode = BrowsingMode.fromBoolean(false)
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalHome(focusOnAddressBar = true)
                        )
                    }
                    HomeMenu.Item.NewPrivateTab -> {
                        hideOnboardingIfNeeded()
                        browsingModeManager.mode = BrowsingMode.fromBoolean(true)
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalHome(focusOnAddressBar = true)
                        )
                    }
                    HomeMenu.Item.SetDefaultBrowser -> {
                        activity?.openSetDefaultBrowserOption()
                    }
                    HomeMenu.Item.Settings -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalSettingsFragment()
                        )
                        requireComponents.analytics.metrics.track(Event.HomeMenuSettingsItemClicked)
                    }
                    HomeMenu.Item.CustomizeHome -> {
                        context.metrics.track(Event.HomeScreenCustomizedHomeClicked)
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalHomeSettingsFragment()
                        )
                    }
                    is HomeMenu.Item.SyncAccount -> {
                        val directions = when (it.accountState) {
                            AccountState.AUTHENTICATED ->
                                BrowserFragmentDirections.actionGlobalAccountSettingsFragment()
                            AccountState.NEEDS_REAUTHENTICATION ->
                                BrowserFragmentDirections.actionGlobalAccountProblemFragment()
                            AccountState.NO_ACCOUNT ->
                                BrowserFragmentDirections.actionGlobalTurnOnSync()
                        }
                        nav(
                            R.id.homeFragment,
                            directions
                        )
                    }
                    HomeMenu.Item.Bookmarks -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalBookmarkFragment(BookmarkRoot.Mobile.id)
                        )
                    }
                    HomeMenu.Item.History -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalHistoryFragment()
                        )
                    }
                    HomeMenu.Item.Downloads -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalDownloadsFragment()
                        )
                    }
                    HomeMenu.Item.Help -> {
                        (activity as HomeActivity).openToBrowserAndLoad(
                            searchTermOrURL = SupportUtils.getSumoURLForTopic(context, HELP),
                            newTab = true,
                            from = BrowserDirection.FromHome
                        )
                    }
                    HomeMenu.Item.WhatsNew -> {
                        WhatsNew.userViewedWhatsNew(context)
                        context.metrics.track(Event.WhatsNewTapped)
                        (activity as HomeActivity).openToBrowserAndLoad(
                            searchTermOrURL = SupportUtils.getWhatsNewUrl(context),
                            newTab = true,
                            from = BrowserDirection.FromHome
                        )
                    }
                    // We need to show the snackbar while the browsing data is deleting(if "Delete
                    // browsing data on quit" is activated). After the deletion is over, the snackbar
                    // is dismissed.
                    HomeMenu.Item.Quit -> activity?.let { activity ->
                        deleteAndQuit(
                            activity,
                            viewLifecycleOwner.lifecycleScope,
                            view?.let { view ->
                                FenixSnackbar.make(
                                    view = view,
                                    isDisplayedWithBrowserToolbar = false
                                )
                            }
                        )
                    }
                    HomeMenu.Item.ReconnectSync -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalAccountProblemFragment()
                        )
                    }
                    HomeMenu.Item.Feedback -> {
                        nav(
                            R.id.homeFragment,
                            HomeFragmentDirections.actionGlobalFeedbackFragment()
                        )
                    }
                    is HomeMenu.Item.DesktopMode -> {
                        context.settings().openNextTabInDesktopMode = it.checked
                    }
                }
            },
            onHighlightPresent = { menuButtonView.get()?.setHighlight(it) },
            onMenuBuilderChanged = { menuButtonView.get()?.menuBuilder = it }
        )

    private fun subscribeToTabCollections(): Observer<List<TabCollection>> {
        return Observer<List<TabCollection>> {
            requireComponents.core.tabCollectionStorage.cachedTabCollections = it
            homeFragmentStore.dispatch(HomeFragmentAction.CollectionsChange(it))
        }.also { observer ->
            requireComponents.core.tabCollectionStorage.getCollections().observe(this, observer)
        }
    }

    private fun subscribeToRandomAnimalBackground() {
        if (requireContext().settings().showKARMAPicture) {
            val randomAnimal =
                requireComponents.core.randomAnimalBackgroundService.getRandomAnimals()

            randomAnimal?.let {
                val resource =
                    resources.getIdentifier(it.imageName, "drawable", requireActivity().packageName)
                binding.randomAnimalsImage.setImageResource(resource)
                binding.randomAnimalsCreditInfo.text = it.infoText + " "
                binding.randomAnimalsCreditAuthor.text = it.author

                val onClickOnImage = object : View.OnClickListener {
                    override fun onClick(v: android.view.View) {

                        if (v == binding.randomAnimalsCreditLayout) {
                            (activity as HomeActivity).openToBrowserAndLoad(it.url, true, BrowserDirection.FromHome)
                        }
                    }
                }

                binding.randomAnimalsImage.setOnClickListener {
                    binding.randomAnimalsCreditLayout.visibility = if(binding.randomAnimalsCreditLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }

                binding.randomAnimalsCreditLayout.setOnClickListener(onClickOnImage)
            }
            binding.randomAnimalsLayout.visibility = View.VISIBLE
            binding.wordmark.visibility = View.GONE
            binding.karmaLogo.visibility = View.VISIBLE


        } else {
            binding.randomAnimalsLayout.visibility = View.GONE
            binding.wordmark.visibility = View.VISIBLE
            binding.karmaLogo.visibility = View.GONE
        }
    }

    private fun registerCollectionStorageObserver() {
        requireComponents.core.tabCollectionStorage.register(collectionStorageObserver, this)
    }

    /**
     * This method will find and scroll to the row of the specified collection Id.
     * */
    private fun scrollAndAnimateCollection(
        collectionIdToSelect: Long = -1
    ) {
        if (view != null && collectionIdToSelect >= 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                val recyclerView = sessionControlView!!.view
                delay(ANIM_SCROLL_DELAY)
                val indexOfCollection =
                    NON_COLLECTION_ITEM_NUM + findIndexOfSpecificCollection(collectionIdToSelect)

                val lastVisiblePosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                        ?: 0

                if (lastVisiblePosition < indexOfCollection) {
                    val onScrollListener = object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == SCROLL_STATE_IDLE) {
                                updatePosition(false)
                                animateCollection(indexOfCollection)
                                recyclerView.removeOnScrollListener(this)
                            }
                        }
                    }
                    recyclerView.addOnScrollListener(onScrollListener)
                    recyclerView.smoothScrollToPosition(indexOfCollection)
                } else {
                    updatePosition(false)
                    animateCollection(indexOfCollection)
                }
            }
        }
    }

    /**
     * Returns index of the collection with the specified id.
     * */
    private fun findIndexOfSpecificCollection(
        changedCollectionId: Long
    ): Int {
        var result = 0
        requireComponents.core.tabCollectionStorage.cachedTabCollections
            .filterIndexed { index, tabCollection ->
                if (tabCollection.id == changedCollectionId) {
                    result = index
                    return@filterIndexed true
                }
                false
            }
        return result
    }

    /**
     * Will highlight the border of the collection with the specified index.
     * */
    private fun animateCollection(indexOfCollection: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val viewHolder =
                sessionControlView!!.view.findViewHolderForAdapterPosition(indexOfCollection)
            val border =
                (viewHolder as? CollectionViewHolder)?.itemView?.findViewById<View>(R.id.selected_border)
            val listener = object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    border?.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animator?) { /* noop */
                }

                override fun onAnimationRepeat(animation: Animator?) { /* noop */
                }

                override fun onAnimationEnd(animation: Animator?) {
                    border?.animate()?.alpha(0.0F)?.setStartDelay(ANIM_ON_SCREEN_DELAY)
                        ?.setDuration(FADE_ANIM_DURATION)
                        ?.start()
                }
            }
            border?.animate()?.alpha(1.0F)?.setStartDelay(ANIM_ON_SCREEN_DELAY)
                ?.setDuration(FADE_ANIM_DURATION)
                ?.setListener(listener)?.start()
        }.invokeOnCompletion {
            val a11yEnabled = context?.settings()?.accessibilityServicesEnabled ?: false
            if (a11yEnabled) {
                focusCollectionForTalkBack(indexOfCollection)
            }
        }
    }

    /**
     * Will focus the collection with [indexOfCollection] for accessibility services.
     * */
    private fun focusCollectionForTalkBack(indexOfCollection: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            var focusedForAccessibility = false
            view?.let { mainView ->
                mainView.accessibilityDelegate = object : AccessibilityDelegate() {
                    override fun onRequestSendAccessibilityEvent(
                        host: ViewGroup,
                        child: View,
                        event: AccessibilityEvent
                    ): Boolean {
                        if (!focusedForAccessibility &&
                            event.eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                        ) {
                            sessionControlView?.view?.findViewHolderForAdapterPosition(
                                indexOfCollection
                            )?.itemView?.let { viewToFocus ->
                                focusedForAccessibility = true
                                viewToFocus.requestFocus()
                                viewToFocus.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                                return false
                            }
                        }
                        return super.onRequestSendAccessibilityEvent(host, child, event)
                    }
                }
            }
        }
    }

    private fun showRenamedSnackbar() {
        view?.let { view ->
            val string = view.context.getString(R.string.snackbar_collection_renamed)
            FenixSnackbar.make(
                view = view,
                duration = Snackbar.LENGTH_LONG,
                isDisplayedWithBrowserToolbar = false
            )
                .setText(string)
                .setAnchorView(snackbarAnchorView)
                .show()
        }
    }

    private fun openTabsTray() {
        findNavController().nav(
            R.id.homeFragment,
            HomeFragmentDirections.actionGlobalTabsTrayFragment()
        )
    }

    // TODO use [FenixTabCounterToolbarButton] instead of [TabCounter]:
    // https://github.com/mozilla-mobile/fenix/issues/16792
    private fun updateTabCounter(browserState: BrowserState) {
        val tabCount = if (browsingModeManager.mode.isPrivate) {
            browserState.privateTabs.size
        } else {
            browserState.normalTabs.size
        }

        binding.tabButton.setCountWithAnimation(tabCount)
        // The add_tabs_to_collections_button is added at runtime. We need to search for it in the same way.
        sessionControlView?.view?.findViewById<MaterialButton>(R.id.add_tabs_to_collections_button)
            ?.isVisible = tabCount > 0
    }

    private fun getRecentTabs(components: Components): List<RecentTab> {
        return if (components.settings.showRecentTabsFeature) {
            components.core.store.state.asRecentTabs()
        } else {
            emptyList()
        }
    }

    private fun shouldEnableWallpaper() =
        FeatureFlags.showWallpapers && !(activity as HomeActivity).themeManager.currentTheme.isPrivate

    companion object {
        const val ALL_NORMAL_TABS = "all_normal"
        const val ALL_PRIVATE_TABS = "all_private"

        private const val FOCUS_ON_ADDRESS_BAR = "focusOnAddressBar"
        private const val FOCUS_ON_COLLECTION = "focusOnCollection"

        /**
         * Represents the number of items in [sessionControlView] that are NOT part of
         * the list of collections. At the moment these are topSites pager, collections header.
         * */
        private const val NON_COLLECTION_ITEM_NUM = 2

        private const val ANIM_SCROLL_DELAY = 100L
        private const val ANIM_ON_SCREEN_DELAY = 200L
        private const val FADE_ANIM_DURATION = 150L
        private const val CFR_WIDTH_DIVIDER = 1.7
        private const val CFR_Y_OFFSET = -20

        // Elevation for undo toasts
        internal const val TOAST_ELEVATION = 80f
    }
}
