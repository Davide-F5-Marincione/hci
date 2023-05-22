package com.mobiliteamdhiwisever.app.modules.nav.ui

import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityNavBinding
import com.mobiliteamdhiwisever.app.modules.nav.`data`.viewmodel.NavVM
import kotlin.String
import kotlin.Unit

class NavActivity : BaseActivity<ActivityNavBinding>(R.layout.activity_nav) {
  private val viewModel: NavVM by viewModels<NavVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.navVM = viewModel
  }

  override fun setUpClicks(): Unit {
    binding.imageArrowleft.setOnClickListener {
      finish()
    }
  }

  companion object {
    const val TAG: String = "NAV_ACTIVITY"

  }
}
