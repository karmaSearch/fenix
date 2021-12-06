package org.mozilla.fenix.ui.robots

import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertTrue
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTime
import org.mozilla.fenix.helpers.TestHelper.getPermissionAllowID
import org.mozilla.fenix.helpers.TestHelper.packageName

class SitePermissionsRobot {

    fun clickAppPermissionButton(allowed: Boolean) {
        if (allowed) {
            allowSystemPermissionButton.waitForExists(TestAssetHelper.waitingTime)
            allowSystemPermissionButton.click()
        } else {
            denySystemPermissionButton.waitForExists(TestAssetHelper.waitingTime)
            denySystemPermissionButton.click()
        }
    }

    fun verifyMicrophonePermissionPrompt(url: String) {
        assertTrue(mDevice.findObject(UiSelector().text("Allow $url to use your microphone?"))
            .waitForExists(waitingTime)
        )
        assertTrue(denyPagePermissionButton.text.equals("Don’t allow"))
        assertTrue(allowPagePermissionButton.text.equals("Allow"))
    }

    fun verifyCameraPermissionPrompt(url: String) {
        assertTrue(mDevice.findObject(UiSelector().text("Allow $url to use your camera?"))
            .waitForExists(waitingTime)
        )
        assertTrue(denyPagePermissionButton.text.equals("Don’t allow"))
        assertTrue(allowPagePermissionButton.text.equals("Allow"))
    }

    fun verifyCameraAndMicPermissionPrompt(url: String) {
        assertTrue(mDevice.findObject(UiSelector().text("Allow $url to use your camera and microphone?"))
            .waitForExists(waitingTime)
        )
        assertTrue(denyPagePermissionButton.text.equals("Don’t allow"))
        assertTrue(allowPagePermissionButton.text.equals("Allow"))
    }

    fun verifyLocationPermissionPrompt(url: String) {
        assertTrue(mDevice.findObject(UiSelector().text("Allow $url to use your location?"))
            .waitForExists(waitingTime)
        )
        assertTrue(denyPagePermissionButton.text.equals("Don’t allow"))
        assertTrue(allowPagePermissionButton.text.equals("Allow"))
    }

    fun verifyNotificationsPermissionPrompt(url: String) {
        assertTrue(mDevice.findObject(UiSelector().text("Allow $url to send notifications?"))
            .waitForExists(waitingTime)
        )
        assertTrue(denyPagePermissionButton.text.equals("Never"))
        assertTrue(allowPagePermissionButton.text.equals("Always"))
    }

    class Transition {
        fun clickPagePermissionButton(allowed: Boolean, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            if (allowed) {
                allowPagePermissionButton.waitForExists(TestAssetHelper.waitingTime)
                allowPagePermissionButton.click()
            } else {
                denyPagePermissionButton.waitForExists(TestAssetHelper.waitingTime)
                denyPagePermissionButton.click()
            }

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

// App permission prompts buttons
private val allowSystemPermissionButton =
    mDevice.findObject(UiSelector().resourceId(getPermissionAllowID() + ":id/permission_allow_button"))

private val denySystemPermissionButton =
    mDevice.findObject(UiSelector().resourceId(getPermissionAllowID() + ":id/permission_deny_button"))

// Page permission prompts buttons
private val allowPagePermissionButton =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/allow_button"))

private val denyPagePermissionButton =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/deny_button"))
