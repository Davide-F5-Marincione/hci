package com.example.mobiliteam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        }

        (activity as TravelActivity).binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}