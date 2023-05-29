package com.example.mobiliteam.ui.travel

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.addImage
import com.example.mobiliteam.createDelay
import com.example.mobiliteam.createDuration
import com.example.mobiliteam.databinding.FragmentSearchBinding
import com.example.mobiliteam.extractTime
import com.example.mobiliteam.summaryMaker
import com.google.android.material.button.MaterialButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentSearchFromInputEdit.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed || binding.fragmentSearchToInputEdit.text.toString().isNotEmpty()) {
                        updateRoutes()
                    }
                }
                false // pass on to other listeners.
            }
        )

        binding.fragmentSearchToInputEdit.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed || binding.fragmentSearchFromInputEdit.text.toString()
                            .isNotEmpty()
                    ) {
                        updateRoutes()
                    }
                }
                false // pass on to other listeners.
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        val travelActivity: TravelActivity = activity as TravelActivity

        val a = binding.fragmentSearchFromInput
        a.editText?.setText(travelActivity.from)
        a.addImage("[current-pos]", R.drawable.current_position,
            resources.getDimensionPixelOffset(R.dimen.dp_30),
            resources.getDimensionPixelOffset(R.dimen.dp_30))
        binding.fragmentSearchToInputEdit.setText(travelActivity.to)
        updateRoutes()
    }

    fun updateRoutes() {
        val inflater = activity?.layoutInflater!!

        val linearLayout: LinearLayout = binding.fragmentSearchContainer
        linearLayout.removeAllViews()

        (activity as TravelActivity).from = binding.fragmentSearchFromInputEdit.text.toString()
        (activity as TravelActivity).to = binding.fragmentSearchToInputEdit.text.toString()

        requestRoutes()

        val routes = (activity as TravelActivity).routes

        // Add banner
        val banner = inflater.inflate(R.layout.list_banner, null) as TextView
        banner.text = "SUGGESTED ROUTES:"
        linearLayout.addView(banner)

        for (i:Int in 0 until routes.length()){
            val route = routes.getJSONObject(i)

            val routeCard = inflater.inflate(R.layout.card_search, null)
            routeCard.findViewById<TextView>(R.id.search_card_times).text= extractTime(route.getString("departure_time"))+" - "+ extractTime(route.getString("arrival_time"))
            routeCard.findViewById<TextView>(R.id.search_card_delta).text = createDuration(route)

            routeCard.findViewById<TextView>(R.id.searc_card_info).text = summaryMaker(route)

            // Fill card's horizontal shower of transits
            val transitsShower : LinearLayout = routeCard.findViewById<LinearLayout>(R.id.search_card_container)
            transitsShower.removeAllViews()
            val routeTransit = route.getJSONArray("transits")
            for(j: Int in 0 until routeTransit.length()) {
                val transit = routeTransit.getJSONObject(j)
                val transportView = inflater.inflate(R.layout.transit_show_small, null)
                transportView.findViewById<TextView>(R.id.small_transit_desc).text = transit.getString("transit_line")
                val delay = createDelay(transit)
                if (delay != null) {
                    transportView.findViewById<TextView>(R.id.small_transit_delay).text = delay
                } else {
                    transportView.findViewById<TextView>(R.id.small_transit_delay).visibility = View.GONE
                }
                when (transit.getString("transit_type")) {
                    "tram" -> transportView.findViewById<ImageView>(R.id.small_transit_img).setImageResource(R.drawable.tram)
                    "bus" -> transportView.findViewById<ImageView>(R.id.small_transit_img).setImageResource(R.drawable.bus)
                }
                transportView.findViewById<ImageView>(R.id.small_transit_img).setColorFilter(Color.parseColor(transit.getString("transit_color")))
                transitsShower.addView(transportView)
                if (j < routeTransit.length() - 1) {
                    transitsShower.addView(inflater.inflate(R.layout.da_arrow, null))
                }
            }

            routeCard.setOnClickListener{
                (activity as TravelActivity).viewingRoute = route
                findNavController().navigate(R.id.action_SearchFragment_to_SelectFragment)
            }
            routeCard.findViewById<MaterialButton>(R.id.search_card_select_button).setOnClickListener{
                (activity as TravelActivity).viewingRoute = route
                (activity?.application as MobiliTeam).route_left = route
                (activity?.application as MobiliTeam).store.push(route)
                findNavController().navigate(R.id.action_SearchFragment_to_PathFollowFragment)
            }

            linearLayout.addView(routeCard)
        }
    }

    fun requestRoutes() {

        val url ="http://"+ (activity?.application as MobiliTeam).ip +":5000/route";// Replace with your API endpoint

        val jsonObject = JSONObject()

        var clearFrom = binding.fragmentSearchFromInputEdit.text.toString().substringAfter("[current-pos] ")

        jsonObject.put("from", clearFrom)
        jsonObject.put("to", binding.fragmentSearchToInputEdit.text)

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").put(jsonObject.toString().toRequestBody()).build()

        (activity as TravelActivity).routes = JSONArray()
        val countDownLatch = CountDownLatch(1)

        (activity?.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        (activity as TravelActivity).routes = JSONArray(response.body?.string())
                    }
                    404 -> {
                    }
                    500 -> {
                    }
                }
                response.close()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()
    }
}