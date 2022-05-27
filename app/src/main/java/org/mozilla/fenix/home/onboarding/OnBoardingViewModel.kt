package org.mozilla.fenix.home.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import mozilla.components.support.locale.LocaleManager
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.library.history.History
import org.mozilla.fenix.settings.advanced.getSelectedLocale
import org.mozilla.fenix.settings.advanced.getSupportedLocales
import java.util.*

class OnBoardingViewModel : ViewModel() {
    var onBoardingPages: List<OnBoarding>

    init {

        onBoardingPages = if (LocaleManager.getSystemDefault().country == "FR") {
            listOf(OnBoarding(R.string.onboarding_title_1, R.string.onboarding_subtitle_1, R.drawable.onboarding_1, R.drawable.onboarding_illus_1, listOf(R.drawable.ic_logo_aspas, R.drawable.ic_l214, R.drawable.ic_naat)),
                OnBoarding(R.string.onboarding_title_2, R.string.onboarding_subtitle_2, R.drawable.onboarding_2, R.drawable.onboarding_illus_2, emptyList()),
                OnBoarding(R.string.onboarding_title_3, R.string.onboarding_subtitle_3, R.drawable.onboarding_3, R.drawable.onboarding_illus_3, emptyList(), R.string.onboarding_finish))

        } else {
            listOf(OnBoarding(R.string.onboarding_title_1, R.string.onboarding_subtitle_1, R.drawable.onboarding_1, R.drawable.onboarding_illus_1, emptyList()),
                OnBoarding(R.string.onboarding_title_2, R.string.onboarding_subtitle_2, R.drawable.onboarding_2, R.drawable.onboarding_illus_2, emptyList()),
                OnBoarding(R.string.onboarding_title_3, R.string.onboarding_subtitle_3, R.drawable.onboarding_3, R.drawable.onboarding_illus_3, emptyList(), R.string.onboarding_finish))
        }

      }

}
