package com.komatsu.ufoterminal

import android.app.Activity
import java.io.File
import java.util.*
import java.util.concurrent.CancellationException

class UFORecorder(
        private val listener: OnRecordTimeChangedListener
) : UFOController.OnUpdateRotationListener {

    var isRecording: Boolean = false

    private var timer: Timer? = null
    private var time: Int = 0
    private var lastRecord: UFORecord = UFORecord(0, true, 0)
    private var record: MutableList<UFORecord> = mutableListOf()

    interface OnRecordTimeChangedListener {
        fun onRecordTimeChanged(time: Int)
        fun onRecordStart()
        fun onRecordEndCancel()
        fun onRecordEnd()
    }

    fun initRecorder() {
        record = mutableListOf()
        lastRecord = UFORecord(0, true, 0)
        time = 0
        updateRecordTime()
    }

    fun start(direction: Boolean, power: Int) {
        if (!isRecording) {
            isRecording = true
            record.add(UFORecord(0, direction, power.toByte()))
            lastRecord = record[0]
        }
        timer = Timer()
        timer!!.scheduleAtFixedRate(recordTask(), 0, UNIT_PERIOD.toLong())
        listener.onRecordStart()
    }

    fun pause() {
        stopRecord()
    }

    fun reset() {
        stopRecord()
        initRecorder()
    }

    fun end() {
        stopRecord()
        isRecording = false

        if (record.size <= 1) {
            initRecorder()
            return
        }

        listener.onRecordEnd()
    }

    fun endCancel() {
        isRecording = true
        listener.onRecordEndCancel()
    }

    fun checkOverwrite(activity: Activity, filename: String): Boolean {
        return File("${activity.filesDir}/$filename.csv").isFile
    }

    fun save(activity: Activity, filename: String): Boolean {
        if (record.isEmpty() || filename == "") return false
        val strRecord: List<List<String>> = record.map {
            listOf(
                    it.time.toString(),
                    if (it.direction) "0" else "1",
                    it.power.toString()
            )
        }
        initRecorder()
        return CSVFile(activity.filesDir.path, filename).write(strRecord)
    }

    override fun onUpdateRotation(power: Int, direction: Boolean) {
        if (
                !isRecording ||
                lastRecord.time == time ||
                (lastRecord.direction == direction && lastRecord.power == power.toByte())
        ) return

        val newRecord = UFORecord(time, direction, power.toByte())
        record.add(newRecord)
        lastRecord = newRecord
    }

    private fun updateRecordTime() {
        listener.onRecordTimeChanged(time)
    }

    private fun stopRecord() {
        try {
            timer?.cancel()
        } catch (e: CancellationException) {
        }
        timer = null
    }

    private fun recordTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                time++
                updateRecordTime()
            }
        }
    }
}