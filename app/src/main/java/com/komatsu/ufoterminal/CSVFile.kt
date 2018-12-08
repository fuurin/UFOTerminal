package com.komatsu.ufoterminal

import android.util.Log
import java.io.File
import java.io.IOException


class CSVFile(filesDir: String, val title: String) {

    val file = File("$filesDir/$title.csv")
    val created = file.created()

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

    fun readLines(line: Int, adapt: (List<String>) -> Any): List<Any> {
        val data = mutableListOf<Any>()
        if (!file.canRead()) return data
        var counter = 0
        file.forEachLine {
            data.add(adapt(it.split(",")))
            if (++counter >= line) return@forEachLine
        }
        return data.toList()
    }

    fun readAll(adapt: (List<String>) -> Any): List<Any> {
        val data = mutableListOf<Any>()
        if (!file.canRead()) return data
        file.forEachLine { data.add(adapt(it.split(","))) }
        return data.toList()
    }

    fun readAll(): List<List<String>> {
        return readAll { it } as List<List<String>>
    }
}