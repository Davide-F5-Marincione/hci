package com.mobiliteamdhiwisever.app.modules.loading.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityLoadingBinding
import com.mobiliteamdhiwisever.app.modules.loading.`data`.viewmodel.LoadingVM
import com.mobiliteamdhiwisever.app.modules.login.ui.LoginActivity
import kotlin.String
import kotlin.Unit

class LoadingActivity : BaseActivity<ActivityLoadingBinding>(R.layout.activity_loading) {
  private val viewModel: LoadingVM by viewModels<LoadingVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    binding.loadingVM = viewModel
    Handler(Looper.getMainLooper()).postDelayed( {
      val destIntent = LoginActivity.getIntent(this, null)
      startActivity(destIntent)
      finish()
      }, 3000)
    }

    override fun setUpClicks(): Unit {
    }

    companion object {
      const val TAG: String = "LOADING_ACTIVITY"


      fun getIntent(context: Context, bundle: Bundle?): Intent {
        val destIntent = Intent(context, LoadingActivity::class.java)
        destIntent.putExtra("bundle", bundle)
        return destIntent
      }
    }
  }
