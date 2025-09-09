/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.notifications

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import org.mozilla.fenix.databinding.DialogNotificationPermissionBinding

/**
 * Custom dialog to request notification permission before showing the system dialog
 */
class NotificationPermissionDialog(
    context: Context,
    private val onContinueClicked: () -> Unit,
    private val onDeclineClicked: () -> Unit,
    private val onDialogClosed: (() -> Unit)? = null
) : Dialog(context) {

    private lateinit var binding: DialogNotificationPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure dialog with rounded corners
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        binding = DialogNotificationPermissionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // Configure dialog window as a popup (not full screen)
        window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt() // 90% of screen width for better appearance
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
        }
        
        setupClickListeners()
        setCanceledOnTouchOutside(false) // Prevent dismissing by touching outside
    }

    private fun setupClickListeners() {
        binding.notificationAcceptButton.setOnClickListener {
            dismiss()
            onContinueClicked()
        }
        
        binding.notificationDeclineButton.setOnClickListener {
            dismiss()
            onDeclineClicked()
        }
        
        setOnDismissListener {
            onDialogClosed?.invoke()
        }
    }
}