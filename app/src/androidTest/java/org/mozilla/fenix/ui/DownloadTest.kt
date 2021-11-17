/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityTestRule
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.helpers.TestAssetHelper.downloadFileName
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTime
import org.mozilla.fenix.helpers.TestHelper.deleteDownloadFromStorage
import org.mozilla.fenix.ui.robots.browserScreen
import org.mozilla.fenix.ui.robots.downloadRobot
import org.mozilla.fenix.ui.robots.navigationToolbar
import org.mozilla.fenix.ui.robots.notificationShade

/**
 *  Tests for verifying basic functionality of download prompt UI
 *
 *  - Initiates a download
 *  - Verifies download prompt
 *  - Verifies download notification
 **/

class DownloadTest {

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    @get:Rule
    val activityTestRule = HomeActivityTestRule()

    @get:Rule
    var mGrantPermissions = GrantPermissionRule.grant(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()

        deleteDownloadFromStorage(downloadFileName)
    }

    @Test
    fun testDownloadPrompt() {
        val defaultWebPage = TestAssetHelper.getDownloadAsset(mockWebServer)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(defaultWebPage.url) {
            mDevice.waitForIdle()
        }

        downloadRobot {
            verifyDownloadPrompt()
        }.closePrompt {}
    }

    @Test
    fun testDownloadNotification() {
        val defaultWebPage = TestAssetHelper.getDownloadAsset(mockWebServer)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(defaultWebPage.url) {
            mDevice.waitForIdle()
        }

        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }

        mDevice.openNotification()
        notificationShade {
            verifySystemNotificationExists("Download completed")
        }
        // close notification shade before the next test
        mDevice.pressBack()
    }

    @Test
    fun downloadJPGTypeTest() {
        val page = mockWebServer.url("pages/downloadVariations.html").toString().toUri()
        val title = "rabbit.jpg"
        val downloadBtn = mDevice.findObject(UiSelector().text("Download rabbit.jpg"))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page) {
            downloadBtn.waitForExists(waitingTime)
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }.closePrompt {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            waitForDownloadsListToExist()
            verifyDownloadedFileName(title)
            verifyDownloadedFileIcon()
        }

        deleteDownloadFromStorage(title)
    }

    @Test
    fun downloadMP3TypeTest() {
        val page = mockWebServer.url("pages/downloadVariations.html").toString().toUri()
        val title = "audioSample.mp3"
        val downloadBtn = mDevice.findObject(UiSelector().text("Download audioSample.mp3"))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page) {
            downloadBtn.waitForExists(waitingTime)
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }.closePrompt {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            waitForDownloadsListToExist()
            verifyDownloadedFileName(title)
            verifyDownloadedFileIcon()
        }

        deleteDownloadFromStorage(title)
    }

    @Test
    fun downloadLargeFileTest() {
        // val page = "https://testfiledownload.com/"
        val page  = mockWebServer.url("pages/downloadVariations.html").toString().toUri()
        val title = ""
        val downloadBtn = mDevice.findObject(UiSelector().text(title))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page) {
            downloadBtn.waitForExists(waitingTime)
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {}
        mDevice.openNotification()
        notificationShade {
            expandNotificationMessage()
            clickSystemNotificationControlButton("Pause")
            clickSystemNotificationControlButton("Resume")
            clickSystemNotificationControlButton("Cancel")
            mDevice.pressBack()
        }
        browserScreen {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            verifyEmptyDownloadsList()
        }
        deleteDownloadFromStorage(title)
    }

    @Test
    fun downloadExeTypeTest() {
        val page = "http://demo.borland.com/testsite/download_testpage.php"
        val title = "SmallExecutable.exe"
        val exe = mDevice.findObject(UiSelector().textContains(title))
        val downloadBtn = mDevice.findObject(UiSelector().text("Download"))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page.toUri()) {
            exe.waitForExists(waitingTime)
            exe.click()
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }.closePrompt {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            waitForDownloadsListToExist()
            verifyDownloadedFileName(title)
            verifyDownloadedFileIcon()
        }

        deleteDownloadFromStorage(title)
    }

    @Test
    fun downloadPDFTypeTest() {
        val page = "http://demo.borland.com/testsite/download_testpage.php"
        val title = "3rdPartyLicenseTexts.pdf"
        val pdf = mDevice.findObject(UiSelector().textContains(title))
        val downloadBtn = mDevice.findObject(UiSelector().text("Download"))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page.toUri()) {
            pdf.waitForExists(waitingTime)
            pdf.click()
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }.closePrompt {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            waitForDownloadsListToExist()
            verifyDownloadedFileName(title)
            verifyDownloadedFileIcon()
        }

        deleteDownloadFromStorage(title)
    }

    @Test
    fun downloadZIPTypeTest() {
        val page = "http://demo.borland.com/testsite/download_testpage.php"
        val title = "Small.zip"
        val zip = mDevice.findObject(UiSelector().textContains(title))
        val downloadBtn = mDevice.findObject(UiSelector().text("Download"))

        navigationToolbar {
        }.enterURLAndEnterToBrowser(page.toUri()) {
            zip.waitForExists(waitingTime)
            zip.click()
            downloadBtn.click()
        }
        downloadRobot {
            verifyDownloadPrompt()
        }.clickDownload {
            verifyDownloadNotificationPopup()
        }.closePrompt {
        }.openThreeDotMenu {
        }.openDownloadsManager {
            waitForDownloadsListToExist()
            verifyDownloadedFileName(title)
            verifyDownloadedFileIcon()
        }

        deleteDownloadFromStorage(title)
    }
}
