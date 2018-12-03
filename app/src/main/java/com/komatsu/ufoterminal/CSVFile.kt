package com.komatsu.ufoterminal

import android.util.Log
import java.io.File
import java.io.IOException


class CSVFile(filesDir: String, val title: String) {

    val file = File("$filesDir/$title.csv")
    val created = created(file)

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

    fun read(adapt: (List<String>) -> Any): List<Any> {
        val data = mutableListOf<Any>()
        if (!file.canRead()) return data
        file.forEachLine { data.add(adapt(it.split(","))) }
        return data.toList()
    }

    fun read(): List<List<String>> {
        return read { it } as List<List<String>>
    }

    fun created(pattern: String = DEFAULT_CREATED_PATTERN): String {
        return created(file, pattern)
    }
}