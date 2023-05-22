package com.mobiliteamdhiwisever.app.modules.travel.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class TravelModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtTextfield: String? = MyApp.getInstance().resources.getString(R.string.lbl_from)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTextfieldOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_to)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFrameTwentyFive: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_where_you_left)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFromOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_from)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtToOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_to)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVerano: String? = MyApp.getInstance().resources.getString(R.string.lbl_verano)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtReginaElenaV: String? = MyApp.getInstance().resources.getString(R.string.lbl_bologna)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_542)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.lbl_12_30_12_50)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTime: String? = MyApp.getInstance().resources.getString(R.string.lbl_delays_1_min)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEnabledbutton: String? = MyApp.getInstance().resources.getString(R.string.lbl_continue)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRecentroutes: String? = MyApp.getInstance().resources.getString(R.string.lbl_recent_routes)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFromVerano: String? = MyApp.getInstance().resources.getString(R.string.lbl_from_verano)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtToPiazzaBolo: String? =
      MyApp.getInstance().resources.getString(R.string.msg_to_piazza_bolo)

)
