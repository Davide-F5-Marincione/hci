package com.example.mobiliteam.data

import android.content.Context
import android.os.Bundle
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

class RecentRoutesStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("recent_routes")
        private val RECENT_ROUTES = stringPreferencesKey("recent_routes")
    }

    private val _recentRoutes: JSONArray = runBlocking {
        val strJson = context.dataStore.data.map { preferences ->
            preferences[RECENT_ROUTES] ?: "" }.first()
        JSONArray(strJson) }

    val recentRoutes: JSONArray = _recentRoutes

    fun push(new_route: JSONObject) = runBlocking {
        // This is ugly af, I don't care
        var i = recentRoutes.length()
        while (i > 0) {
            if (i < 3) recentRoutes.put(i, recentRoutes[i-1])
            i -= 1
        }
        recentRoutes.put(0, new_route)
        context.dataStore.edit { preferences -> preferences[RECENT_ROUTES] = recentRoutes.toString() }
    }
}