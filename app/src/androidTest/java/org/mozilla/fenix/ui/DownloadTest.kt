/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.customannotations.SmokeTest
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityTestRule
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.helpers.TestAssetHelper.downloadFileName
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
 *  - Verifies various file types downloads and their appearance inside the Downloads list.
 **/

class DownloadTest {
    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    /* Remote test page managed by Mozilla Mobile QA team, used for testing various file types and sizes.*/
    private val testPage  = "https://sv-ohorvath.github.io/testapp/downloads"

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

@Ignore
@Test
fun testDownloadPrompt() {
    /* test page that downloads automatically a SVG file
     - we need this to control the presence of the download prompt
     - prevents opening the image in the browser
     */
    val defaultWebPage = TestAssetHelper.getDownloadAsset(mockWebServer)

    navigationToolbar {
    }.enterURLAndEnterToBrowser(defaultWebPage.url) {
        mDevice.waitForIdle()
    }

    downloadRobot {
        verifyDownloadPrompt()
    }.closePrompt {}
}

@Ignore
@Test
fun testDownloadNotification() {
    /* test page that downloads automatically a SVG file
     - we need this to control the presence of the download prompt
     - prevents opening the image in the browser
     */
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

@SmokeTest
@Test
fun downloadLargeFileTest() {
    val fileName = "200MB.zip"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
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
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadPDFTypeTest() {
    val fileName = "washington.pdf"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadMP3TypeTest() {
    val fileName = "audioSample.mp3"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadExeTypeTest() {
    val fileName = "executable.exe"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadDatTypeTest() {
    val fileName = "Data1kb.dat"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadDocXTypeTest() {
    val fileName = "MyDocument.docx"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadDocTypeTest() {
    val fileName = "MyOldWordDocument.doc"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadTxtTypeTest() {
    val fileName = "textfile.txt"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadPNGTypeTest() {
    val fileName = "web_icon.png"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            verifyDownloadPrompt()
            downloadRobot {
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadWebmTypeTest() {
    val fileName = "videoSample.webm"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadZIPTypeTest() {
    val fileName = "smallZip.zip"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadCSVTypeTest() {
    val fileName = "CSVfile.csv"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadHMTLTypeTest() {
    val fileName = "htmlFile.html"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}

@SmokeTest
@Test
fun downloadXMLTypeTest() {
    val fileName = "XMLfile.xml"

    navigationToolbar {
    }.enterURLAndEnterToBrowser(testPage.toUri()) {
    }.clickDownloadLink(fileName) {
        // checking if a download was triggered or
        // the browser opened the file based on the same origin policy rule
        if (isDownloadTriggered) {
            downloadRobot {
                verifyDownloadPrompt()
            }.clickDownload {
                verifyDownloadNotificationPopup()
            }.closePrompt {
            }.openThreeDotMenu {
            }.openDownloadsManager {
                waitForDownloadsListToExist()
                verifyDownloadedFileName(fileName)
                verifyDownloadedFileIcon()
            }
        } else {
            browserScreen {
                verifyUrl(fileName)
            }
        }
    }
    deleteDownloadFromStorage(fileName)
}
}
