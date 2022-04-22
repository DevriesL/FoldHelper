package io.github.devriesl.foldhelper

import android.content.Context
import android.content.SharedPreferences

class AppSharedPrefs private constructor(context: Context) {
    private var sharedPrefs: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        const val SHARED_PREFS_NAME = "fold_helper"

        @Volatile
        private var instance: AppSharedPrefs? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: AppSharedPrefs(context).also { instance = it }
            }
    }
}