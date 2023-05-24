package com.example.mobiliteam


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobiliteam.databinding.ActivityTravelBinding
import com.example.mobiliteam.ui.home.actual_route


class TravelActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityTravelBinding
    public var actual_route: actual_route = actual_route()
    public var from : String = ""
    public var to : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        var intent : Intent = getIntent();
        if (intent.hasExtra("from")) {
            Log.d("From", intent.getStringExtra("from").toString())
            from = intent.getStringExtra("from").toString()

        }
        if (intent.hasExtra("to")) {
            Log.d("To", intent.getStringExtra("to").toString())
            to = intent.getStringExtra("to").toString()
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityTravelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_search_layout)

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            val a = navController.navigateUp()
            if (!a) {
                finish()
            }
        })


        val b = intent.extras
        if (b != null)
            if (b.getInt("key") == 0) {
                // Add the "from" and "to" to their respective inputs and make request
            } else {
                // Payload means we have already a route to follow, skip past all the other fragments!
                navController.navigate(R.id.action_SearchFragment_to_PathFollowFragment)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_search_layout)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}