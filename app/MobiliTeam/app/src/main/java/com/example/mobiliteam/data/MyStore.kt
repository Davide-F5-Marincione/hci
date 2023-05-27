package com.example.mobiliteam.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class MyStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val RECENT_ROUTES = stringPreferencesKey("recent_routes")
    }

    var recentRoutes: JSONArray = JSONArray()
        get() = runBlocking { JSONArray(context.dataStore.data.map { preferences ->
            preferences[RECENT_ROUTES] ?: "[]"
        }.first()) }

    fun push(new_route: JSONObject) = runBlocking {
        // This is ugly af, I don't care
        val routesCopy = recentRoutes
        var i = routesCopy.length()
        while (i > 0) {
            if (i < 3) routesCopy.put(i, routesCopy[i-1])
            i -= 1
        }
        routesCopy.put(0,new_route)
        context.dataStore.edit { preferences -> preferences[RECENT_ROUTES] = routesCopy.toString() }
    }

    var username: String
        get() = runBlocking { context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }.first() }
        set(newUsername:String) = runBlocking { context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = newUsername
        } }
}