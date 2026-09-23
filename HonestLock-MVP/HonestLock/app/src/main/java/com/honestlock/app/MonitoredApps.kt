package com.honestlock.app

/**
 * Package names of the apps this MVP locks. If you use regional variants
 * (e.g. Facebook Lite = com.facebook.lite) add them here.
 */
object MonitoredApps {
    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"
    const val FACEBOOK = "com.facebook.katana"

    val ALL = setOf(INSTAGRAM, YOUTUBE, FACEBOOK)
}
