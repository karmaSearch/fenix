/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.affiliatesites

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import karma.service.affiliatesites.AffiliateSite
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.databinding.AffiliateSiteItemBinding
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.loadIntoView
import org.mozilla.fenix.home.sessioncontrol.AffiliateSiteInteractor
import org.mozilla.fenix.utils.view.ViewHolder
import java.net.URL


class AffiliateSiteItemViewHolder(
    view: View,
    @Suppress("UNUSED_PARAMETER") appStore: AppStore,
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: AffiliateSiteInteractor,
) : ViewHolder(view) {
    
    private lateinit var affiliateSite: AffiliateSite
    private val binding = AffiliateSiteItemBinding.bind(view)

    init {
        binding.affiliateSiteItem.setOnLongClickListener {
            val affiliateSiteMenu = AffiliateSiteItemMenu(
                context = view.context,
                affiliateSite = affiliateSite,
            ) { item ->
                when (item) {
                    AffiliateSiteItemMenu.Item.OpenInPrivateTab -> interactor.onOpenInPrivateTabClicked(
                        affiliateSite,
                    )
                }
            }
            
            affiliateSiteMenu.menuBuilder.build(view.context).show(
                anchor = binding.affiliateSiteItem,
            )

            true
        }
    }

    fun bind(affiliateSite: AffiliateSite, position: Int) {
        this.affiliateSite = affiliateSite
        binding.affiliateSiteTitle.text = affiliateSite.siteName
        // Load favicon/image for the affiliate site
        val url = URL(affiliateSite.imageUrl)
        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        binding.affiliateSiteFavicon.setImageBitmap(bmp)

        itemView.setOnClickListener {
            interactor.onSelectAffiliateSite(affiliateSite, position)
        }

        binding.affiliateSiteItem.setOnTouchListener(
            object : View.OnTouchListener {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        v.background?.apply {
                            mutate()
                            alpha = PRESSED_BACKGROUND_ALPHA
                        }
                    } else if (
                        event.action == MotionEvent.ACTION_UP ||
                        event.action == MotionEvent.ACTION_CANCEL
                    ) {
                        v.background?.apply {
                            mutate()
                            alpha = NORMAL_BACKGROUND_ALPHA
                        }
                    }
                    return false
                }
            },
        )
    }

    companion object {
        const val LAYOUT_ID = R.layout.affiliate_site_item
        private const val PRESSED_BACKGROUND_ALPHA = 70
        private const val NORMAL_BACKGROUND_ALPHA = 255
    }
}
