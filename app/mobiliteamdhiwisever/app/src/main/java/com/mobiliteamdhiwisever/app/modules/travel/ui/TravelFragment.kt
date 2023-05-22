package com.mobiliteamdhiwisever.app.modules.travel.ui

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseFragment
import com.mobiliteamdhiwisever.app.databinding.FragmentTravelBinding
import com.mobiliteamdhiwisever.app.modules.loading.ui.LoadingActivity
import com.mobiliteamdhiwisever.app.modules.travel.`data`.viewmodel.TravelVM
import kotlin.String
import kotlin.Unit

class TravelFragment : BaseFragment<FragmentTravelBinding>(R.layout.fragment_travel) {
  private val viewModel: TravelVM by viewModels<TravelVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = arguments
    binding.travelVM = viewModel
    Handler(Looper.getMainLooper()).postDelayed( {
      val destIntent = LoadingActivity.getIntent(requireActivity(), null)
      startActivity(destIntent)
      requireActivity().onBackPressed()
      }, 3000)
    }

    override fun setUpClicks(): Unit {
    }

    companion object {
      const val TAG: String = "TRAVEL_FRAGMENT"

    }
  }
