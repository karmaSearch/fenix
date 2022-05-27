package org.mozilla.fenix.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.mozilla.fenix.GleanMetrics.SearchWidget
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentAddWidgetBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.gecko.search.SearchWidgetProvider

class AddKarmaWidgetFragment: DialogFragment() {
    private var _binding: FragmentAddWidgetBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.HomeOnboardingDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWidgetBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addWidget.setOnClickListener {
            handleOpenRequestPinAppWidget()
            context?.settings()?.userDismissedAddWidgetCard = true
        }

        binding.addWidgetLater.setOnClickListener {
            dismiss()
            context?.settings()?.userDismissedAddWidgetCard = true
        }

    }

    private fun handleOpenRequestPinAppWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = requireContext().getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(requireContext(), SearchWidgetProvider::class.java)

            appWidgetManager.requestPinAppWidget(myProvider,null, null)
            dismiss()
        }
    }
}