package com.example.mobiliteam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobiliteam.databinding.ActivityTravelBinding
import com.google.android.material.appbar.MaterialToolbar


class TravelActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityTravelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityTravelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_search_layout)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
//        val toolbarIcon = toolbar.getChildAt(0)
//        toolbarIcon.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this@TravelActivity, HomeActivity::class.java)
//            startActivity(intent)
//        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_search_layout)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}