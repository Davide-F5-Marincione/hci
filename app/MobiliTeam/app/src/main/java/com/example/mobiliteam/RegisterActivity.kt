package com.example.mobiliteam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        val registerGoTo = findViewById<ImageView>(R.id.backButton)
        registerGoTo.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LogInActivity::class.java)
            startActivity(intent)
        }
    }
}