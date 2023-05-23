package com.example.mobiliteam

import android.app.Application
import okhttp3.OkHttpClient

class MobiliTeam : Application() {
    var auth: String? = null
    val client: OkHttpClient = OkHttpClient()
}