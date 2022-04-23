package io.github.devriesl.foldhelper

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel

@ExperimentalMaterialApi
@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState = settingsViewModel.uiState.collectAsState()

    Column {
        ListItem(
            text = { Text(stringResource(R.string.ignore_phone_mode_switch_title)) },
            secondaryText =  { Text(stringResource(R.string.ignore_phone_mode_switch_desc)) },
            trailing = {
                Switch(
                    checked = uiState.value.ignorePhoneMode,
                    onCheckedChange = { settingsViewModel.updateIgnorePhoneMode(it) }
                )
            }
        )
        ListItem(
            text = { Text(stringResource(R.string.forget_next_day_switch_title)) },
            secondaryText =  { Text(stringResource(R.string.forget_next_day_switch_desc)) },
            trailing = {
                Switch(
                    checked = uiState.value.forgetNextDay,
                    onCheckedChange = { settingsViewModel.updateForgetNextDay(it) }
                )
            }
        )
        ListItem(
            text = { Text(stringResource(R.string.skip_recent_launch_switch_title)) },
            secondaryText =  { Text(stringResource(R.string.skip_recent_launch_switch_desc)) },
            trailing = {
                Switch(
                    checked = uiState.value.skipRecentLaunch,
                    onCheckedChange = { settingsViewModel.updateSkipRecentLaunch(it) }
                )
            }
        )
    }
}