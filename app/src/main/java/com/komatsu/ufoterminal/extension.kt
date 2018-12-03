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

fun created(file: File, pattern: String="yyyy-MM-dd HH:mm:ss"): String {
    if (!file.isFile) return ""
    val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    val time = attrs.creationTime()
    val dateFormat = SimpleDateFormat(pattern)
    return dateFormat.format(Date(time.toMillis()))
}