package org.mozilla.fenix.home.onboarding

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.databinding.OnboardingDialogLearnandactBinding
import org.mozilla.fenix.databinding.OnboardingJumpBackInCfrBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.learnandact.viewholders.LearnAndActViewHolder
import org.mozilla.fenix.home.recenttabs.view.RecentTabsHeaderViewHolder

class LearnAndActOnBoardingDialog(val recyclerView: RecyclerView) {

    /**
     * Try to show the crf dialog if it hasn't been shown before.
     */
    fun showIfNeeded() {
        val jumpBackInView = findJumpBackInView()
        jumpBackInView?.let {
            val crfDialog = createJumpCRF(anchor = jumpBackInView)
            crfDialog?.let {
                val context = jumpBackInView.context
                context.settings().shouldShowLearnAndActCompanion = false
                it.show()
            }
        }
    }

    private fun findJumpBackInView(): View? {
        val count = recyclerView.adapter?.itemCount ?: return null

        for (index in 0..count) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(index)
            if (viewHolder is LearnAndActViewHolder) {
                return viewHolder.containerView
            }
        }
        return null
    }

    private fun createJumpCRF(anchor: View): Dialog? {
        val context: Context = recyclerView.context
        if (!context.settings().shouldShowLearnAndActCompanion) {
            return null
        }
        val anchorPosition = IntArray(2)
        val popupBinding = OnboardingDialogLearnandactBinding.inflate(LayoutInflater.from(context))
        val popup = Dialog(context)

        popup.apply {
            setContentView(popupBinding.root)
            setCancelable(false)
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