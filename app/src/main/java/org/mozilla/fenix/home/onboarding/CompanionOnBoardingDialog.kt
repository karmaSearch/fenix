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
import org.mozilla.fenix.databinding.OnboardingDialogCustomHomeBinding
import org.mozilla.fenix.databinding.OnboardingDialogLearnandactBinding
import org.mozilla.fenix.databinding.OnboardingDialogSearchbarBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.affiliatesites.AffiliateSitesPagerViewHolder
import org.mozilla.fenix.home.learnandact.viewholders.LearnAndActHeaderViewHolder
import org.mozilla.fenix.home.sessioncontrol.viewholders.CustomizeHomeButtonViewHolder

class CompanionOnBoardingDialog(private val searchBar: View, private val recyclerView: RecyclerView) {
    /**
     * Try to show the dialog if it hasn't been shown before.
     */
    private var searchBarCrf: Dialog?
    private var learnAndActCrf: Dialog? = null
    private var affiliatesCrf: Dialog? = null
    private var customHomeCrf: Dialog? = null
    private val context: Context = recyclerView.context

    val isShowing: Boolean
        get() = searchBarCrf?.isShowing == true || learnAndActCrf?.isShowing == true || affiliatesCrf?.isShowing == true || customHomeCrf?.isShowing == true


    init {
        searchBarCrf = createSearchBarCRF(searchBar)
    }

    fun showIfNeeded() {
        // Don't show if any companion is already showing
        if (isShowing) {
            return
        }
        
        val shouldShowSearchBar = context.settings().shouldShowCompanion
        val shouldShowAffiliates = context.settings().shouldShowAffiliateSitesCFR
        
        // Allow affiliates to show even if other companions have been shown
        if (!shouldShowAffiliates && !shouldShowSearchBar) {
            return
        }

        val affiliateSitesOnCancelListener = DialogInterface.OnDismissListener {
            // Show learn and act CFR after affiliate sites only for new users
            if (shouldShowSearchBar) {
                val learnAndActView = findLearnAndActInView()
                if (learnAndActView != null) {
                    learnAndActCrf = createLearnAndActCRF(learnAndActView)
                    learnAndActCrf?.setOnDismissListener(getLearnAndActDismissListener())
                    learnAndActCrf?.show()
                }
            } else {
                // For existing users, show custom home CFR after affiliate sites
                showCustomHomeCFRIfNeeded()
            }
        }

        val searchBarOnCancelListener = DialogInterface.OnDismissListener {
            val affiliateSitesView = findAffiliateSitesInView()
            if (affiliateSitesView != null && shouldShowAffiliates) {
                // Show affiliate sites after search bar
                showAffiliateSitesCFRWithListener(affiliateSitesOnCancelListener)
            } else {
                // No affiliate sites to show, show learn and act CFR directly
                val learnAndActView = findLearnAndActInView()
                if (learnAndActView != null) {
                    learnAndActCrf = createLearnAndActCRF(learnAndActView)
                    learnAndActCrf?.setOnDismissListener(getLearnAndActDismissListener())
                    learnAndActCrf?.show()
                }
            }
        }

        // For new users: start with search bar first (if needed), then affiliates, then learn&act
        if (shouldShowSearchBar) {
            context.settings().shouldShowCompanion = false
            searchBarCrf?.show()
            searchBarCrf?.setOnDismissListener(searchBarOnCancelListener)
            return
        }
        
        // For existing users who only need to see affiliates (no learn&act after)
        if (shouldShowAffiliates) {
            val affiliateSitesView = findAffiliateSitesInView()
            if (affiliateSitesView != null) {
                val existingUserAffiliateListener = DialogInterface.OnDismissListener {
                    // Show custom home CFR after affiliate for existing users
                    showCustomHomeCFRIfNeeded()
                }
                showAffiliateSitesCFRWithListener(existingUserAffiliateListener)
            } else {
                // No affiliate sites to show, show custom home CFR directly
                showCustomHomeCFRIfNeeded()
            }
        } else {
            // No affiliates, no search bar - just show custom home CFR if needed
            showCustomHomeCFRIfNeeded()
        }
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
            affiliatesCrf = createAffiliatesCRF(affiliateSitesView)
            affiliatesCrf?.setOnDismissListener(onDismissListener)
            affiliatesCrf?.show()
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

    private fun createCustomHomeCRF(x: Int, y: Int): Dialog {
        val popupBinding = OnboardingDialogCustomHomeBinding.inflate(LayoutInflater.from(context))
        val popup = Dialog(context)

        popup.apply {
            setContentView(popupBinding.root)
            setCancelable(false)
            // removing title or setting it as an empty string does not prevent a11y services from assigning one
            setTitle(" ")
        }

        popup.window?.apply {
            val attr = attributes
            setGravity(Gravity.END or Gravity.TOP)
            attr.x = x
            attr.y = y
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)
        return popup
    }

    private fun getLearnAndActDismissListener(): DialogInterface.OnDismissListener {
        return DialogInterface.OnDismissListener {
            // Show custom home CFR after learn and act
            showCustomHomeCFRIfNeeded()
        }
    }

    private fun showCustomHomeCFRIfNeeded() {
        if (context.settings().shouldShowCustomHomeCFR) {
            val tv = TypedValue()
            var y = if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    context.getResources()
                        .getDisplayMetrics()
                )
                actionBarHeight / 2

            } else {
                20
            }
            customHomeCrf = createCustomHomeCRF(0, y)
            customHomeCrf?.show()
            context.settings().shouldShowCustomHomeCFR = false
        }
    }
}
