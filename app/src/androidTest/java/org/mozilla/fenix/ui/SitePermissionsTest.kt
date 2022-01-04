/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.core.net.toUri
import androidx.test.rule.GrantPermissionRule
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityIntentTestRule
import org.mozilla.fenix.ui.robots.navigationToolbar

/**
 *  Tests for verifying site permissions prompts & functionality
 *
 */
class SitePermissionsTest {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = HomeActivityIntentTestRule()

    @get:Rule
    var mGrantPermissions = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @Test
    fun microphonePermissionPromptTest() {
        val webRTCtestPage = "https://mozilla.github.io/webrtc-landing/gum_test.html"
        val testPageSubstring = "https://mozilla.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webRTCtestPage.toUri()) {
        }.clickStartMicrophoneButton {
            verifyMicrophonePermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("NotAllowedError")
        }.clickStartMicrophoneButton {
        }.clickPagePermissionButton(true) {
            verifyPageContent("Stop")
        }
    }

    @Test
    fun cameraPermissionPromptTest() {
        val testPage = "https://mozilla.github.io/webrtc-landing/gum_test.html"
        val testPageSubstring = "https://mozilla.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartCameraButton {
            verifyCameraPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("NotAllowedError")
        }.clickStartCameraButton {
        }.clickPagePermissionButton(true) {
            verifyPageContent("Stop")
        }
    }

    @Test
    fun cameraAndMicPermissionPromptTest() {
        val testPage = "https://mozilla.github.io/webrtc-landing/gum_test.html"
        val testPageSubstring = "https://mozilla.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartCameraAndMicrophoneButton {
            verifyCameraAndMicPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("NotAllowedError")
        }.clickStartCameraAndMicrophoneButton {
        }.clickPagePermissionButton(true) {
            verifyPageContent("Stop")
        }
    }

    @Test
    fun blockNotificationsPermissionPromptTest() {
        val testPage = "https://mozilla-mobile.github.io/testapp/"
        val testPageSubstring = "https://mozilla-mobile.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickOpenNotificationButton {
            verifyNotificationsPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
        }.clickOpenNotificationButton {
            verifyNotificationsPermissionPrompt(testPageSubstring, true)
        }
    }
}