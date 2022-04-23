package io.github.devriesl.foldhelper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.github.devriesl.foldhelper.Constants.FORGET_NEXT_DAY
import io.github.devriesl.foldhelper.Constants.IGNORE_PHONE_MODE_KEY
import io.github.devriesl.foldhelper.Constants.SKIP_RECENT_LAUNCH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(
    val ignorePhoneMode: Boolean,
    val forgetNextDay: Boolean,
    val skipRecentLaunch: Boolean
)

class SettingsViewModel(application: Application) : AndroidViewModel(application)  {
    private val appSharedPrefs: AppSharedPrefs by lazy {
        AppSharedPrefs.getInstance(application)
    }

    private val _uiState = MutableStateFlow(SettingsUiState(
        ignorePhoneMode = appSharedPrefs.getSwitchPreference(IGNORE_PHONE_MODE_KEY),
        forgetNextDay = appSharedPrefs.getSwitchPreference(FORGET_NEXT_DAY),
        skipRecentLaunch = appSharedPrefs.getSwitchPreference(SKIP_RECENT_LAUNCH)
    ))
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun updateIgnorePhoneMode(value: Boolean) {
        appSharedPrefs.setSwitchPreference(IGNORE_PHONE_MODE_KEY, value)
        _uiState.value = _uiState.value.copy(ignorePhoneMode = value)
    }

    fun updateForgetNextDay(value: Boolean) {
        appSharedPrefs.setSwitchPreference(FORGET_NEXT_DAY, value)
        _uiState.value = _uiState.value.copy(forgetNextDay = value)
    }

    fun updateSkipRecentLaunch(value: Boolean) {
        appSharedPrefs.setSwitchPreference(SKIP_RECENT_LAUNCH, value)
        _uiState.value = _uiState.value.copy(skipRecentLaunch = value)
    }
}