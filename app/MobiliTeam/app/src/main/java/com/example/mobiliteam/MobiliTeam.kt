package com.example.mobiliteam

import android.app.Application
import android.view.View
import com.example.mobiliteam.data.MyStore
import com.google.android.material.button.MaterialButton
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime


class MobiliTeam : Application() {
    // Volatile stuff
    var auth: String? = null
    var route_left: JSONObject? = null
    var last_signal: LocalDateTime = LocalDateTime.now() - Duration.ofMinutes(10)
    var buttonsControl: MutableList<View> = mutableListOf<View>()

    var ip: String = "10.0.2.2" //CHANGE HERE FROM ipconfig!

    // Permanent stuff
    val store: MyStore = MyStore(this)

    // Support stuff
    val client: OkHttpClient = OkHttpClient()
}