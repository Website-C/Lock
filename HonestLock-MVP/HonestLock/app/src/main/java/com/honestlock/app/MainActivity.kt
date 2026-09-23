package com.honestlock.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        if (!prefs.isPinSet()) showSetPinScreen() else showPinEntryScreen()
    }

    private fun showSetPinScreen() {
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.titleText).text = getString(R.string.set_pin_title)
        val pinInput = findViewById<EditText>(R.id.pinInput)
        findViewById<Button>(R.id.primaryButton).apply {
            text = getString(R.string.set_pin_action)
            setOnClickListener {
                val pin = pinInput.text.toString()
                if (pin.length >= 4) {
                    prefs.setPin(pin)
                    showDashboard()
                } else {
                    pinInput.error = getString(R.string.pin_too_short)
                }
            }
        }
    }

    private fun showPinEntryScreen() {
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.titleText).text = getString(R.string.enter_pin_title)
        val pinInput = findViewById<EditText>(R.id.pinInput)
        findViewById<Button>(R.id.primaryButton).apply {
            text = getString(R.string.enter_pin_action)
            setOnClickListener {
                if (prefs.checkPin(pinInput.text.toString())) {
                    showDashboard()
                } else {
                    pinInput.error = getString(R.string.wrong_pin)
                }
            }
        }
    }

    private fun showDashboard() {
        setContentView(R.layout.activity_dashboard)

        findViewById<Button>(R.id.grantAccessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.grantOverlayButton).setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        findViewById<Button>(R.id.grantNotifButton).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                )
            }
        }

        findViewById<TextView>(R.id.statusText).text = buildString {
            append(getString(R.string.accessibility_status, if (isAccessibilityServiceEnabled()) "ON" else "OFF"))
            append("\n")
            append(getString(R.string.overlay_status, if (Settings.canDrawOverlays(this@MainActivity)) "ON" else "OFF"))
        }

        val totalMin = TimeUnit.MILLISECONDS.toMinutes(prefs.todayMs())
        findViewById<TextView>(R.id.usageText).text = getString(R.string.usage_today, totalMin)

        val igMin = TimeUnit.MILLISECONDS.toMinutes(prefs.appMs(MonitoredApps.INSTAGRAM))
        val ytMin = TimeUnit.MILLISECONDS.toMinutes(prefs.appMs(MonitoredApps.YOUTUBE))
        val fbMin = TimeUnit.MILLISECONDS.toMinutes(prefs.appMs(MonitoredApps.FACEBOOK))
        findViewById<TextView>(R.id.breakdownText).text =
            getString(R.string.usage_breakdown, igMin, ytMin, fbMin)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == packageName }
    }
}
