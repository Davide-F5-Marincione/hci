package com.example.mobiliteam

import android.app.Application
import com.example.mobiliteam.data.RecentRoutesStore
import com.example.mobiliteam.data.UsernameStore
import okhttp3.OkHttpClient
import org.json.JSONObject

class MobiliTeam : Application() {
    // Volatile stuff
    var auth: String? = null
    var route_left: JSONObject? = null

    // Permanent stuff
    val usernameStore: UsernameStore = UsernameStore(this)
    val recentRoutesStore: RecentRoutesStore = RecentRoutesStore(this)

    // Support stuff
    val client: OkHttpClient = OkHttpClient()
}