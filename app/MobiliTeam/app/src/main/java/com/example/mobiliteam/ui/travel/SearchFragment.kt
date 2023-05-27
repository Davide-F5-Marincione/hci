package com.example.mobiliteam.ui.travel

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentSearchBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.selects.select
import org.json.JSONArray
import org.json.JSONObject

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

        val travelActivity: TravelActivity = activity as TravelActivity
        binding.fromInput.editText?.setText(travelActivity.from)
        binding.toInput.editText?.setText(travelActivity.to)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<MaterialButton>(R.id.cardSelectButton).setOnClickListener {
            findNavController().navigate(R.id.action_SearchFragment_to_SelectFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        updateRoutes()
    }

    fun updateRoutes() {
        val inflater = activity?.layoutInflater!!

        val linearLayout: LinearLayout = binding.searchlayout
        linearLayout.removeAllViews()

        requestRoutes()

        val routes = (activity as TravelActivity).routes

        // Add banner
        val banner = inflater.inflate(R.layout.list_banner, linearLayout, true) as TextView
        banner.text = "SUGGESTED ROUTES:"

        for (i:Int in 0 until routes.length()){
            val route = routes.getJSONObject(i)

            val routeCard = inflater.inflate(R.layout.card_search, linearLayout, true)
            routeCard.findViewById<TextView>(R.id.bus_time).text= route.getString("departure_time")+" - "+ route.getString("arrival_time")
            routeCard.findViewById<TextView>(R.id.delta).text = createDuration(route)

            // Fill card's horizontal shower of transits
            val transitsShower : HorizontalScrollView = routeCard.findViewById<HorizontalScrollView>(R.id.card_layout)
            transitsShower.removeAllViews()
            val routeTransit = route.getJSONArray("transits")
            for(j: Int in 0 until routeTransit.length()) {
                val transit = routeTransit.getJSONObject(j)
                val transportView = inflater.inflate(R.layout.transit_show_small, transitsShower, true)
                transportView.findViewById<TextView>(R.id.transit_desc).text = transit.getString("transit_line")
//                if (transport.type == "Tram") {
//                    transport_view.findViewById<ImageView>(R.id.transit_img).setImageResource(R.drawable.tram)
//                }

                if (j < routeTransit.length() - 1) {
                    inflater.inflate(R.layout.da_arrow, transitsShower, true)
                }
            }


            routeCard.setOnClickListener{
                (activity as TravelActivity).viewingRoute = route
                findNavController().navigate(R.id.action_SearchFragment_to_SelectFragment)
            }
            routeCard.findViewById<MaterialButton>(R.id.cardSelectButton).setOnClickListener{
                (activity as TravelActivity).viewingRoute = route
                findNavController().navigate(R.id.action_SearchFragment_to_PathFollowFragment)
            }
        }
    }

    fun requestRoutes() {
        // Gotta do some requests!
    }

    fun createDuration(route: JSONObject): String {
        return "IMPLEMENT ME!"
    }
}