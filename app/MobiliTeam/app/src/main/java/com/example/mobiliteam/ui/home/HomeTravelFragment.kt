package com.example.mobiliteam.ui.home

import android.content.Intent
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentTravelBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

class Route(){
    public var from: String = ""
    public var to: String = ""
    public var starting_time: String = ""
    public var arrival_time: String = ""
    public var list_of_transport: MutableList<transport> = mutableListOf<transport>()

    fun GetDuration():String{
        var start = starting_time.split(":")
        var end = arrival_time.split(":")
        var start_hour = start[0].toInt()
        var start_min = start[1].toInt()
        var end_hour = end[0].toInt()
        var end_min = end[1].toInt()
        var duration_hour = end_hour - start_hour
        var duration_min = end_min - start_min
        if (duration_min<0){
            duration_hour = duration_hour - 1
            duration_min = duration_min + 60
        }

        if (duration_hour==0){
            return duration_min.toString() + "min"
        }
        return  duration_hour.toString() + "h " + duration_min.toString() + "min"
    }
}
class transport(){
    public var from: String = ""
    public var to: String = ""
    public var line: String = ""
    public var starting_time: String = ""
    public var arrival_time: String = ""
    public var duration: String = ""
    public var type: String = ""
    public var list_of_stops: MutableList<String> = mutableListOf<String>()
}
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
        var actualRoute = Route()
        actualRoute.from = "Piazza Bologna"
        actualRoute.to = "Verano"
        actualRoute.starting_time = "12:00"
        actualRoute.arrival_time = "12:10"
        actualRoute.list_of_transport.add(transport1)
        //EXAMPLE TRANSPORT//


        val linearLayout: LinearLayout = binding.linearLayout
        val recent_text = binding.textView4
        val continue_text = binding.textView
        linearLayout.removeAllViews()

        //check if actual Route is not null, if its not print it and add it to card_left
        if (actualRoute!=null){
            linearLayout.addView(continue_text)
            var cardLeftBinding = inflater.inflate(R.layout.card_left, container, false)
            cardLeftBinding.findViewById<TextView>(R.id.actual_from).text = actualRoute.from
            cardLeftBinding.findViewById<TextView>(R.id.actual_to).text = actualRoute.to
            cardLeftBinding.findViewById<TextView>(R.id.bus_time).text = actualRoute.starting_time+" - "+actualRoute.arrival_time
            var busseslist : HorizontalScrollView = cardLeftBinding.findViewById<HorizontalScrollView>(R.id.transit_viewer)
            busseslist.removeAllViews()
            for(transport in actualRoute.list_of_transport) {
                var transportView = inflater.inflate(R.layout.transit_show_small, null)
                transportView.findViewById<TextView>(R.id.transit_desc).text=transport.line
                busseslist.addView(transportView)
            }
            cardLeftBinding.findViewById<Button>(R.id.continueButton).setOnClickListener{
                continueRoute()
            }
            cardLeftBinding.findViewById<ImageView>(R.id.close_x).setOnClickListener{
                linearLayout.removeView(cardLeftBinding)
                linearLayout.removeView(continue_text)
            }
            linearLayout.addView(cardLeftBinding)
        }


        //check if file exists if not create and populate it
        var fileReader : FileReader
        val file = File(context?.filesDir, "recents.txt")
        try {
            fileReader = FileReader(file)
            Log.d("file_opener", "file exists at ${file.absolutePath}")
        } catch (e: FileNotFoundException) {
            //since file doesn't exists yet, create it and save it
            file.createNewFile()
            file.writeText("from: Piazza Bologna\tto: Verano")
            Log.d("file_opener", "created at ${file.absolutePath}")
            fileReader = FileReader(file)
        }

        //read file and populate recent_routes
        val recent_routes : MutableList<Array<String>> = mutableListOf()




        for (Line in fileReader.readLines()) {
            var splitted_line = Line.split("\t", limit = 2).toMutableList()
            for (i in 0..1) {
               splitted_line[i] = splitted_line[i].split(": ")[1]
            }
            recent_routes.add(splitted_line.toTypedArray()) }
       fileReader.close()
        //if there are routes print them and add them to card_recent
        if(recent_routes.size==0){

        }
        else {
            linearLayout.addView(recent_text)
            for (route in recent_routes) {
                if(linearLayout.childCount ==10){
                    break
                }
                Log.d("route", "from:" + route[0] + " to: " + route[1])
                val cardRecentView = inflater.inflate(R.layout.card_recent, null)
                cardRecentView.findViewById<TextView>(R.id.actual_from).text = route[0]
                cardRecentView.findViewById<TextView>(R.id.actual_to).text = route[1]
                cardRecentView.setOnClickListener {
                    val intent = Intent(context, TravelActivity::class.java)
                    intent.putExtra("from",route[0])
                    intent.putExtra("to", route[1])
                    startActivity(intent)
                }
                linearLayout.addView(cardRecentView)
            }
        }














        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // binding.scrollContent

        binding.toInputEdit.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        val intent = Intent(activity, TravelActivity::class.java)
                        intent.putExtra("from", binding.fromInputEdit.text.toString())
                        intent.putExtra("to", binding.toInputEdit.text.toString())
                        startActivity(intent)
                    }
                }
                false // pass on to other listeners.
            }
        )
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


        // We have to add all the rest of the info here!



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