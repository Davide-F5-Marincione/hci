package com.example.mobiliteam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED, Intent())
            finish()
        }

        val registerButton = findViewById<MaterialButton>(R.id.registerButton)
        registerButton.setOnClickListener {
            tryRegister()
        }
    }

    fun tryRegister() {
        // take value from username field
        val username = findViewById<TextInputLayout>(R.id.usernameInput).editText?.text.toString()
        // perform a get request to the server to check if the username exists
        // if it does, then go to the home page
        // else, display an error message

        // Davide - Must change as the endpoint for login is now on /session
        val url ="http://"+ MobiliTeam.ip + ":5000/users";// Replace with your API endpoint

        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("firstname", findViewById<TextInputLayout>(R.id.firstNameInput).editText?.text.toString())
        jsonObject.put("lastname", findViewById<TextInputLayout>(R.id.lastNameInput).editText?.text.toString())
        jsonObject.put("email", findViewById<TextInputLayout>(R.id.emailInput).editText?.text.toString())

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").post(jsonObject.toString().toRequestBody()).build()

        var ok = false
        val countDownLatch = CountDownLatch(1)

        (this.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    204 -> {
                        ok = true
                    }
                    403 -> {
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

        if (ok) {
            val intent = Intent()
            intent.putExtra("username", username)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}