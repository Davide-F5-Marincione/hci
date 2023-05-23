package com.example.mobiliteam.ui.donations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobiliteam.DonationsActivity
import com.example.mobiliteam.TravelActivity
import com.example.mobiliteam.databinding.FragmentThanksBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ThanksFragment : Fragment() {

    private var _binding: FragmentThanksBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentThanksBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as DonationsActivity).binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            (activity as DonationsActivity).finish()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}