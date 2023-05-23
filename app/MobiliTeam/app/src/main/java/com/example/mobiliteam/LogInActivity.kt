package com.example.mobiliteam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class LogInActivity : AppCompatActivity() {
    var client: OkHttpClient = OkHttpClient()
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
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Do some funky stuff
                launchLogin()
            }
        }
        startForResult.launch(intent)
    }

    fun launchLogin() {
        // take value from username field
        val username = findViewById<TextView>(R.id.usernameInput).text.toString()
        // perform a get request to the server to check if the username exists
        // if it does, then go to the home page
        // else, display an error message
        val url ="http://127.0.0.1:5000/users/"+ username;// Replace with your API endpoint

        val request: Request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = client.newCall(request).execute()
            val responseData = response.body!!.string()

            //if there is a response, then the username exists
            if (responseData != null) {
                // go to the home page
                val intent = Intent(this@LogInActivity, HomeActivity::class.java)
                startActivity(intent)
            } else {
                // display an error message
                val errorMessage = findViewById<TextView>(R.id.errorMessage)
                errorMessage.text = "Username does not exist"
            }
        } catch (e: IOException) {
            //display an error message
            val errorMessage = findViewById<TextView>(R.id.errorMessage)
            e.printStackTrace()
        }
        val intent = Intent(this@LogInActivity, HomeActivity::class.java)
        startActivity(intent)
    }
}