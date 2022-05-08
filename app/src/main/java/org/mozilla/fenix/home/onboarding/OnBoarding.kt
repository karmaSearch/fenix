package org.mozilla.fenix.home.onboarding

import android.graphics.drawable.Drawable
import org.mozilla.fenix.R

data class OnBoarding(val title: Int,
val subTitle: Int,
val backgroundImage: Int,
val icon: Int,
val images: List<Int>,
val nextButtonTitle: Int = R.string.onboarding_skip)
