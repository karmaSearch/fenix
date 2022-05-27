package org.mozilla.fenix.home.onboarding

import android.R
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
import org.mozilla.fenix.databinding.OnboardingDialogCustomHomeBinding
import org.mozilla.fenix.databinding.OnboardingDialogLearnandactBinding
import org.mozilla.fenix.databinding.OnboardingDialogSearchbarBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.learnandact.viewholders.LearnAndActViewHolder

class CompanionOnBoardingDialog(private val searchBar: View, private val recyclerView: RecyclerView) {
    /**
     * Try to show the dialog if it hasn't been shown before.
     */
    private var searchBarCrf: Dialog?
    private var learnAndActCrf: Dialog? = null
    private var customHomeCrf: Dialog
    private val context: Context = recyclerView.context

    val isShowing: Boolean
        get() = searchBarCrf?.isShowing == true || learnAndActCrf?.isShowing == true || customHomeCrf.isShowing


    init {
        searchBarCrf = createSearchBarCRF(searchBar)

        val tv = TypedValue()

        var y = if (context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
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

    }

    fun showIfNeeded() {
        if (!context.settings().shouldShowCompanion) {
            return
        }
        context.settings().shouldShowCompanion = false

        val customHomeOnCancelListener = DialogInterface.OnDismissListener {
            customHomeCrf.setOnDismissListener {
                TopSiteOnBoardingDialog(recyclerView).showIfNeeded()
            }
        }

        val learnAndActOnCancelListener = DialogInterface.OnDismissListener {
            customHomeCrf.show()
            customHomeCrf.setOnDismissListener {
                TopSiteOnBoardingDialog(recyclerView).showIfNeeded()
            }
            customHomeCrf.setOnDismissListener(customHomeOnCancelListener)
        }

        val searchBarOnCancelListener = DialogInterface.OnDismissListener {
            val learnAndActView = findLearnAndActInView()
            if (learnAndActView == null) {
                customHomeCrf.show()
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
            if (viewHolder is LearnAndActViewHolder) {
                return viewHolder.containerView
            }
        }
        return null
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
}