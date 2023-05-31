package com.example.mobiliteam.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiliteam.DonationsActivity
import com.example.mobiliteam.MobiliTeam
import com.example.mobiliteam.databinding.FragmentDonationsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch

class HomeDonationsFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentDonationsBinding? = null

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

        _binding = FragmentDonationsBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.wwfRedirect.setOnClickListener {
            moveWWF()
        }
        binding.icrcRedirect.setOnClickListener {
            moveICRC()
        }
        binding.greenRedirect.setOnClickListener {
            moveGreen()
        }
        binding.wfpRedirect.setOnClickListener {
            moveWFP()
        }

        return root
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
        fun newInstance(sectionNumber: Int): HomeDonationsFragment {
            return HomeDonationsFragment().apply {
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

    override fun onResume() {
        super.onResume()
        reloadCredits()
    }

    fun moveWWF() {
        val intent = Intent(activity, DonationsActivity::class.java)
        val b = Bundle()
        b.putInt("ngo", 0)
        intent.putExtras(b)
        startActivity(intent)
    }
    fun moveICRC() {
        val intent = Intent(activity, DonationsActivity::class.java)
        val b = Bundle()
        b.putInt("ngo", 1)
        intent.putExtras(b)
        startActivity(intent)
    }
    fun moveGreen() {
        val intent = Intent(activity, DonationsActivity::class.java)
        val b = Bundle()
        b.putInt("ngo", 2)
        intent.putExtras(b)
        startActivity(intent)
    }
    fun moveWFP() {
        val intent = Intent(activity, DonationsActivity::class.java)
        val b = Bundle()
        b.putInt("ngo", 3)
        intent.putExtras(b)
        startActivity(intent)
    }

    fun reloadCredits() {
        Log.d("ProfileInfoGET", "Getting info for " + (activity?.application as MobiliTeam).store.username)

        val username = (activity?.application as MobiliTeam).store.username

        //check if the username is 'admin' then directly go to the home page
        if(username == "admin"){
            // Do something if Admin
            return
        }
        val url ="http://"+ MobiliTeam.ip + ":5000/users/" + username;

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
                        credits = obj.getInt("credits").toString()
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

        if (credits != null) {
            binding.fragmentDonationsNumCredits.text = credits
        }
    }
}