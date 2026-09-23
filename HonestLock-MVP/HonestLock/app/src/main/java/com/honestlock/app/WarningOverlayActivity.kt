package com.honestlock.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WarningOverlayActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_STAGE = "stage"
        private const val EXTRA_FINAL = "is_final"

        fun launch(context: Context, stage: Int, isFinal: Boolean) {
            val intent = Intent(context, WarningOverlayActivity::class.java).apply {
                putExtra(EXTRA_STAGE, stage)
                putExtra(EXTRA_FINAL, isFinal)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_overlay)

        val stage = intent.getIntExtra(EXTRA_STAGE, 1)
        val isFinal = intent.getBooleanExtra(EXTRA_FINAL, false)

        val quote = when (stage) {
            1 -> Quotes.random(Quotes.warning1)
            2 -> Quotes.random(Quotes.warning2)
            else -> Quotes.random(Quotes.warningFinal)
        }
        findViewById<TextView>(R.id.warningText).text = quote

        findViewById<Button>(R.id.closeSelfButton).setOnClickListener {
            Prefs(this).clearGate()
            AppMonitorAccessibilityService.closeCurrentApp()
            finish()
        }

        val dismissButton = findViewById<Button>(R.id.dismissButton)
        if (isFinal) dismissButton.text = getString(R.string.warning_final_dismiss)
        dismissButton.setOnClickListener { finish() }
    }
}
