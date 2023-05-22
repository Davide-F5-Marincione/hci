package com.mobiliteamdhiwisever.app.modules.loading.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class LoadingModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtMobiliTeam: String? = MyApp.getInstance().resources.getString(R.string.lbl_mobiliteam)

)
