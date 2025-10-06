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
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import org.mozilla.fenix.GleanMetrics.SearchWidget
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentAddWidgetBinding
import org.mozilla.fenix.ext.settings
import org.mozilla.gecko.search.SearchWidgetProvider

class AddKarmaWidgetFragment: DialogFragment() {
    private var _binding: FragmentAddWidgetBinding? = null
    private val binding get() = _binding!!
    private var originalNavigationBarColor: Int? = null
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

        // Set navigation bar to black
        activity?.window?.let { window ->
            originalNavigationBarColor = window.navigationBarColor
            window.navigationBarColor = ContextCompat.getColor(requireContext(), android.R.color.black)
            WindowCompat.getInsetsController(window, view).let { controller ->
                controller.isAppearanceLightNavigationBars = false
            }
        }

        binding.addWidget.setOnClickListener {
            handleOpenRequestPinAppWidget()
            context?.settings()?.userDismissedAddWidgetCard = true
        }

        binding.addWidgetLater.setOnClickListener {
            dismiss()
            context?.settings()?.userDismissedAddWidgetCard = true
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Restore original navigation bar color
        activity?.window?.let { window ->
            originalNavigationBarColor?.let { color ->
                window.navigationBarColor = color
            }
        }
        
        _binding = null
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