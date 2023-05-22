package com.mobiliteamdhiwisever.app.modules.donations.ui

import androidx.fragment.app.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseFragment
import com.mobiliteamdhiwisever.app.databinding.FragmentDonationsBinding
import com.mobiliteamdhiwisever.app.modules.donations.`data`.viewmodel.DonationsVM
import kotlin.String
import kotlin.Unit

class DonationsFragment : BaseFragment<FragmentDonationsBinding>(R.layout.fragment_donations) {
  private val viewModel: DonationsVM by viewModels<DonationsVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = arguments
    binding.donationsVM = viewModel
  }

  override fun setUpClicks(): Unit {
  }

  companion object {
    const val TAG: String = "DONATIONS_FRAGMENT"

  }
}
