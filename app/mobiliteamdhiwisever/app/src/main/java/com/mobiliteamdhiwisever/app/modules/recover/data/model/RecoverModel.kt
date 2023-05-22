package com.mobiliteamdhiwisever.app.modules.recover.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class RecoverModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtUsernameRecove: String? =
      MyApp.getInstance().resources.getString(R.string.msg_username_recove)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.msg_having_an_ident)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var etTextfieldValue: String? = null
)
