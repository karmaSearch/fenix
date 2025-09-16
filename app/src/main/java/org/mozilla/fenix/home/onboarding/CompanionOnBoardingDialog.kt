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
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.OnboardingDialogAffiliatesBinding
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

        val affiliateSitesOnCancelListener = DialogInterface.OnDismissListener {
            // Show learn and act CFR after affiliate sites
            val learnAndActView = findLearnAndActInView()
            if (learnAndActView != null) {
                learnAndActCrf = createLearnAndActCRF(learnAndActView)
                learnAndActCrf?.show()
            }
        }

        val searchBarOnCancelListener = DialogInterface.OnDismissListener {
            val affiliateSitesView = findAffiliateSitesInView()
            if (affiliateSitesView == null) {
                // No affiliate sites found, show learn and act CFR directly
                val learnAndActView = findLearnAndActInView()
                if (learnAndActView != null) {
                    learnAndActCrf = createLearnAndActCRF(learnAndActView)
                    learnAndActCrf?.show()
                }
            } else {
                showAffiliateSitesCFRWithListener(affiliateSitesOnCancelListener)
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

    private fun showAffiliateSitesCFRWithListener(onDismissListener: DialogInterface.OnDismissListener) {
        val affiliateSitesView = findAffiliateSitesInView()
        if (affiliateSitesView != null && context.settings().shouldShowAffiliateSitesCFR) {
            val affiliatesCRF = createAffiliatesCRF(affiliateSitesView)
            affiliatesCRF?.setOnDismissListener(onDismissListener)
            affiliatesCRF?.show()
            context.settings().shouldShowAffiliateSitesCFR = false
        } else {
            // If no affiliate sites or CFR already shown, call the dismiss listener to continue the flow
            onDismissListener.onDismiss(null)
        }
    }

    private fun createAffiliatesCRF(anchor: View): Dialog? {
        val context: Context = recyclerView.context

        val anchorPosition = IntArray(2)
        val popupBinding = OnboardingDialogAffiliatesBinding.inflate(LayoutInflater.from(context))
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
            // Position at the top of the anchor instead of centered
            attr.y = y - popupBinding.root.measuredHeight - 50
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)
        return popup
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
            // Position at the top of the anchor instead of centered
            attr.y = y - popupBinding.root.measuredHeight - 50
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)

        return popup
    }

}
