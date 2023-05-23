package com.example.mobiliteam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch


class LogInActivity : AppCompatActivity() {

    val registerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data

            val usernameInput = findViewById<TextInputLayout>(R.id.usernameInput)
            usernameInput.editText?.setText(intent?.getStringExtra("username"))

            launchLogin()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val registerGoTo = findViewById<TextView>(R.id.registerGoTo)
        registerGoTo.setOnClickListener {
            launchRegistration()
        }

        val forgotGoTo = findViewById<TextView>(R.id.forgotGoTo)
        forgotGoTo.setOnClickListener {
            val intent = Intent(this@LogInActivity, RecoverActivity::class.java)
            startActivity(intent)
        }


        val logInButton = findViewById<MaterialButton>(R.id.registerButton)
        logInButton.setOnClickListener {
            launchLogin()
        }
    }

    fun launchRegistration() {
        val intent = Intent(this@LogInActivity, RegisterActivity::class.java)
        registerResult.launch(intent)
    }

    fun launchLogin() {
        // take value from username field
        val username = findViewById<TextInputLayout>(R.id.usernameInput).editText?.text.toString()
        // perform a get request to the server to check if the username exists
        // if it does, then go to the home page
        // else, display an error message

       //check if the username is 'admin' than directly go to the home page
        if(username == "admin"){
            val intent = Intent()
            intent.putExtra("username", username)
            setResult(RESULT_OK, intent)
            finish()
        }
        val url ="http://10.0.2.2:5000/session";// Replace with your API endpoint

        val jsonObject = JSONObject()
        jsonObject.put("username", username)

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").post(jsonObject.toString().toRequestBody()).build()

        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        val mHandler = Handler(Looper.getMainLooper())
        var auth: String? = null
        val countDownLatch = CountDownLatch(1)

        (this.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response
            ) {
                when (response.code) {
                    200 -> {
                        auth = JSONObject(response.body?.string()).getString("auth").toString()
                    }
                    404 -> {
//                        mHandler.post(Runnable {
//                            errorMessage.text = "User not found"
//                        })
                    }
                    500 -> {
                        mHandler.post(Runnable {
                            errorMessage.text = "Internal server error"
                        })
                    }
                }
                response.close()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call, e: IOException) {
                mHandler.post(Runnable {
                    errorMessage.text = "Internal server error"
                })
                e.printStackTrace()
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()

        if (auth != null) {
            val intent = Intent()
            intent.putExtra("username", username)
            (application as MobiliTeam).auth = auth
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}