package com.mobiliteamdhiwisever.app.modules.thanks.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.thanks.`data`.model.RandomflowerRowModel
import com.mobiliteamdhiwisever.app.modules.thanks.`data`.model.ThanksModel
import kotlin.collections.MutableList
import org.koin.core.KoinComponent

class ThanksVM : ViewModel(), KoinComponent {
  val thanksModel: MutableLiveData<ThanksModel> = MutableLiveData(ThanksModel())

  var navArguments: Bundle? = null

  val randomFlowerList: MutableLiveData<MutableList<RandomflowerRowModel>> =
      MutableLiveData(mutableListOf())
}
