package com.example.mobiliteam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.ImageView
import com.google.android.material.button.MaterialButton

class RecoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recover_username)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this@RecoverActivity, LogInActivity::class.java)
            startActivity(intent)
        }

        val submitButton = findViewById<MaterialButton>(R.id.submitButton)
        submitButton.setOnClickListener {
            val intent = Intent(this@RecoverActivity, LogInActivity::class.java)
            startActivity(intent)
        }
    }
}