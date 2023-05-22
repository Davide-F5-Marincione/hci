package com.mobiliteamdhiwisever.app.modules.travel.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.travel.`data`.model.TravelModel
import org.koin.core.KoinComponent

class TravelVM : ViewModel(), KoinComponent {
  val travelModel: MutableLiveData<TravelModel> = MutableLiveData(TravelModel())

  var navArguments: Bundle? = null
}
