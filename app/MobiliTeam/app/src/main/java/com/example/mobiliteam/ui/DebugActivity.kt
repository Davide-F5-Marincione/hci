package com.example.mobiliteam
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class DebugActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

       var view=layoutInflater.inflate(R.layout.activity_debug, null)
        setContentView(view)

        val IP  = findViewById<TextInputLayout>(R.id.IP)
        //remove the return key
        IP.editText?.setSingleLine(false)
        IP.editText?.maxLines = 1
        IP.editText?.setText(MobiliTeam.ip)


        val button  = findViewById<Button>(R.id.DebugConfirm)

        button.setOnClickListener {
            val ip = IP.editText?.text.toString()
            MobiliTeam.ip = ip
            finish()
        }
        val toolbar=findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener {
            finish()
        }


    }

}