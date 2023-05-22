package com.mobiliteamdhiwisever.app.modules.login.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class LoginModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.lbl_mobiliteam2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtForgotyourlog: String? =
      MyApp.getInstance().resources.getString(R.string.msg_forgot_your_log)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOr: String? = MyApp.getInstance().resources.getString(R.string.lbl_or)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtConfirmation: String? =
      MyApp.getInstance().resources.getString(R.string.msg_don_t_have_an_a)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRegisterhere: String? = MyApp.getInstance().resources.getString(R.string.lbl_register_here)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var etTextfieldValue: String? = null
)
