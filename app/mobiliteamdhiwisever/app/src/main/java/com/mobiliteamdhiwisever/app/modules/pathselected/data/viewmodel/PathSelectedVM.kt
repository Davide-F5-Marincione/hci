package com.mobiliteamdhiwisever.app.modules.pathselected.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.pathselected.`data`.model.PathSelectedModel
import org.koin.core.KoinComponent

class PathSelectedVM : ViewModel(), KoinComponent {
  val pathSelectedModel: MutableLiveData<PathSelectedModel> = MutableLiveData(PathSelectedModel())

  var navArguments: Bundle? = null
}
