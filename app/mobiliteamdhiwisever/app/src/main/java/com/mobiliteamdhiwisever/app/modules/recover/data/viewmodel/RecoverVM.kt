package com.mobiliteamdhiwisever.app.modules.recover.`data`.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiliteamdhiwisever.app.modules.recover.`data`.model.RecoverModel
import org.koin.core.KoinComponent

class RecoverVM : ViewModel(), KoinComponent {
  val recoverModel: MutableLiveData<RecoverModel> = MutableLiveData(RecoverModel())

  var navArguments: Bundle? = null
}
