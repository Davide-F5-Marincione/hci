package com.example.mobiliteam.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.VerticalImageSpan
import com.example.mobiliteam.addImage
import com.example.mobiliteam.createDelay
import com.example.mobiliteam.databinding.FragmentTravelBinding
import com.example.mobiliteam.extractTime
import com.example.mobiliteam.summaryMaker
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

class HomeTravelFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentTravelBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewEdit = binding.fragmentTravelFromInput
        textViewEdit.editText?.setText("[current-pos] Vicolo Corto")
        binding.fragmentTravelFromInput.addImage("[current-pos]", R.drawable.current_position,
            resources.getDimensionPixelOffset(R.dimen.dp_30),
            resources.getDimensionPixelOffset(R.dimen.dp_30))

        binding.fragmentTravelFromInputEdit.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed || binding.fragmentTravelToInputEdit.text.toString().isNotEmpty()) {
                        searchRoute(binding.fragmentTravelFromInputEdit.text.toString(), binding.fragmentTravelToInputEdit.text.toString())
                    }
                }
                false // pass on to other listeners.
            }
        )

        binding.fragmentTravelToInputEdit.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed || binding.fragmentTravelFromInputEdit.text.toString().isNotEmpty()) {
                        searchRoute(binding.fragmentTravelFromInputEdit.text.toString(), binding.fragmentTravelToInputEdit.text.toString())
                    }
                }
                false // pass on to other listeners.
            }
        )
    }

    override fun onResume() {
        super.onResume()

        val inflater = activity?.layoutInflater!!

        val linearLayout: LinearLayout = binding.fragmentTravelContainer
        linearLayout.removeAllViews()


        //Check if actual Route is not null, if its not print it and add it to card_left
        if ((activity?.application as MobiliTeam).route_left!=null){
            val route = (activity?.application as MobiliTeam).route_left!!

            // Add banner
            val banner = inflater.inflate(R.layout.list_banner, null) as TextView
            banner.text = "WHERE YOU LEFT"
            linearLayout.addView(banner)

            // Create card
            val cardLeftBinding = inflater.inflate(R.layout.card_left, null)
            cardLeftBinding.findViewById<TextView>(R.id.left_card_actualFrom).text = route.getString("from")
            cardLeftBinding.findViewById<TextView>(R.id.left_card_actualTo).text = route.getString("to")
            cardLeftBinding.findViewById<TextView>(R.id.left_card_times).text = extractTime(route.getString("departure_time"))+" - "+ extractTime(route.getString("arrival_time"))

            cardLeftBinding.findViewById<TextView>(R.id.left_card_info).text = summaryMaker(route)

            // Fill card's horizontal shower of transits
            val transitsShower : LinearLayout = cardLeftBinding.findViewById<LinearLayout>(R.id.left_card_container)
            transitsShower.removeAllViews()
            val routeTransit = route.getJSONArray("transits")
            for(i: Int in 0 until routeTransit.length()) {
                val transit = routeTransit.getJSONObject(i)
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

                if (i < routeTransit.length() - 1) {
                    transitsShower.addView(inflater.inflate(R.layout.da_arrow,null))
                }
            }

            // Add actions to card
            cardLeftBinding.findViewById<Button>(R.id.left_card_continueButton).setOnClickListener{
                continueRoute()
            }
            cardLeftBinding.findViewById<ImageView>(R.id.left_card_closeX).setOnClickListener{
                linearLayout.removeView(cardLeftBinding)
                linearLayout.removeView(banner)
                (activity?.application as MobiliTeam).route_left = null
            }

            linearLayout.addView(cardLeftBinding)
        }


        val recentRoutes = (activity?.application as MobiliTeam).store.recentRoutes

        Log.d("AAAAA", recentRoutes.toString())

        if(recentRoutes.length() > 0){
            // Add banner
            val banner = inflater.inflate(R.layout.list_banner, null) as TextView
            banner.text = "RECENT ROUTES"
            linearLayout.addView(banner)


            // Add all routes
            for (i: Int in 0 until recentRoutes.length()) {

                val route: JSONObject = recentRoutes.getJSONObject(i)

                val cardRecentView = inflater.inflate(R.layout.card_recent, null)
                cardRecentView.findViewById<TextView>(R.id.recent_card_actualFrom).text = route.getString("from")
                cardRecentView.findViewById<TextView>(R.id.recent_card_actualTo).text = route.getString("to")
                cardRecentView.setOnClickListener {
                    searchRoute(route.getString("from"), route.getString("to"))
                }

                linearLayout.addView(cardRecentView)
            }
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): HomeTravelFragment {
            return HomeTravelFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun continueRoute() {

        // Adding payload to represent

        val intent = Intent(context, TravelActivity::class.java)
        val b = Bundle()
        b.putInt("key", 1) // Follow route ID is 1

        intent.putExtras(b)
        startActivity(intent)
    }

    fun searchRoute(from: String, to: String) {
        val intent = Intent(context, TravelActivity::class.java)
        val b = Bundle()
        b.putInt("key", 0) // Search ID is 0
        b.putString("from", from)
        b.putString("to", to)
        intent.putExtras(b)
        startActivity(intent)
    }
}