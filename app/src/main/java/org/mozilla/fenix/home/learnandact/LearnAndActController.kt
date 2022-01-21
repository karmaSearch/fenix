package org.mozilla.fenix.home.learnandact

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import karma.service.learnandact.LearnAndAct
import mozilla.components.lib.state.Store
import mozilla.components.service.pocket.PocketRecommendedStory
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.home.HomeFragmentAction
import org.mozilla.fenix.home.HomeFragmentStore
import org.mozilla.fenix.home.pocket.POCKET_CATEGORIES_SELECTED_AT_A_TIME_COUNT
import org.mozilla.fenix.home.pocket.PocketRecommendedStoriesCategory
import org.mozilla.fenix.home.pocket.PocketStoriesController

interface LearnAndActController {
    /**
     * Callback to decide what should happen as an effect of a new list of learnandact being shown.
     *
     * @param learnAndAct the new list of [LearnAndAct]es shown to the user.
     */
    fun handleLearnAndActShown(learnAndAct: List<LearnAndAct>)

    /**
     * Callback for when the user clicks on a specific blocs.
     *
     * @param storyClicked The just clicked [LearnAndAct] URL.
     * @param position `row x column` matrix representing the grid position of the clicked story.
     */
    fun handleLearnAndActClicked(learnAndAct: LearnAndAct, position: Pair<Int, Int>)

}

/**
 * Default behavior for handling all user interactions with the Pocket recommended stories feature.
 *
 * @param homeActivity [HomeActivity] used to open URLs in a new tab.
 * @param homeStore [Store] from which to read the current Pocket recommendations and dispatch new actions on.
 * @param navController [NavController] used for navigation.
 */
internal class DefaultLearnAndActController(
    private val homeActivity: HomeActivity,
    private val homeStore: HomeFragmentStore,
    private val navController: NavController
) : LearnAndActController {

    override fun handleLearnAndActShown(learnAndAct: List<LearnAndAct>) {
        homeStore.dispatch(HomeFragmentAction.LearnAndActShown(learnAndAct))
    }

    override fun handleLearnAndActClicked(learnAndAct: LearnAndAct, position: Pair<Int, Int>) {
        homeActivity.openToBrowserAndLoad(learnAndAct.actionUrl, false, BrowserDirection.FromHome)
    }
}
