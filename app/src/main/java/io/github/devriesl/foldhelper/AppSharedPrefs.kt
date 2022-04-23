package io.github.devriesl.foldhelper

import android.content.Context
import android.content.SharedPreferences

class AppSharedPrefs private constructor(context: Context) {
    private var sharedPrefs: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val cachedPrefs: HashMap<String, Any> = hashMapOf()

    fun getSwitchPreference(key: String, default: Boolean = false): Boolean {
        return if (cachedPrefs.containsKey(key)) {
            (cachedPrefs[key] as Boolean)
        } else {
            sharedPrefs.getBoolean(key, default).also { cachedPrefs[key] = it }
        }
    }

    fun setSwitchPreference(key: String, value: Boolean) {
        with(sharedPrefs.edit()) {
            putBoolean(key, value)
            commit()
        }
        cachedPrefs[key] = value
    }

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