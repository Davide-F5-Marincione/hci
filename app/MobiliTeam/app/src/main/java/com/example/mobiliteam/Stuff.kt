package com.example.mobiliteam

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


fun TextInputLayout.addImage(atText: String, @DrawableRes imgSrc: Int, imgWidth: Int, imgHeight: Int) {
    val ssb = SpannableStringBuilder(this.editText?.text)

    val drawable = ContextCompat.getDrawable(this.context, imgSrc) ?: return
    drawable.mutate()
    drawable.setBounds(0, 0,
        imgWidth,
        imgHeight)
    val start = this.editText?.text?.indexOf(atText)!!
    if (start < 0) return
    ssb.setSpan(VerticalImageSpan(drawable), start, start + atText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    this.editText?.setText(ssb, TextView.BufferType.SPANNABLE)
}

class ProgressBarAnimation(
    private val progressBar: ProgressBar,
    private val from: Float,
    private val to: Float
) :
    Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
    }
}

fun createDelay(transit: JSONObject) : String? {
    val delay = transit.getDouble("delay")
    if (delay < 60f) {
        return null
    }
    val minutes = delay.toInt() / 60

    return "+$minutes min"
}

fun createDuration(route: JSONObject): String {
    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
    val departure = LocalDateTime.parse(route.getString("departure_time"), timeFormatter)
    val arrival = LocalDateTime.parse(route.getString("arrival_time"), timeFormatter)
    val minutes = Duration.between(departure, arrival).toMinutes()

    return "$minutes min"
}

fun extractTime(time: String): String {
    return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun summaryMaker(route: JSONObject): String {
    val transits = route.getJSONArray("transits")

    val now = LocalDateTime.now()
    var n: Int = 0

    for (i: Int in 0 until transits.length()) {
        val transit = transits.getJSONObject(i)

        val last_seen = LocalDateTime.parse(transit.getString("last_seen"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val diff = Duration.between(last_seen, now).toMinutes()

        if (transit.getDouble("crowdedness_mu") > .8f && diff < 10) {
            n += 1
        }
    }

    if (n < 1) {
        return ""
    } else {
        return "Overcrowded transits (n.$n)"
    }
}