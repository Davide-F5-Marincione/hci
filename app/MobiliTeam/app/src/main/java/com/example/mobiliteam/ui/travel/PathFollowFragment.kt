package com.example.mobiliteam.ui.travel

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.createDelay
import com.example.mobiliteam.databinding.FragmentPathFollowBinding
import com.example.mobiliteam.extractTime
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PathFollowFragment : Fragment() {

    private var _binding: FragmentPathFollowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPathFollowBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentPathEndRoute.setOnClickListener {
            (activity?.application as MobiliTeam).route_left = null
            (activity as TravelActivity).finish()
        }

        (activity as TravelActivity).binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            (activity as TravelActivity).finish()
        })
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
        binding.fragmentPathFromTo.text = "From: " + route.getString("from") + " To: " + route.getString("to")
        binding.fragmentPathTimes.text = extractTime(route.getString("departure_time")) + " - " + extractTime(route.getString("arrival_time"))

        val now = LocalDateTime.now()

        val linearLayout : LinearLayout = binding.fragmentPathContainer
        linearLayout.removeAllViews()

        val transits = route.getJSONArray("transits")

        for (i: Int in 0 until transits.length()) {
            val transit = transits.getJSONObject(i)
            val stops = transit.getJSONArray("stops")
            val firstStop = stops.getJSONObject(0)
            val lastStop = stops.getJSONObject(stops.length() - 1)

            val cardSelect = inflater.inflate(R.layout.card_path, null)
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
                if (imButton.rotation > 90f) { // Is open
                    imButton.rotation = 0f
                    stopsShower.visibility = View.GONE
                } else { // Is closed
                    imButton.rotation = 180f
                    stopsShower.visibility = View.VISIBLE
                }
            }

            val overcrowded = cardSelect.findViewById<MaterialButton>(R.id.path_card_overcrowdedButton)
            val notOvercrowded = cardSelect.findViewById<MaterialButton>(R.id.path_card_notOvercrowdedButton)

            overcrowded.setOnClickListener {
                signalToServer(transit.getString("transit_name"), true)
            }

            notOvercrowded.setOnClickListener {
                signalToServer(transit.getString("transit_name"), false)
            }

            linearLayout.addView(cardSelect)
        }
    }

    fun signalToServer(transit_name: String, overcrowded: Boolean) {
        // TO IMPLEMENT
    }
}