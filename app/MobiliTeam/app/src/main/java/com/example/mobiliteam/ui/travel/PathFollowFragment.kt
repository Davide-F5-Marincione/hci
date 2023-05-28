package com.example.mobiliteam.ui.travel

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.ProgressBarAnimation
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.createDelay
import com.example.mobiliteam.databinding.FragmentPathFollowBinding
import com.example.mobiliteam.extractTime
import com.google.android.material.button.MaterialButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch


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
            (activity?.application as MobiliTeam).buttonsControl.clear()
        }

        (activity as TravelActivity).binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            (activity as TravelActivity).finish()
            (activity?.application as MobiliTeam).buttonsControl.clear()
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

            cardSelect.findViewById<ProgressBar>(R.id.path_card_ocProgress).visibility = View.INVISIBLE
            cardSelect.findViewById<ProgressBar>(R.id.path_card_nOCProgress).visibility = View.INVISIBLE
            val overcrowded = cardSelect.findViewById<MaterialButton>(R.id.path_card_overcrowdedButton)
            val notOvercrowded = cardSelect.findViewById<MaterialButton>(R.id.path_card_notOvercrowdedButton)

            overcrowded.setOnClickListener {
                if (canDo()) {
                    val done = signalToServer(transit.getString("transit_name"), true)
                    if (done) {
                        (activity?.application as MobiliTeam).last_signal = LocalDateTime.now()
                        // Animation single button
                        // Disable all
                        disableAll()
                    }
                }
            }

            notOvercrowded.setOnClickListener {
                if (canDo()) {
                    val done = signalToServer(transit.getString("transit_name"), false)
                    if (done) {
                        (activity?.application as MobiliTeam).last_signal = LocalDateTime.now()
                        // Animation single button
                        // Disable all
                        disableAll()
                    }
                }
            }

            (activity?.application as MobiliTeam).buttonsControl.add(cardSelect as View)
            linearLayout.addView(cardSelect)
        }

        val diff2 = Duration.between((activity?.application as MobiliTeam).last_signal, now).seconds

        if (diff2 < 120) {
            disableAll(diff2.toFloat())
        }
    }

    fun disableAll(progress: Float = 0f) {
        for (view in (activity?.application as MobiliTeam).buttonsControl) {
            val overcrowded = view.findViewById<MaterialButton>(R.id.path_card_overcrowdedButton)
            val notOvercrowded = view.findViewById<MaterialButton>(R.id.path_card_notOvercrowdedButton)
            val progOver = view.findViewById<ProgressBar>(R.id.path_card_ocProgress)
            val progNotOver = view.findViewById<ProgressBar>(R.id.path_card_nOCProgress)

            progOver.visibility = View.VISIBLE
            progNotOver.visibility = View.VISIBLE

            val diff = ((120f - progress)* 1000).toLong()

            val animProg = ProgressBarAnimation(progOver, progress, 120f)
            animProg.duration = diff
            val animNotProg = ProgressBarAnimation(progNotOver, progress, 120f)
            animNotProg.duration = diff

            overcrowded.setBackgroundColor(Color.parseColor("#FF181818"))
            overcrowded.setTextColor(Color.parseColor("#FF828282"))

            notOvercrowded.setBackgroundColor(Color.parseColor("#FF181818"))
            notOvercrowded.setTextColor(Color.parseColor("#FF828282"))

            progNotOver.startAnimation(animNotProg)
            progOver.startAnimation(animProg)

            progNotOver.postDelayed({
                progNotOver.visibility = View.INVISIBLE
                notOvercrowded.setBackgroundColor(Color.parseColor("#FF82B955"))
                notOvercrowded.setTextColor(Color.parseColor("#FFF8F8F8"))
            }, diff)
            progOver.postDelayed({
                progOver.visibility = View.INVISIBLE
                overcrowded.setBackgroundColor(Color.parseColor("#FF82B955"))
                overcrowded.setTextColor(Color.parseColor("#FFF8F8F8"))
            }, diff)
        }
    }

    fun canDo() : Boolean {
        val now = LocalDateTime.now()
        val last = (activity?.application as MobiliTeam).last_signal
        val diff = Duration.between(last, now).seconds
        return diff > 120
    }

    fun signalToServer(transit_name: String, overcrowded: Boolean): Boolean {

        Log.d("Reporting", (activity?.application as MobiliTeam).store.username + " is reporting " + transit_name + " as overcrowded=" + overcrowded.toString())

        //check if the username is 'admin' then directly go to the home page
        if((activity?.application as MobiliTeam).store.username == "admin"){
            // Do something if Admin
            return false
        }
        val url ="http://10.0.2.2:5000/buses/" + transit_name + "/signal";

        val jsonObject = JSONObject()
        jsonObject.put("is_overcrowded", overcrowded)

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").header("Authorization", "Bearer " + (activity?.application as MobiliTeam).auth).put(jsonObject.toString().toRequestBody()).build()


        var ok  = false
        val countDownLatch = CountDownLatch(1)

        (activity?.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response
            ) {
                when (response.code) {
                    200 -> {
                        ok = true
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

        return ok
    }
}