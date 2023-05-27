package com.example.mobiliteam.ui.travel

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.R
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentSelectBinding

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
            findNavController().navigate(R.id.action_SelectFragment_to_PathFollowFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        val travelActivity: TravelActivity = activity as TravelActivity

        val route = travelActivity.viewingRoute
        binding.textView5.text = "From: " + route.getString("from") + " To: " + route.getString("to")
        binding.textView6.text = route.getString("departure_time") + " - " + route.getString("arrival_time")


        val linearLayout : LinearLayout = binding.linearLayout
        linearLayout.removeAllViews()

        for (transport in route.list_of_transport){
            var transportCard= inflater.inflate(R.layout.card_select, null)
            linearLayout.addView(transportCard)
        }
    }
}