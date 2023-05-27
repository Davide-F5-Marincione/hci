package com.example.mobiliteam

import android.app.Application
import com.example.mobiliteam.data.MyStore
import okhttp3.OkHttpClient
import org.json.JSONObject


class MobiliTeam : Application() {
    // Volatile stuff
    var auth: String? = null
    var route_left: JSONObject? = null

    // Permanent stuff
    val store: MyStore = MyStore(this)

    // Support stuff
    val client: OkHttpClient = OkHttpClient()
}