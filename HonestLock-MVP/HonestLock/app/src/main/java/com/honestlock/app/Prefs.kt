package com.honestlock.app

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.Calendar

/**
 * Single place for all locally-stored state. Nothing here leaves the device.
 */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("honest_lock_prefs", Context.MODE_PRIVATE)

    // --- PIN that guards opening Honest Lock itself ---

    fun isPinSet(): Boolean = sp.contains("pin_hash")

    fun setPin(pin: String) {
        sp.edit().putString("pin_hash", hash(pin)).apply()
    }

    fun checkPin(pin: String): Boolean = sp.getString("pin_hash", null) == hash(pin)

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // --- Honesty gate covering the whole monitored category ---
    // True once the user has confirmed honesty and not yet left the category.

    var categoryGatePassed: Boolean
        get() = sp.getBoolean("gate_passed", false)
        set(value) = sp.edit().putBoolean("gate_passed", value).apply()

    // --- Current sitting timer (resets whenever the gate resets) ---

    var sessionMs: Long
        get() = sp.getLong("session_ms", 0L)
        set(value) = sp.edit().putLong("session_ms", value).apply()

    var warningStage: Int
        get() = sp.getInt("warning_stage", 0)
        set(value) = sp.edit().putInt("warning_stage", value).apply()

    var finalWarningShownAt: Long
        get() = sp.getLong("final_warning_at", 0L)
        set(value) = sp.edit().putLong("final_warning_at", value).apply()

    fun resetSessionTimer() {
        sessionMs = 0L
        warningStage = 0
        finalWarningShownAt = 0L
    }

    fun clearGate() {
        categoryGatePassed = false
        resetSessionTimer()
    }

    // --- Daily cumulative total across all three apps (hard 4h cap) ---

    fun addTodayMs(delta: Long) {
        rolloverIfNewDay()
        sp.edit().putLong("today_ms", todayMs() + delta).apply()
    }

    fun todayMs(): Long {
        rolloverIfNewDay()
        return sp.getLong("today_ms", 0L)
    }

    // --- Per-app breakdown, for the dashboard ---

    fun addAppMs(pkg: String, delta: Long) {
        rolloverIfNewDay()
        val key = "app_ms_$pkg"
        sp.edit().putLong(key, sp.getLong(key, 0L) + delta).apply()
    }

    fun appMs(pkg: String): Long {
        rolloverIfNewDay()
        return sp.getLong("app_ms_$pkg", 0L)
    }

    private fun rolloverIfNewDay() {
        val today = dayStamp()
        if (sp.getString("today_stamp", null) != today) {
            sp.edit()
                .putString("today_stamp", today)
                .putLong("today_ms", 0L)
                .putLong("app_ms_${MonitoredApps.INSTAGRAM}", 0L)
                .putLong("app_ms_${MonitoredApps.YOUTUBE}", 0L)
                .putLong("app_ms_${MonitoredApps.FACEBOOK}", 0L)
                .apply()
        }
    }

    private fun dayStamp(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }

    // --- Lockout window: escalation close (15 min) or daily cap (until midnight) ---

    var lockoutUntil: Long
        get() = sp.getLong("lockout_until", 0L)
        set(value) = sp.edit().putLong("lockout_until", value).apply()

    fun isLockedOut(): Boolean = System.currentTimeMillis() < lockoutUntil

    fun lockoutRemainingMs(): Long =
        (lockoutUntil - System.currentTimeMillis()).coerceAtLeast(0L)

    fun lockFor(durationMs: Long) {
        lockoutUntil = System.currentTimeMillis() + durationMs
    }

    fun lockUntilMidnight() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        lockoutUntil = c.timeInMillis
    }
}
