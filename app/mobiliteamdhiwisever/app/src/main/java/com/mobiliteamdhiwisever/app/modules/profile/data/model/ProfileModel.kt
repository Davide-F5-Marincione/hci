package com.mobiliteamdhiwisever.app.modules.profile.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class ProfileModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtGiulioMaps: String? = MyApp.getInstance().resources.getString(R.string.lbl_giuliomaps)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtCurrentCredits: String? =
      MyApp.getInstance().resources.getString(R.string.msg_current_credits)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPersonalInfo: String? = MyApp.getInstance().resources.getString(R.string.lbl_personal_info)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.lbl_first_name2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguageOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_giulio)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguageTwo: String? = MyApp.getInstance().resources.getString(R.string.lbl_last_name3)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLombardi: String? = MyApp.getInstance().resources.getString(R.string.lbl_lombardi)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguageThree: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_email_address)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEmail: String? = MyApp.getInstance().resources.getString(R.string.msg_giulio_lombardi)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtStats: String? = MyApp.getInstance().resources.getString(R.string.lbl_stats)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTotalReports: String? = MyApp.getInstance().resources.getString(R.string.msg_total_reports)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguageFour: String? =
      MyApp.getInstance().resources.getString(R.string.msg_total_donations)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLogout: String? = MyApp.getInstance().resources.getString(R.string.lbl_logout)

)
