package com.mobiliteamdhiwisever.app.modules.donations.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class DonationsModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtAvailableCredi: String? =
      MyApp.getInstance().resources.getString(R.string.msg_available_credi)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtWwfOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_wwf)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtDescription: String? =
      MyApp.getInstance().resources.getString(R.string.msg_wwf_works_to_he)

)
