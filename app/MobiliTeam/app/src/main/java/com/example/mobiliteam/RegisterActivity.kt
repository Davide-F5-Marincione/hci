package com.example.mobiliteam

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class RegisterActivity : AppCompatActivity() {

    val client: OkHttpClient = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
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
        val url ="http://127.0.0.1:5000/user";// Replace with your API endpoint

        val jsonObject = JSONObject()
            jsonObject.put("username", username)
            jsonObject.put("firstname", findViewById<TextInputLayout>(R.id.firstNameInput).editText?.text.toString())
            jsonObject.put("lastname", findViewById<TextInputLayout>(R.id.lastNameInput).editText?.text.toString())
            jsonObject.put("email", findViewById<TextInputLayout>(R.id.emailInput).editText?.text.toString())

        val request: Request = Request.Builder()
            .url(url).post(jsonObject.toString().toRequestBody()).build()

        try {

            val response = client.newCall(request).execute()
            when (response.code) {
                204 -> {
                    val intent = Intent()
                    intent.putExtra("username", username)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                403 -> {
                    val errorMessage = findViewById<TextView>(R.id.errorMessage)
                    errorMessage.text = "Username already exists"
                }
                500 -> {
                    val errorMessage = findViewById<TextView>(R.id.errorMessage)
                    errorMessage.text = "Internal server error"
                }
            }

        } catch (e: IOException) {
            //display an error message
            val errorMessage = findViewById<TextView>(R.id.errorMessage)
            e.printStackTrace()
        }
    }
}