package com.example.mobiliteam.ui.travel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentPathFollowBinding

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

        binding.endRouteButton.setOnClickListener {
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
}