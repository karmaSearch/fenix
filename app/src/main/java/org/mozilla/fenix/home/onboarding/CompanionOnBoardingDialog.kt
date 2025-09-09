package org.mozilla.fenix.home.onboarding

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.ui.unit.dp
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.cfr.CFRPopup
import org.mozilla.fenix.compose.cfr.CFRPopupProperties
import org.mozilla.fenix.databinding.OnboardingDialogLearnandactBinding
import org.mozilla.fenix.databinding.OnboardingDialogSearchbarBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.affiliatesites.AffiliateSitesPagerViewHolder
import org.mozilla.fenix.home.learnandact.viewholders.LearnAndActHeaderViewHolder

class CompanionOnBoardingDialog(private val searchBar: View, private val recyclerView: RecyclerView) {
    /**
     * Try to show the dialog if it hasn't been shown before.
     */
    private var searchBarCrf: Dialog?
    private var learnAndActCrf: Dialog? = null
    private val context: Context = recyclerView.context

    val isShowing: Boolean
        get() = searchBarCrf?.isShowing == true || learnAndActCrf?.isShowing == true


    init {
        searchBarCrf = createSearchBarCRF(searchBar)
    }

    fun showIfNeeded() {
        if (!context.settings().shouldShowCompanion) {
            return
        }
        context.settings().shouldShowCompanion = false

        val learnAndActOnCancelListener = DialogInterface.OnDismissListener {
            // Show affiliate sites CFR after learn and act
            showAffiliateSitesCFRIfNeeded()
        }

        val searchBarOnCancelListener = DialogInterface.OnDismissListener {
            val learnAndActView = findLearnAndActInView()
            if (learnAndActView == null) {
                // No learn and act found, show affiliate sites CFR directly
                showAffiliateSitesCFRIfNeeded()
            } else {
                learnAndActCrf = createLearnAndActCRF(learnAndActView)
                learnAndActCrf?.setOnDismissListener(learnAndActOnCancelListener)
                learnAndActCrf?.show()
            }
        }

        searchBarCrf?.show()
        searchBarCrf?.setOnDismissListener(searchBarOnCancelListener)
    }


    private fun createSearchBarCRF(anchor: View): Dialog? {
        val context: Context = searchBar.context

        val anchorPosition = IntArray(2)
        val popupBinding = OnboardingDialogSearchbarBinding.inflate(LayoutInflater.from(context))
        val popup = Dialog(context)

        popup.apply {
            setContentView(popupBinding.root)
            // removing title or setting it as an empty string does not prevent a11y services from assigning one
            setTitle(" ")
        }

        anchor.getLocationOnScreen(anchorPosition)
        val (x, y) = anchorPosition

        if (x == 0 && y == 0) {
            return null
        }


        popupBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        popup.window?.apply {
            val attr = attributes
            setGravity(Gravity.START or Gravity.TOP)
            attr.x = x
            attr.y = y - popupBinding.root.measuredHeight - 50
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)
        return popup
    }


    private fun findLearnAndActInView(): View? {
        val count = recyclerView.adapter?.itemCount ?: return recyclerView

        for (index in 0..count) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(index)
            if (viewHolder is LearnAndActHeaderViewHolder) {
                return viewHolder.itemView
            }
        }
        return null
    }

    private fun findAffiliateSitesInView(): View? {
        val count = recyclerView.adapter?.itemCount ?: return null

        for (index in 0..count) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(index)
            if (viewHolder is AffiliateSitesPagerViewHolder) {
                return viewHolder.itemView
            }
        }
        return null
    }

    private fun showAffiliateSitesCFRIfNeeded() {
        val affiliateSitesView = findAffiliateSitesInView()
        if (affiliateSitesView != null && context.settings().shouldShowAffiliateSitesCFR) {
            CFRPopup(
                text = context.getString(org.mozilla.fenix.R.string.onboarding_companion_affiliate),
                anchor = affiliateSitesView,
                properties = CFRPopupProperties(
                    indicatorDirection = CFRPopup.IndicatorDirection.DOWN,
                    popupVerticalOffset = (-40).dp,
                ),
            ).show()
            context.settings().shouldShowAffiliateSitesCFR = false
        } else {
            // If no affiliate sites or CFR already shown, show TopSite onboarding
            TopSiteOnBoardingDialog(recyclerView).showIfNeeded()
        }
    }

    private fun createLearnAndActCRF(anchor: View): Dialog? {
        val context: Context = recyclerView.context

        val anchorPosition = IntArray(2)
        val popupBinding = OnboardingDialogLearnandactBinding.inflate(LayoutInflater.from(context))
        val popup = Dialog(context)

        popup.apply {
            setContentView(popupBinding.root)
            // removing title or setting it as an empty string does not prevent a11y services from assigning one
            setTitle(" ")
        }
        anchor.getLocationOnScreen(anchorPosition)
        val (x, y) = anchorPosition

        if (x == 0 && y == 0) {
            return null
        }

        popupBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        popup.window?.apply {
            val attr = attributes
            setGravity(Gravity.START or Gravity.TOP)
            attr.x = x
            attr.y = y - popupBinding.root.measuredHeight
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)

        return popup
    }

}
