package com.example.mobiliteam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val registerGoTo = findViewById<TextView>(R.id.registerGoTo)
        registerGoTo.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        val logInButton = findViewById<MaterialButton>(R.id.logInButton)
        logInButton.setOnClickListener {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}