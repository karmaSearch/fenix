package org.mozilla.fenix.home.onboarding

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.OnBoardingItemBinding
import org.mozilla.fenix.utils.view.ViewHolder
import android.widget.LinearLayout
import androidx.compose.ui.unit.dp
import androidx.core.view.setMargins


class OnBoardingPagerViewHolder(
    view: View,
    private val interactor: OnBoardingInteractor
) : ViewHolder(view) {
    private lateinit var onboarding: OnBoarding
    private val binding = OnBoardingItemBinding.bind(view)

    fun bind(onboarding: OnBoarding) {
        binding.onboardingTitle.text = itemView.context.getString(onboarding.title)
        binding.onboardingSubTitle.text = itemView.context.getString(onboarding.subTitle)
        binding.onboardingBackground.setImageDrawable(itemView.context.getDrawable(onboarding.backgroundImage))
        binding.onboardingIcon.setImageDrawable(itemView.context.getDrawable(onboarding.icon))

        binding.onboardingButton.setOnClickListener {
            interactor.showNextPage()
        }

        if (onboarding.images.isNotEmpty()) {
            onboarding.images.forEach { image ->
                val imageView = ImageView(itemView.context)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.weight = 1F
                params.setMargins(15)
                imageView.setImageResource(image)
                imageView.setLayoutParams(params)
                binding.onboardingOrganisationsLayout.addView(imageView)
                binding.onboardingOrganisationsLayout.visibility = View.VISIBLE
            }
        } else {
            binding.onboardingOrganisationsLayout.visibility = View.INVISIBLE
        }

        this.onboarding = onboarding
    }

    companion object {
        const val LAYOUT_ID = R.layout.on_boarding_item
    }
}