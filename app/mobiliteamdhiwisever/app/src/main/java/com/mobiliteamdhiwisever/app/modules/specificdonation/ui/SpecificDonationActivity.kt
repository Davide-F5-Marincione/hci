package com.mobiliteamdhiwisever.app.modules.specificdonation.ui

import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivitySpecificDonationBinding
import com.mobiliteamdhiwisever.app.modules.specificdonation.`data`.viewmodel.SpecificDonationVM
import kotlin.String
import kotlin.Unit

class SpecificDonationActivity :
    BaseActivity<ActivitySpecificDonationBinding>(R.layout.activity_specific_donation) {
  private val viewModel: SpecificDonationVM by viewModels<SpecificDonationVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.specificDonationVM = viewModel
  }

  override fun setUpClicks(): Unit {
    binding.imageArrowleft.setOnClickListener {
      finish()
    }
  }

  companion object {
    const val TAG: String = "SPECIFIC_DONATION_ACTIVITY"

  }
}
