package com.example.mobiliteam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class UsernameStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    var username: String
        get() = runBlocking { context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }.first() }
        set(newUsername:String) = runBlocking { context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = newUsername
        } }
}