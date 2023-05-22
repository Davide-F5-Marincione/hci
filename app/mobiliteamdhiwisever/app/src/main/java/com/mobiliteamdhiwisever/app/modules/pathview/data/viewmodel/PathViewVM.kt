package com.mobiliteamdhiwisever.app.modules.pathview.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.pathview.`data`.model.PathViewModel
import org.koin.core.KoinComponent

class PathViewVM : ViewModel(), KoinComponent {
  val pathViewModel: MutableLiveData<PathViewModel> = MutableLiveData(PathViewModel())

  var navArguments: Bundle? = null
}
