package com.honestlock.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HonestyGateActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TARGET_PKG = "target_pkg"

        fun launch(context: Context, targetPackage: String) {
            val intent = Intent(context, HonestyGateActivity::class.java).apply {
                putExtra(EXTRA_TARGET_PKG, targetPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_honesty_gate)

        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PKG)
        findViewById<TextView>(R.id.quoteText).text = Quotes.random(Quotes.gate)

        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val prefs = Prefs(this)
            prefs.categoryGatePassed = true
            prefs.resetSessionTimer()
            UsageMonitorService.start(this)

            targetPackage?.let { pkg ->
                packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
            }
            finish()
        }

        findViewById<Button>(R.id.cancelButton).setOnClickListener { finish() }
    }
}
