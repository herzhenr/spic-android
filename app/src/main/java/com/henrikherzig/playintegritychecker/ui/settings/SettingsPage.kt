package com.henrikherzig.playintegritychecker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.henrikherzig.playintegritychecker.R
import com.henrikherzig.playintegritychecker.dataStore
import com.henrikherzig.playintegritychecker.ui.*

@Composable
fun Settings(playServiceVersion: String?) {
    // get context, viewModel and stateTheme observer for detecting/setting ui changes
    val context = LocalContext.current
    val viewModel = remember { CustomViewModel(context.dataStore) }
    val themeValue = viewModel.stateTheme.observeAsState().value

    // variable to store state of theme selector
    val themeSelectorMode by remember(themeValue) {
        val mode = when (themeValue) {
            null -> "system"
            true -> "dark"
            false -> "light"
        }
        mutableStateOf(mode)
    }
    // theme selector options
    val system: String = stringResource(id = R.string.settings_theme_system)
    val light: String = stringResource(id = R.string.settings_theme_light)
    val dark: String = stringResource(id = R.string.settings_theme_dark)

    val themeSelectorOptions: List<List<String>> =
        listOf(listOf("system", system), listOf("light", light), listOf("dark", dark))
    val modeChanged: (String) -> Unit = {
        when (it) {
            "system" -> viewModel.switchToUseSystemSettings(true)
            "dark" -> viewModel.switchToUseDarkMode(true)
            "light" -> viewModel.switchToUseDarkMode(false)
        }
    }

    // update viewModel variables on change
    LaunchedEffect(viewModel) {
        viewModel.requestTheme()
        viewModel.requestURL()
    }

    // get url variable for textField
    val urlValue = viewModel.stateURL.observeAsState().value

    // variable for textField to set serverURL
    var text by remember(urlValue) {
        val text = when (urlValue) {
            null -> ""
            else -> urlValue
        }
        mutableStateOf(text)
    }

    // if clicked outside of the TextInputField for entering ServerURL the focus to it should be lost
    val interactionSource = MutableInteractionSource()
    var hideKeyboard by remember { mutableStateOf(false) }

    // define settings UI
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable( // no animation when clicked to lose focus of url textField
                interactionSource = interactionSource,
                indication = null
            ) { hideKeyboard = true }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(all = 12.dp)
        ) {
            DeviceInfoContent(playServiceVersion)
            Spacer(Modifier.size(12.dp))
            CustomCardTitle(stringResource(R.string.settings_title))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                //horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /* workaround: .weight is not accessible in button directly and also not if box
                   is extracted to other method, have to investigate this */
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.3f)
                    //.weight(0.3f)
                    //.absolutePadding(right = 12.dp)
                ) {
                    Text(stringResource(R.string.settings_theme))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    //.absolutePadding(left = 12.dp)
                ) {
                    ToggleGroup(themeSelectorMode, themeSelectorOptions, modeChanged, 35.dp)
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                //horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /* workaround: .weight is not accessible in button directly and also not if box
                   is extracted to other method, have to investigate this */
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.3f)
                    //.weight(0.3f)
                    //.absolutePadding(right = 12.dp)
                ) {
                    Text(stringResource(R.string.settings_url))
                }
                CustomTextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    hideKeyboard = hideKeyboard,
                    onFocusClear = {
                        viewModel.setURL(text)
                        hideKeyboard = false
                    },
                    onSearch = {
                        viewModel.setURL(text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(8.dp)
                        .height(35.dp),
                    placeholder = { Text(stringResource(R.string.settings_url_hint)) }
                )
            }
        }
    }
}