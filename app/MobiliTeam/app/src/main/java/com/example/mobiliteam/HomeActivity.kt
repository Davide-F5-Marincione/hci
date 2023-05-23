package com.example.mobiliteam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.mobiliteam.data.SettingsStore
import com.example.mobiliteam.databinding.ActivityHomeBinding
import com.example.mobiliteam.ui.home.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch





private val TAB_TITLES = arrayOf(
    "Travel",
    "Profile",
    "Donations"
)

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    val _store = SettingsStore(this)

    val logInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val username = intent?.getStringExtra("username")

            Log.d("GivenUsername", username.toString())
            _store.username = username.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (this.application as MobiliTeam).auth = logIn(_store.username)
        if ((this.application as MobiliTeam).auth == null) {
            launchLogIn()
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, this)
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()
    }

    fun launchLogIn() {
        val intent = Intent(this@HomeActivity, LogInActivity::class.java)
        logInResult.launch(intent)
    }

    fun logIn(username: String): String? {
        if (username == "") return null

        val url ="http://10.0.2.2:5000/session";// Replace with your API endpoint

        val jsonObject = JSONObject()
        jsonObject.put("username", username)

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").post(jsonObject.toString().toRequestBody()).build()

        var auth: String? = null
        val countDownLatch = CountDownLatch(1)

        (this.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        auth = JSONObject(response.body?.string()).getString("auth").toString()
                    }
                    404 -> {
                    }
                    500 -> {
                    }
                }
                response.close()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()
        Log.d("Logged_in_info", "logged user=" + username + ", with auth=" + auth)
        return auth
    }
}