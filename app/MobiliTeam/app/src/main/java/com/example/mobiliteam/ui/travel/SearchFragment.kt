package com.example.mobiliteam.ui.travel

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentSearchBinding
import com.google.android.material.button.MaterialButton
import com.example.mobiliteam.ui.home.Route
import com.example.mobiliteam.ui.home.transport
import kotlinx.coroutines.selects.select

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */


fun GetRoutes(From: String,to : String) : MutableList<Route>{

    val routes = mutableListOf<Route>()

 //TO IMPLEMENT
    //EXAMPLE TRANSPORT//
    var transport1 = transport()
    transport1.from = "Piazza Bologna"
    transport1.to = "Verano"
    transport1.line = "61"
    transport1.starting_time = "12:00"
    transport1.arrival_time = "12:10"
    transport1.duration = "10 min"
    transport1.type = "Bus"
    transport1.list_of_stops.add("Piazza Bologna")
    transport1.list_of_stops.add("Verano")
    var transport3 = transport()
    transport3.from = "Piazza Bologna"
    transport3.to = "Verano"
    transport3.line = "3L"
    transport3.starting_time = "12:00"
    transport3.arrival_time = "12:10"
    transport3.duration = "10 min"
    transport3.type = "Tram"
    transport3.list_of_stops.add("Piazza Bologna")
    transport3.list_of_stops.add("Verano")
    var actualRoute = Route()
    actualRoute.from = "Piazza Bologna"
    actualRoute.to = "Verano"
    actualRoute.starting_time = "12:00"
    actualRoute.arrival_time = "12:10"
    actualRoute.list_of_transport.add(transport1)
    actualRoute.list_of_transport.add(transport3)
    routes.add(actualRoute)
    //EXAMPLE TRANSPORT//

    var transport2 = transport()
    transport2.from = "Piazza Bologna"
    transport2.to = "Verano"
    transport2.line = "3L"
    transport2.starting_time = "12:00"
    transport2.arrival_time = "12:10"
    transport2.duration = "10 min"
    transport2.type = "Bus"
    transport2.list_of_stops.add("Piazza Bologna")
    transport2.list_of_stops.add("Verano")
    var actualRoute2 = Route()
    actualRoute2.from = "Piazza Bologna"
    actualRoute2.to = "Verano"
    actualRoute2.starting_time = "12:00"
    actualRoute2.arrival_time = "12:10"
    actualRoute2.list_of_transport.add(transport2)
    routes.add(actualRoute2)


    var transport4 = transport()
    transport4.from = "Piazza Bologna"
    transport4.to = "Verano"
    transport4.line = "3L"
    transport4.starting_time = "12:00"
    transport4.arrival_time = "12:10"
    transport4.duration = "10 min"
    transport4.type = "Bus"
    transport4.list_of_stops.add("Piazza Bologna")
    transport4.list_of_stops.add("Verano")
    var actualRoute3 = Route()
    actualRoute3.from = "Piazza Bologna"
    actualRoute3.to = "Verano"
    actualRoute3.starting_time = "12:00"
    actualRoute3.arrival_time = "12:10"
    actualRoute3.list_of_transport.add(transport4)
    routes.add(actualRoute3)

    return routes
}


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



        var travelActivity: TravelActivity = activity as TravelActivity
        binding.fromInput.editText?.setText(travelActivity.from)
        binding.toInput.editText?.setText(travelActivity.to)
        //var arrow = R.drawable.arrow
        binding.searchCard.transitViewer.removeAllViews()
        var routes = GetRoutes(travelActivity.from,travelActivity.to)
        val linearLayout: LinearLayout = binding.searchlayout
        var text =binding.textView

        linearLayout.removeAllViews()

        linearLayout.addView(text)

        for (route in routes){
            var RouteCard = inflater.inflate(R.layout.card_search, null)
            RouteCard.findViewById<TextView>(R.id.bus_time).text= route.starting_time+"- "+route.arrival_time
            RouteCard.findViewById<TextView>(R.id.delta).text=route.GetDuration()

            var CardLayout= RouteCard.findViewById<LinearLayout>(R.id.card_layout)
            CardLayout.removeAllViews()
            for (transport in route.list_of_transport){
                var transport_view = inflater.inflate(R.layout.transit_show_small, null)
                if (transport.type == "Tram") {
                    transport_view.findViewById<ImageView>(R.id.transit_img).setImageResource(R.drawable.tram)
                }
                CardLayout.addView(transport_view)
                var arrow = inflater.inflate(R.layout.da_arrow, null)
                CardLayout.addView(arrow)
                transport_view.findViewById<TextView>(R.id.transit_desc).text=transport.line
            }
            CardLayout.removeViewAt(CardLayout.childCount-1)

            RouteCard.findViewById<MaterialButton>(R.id.cardSelectButton).setOnClickListener {
                travelActivity.actual_route = route
                findNavController().navigate(R.id.action_SearchFragment_to_PathFollowFragment)
            }
            linearLayout.addView(RouteCard)
        }



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
    }
}