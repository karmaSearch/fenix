/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.core.net.toUri
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.customannotations.SmokeTest
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityIntentTestRule
import org.mozilla.fenix.ui.robots.browserScreen
import org.mozilla.fenix.ui.robots.navigationToolbar

/**
 *  Tests for verifying site permissions prompts & functionality
 *
 */
class SitePermissionsTest {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = HomeActivityIntentTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @SmokeTest
    @Test
    fun microphonePermissionPromptTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartMicrophoneButton {
            clickAppPermissionButton(true)
            verifyMicrophonePermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("Microphone not allowed")
        }.clickStartMicrophoneButton {
        }.clickPagePermissionButton(true) {
            verifyPageContent("Microphone allowed")
        }
    }

    @SmokeTest
    @Test
    fun saveMicrophonePermissionChoiceTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartMicrophoneButton {
            clickAppPermissionButton(true)
            verifyMicrophonePermissionPrompt(testPageSubstring)
            selectRememberDecision()
        }.clickPagePermissionButton(false) {
            verifyPageContent("Microphone not allowed")
        }.clickStartMicrophoneButton { }
        browserScreen {
            verifyPageContent("Microphone not allowed")
        }
    }

    @SmokeTest
    @Test
    fun blockAppUsingMicrophoneTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartMicrophoneButton {
            clickAppPermissionButton(false)
        }
        browserScreen {
            verifyPageContent("Microphone not allowed")
        }
    }

    @SmokeTest
    @Test
    fun cameraPermissionPromptTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartCameraButton {
            clickAppPermissionButton(true)
            verifyCameraPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("Camera not allowed")
        }.clickStartCameraButton {
        }.clickPagePermissionButton(true) {
            verifyPageContent("Camera allowed")
        }
    }

    @SmokeTest
    @Test
    fun saveCameraPermissionChoiceTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartCameraButton {
            clickAppPermissionButton(true)
            verifyCameraPermissionPrompt(testPageSubstring)
            selectRememberDecision()
        }.clickPagePermissionButton(false) {
            verifyPageContent("Camera not allowed")
        }.clickStartCameraButton { }
        browserScreen {
            verifyPageContent("Camera not allowed")
        }
    }

    @SmokeTest
    @Test
    fun blockAppUsingCameraTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickStartCameraButton {
            clickAppPermissionButton(false)
        }
        browserScreen {
            verifyPageContent("Camera not allowed")
        }
    }

    @Test
    fun blockNotificationsPermissionPromptTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickOpenNotificationButton {
            verifyNotificationsPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("Notifications not allowed")
        }.openThreeDotMenu {
        }.refreshPage {
        }.clickOpenNotificationButton {
            verifyNotificationsPermissionPrompt(testPageSubstring, true)
        }
    }

    @Test
    fun allowNotificationsPermissionPromptTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickOpenNotificationButton {
            verifyNotificationsPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(true) {
            verifyPageContent("Notifications allowed")
        }
    }

    @Ignore("Needs mocking location for Firebase - to do: ")
    @Test
    fun allowLocationPermissionsTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickGetLocationButton {
            clickAppPermissionButton(true)
            verifyLocationPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(true) {
            verifyPageContent("longitude")
            verifyPageContent("latitude")
        }
    }

    @Ignore("Needs mocking location for Firebase - to do: ")
    @Test
    fun blockLocationPermissionsTest() {
        val testPage = "https://sv-ohorvath.github.io/testapp/permissions"
        val testPageSubstring = "https://sv-ohorvath.github.io:443"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(testPage.toUri()) {
        }.clickGetLocationButton {
            clickAppPermissionButton(true)
            verifyLocationPermissionPrompt(testPageSubstring)
        }.clickPagePermissionButton(false) {
            verifyPageContent("User denied geolocation prompt")
        }
    }
}