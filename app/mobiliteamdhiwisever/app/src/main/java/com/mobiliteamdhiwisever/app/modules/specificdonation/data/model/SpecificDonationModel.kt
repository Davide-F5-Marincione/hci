package com.mobiliteamdhiwisever.app.modules.specificdonation.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class SpecificDonationModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtDonations: String? = MyApp.getInstance().resources.getString(R.string.lbl_donations)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEntertheamoun: String? =
      MyApp.getInstance().resources.getString(R.string.msg_enter_the_amoun)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtUsernamefield: String? = MyApp.getInstance().resources.getString(R.string.lbl_max_50)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtDescription: String? =
      MyApp.getInstance().resources.getString(R.string.msg_wwf_works_to_he2)

)
