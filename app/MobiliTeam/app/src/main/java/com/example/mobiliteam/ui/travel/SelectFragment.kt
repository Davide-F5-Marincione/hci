package com.example.mobiliteam.ui.travel

import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.createDelay
import com.example.mobiliteam.databinding.FragmentSelectBinding
import com.example.mobiliteam.extractTime
import com.google.android.material.timepicker.TimeFormat
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SelectFragment : Fragment() {

    private var _binding: FragmentSelectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentSelectButton.setOnClickListener {
            (activity?.application as MobiliTeam).route_left = (activity as TravelActivity).viewingRoute
            (activity?.application as MobiliTeam).store.push((activity as TravelActivity).viewingRoute)
            findNavController().navigate(R.id.action_SelectFragment_to_PathFollowFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        val inflater = activity?.layoutInflater!!

        val travelActivity: TravelActivity = activity as TravelActivity

        val route = travelActivity.viewingRoute
        binding.fragmentSelectFromTo.text = "From: " + route.getString("from") + " To: " + route.getString("to")
        binding.fragmentSelectTimes.text = extractTime(route.getString("departure_time")) + " - " + extractTime(route.getString("arrival_time"))


        val linearLayout : LinearLayout = binding.fragmentSelectContainer
        linearLayout.removeAllViews()

        val now = LocalDateTime.now()

        val transits = route.getJSONArray("transits")

        for (i: Int in 0 until transits.length()) {
            val transit = transits.getJSONObject(i)
            val stops = transit.getJSONArray("stops")
            val firstStop = stops.getJSONObject(0)
            val lastStop = stops.getJSONObject(stops.length() - 1)

            val cardSelect = inflater.inflate(R.layout.card_select, null)
            cardSelect.findViewById<TextView>(R.id.base_actualFrom).text = firstStop.getString("stop-name") + " - " + extractTime(firstStop.getString("time"))
            cardSelect.findViewById<TextView>(R.id.base_actualTo).text = lastStop.getString("stop-name") + " - " + extractTime(lastStop.getString("time"))

            val last_seen = LocalDateTime.parse(transit.getString("last_seen"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val diff = Duration.between(last_seen, now).toMinutes()

            cardSelect.findViewById<View>(R.id.base_OCImage).visibility = View.INVISIBLE
            cardSelect.findViewById<View>(R.id.base_OCText).visibility = View.INVISIBLE
            cardSelect.findViewById<View>(R.id.base_nCBImage).visibility = View.INVISIBLE
            cardSelect.findViewById<View>(R.id.base_nCBText).visibility = View.INVISIBLE

            if (diff > 10) {
                cardSelect.findViewById<View>(R.id.base_nCBImage).visibility = View.VISIBLE
                cardSelect.findViewById<View>(R.id.base_nCBText).visibility = View.VISIBLE
            } else if (transit.getDouble("crowdedness_mu") > .8f) {
                cardSelect.findViewById<View>(R.id.base_OCImage).visibility = View.VISIBLE
                cardSelect.findViewById<View>(R.id.base_OCText).visibility = View.VISIBLE
            }

            // Fill card's horizontal shower of transits
            val transitsShower = cardSelect.findViewById<ConstraintLayout>(R.id.base_transit)
            transitsShower.findViewById<TextView>(R.id.big_transit_desc).text = transit.getString("transit_line")
            val delay = createDelay(transit)
            if (delay != null) {
                transitsShower.findViewById<TextView>(R.id.big_transit_delay).text = delay
            } else {
                transitsShower.findViewById<TextView>(R.id.big_transit_delay).visibility = View.GONE
            }
            when (transit.getString("transit_type")) {
                "tram" -> transitsShower.findViewById<ImageView>(R.id.big_transit_img).setImageResource(R.drawable.tram)
                "bus" -> transitsShower.findViewById<ImageView>(R.id.big_transit_img).setImageResource(R.drawable.bus)
            }
            transitsShower.findViewById<ImageView>(R.id.big_transit_img).setColorFilter(Color.parseColor(transit.getString("transit_color")))

            val stopsShower = cardSelect.findViewById<LinearLayout>(R.id.base_container)
            stopsShower.removeAllViews()

            for (j: Int in 0 until stops.length()) {
                val stop = stops.getJSONObject(j)
                val elem = inflater.inflate(R.layout.card_stops_item, null)

                elem.findViewById<TextView>(R.id.stop_item_text).text = stop.getString("stop-name")

                val img = elem.findViewById<ImageView>(R.id.stop_item_img)

                if (j >= stops.length() - 1) {
                    img.setImageResource(R.drawable.line_end)
                }

                img.setColorFilter(Color.parseColor(transit.getString("transit_color")))
                stopsShower.addView(elem)
            }

            val imButton = cardSelect.findViewById<ImageButton>(R.id.base_wedgeButton)
            imButton.rotation = 0f
            stopsShower.visibility = View.GONE

            // Add actions to card
            imButton.setOnClickListener{
                if (imButton.rotationX > 90f) { // Is open
                    imButton.animate().rotationX(0f).setDuration(300)
                    stopsShower.animate().scaleY(0f).setDuration(300)
                    stopsShower.visibility = View.GONE
                } else { // Is closed
                    imButton.animate().rotationX(180f).setDuration(300)
                    stopsShower.animate().scaleY(1f).setDuration(300)
                    stopsShower.visibility = View.VISIBLE
                }
            }

            linearLayout.addView(cardSelect)
        }
    }
}