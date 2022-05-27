package org.mozilla.fenix.home.onboarding

import androidx.navigation.NavController
import org.mozilla.fenix.settings.account.AccountSettingsFragmentStore

interface OnBoardingInteractor {
    fun showNextPage()
}

class OnBoardingInteractorImpl(
    private val showNextPage: () -> Unit
) : OnBoardingInteractor {

    override fun showNextPage() {
        showNextPage.invoke()
    }
}