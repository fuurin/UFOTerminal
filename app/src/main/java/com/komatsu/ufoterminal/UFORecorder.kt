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

    fun start() {
        isRecording = true
        timer = Timer()
        timer!!.scheduleAtFixedRate(recordTask(), 0, recordPeriod.toLong())
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
        openSaveDialog()
    }


    override fun onUpdateRotation(power: Byte, direction: Boolean) {
        if (!isRecording || lastRecord.first == recordTime || (lastRecord.second == direction && lastRecord.third == power)) return
        val newRecord = Triple(recordTime, direction, power)
        record.add(newRecord)
        lastRecord = newRecord
    }


    private val recordPeriod: Float = 100f

    private var isRecording: Boolean = false
    private var timer: Timer? = null
    private var recordTime: Int = 0
    private var lastRecord: Triple<Int, Boolean, Byte> = Triple(0, true, 0)
    private var record: MutableList<Triple<Int, Boolean, Byte>> = mutableListOf<Triple<Int, Boolean, Byte>>()

    private fun updateRecordTime() {
        listener.onRecordTimeChanged(recordTime * (recordPeriod / 1000))
    }

    private fun recordTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                recordTime++
                updateRecordTime()
            }
        }
    }

    private fun stopRecord() {
        isRecording = false
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
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
                .setNeutralButton(R.string.record_cancel) { _, _ -> }
                .show()
    }

    private fun save(filename: String) {
        if (record.isEmpty()) return
        val strRecord: List<List<String>> = record.map {
            listOf(
                    it.first.toString(),
                    if (it.second) "0" else "1",
                    it.third.toString()
            )
        }
        CSVFile(activity.filesDir.path, filename).write(strRecord)
    }

    private fun initRecorder() {
        record = mutableListOf()
        lastRecord = Triple(0, true, 0)
        recordTime = 0
        updateRecordTime()
    }
}