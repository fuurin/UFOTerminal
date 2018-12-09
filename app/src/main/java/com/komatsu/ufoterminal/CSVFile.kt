package com.komatsu.ufoterminal

import android.util.Log
import java.io.File
import java.io.IOException


class CSVFile(filesDir: String, val title: String) {

    val file = File("$filesDir/$title.csv")
    val created = file.created()

    fun write(data: List<List<String>>): Boolean {
        try {
            val content = data.joinToString("\n") { it.joinToString(",") }
            file.writeText(content)
        } catch (e: Exception) {
            "failed to save csv¥n$e".log()
            return false
        }
        return true
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