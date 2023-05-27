package com.example.mobiliteam.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiliteam.HomeActivity
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.databinding.FragmentProfileBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch

class HomeProfileFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentProfileBinding? = null

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

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.logoutThingie.setOnClickListener {
            val act = activity as HomeActivity
            (activity?.application as MobiliTeam).usernameStore.username = ""
            act.launchLogIn()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        fillProfileInfo()
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
        fun newInstance(sectionNumber: Int): HomeProfileFragment {
            return HomeProfileFragment().apply {
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

    fun fillProfileInfo() {
        Log.d("ProfileInfoGET", "Getting info for " + (activity?.application as MobiliTeam).usernameStore.username)

        val username = (activity?.application as MobiliTeam).usernameStore.username

        //check if the username is 'admin' then directly go to the home page
        if(username == "admin"){
            // Do something if Admin
            return
        }
        val url ="http://10.0.2.2:5000/users/" + username;

        val request: Request = Request.Builder()
            .url(url).get().header("Authorization", "Bearer " + (activity?.application as MobiliTeam).auth).build()

        var firstname: String? = null
        var lastname: String? = null
        var email: String? = null
        var credits: String? = null
        var donations: String? = null
        var reports: String? = null
        val countDownLatch = CountDownLatch(1)

        (activity?.application as MobiliTeam).client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response
            ) {
                when (response.code) {
                    200 -> {
                        val obj = JSONObject(response.body?.string())
                        firstname = obj.getString("first_name").toString()
                        lastname = obj.getString("last_name").toString()
                        email = obj.getString("email").toString()
                        credits = obj.getInt("credits").toString()
                        donations = obj.getInt("donations_counter").toString()
                        reports = obj.getInt("reports_counter").toString()
                    }
                    404 -> {
//                        mHandler.post(Runnable {
//                            errorMessage.text = "User not found"
//                        })
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

        if (firstname != null) {
            binding.profileName.text = username
            binding.numCredits.text = credits
            binding.actualFirstname.text = firstname
            binding.actualLastname.text = lastname
            binding.actualEmail.text = email
            binding.numReports.text = reports
            binding.numDonations.text = donations
        }
    }
}