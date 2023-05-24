package com.example.mobiliteam

import android.app.Application
import com.example.mobiliteam.data.SettingsStore
import okhttp3.OkHttpClient

class MobiliTeam : Application() {
    var auth: String? = null
    val store: SettingsStore = SettingsStore(this)
    val client: OkHttpClient = OkHttpClient()
}