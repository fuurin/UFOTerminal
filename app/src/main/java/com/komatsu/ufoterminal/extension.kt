package com.komatsu.ufoterminal

import android.util.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

fun Any?.log() {
    Log.v("DEBUG", this.toString())
}

const val UNIT_PERIOD: Float = 100f

fun Float.timeFormat(): String {
    return String.format("%.1f", this)
}

fun Float.toUnitPeriods(): Int {
    return (this * (1000 / UNIT_PERIOD)).toInt()
}

fun Int.toSecond(): Float {
    return this * (UNIT_PERIOD / 1000)
}

fun Int.loop(iterFunc: (Int) -> Any) {
    if (this < 0) return
    for (i in 0 until this) iterFunc(i)
}

const val DEFAULT_CREATED_PATTERN: String = "yyyy-MM-dd HH:mm:ss"

fun created(file: File, pattern: String= DEFAULT_CREATED_PATTERN): String {
    if (!file.isFile) return ""
    val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    val time = attrs.creationTime()
    val dateFormat = SimpleDateFormat(pattern)
    return dateFormat.format(Date(time.toMillis()))
}

fun CSVFile.created(pattern: String = DEFAULT_CREATED_PATTERN): String {
    return created(file, pattern)
}