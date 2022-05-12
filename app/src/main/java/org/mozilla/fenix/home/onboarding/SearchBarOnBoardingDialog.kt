package org.mozilla.fenix.home.onboarding

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import org.mozilla.fenix.databinding.OnboardingDialogSearchbarBinding
import org.mozilla.fenix.ext.settings

class SearchBarOnBoardingDialog(private val searchBar: View) {
    /**
     * Try to show the dialog if it hasn't been shown before.
     */
    fun showIfNeeded() {
        val searchBarView = searchBar
        searchBarView.let {
            val crfDialog = createCRF(anchor = searchBarView)
            crfDialog?.let {
                val context = searchBarView.context
                context.settings().shouldShowCompanion = false
                it.show()
            }
        }
    }


    private fun createCRF(anchor: View): Dialog? {
        val context: Context = searchBar.context
        if (!context.settings().shouldShowCompanion) {
            return null
        }
        val anchorPosition = IntArray(2)
        val popupBinding = OnboardingDialogSearchbarBinding.inflate(LayoutInflater.from(context))
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
            attr.y = y - popupBinding.root.measuredHeight - 50
            attributes = attr
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        popup.setCanceledOnTouchOutside(true)
        return popup
    }
}