package com.example.tasktracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

object PreferenceKeys {
    val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    val USER_NAME = stringPreferencesKey("user_name")
}

class UserPreferences(private val context: Context) {

    // Flow: true if the user has never entered their name, show Card
    val isFirstTimeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PreferenceKeys.IS_FIRST_TIME] ?: true }

    // Flow: the saved user name, empty if never set
    val userNameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[PreferenceKeys.USER_NAME] ?: "" }

    // Save name (call inside coroutine)
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.USER_NAME] = name
            prefs[PreferenceKeys.IS_FIRST_TIME] = false
        }
    }

    // Set first-time status (for logout or reset)
    suspend fun setFirstTime(isFirstTime: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_FIRST_TIME] = isFirstTime
            if (isFirstTime) prefs[PreferenceKeys.USER_NAME] = ""
        }
    }
}
