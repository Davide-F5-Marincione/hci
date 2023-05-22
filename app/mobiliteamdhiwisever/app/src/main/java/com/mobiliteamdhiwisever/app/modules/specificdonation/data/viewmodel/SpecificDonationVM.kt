package com.mobiliteamdhiwisever.app.modules.specificdonation.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.specificdonation.`data`.model.SpecificDonationModel
import org.koin.core.KoinComponent

class SpecificDonationVM : ViewModel(), KoinComponent {
  val specificDonationModel: MutableLiveData<SpecificDonationModel> =
      MutableLiveData(SpecificDonationModel())

  var navArguments: Bundle? = null
}
