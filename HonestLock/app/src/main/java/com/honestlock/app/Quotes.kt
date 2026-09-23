package com.honestlock.app

/**
 * All user-facing "harsh honesty" copy lives here so you can edit tone
 * without touching any logic.
 */
object Quotes {
    val gate = listOf(
        "You already know why you're reaching for this. Say it, or put the phone down.",
        "Nobody is forcing you to open this. Be honest about why you are.",
        "This isn't about the app. It's about what you're avoiding.",
        "You can lie to this screen. You can't lie to the hour you're about to lose.",
        "Be straight with yourself for once. What are you actually doing right now?"
    )

    val warning1 = listOf(
        "Thirty minutes gone. You felt that scroll, but did you feel the time?",
        "You told yourself 'just a few minutes.' That was thirty minutes ago.",
        "This is the moment most people pretend not to notice. Don't be most people."
    )

    val warning2 = listOf(
        "Forty-five minutes. You saw the first warning and kept going anyway.",
        "You're not relaxing anymore. You're avoiding something. Go deal with it.",
        "Every extra minute here is a minute you'll complain about not having later."
    )

    val warningFinal = listOf(
        "Last warning. In one minute this closes itself, because you won't.",
        "You had two chances to stop yourself. This is the app stopping you instead.",
        "Fifty-five minutes of your day, gone. This ends now, one way or another."
    )

    val lockedOut = listOf(
        "Locked. You had the warnings. Go do something that isn't this.",
        "This door is closed for a while. Use the time on something real.",
        "The apps are locked. That was the deal you agreed to when you were being honest."
    )

    fun random(list: List<String>): String = list.random()
}
