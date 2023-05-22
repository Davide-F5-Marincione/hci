package com.mobiliteamdhiwisever.app.modules.nav.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.nav.`data`.model.NavModel
import org.koin.core.KoinComponent

class NavVM : ViewModel(), KoinComponent {
  val navModel: MutableLiveData<NavModel> = MutableLiveData(NavModel())

  var navArguments: Bundle? = null
}
