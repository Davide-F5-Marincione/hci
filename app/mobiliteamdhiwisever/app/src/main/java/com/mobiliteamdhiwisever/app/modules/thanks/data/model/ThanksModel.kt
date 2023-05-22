package com.mobiliteamdhiwisever.app.modules.thanks.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class ThanksModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtThankyou: String? = MyApp.getInstance().resources.getString(R.string.lbl_thank_you)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtDonations: String? = MyApp.getInstance().resources.getString(R.string.lbl_donations)

)
