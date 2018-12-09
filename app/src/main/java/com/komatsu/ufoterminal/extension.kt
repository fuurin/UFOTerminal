package com.komatsu.ufoterminal

import android.icu.util.Freezable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*



fun Any?.log() {
    Log.v("DEBUG", this.toString())
}



fun Int.loop(iterFunc: (Int) -> Any) {
    if (this < 0) return
    for (i in 0 until this) iterFunc(i)
}



const val UNIT_PERIOD: Float = 100f

const val DEFAULT_RECORD_TIME_PATTERN = "HH:mm:ss.S"

val lengthFormatter = SimpleDateFormat(DEFAULT_RECORD_TIME_PATTERN).also {
    it.timeZone = TimeZone.getTimeZone("GMT")
}

fun Int.recordTimeFormat(): String {
    return lengthFormatter.format(this * UNIT_PERIOD)
}



const val DEFAULT_CREATED_PATTERN: String = "yyyy-MM-dd HH:mm:ss"

fun File.created(pattern: String= DEFAULT_CREATED_PATTERN): String {
    if (!this.isFile) return ""
    val attrs = Files.readAttributes(this.toPath(), BasicFileAttributes::class.java)
    val time = attrs.creationTime()
    val dateFormat = SimpleDateFormat(pattern)
    return dateFormat.format(Date(time.toMillis()))
}

fun CSVFile.created(pattern: String = DEFAULT_CREATED_PATTERN): String {
    return file.created(pattern)
}



fun EditText.withMarginLayout(left: Int=50, top: Int=0, right: Int=50, bottom: Int=0): LinearLayout {
    val container = LinearLayout(this.context)
    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    lp.setMargins(left, top, right, bottom)
    this.layoutParams = lp
    container.addView(this)
    return container
}