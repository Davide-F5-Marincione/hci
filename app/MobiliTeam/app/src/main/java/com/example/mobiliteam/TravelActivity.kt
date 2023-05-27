package com.example.mobiliteam


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobiliteam.databinding.ActivityTravelBinding
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TravelActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityTravelBinding
    var from : String = ""
    var to : String = ""
    var routes: JSONArray = JSONArray()
    var viewingRoute: JSONObject = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {

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
                // Just add this stuff
                from = intent.getStringExtra("from").toString()
                to = intent.getStringExtra("to").toString()
            } else {
                // Payload means we have already a route to follow, skip past all the other fragments!
                viewingRoute = (this.application as MobiliTeam).route_left!!
                from = viewingRoute.getString("from")
                to = viewingRoute.getString("to")
                navController.navigate(R.id.action_SearchFragment_to_PathFollowFragment)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_search_layout)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}