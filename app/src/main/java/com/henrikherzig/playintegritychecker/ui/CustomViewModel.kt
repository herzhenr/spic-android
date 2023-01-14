package com.henrikherzig.playintegritychecker.ui

import androidx.compose.runtime.MutableState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val forceDarkModeKey = booleanPreferencesKey("theme")
    private val serverURL = stringPreferencesKey("server_url")

    val stateTheme = MutableLiveData<Boolean?>(null)
    val stateURL: MutableLiveData<String> = MutableLiveData("")

    fun requestTheme() {
        viewModelScope.launch {
            dataStore.data.collectLatest {
                stateTheme.value = it[forceDarkModeKey]
            }
        }
    }

    fun requestURL() {
        viewModelScope.launch {
            dataStore.data.collectLatest {
                stateURL.value = it[serverURL]
            }
        }
    }

    fun switchToUseSystemSettings(isSystemSettings: Boolean) {
        viewModelScope.launch {
            if (isSystemSettings) {
                dataStore.edit {
                    it.remove(forceDarkModeKey)
                }
            }
        }
    }

    fun switchToUseDarkMode(isDarkTheme: Boolean) {
        viewModelScope.launch {
            dataStore.edit {
                it[forceDarkModeKey] = isDarkTheme
            }
        }
    }

    fun setURL(url: String) {
        if (url == "") {
            removeURL()
            return
        }
        viewModelScope.launch {
            dataStore.edit {
                it[serverURL] = url
            }
        }
    }

    private fun removeURL() {
        viewModelScope.launch {
            dataStore.edit {
                it.remove(serverURL)
            }
        }
    }
}