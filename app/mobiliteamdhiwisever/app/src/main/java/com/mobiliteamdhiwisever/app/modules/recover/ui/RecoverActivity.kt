package com.mobiliteamdhiwisever.app.modules.recover.ui

import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityRecoverBinding
import com.mobiliteamdhiwisever.app.modules.recover.`data`.viewmodel.RecoverVM
import kotlin.String
import kotlin.Unit

class RecoverActivity : BaseActivity<ActivityRecoverBinding>(R.layout.activity_recover) {
  private val viewModel: RecoverVM by viewModels<RecoverVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.recoverVM = viewModel
  }

  override fun setUpClicks(): Unit {
  }

  companion object {
    const val TAG: String = "RECOVER_ACTIVITY"

  }
}
