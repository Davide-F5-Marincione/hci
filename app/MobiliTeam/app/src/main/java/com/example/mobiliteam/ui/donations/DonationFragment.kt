package com.example.mobiliteam.ui.donations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.R
import com.example.mobiliteam.databinding.FragmentWwfdonationsBinding
import com.google.android.material.button.MaterialButton

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DonationFragment : Fragment() {

    private var _binding: FragmentWwfdonationsBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWwfdonationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<MaterialButton>(R.id.donateButton).setOnClickListener {
            findNavController().navigate(R.id.action_DonationFragment_to_ThanksFragment)
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