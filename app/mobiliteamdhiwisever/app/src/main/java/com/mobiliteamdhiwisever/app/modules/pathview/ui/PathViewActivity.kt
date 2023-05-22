package com.mobiliteamdhiwisever.app.modules.pathview.ui

import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityPathViewBinding
import com.mobiliteamdhiwisever.app.modules.pathview.`data`.viewmodel.PathViewVM
import kotlin.String
import kotlin.Unit

class PathViewActivity : BaseActivity<ActivityPathViewBinding>(R.layout.activity_path_view) {
  private val viewModel: PathViewVM by viewModels<PathViewVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.pathViewVM = viewModel
  }

  override fun setUpClicks(): Unit {
    binding.imageArrowleft.setOnClickListener {
      finish()
    }
  }

  companion object {
    const val TAG: String = "PATH_VIEW_ACTIVITY"

  }
}
