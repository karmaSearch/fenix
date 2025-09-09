/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for handling storage permissions across different Android versions.
 * Handles the transition from legacy READ_EXTERNAL_STORAGE to granular media permissions
 * introduced in Android 13 (API 33).
 */
object StoragePermissions {

    /**
     * Media type enum for granular permissions
     */
    enum class MediaType {
        IMAGES,
        VIDEO,
        AUDIO,
        ALL
    }

    /**
     * Get the appropriate storage permissions based on Android version and media type
     */
    fun getStoragePermissions(mediaType: MediaType = MediaType.ALL): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ granular permissions
            when (mediaType) {
                MediaType.IMAGES -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                MediaType.VIDEO -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                MediaType.AUDIO -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                MediaType.ALL -> arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
        } else {
            // Android 12 and below - legacy permission
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Get permissions for visual media selection (Android 14+)
     */
    fun getVisualMediaPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ includes selective media access
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            // Android 12 and below
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Check if storage permissions are granted for the specified media type
     */
    fun hasStoragePermissions(context: Context, mediaType: MediaType = MediaType.ALL): Boolean {
        val permissions = getStoragePermissions(mediaType)
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if visual media permissions are granted
     */
    fun hasVisualMediaPermissions(context: Context): Boolean {
        val permissions = getVisualMediaPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if any media permission is granted (useful for partial access scenarios)
     */
    fun hasAnyMediaPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ).any { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get permission strings for display in UI (for educational purposes)
     */
    fun getPermissionDisplayNames(mediaType: MediaType = MediaType.ALL): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (mediaType) {
                MediaType.IMAGES -> listOf("Images")
                MediaType.VIDEO -> listOf("Videos")
                MediaType.AUDIO -> listOf("Audio files")
                MediaType.ALL -> listOf("Images", "Videos", "Audio files")
            }
        } else {
            listOf("Storage")
        }
    }
}
