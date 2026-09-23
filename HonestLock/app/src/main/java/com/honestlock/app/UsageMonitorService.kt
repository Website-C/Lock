package com.honestlock.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

/**
 * Ticks once a second while a monitored app is in the foreground, adding to
 * the daily total and the current-sitting session timer, and firing the
 * 30 / 45 / 55 minute escalation plus the 4-hour daily hard cap.
 *
 * All the thresholds below are the only things you need to change to retune
 * the behaviour.
 */
class UsageMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "honest_lock_monitor"
        private const val NOTIF_ID = 1

        private const val STAGE1_MS = 30 * 60_000L       // first pop-up
        private const val STAGE2_MS = 45 * 60_000L       // warning
        private const val STAGE3_MS = 55 * 60_000L       // last warning
        private const val FORCE_CLOSE_GRACE_MS = 60_000L // grace after last warning before auto-close
        private const val LOCKOUT_DURATION_MS = 15 * 60_000L
        private const val DAILY_CAP_MS = 4 * 60 * 60_000L
        private const val TICK_MS = 1000L

        @Volatile private var isForegroundAppMonitored = false
        @Volatile private var running = false

        fun notifyForegroundChanged(context: Context, isMonitoredAppInForeground: Boolean) {
            isForegroundAppMonitored = isMonitoredAppInForeground
            if (!isMonitoredAppInForeground) {
                Prefs(context).clearGate()
            }
        }

        fun start(context: Context) {
            if (running) return
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs

    private val ticker = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        running = true
        startForeground(NOTIF_ID, buildNotification())
        handler.post(ticker)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }

    private fun tick() {
        if (!isForegroundAppMonitored || prefs.isLockedOut()) return

        val pkg = AppMonitorAccessibilityService.activePackage
        prefs.addTodayMs(TICK_MS)
        if (pkg != null) prefs.addAppMs(pkg, TICK_MS)
        prefs.sessionMs += TICK_MS

        if (prefs.todayMs() >= DAILY_CAP_MS) {
            lockOutForRestOfDay()
            return
        }

        evaluateEscalation()
    }

    private fun evaluateEscalation() {
        val session = prefs.sessionMs
        when (prefs.warningStage) {
            0 -> if (session >= STAGE1_MS) {
                prefs.warningStage = 1
                WarningOverlayActivity.launch(this, 1, isFinal = false)
            }
            1 -> if (session >= STAGE2_MS) {
                prefs.warningStage = 2
                WarningOverlayActivity.launch(this, 2, isFinal = false)
            }
            2 -> if (session >= STAGE3_MS) {
                prefs.warningStage = 3
                prefs.finalWarningShownAt = System.currentTimeMillis()
                WarningOverlayActivity.launch(this, 3, isFinal = true)
            }
            3 -> {
                val sinceFinal = System.currentTimeMillis() - prefs.finalWarningShownAt
                if (sinceFinal >= FORCE_CLOSE_GRACE_MS) {
                    forceCloseAndLock()
                }
            }
        }
    }

    private fun forceCloseAndLock() {
        AppMonitorAccessibilityService.closeCurrentApp()
        prefs.clearGate()
        prefs.lockFor(LOCKOUT_DURATION_MS)
        LockedOutActivity.launch(this, LOCKOUT_DURATION_MS)
    }

    private fun lockOutForRestOfDay() {
        AppMonitorAccessibilityService.closeCurrentApp()
        prefs.clearGate()
        prefs.lockUntilMidnight()
        LockedOutActivity.launch(this, prefs.lockoutRemainingMs())
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Honest Lock monitoring",
                NotificationManager.IMPORTANCE_MIN
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Honest Lock is watching")
            .setContentText("Tracking time on your locked apps.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
