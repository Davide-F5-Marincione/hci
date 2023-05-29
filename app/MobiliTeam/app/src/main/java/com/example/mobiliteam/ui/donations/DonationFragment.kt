package com.example.mobiliteam.ui.donations

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobiliteam.DonationsActivity
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.R
import com.example.mobiliteam.databinding.FragmentDonationsFrBinding
import com.google.android.material.button.MaterialButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DonationFragment : Fragment() {

    private var _binding: FragmentDonationsFrBinding? = null


    private val binding get() = _binding!!

    private var maxDonation = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDonationsFrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // I know I know, I'm a genius
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
            makeDonation()
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
        reloadCredits()
    }

    fun reloadCredits() {
        Log.d("ProfileInfoGET", "Getting info for " + (activity?.application as MobiliTeam).store.username)

        val username = (activity?.application as MobiliTeam).store.username

        //check if the username is 'admin' then directly go to the home page
        if(username == "admin"){
            // Do something if Admin
            return
        }
        val url ="http://"+ (activity?.application as MobiliTeam).ip +":5000/users/" + username;

        val request: Request = Request.Builder()
            .url(url).get().header("Authorization", "Bearer " + (activity?.application as MobiliTeam).auth).build()

        var credits: String? = null
        val countDownLatch = CountDownLatch(1)

        (activity?.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response
            ) {
                when (response.code) {
                    200 -> {
                        val obj = JSONObject(response.body?.string())
                        maxDonation = obj.getInt("credits")
                        credits = maxDonation.toString()
                    }
                    404 -> {

                    }
                    500 -> {
                    }
                }
                response.close()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()

        if (credits != null) {
            binding.currentcredits.text = credits
        }
    }

    fun makeDonation() {

        val donation_amount = binding.donationInput.editText?.text.toString().toInt()

        if (donation_amount > maxDonation || donation_amount == 0) {
            // OOOOO wow, do something
            return
        }

        Log.d("Donating", (activity?.application as MobiliTeam).store.username + " is donating " + donation_amount.toString())

        val username = (activity?.application as MobiliTeam).store.username

        //check if the username is 'admin' then directly go to the home page
        if(username == "admin"){
            // Do something if Admin
            return
        }
        val url ="http://"+ (activity?.application as MobiliTeam).ip +":5000/users/" + username + "/donate";

        val jsonObject = JSONObject()
        jsonObject.put("credits", donation_amount)

        val request: Request = Request.Builder()
            .url(url).header("Content-Type","application/json").header("Authorization", "Bearer " + (activity?.application as MobiliTeam).auth).put(jsonObject.toString().toRequestBody()).build()


        var ok  = false
        val countDownLatch = CountDownLatch(1)

        (activity?.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response
            ) {
                when (response.code) {
                    200 -> {
                        ok = true
                    }
                    404 -> {

                    }
                    500 -> {
                    }
                }
                response.close()
                countDownLatch.countDown()
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()

        if (ok) {
            findNavController().navigate(R.id.action_DonationFragment_to_ThanksFragment)
        }
    }
}