package com.mobiliteamdhiwisever.app.modules.thanks.ui

import android.view.View
import androidx.activity.viewModels
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.base.BaseActivity
import com.mobiliteamdhiwisever.app.databinding.ActivityThanksBinding
import com.mobiliteamdhiwisever.app.modules.thanks.`data`.model.RandomflowerRowModel
import com.mobiliteamdhiwisever.app.modules.thanks.`data`.viewmodel.ThanksVM
import kotlin.Int
import kotlin.String
import kotlin.Unit

class ThanksActivity : BaseActivity<ActivityThanksBinding>(R.layout.activity_thanks) {
  private val viewModel: ThanksVM by viewModels<ThanksVM>()

  override fun onInitialized(): Unit {
    viewModel.navArguments = intent.extras?.getBundle("bundle")
    val randomFlowerAdapter =
    RandomFlowerAdapter(viewModel.randomFlowerList.value?:mutableListOf())
    binding.recyclerRandomFlower.adapter = randomFlowerAdapter
    randomFlowerAdapter.setOnItemClickListener(
    object : RandomFlowerAdapter.OnItemClickListener {
      override fun onItemClick(view:View, position:Int, item : RandomflowerRowModel) {
        onClickRecyclerRandomFlower(view, position, item)
      }
    }
    )
    viewModel.randomFlowerList.observe(this) {
      randomFlowerAdapter.updateData(it)
    }
    binding.thanksVM = viewModel
  }

  override fun setUpClicks(): Unit {
    binding.imageCloseFive.setOnClickListener {
      finish()
    }
    binding.imageArrowleft.setOnClickListener {
      finish()
    }
    binding.imageCloseSeven.setOnClickListener {
      finish()
    }
    binding.imageCloseSix.setOnClickListener {
      finish()
    }
    binding.imageCloseTwo.setOnClickListener {
      finish()
    }
    binding.imageClose.setOnClickListener {
      finish()
    }
    binding.imageCloseThree.setOnClickListener {
      finish()
    }
    binding.imageCloseOne.setOnClickListener {
      finish()
    }
    binding.imageCloseFour.setOnClickListener {
      finish()
    }
  }

  fun onClickRecyclerRandomFlower(
    view: View,
    position: Int,
    item: RandomflowerRowModel
  ): Unit {
    when(view.id) {
    }
  }

  companion object {
    const val TAG: String = "THANKS_ACTIVITY"

  }
}
