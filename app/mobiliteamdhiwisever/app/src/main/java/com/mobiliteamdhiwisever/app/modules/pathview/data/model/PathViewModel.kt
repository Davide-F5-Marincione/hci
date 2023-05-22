package com.mobiliteamdhiwisever.app.modules.pathview.`data`.model

import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.appcomponents.di.MyApp
import kotlin.String

data class PathViewModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtSearchingRoute: String? =
      MyApp.getInstance().resources.getString(R.string.msg_searching_route)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVeranoUniversi: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_verano_bologna)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txt20002200: String? = MyApp.getInstance().resources.getString(R.string.lbl_12_30_12_452)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFrom: String? = MyApp.getInstance().resources.getString(R.string.lbl_from)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFromVerano: String? = MyApp.getInstance().resources.getString(R.string.lbl_verano_12_30)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTo: String? = MyApp.getInstance().resources.getString(R.string.lbl_to)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtReginaElenaV: String? =
      MyApp.getInstance().resources.getString(R.string.msg_regina_elena_v)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMeasurement: String? = MyApp.getInstance().resources.getString(R.string.lbl_3l)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtStops: String? = MyApp.getInstance().resources.getString(R.string.lbl_stops)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtCommunitybased: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_community_based)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOvercrowded: String? = MyApp.getInstance().resources.getString(R.string.lbl_overcrowded)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtFromOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_from)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtReginaElenaVOne: String? =
      MyApp.getInstance().resources.getString(R.string.msg_regina_elena_v2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtToOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_to)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.msg_bologna_12_4)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtThree: String? = MyApp.getInstance().resources.getString(R.string.lbl_310)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtStopsOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_stops)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtCommunitybasedOne: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_community_based)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOvercrowdedOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_overcrowded)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVerano: String? = MyApp.getInstance().resources.getString(R.string.msg_regina_elena_v3)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVeranoOne: String? = MyApp.getInstance().resources.getString(R.string.msg_ippocrate_march)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVeranoTwo: String? = MyApp.getInstance().resources.getString(R.string.msg_ippocrate_provi)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVeranoThree: String? =
      MyApp.getInstance().resources.getString(R.string.msg_ippocrate_macch)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVeranoFour: String? = MyApp.getInstance().resources.getString(R.string.lbl_bologna)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEnabledbutton: String? = MyApp.getInstance().resources.getString(R.string.lbl_select)

)
