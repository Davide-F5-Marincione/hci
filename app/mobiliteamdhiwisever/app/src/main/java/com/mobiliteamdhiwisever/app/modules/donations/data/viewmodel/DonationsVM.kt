package com.mobiliteamdhiwisever.app.modules.donations.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.donations.`data`.model.DonationsModel
import org.koin.core.KoinComponent

class DonationsVM : ViewModel(), KoinComponent {
  val donationsModel: MutableLiveData<DonationsModel> = MutableLiveData(DonationsModel())

  var navArguments: Bundle? = null
}
