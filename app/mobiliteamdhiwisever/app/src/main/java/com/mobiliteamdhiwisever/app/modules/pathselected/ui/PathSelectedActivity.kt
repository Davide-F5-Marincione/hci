package com.mobiliteamdhiwisever.app.modules.pathselected.ui

import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityPathSelectedBinding
import com.mobiliteamdhiwisever.app.modules.pathselected.`data`.viewmodel.PathSelectedVM
import kotlin.String
import kotlin.Unit

class PathSelectedActivity :
    BaseActivity<ActivityPathSelectedBinding>(R.layout.activity_path_selected) {
  private val viewModel: PathSelectedVM by viewModels<PathSelectedVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.pathSelectedVM = viewModel
  }

  override fun setUpClicks(): Unit {
    binding.imageArrowleft.setOnClickListener {
      finish()
    }
  }

  companion object {
    const val TAG: String = "PATH_SELECTED_ACTIVITY"

  }
}
