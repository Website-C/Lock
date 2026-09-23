package com.honestlock.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * The core watcher. Gets a callback every time the foreground window changes,
 * on every app (see accessibility_service_config.xml). When one of the three
 * monitored apps comes to the front it decides, in order:
 *
 *   1. Are we in a lockout window? -> bounce home, show LockedOutActivity.
 *   2. Has the honesty gate been passed for this sitting? -> bounce home,
 *      show HonestyGateActivity (which relaunches the app once confirmed).
 *   3. Otherwise -> let it through and make sure the usage timer is running.
 */
class AppMonitorAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile var activePackage: String? = null
        @Volatile private var instance: AppMonitorAccessibilityService? = null

        fun closeCurrentApp() {
            instance?.performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // ignore Honest Lock's own screens

        activePackage = pkg

        if (pkg !in MonitoredApps.ALL) {
            UsageMonitorService.notifyForegroundChanged(this, isMonitoredAppInForeground = false)
            return
        }

        val prefs = Prefs(this)

        if (prefs.isLockedOut()) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            LockedOutActivity.launch(this, prefs.lockoutRemainingMs())
            return
        }

        if (!prefs.categoryGatePassed) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            HonestyGateActivity.launch(this, pkg)
            return
        }

        UsageMonitorService.notifyForegroundChanged(this, isMonitoredAppInForeground = true)
        UsageMonitorService.start(this)
    }

    override fun onInterrupt() {}
}
