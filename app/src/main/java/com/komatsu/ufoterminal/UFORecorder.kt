package com.komatsu.ufoterminal

import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import java.util.*

class UFORecorder(
        private val activity: Activity,
        private val listener: OnRecordTimeChangedListener
) : UFOController.OnUpdateRotationListener {

    interface OnRecordTimeChangedListener {
        fun onRecordTimeChanged(time: Float)
        fun onRecordStart()
        fun onRecordStop()
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

    fun stop() {
        stopRecord()
        initRecorder()
    }

    fun end() {
        stopRecord()
        isRecording = false
        openSaveDialog()
    }


    override fun onUpdateRotation(power: Byte, direction: Boolean) {
        if (!isRecording || lastRecord.time == time || (lastRecord.direction == direction && lastRecord.power == power)) return
        val newRecord = UFORecord(time, direction, power)
        record.add(newRecord)
        lastRecord = newRecord
    }

    private var isRecording: Boolean = false
    private var timer: Timer? = null
    private var time: Int = 0
    private var lastRecord: UFORecord = UFORecord(0, true, 0)
    private var record: MutableList<UFORecord> = mutableListOf()

    private fun updateRecordTime() {
        listener.onRecordTimeChanged(time.toSecond())
    }

    private fun recordTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                time++
                updateRecordTime()
            }
        }
    }

    private fun stopRecord() {
        timer?.cancel()
        timer = null
        listener.onRecordStop()
    }

    private fun openSaveDialog() {
        if (record.isEmpty()) return
        val editView = EditText(activity)
        AlertDialog.Builder(activity)
            .setTitle(R.string.record_save_title)
            .setMessage(R.string.record_save_message)
            .setView(editView)
            .setPositiveButton(R.string.record_save) { _, _ -> save(editView.text.toString()) }
            .setNegativeButton(R.string.record_abandon) { _, _ -> initRecorder() }
            .setNeutralButton(R.string.record_cancel) { _, _ -> isRecording = true }
            .show()
    }

    private fun save(filename: String) {
        if (record.isEmpty()) return
        val strRecord: List<List<String>> = record.map {
            listOf(
                it.time.toString(),
                if (it.direction) "0" else "1",
                it.power.toString()
            )
        }
        CSVFile(activity.filesDir.path, filename).write(strRecord)
    }

    private fun initRecorder() {
        record = mutableListOf()
        lastRecord = UFORecord(0, true, 0)
        time = 0
        updateRecordTime()
    }
}