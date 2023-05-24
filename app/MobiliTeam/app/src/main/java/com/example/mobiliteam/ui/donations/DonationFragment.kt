package com.example.mobiliteam.ui.donations

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.DonationsActivity
import com.example.mobiliteam.R
import com.example.mobiliteam.databinding.FragmentDonationsFrBinding
import com.google.android.material.button.MaterialButton

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DonationFragment : Fragment() {

    private var _binding: FragmentDonationsFrBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDonationsFrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when ((activity as DonationsActivity).ngo) {
            0 -> {
                binding.ngoImage.setImageResource(R.drawable.img_wwf)
                binding.myRectangleView.setBackgroundColor(Color.parseColor("#BFE476"))
                binding.ngoDesc.text = "WWF works to help local communities conserve the natural resources they depend upon; transform markets and policies toward sustainability; and protect and restore species and their habitats. Our efforts ensure that the value of nature is reflected in decision-making from a local to a global scale."
            }
            1 -> {
                binding.ngoImage.setImageResource(R.drawable.icrc)
                binding.myRectangleView.setBackgroundColor(Color.parseColor("#F08B66"))
                binding.ngoDesc.text = "The International Committee of the Red Cross (ICRC) is an impartial, neutral and independent organization whose exclusively humanitarian mission is to protect the lives and dignity of victims of armed conflict and other situations of violence and to provide them with assistance."
            }
            2 -> {
                binding.ngoImage.setImageResource(R.drawable.greenpeace)
                binding.myRectangleView.setBackgroundColor(Color.parseColor("#BFE476"))
                binding.ngoDesc.text = "Greenpeace is a global network of independent campaigning organizations that use peaceful protest and creative communication to expose global environmental problems and promote solutions that are essential to a green and peaceful future."
            }

            3 -> {
                binding.ngoImage.setImageResource(R.drawable.wfp)
                binding.myRectangleView.setBackgroundColor(Color.parseColor("#F3C45F"))
                binding.ngoDesc.text = "The World Food Programme (WFP) is the leading humanitarian organization saving lives and changing lives, delivering food assistance in emergencies and working with communities to improve nutrition and build resilience."
            }
        }

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