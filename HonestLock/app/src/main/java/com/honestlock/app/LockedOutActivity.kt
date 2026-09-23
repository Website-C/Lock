package com.honestlock.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class LockedOutActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_REMAINING_MS = "remaining_ms"

        fun launch(context: Context, remainingMs: Long) {
            val intent = Intent(context, LockedOutActivity::class.java).apply {
                putExtra(EXTRA_REMAINING_MS, remainingMs)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locked_out)

        val remainingMs = intent.getLongExtra(EXTRA_REMAINING_MS, 0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) + 1

        findViewById<TextView>(R.id.lockedQuoteText).text = Quotes.random(Quotes.lockedOut)
        findViewById<TextView>(R.id.lockedTimeText).text =
            getString(R.string.locked_time_remaining, minutes)

        findViewById<Button>(R.id.okButton).setOnClickListener { finish() }
    }
}
