package com.komatsu.ufoterminal

import android.util.Log
import java.io.File
import java.io.IOException


class CSVFile(filesDir: String, fileNameWithoutDotExt: String) {

    val file = File("$filesDir/$fileNameWithoutDotExt.csv")

    fun appendLine(data: List<String>): Boolean {
        file.appendText(data.joinToString(",") + "\n")
        return true
    }

    fun write(data: List<List<String>>): Boolean {
        try {
            val content = data.joinToString("\n") { it.joinToString(",") }
            file.writeText(content)
        } catch (e: IOException) {
            Log.v("failed to save csv", e.toString())
            return false
        }
        return true
    }

    fun read(): List<List<String>> {
        val data = mutableListOf<List<String>>()
        if (!file.canRead()) return data
        file.forEachLine { data.add(it.split(",")) }
        return data.toList()
    }

    fun created(pattern: String): String {
        return created(file, pattern)
    }
}