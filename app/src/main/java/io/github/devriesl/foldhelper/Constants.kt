package io.github.devriesl.foldhelper

object Constants {
    val APP_BLACK_LIST: List<String> = listOf(
        "systemui",
        "launcher2"
    )

    const val IGNORE_PHONE_MODE_KEY = "ignore_phone_mode"
    const val FORGET_NEXT_DAY = "forget_next_day"
    const val SKIP_RECENT_LAUNCH = "skip_recent_launch"
}