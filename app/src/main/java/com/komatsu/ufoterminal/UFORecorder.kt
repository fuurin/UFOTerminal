package com.komatsu.ufoterminal

import android.app.Activity
import android.app.AlertDialog
import android.view.WindowManager
import android.widget.EditText
import java.io.File
import java.nio.file.Files.delete
import java.util.*

class UFORecorder(
        private val activity: Activity,
        private val listener: OnRecordTimeChangedListener
) : UFOController.OnUpdateRotationListener {

    private var isRecording: Boolean = false
    private var timer: Timer? = null
    private var time: Int = 0
    private var lastRecord: UFORecord = UFORecord(0, true, 0)
    private var record: MutableList<UFORecord> = mutableListOf()

    interface OnRecordTimeChangedListener {
        fun onRecordTimeChanged(time: Float)
        fun onRecordStart()
        fun onRecordEndCancel()
        fun onRecordEnd()
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
        listener.onRecordEnd()
        openSaveDialog()
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
        listener.onRecordTimeChanged(time.toSecond())
    }

    private fun stopRecord() {
        if (timer == null) return
        timer?.cancel()
        timer = null
    }

    private fun endCancel() {
        isRecording = true
        listener.onRecordEndCancel()
    }

    private fun openSaveDialog() {
        if (record.isEmpty()) return
        val editView = EditText(activity)
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder
            .setTitle(R.string.record_save_title)
            .setMessage(R.string.record_save_message)
            .setView(editView)
            .setPositiveButton(R.string.record_save) { _, _ -> checkOverwrite(editView.text.toString()) }
            .setNegativeButton(R.string.record_abandon) { _, _ -> openRecordAbandonConfirmDialog() }
            .setNeutralButton(R.string.record_cancel) { _, _ -> endCancel() }
        val dialog = dialogBuilder.create()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun openRecordAbandonConfirmDialog() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_abandon_title)
                .setMessage(R.string.confirm_record_abandon_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ -> initRecorder() }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> endCancel() }
                .create().show()
    }

    private fun openOverwriteConfirmDialog(filename: String) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_overwrite_title)
                .setMessage(R.string.confirm_record_overwrite_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ -> save(filename) }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> endCancel() }
                .create().show()
    }

    private fun checkOverwrite(filename: String) {
        if (File("${activity.filesDir}/$filename.csv").isFile) {
            openOverwriteConfirmDialog(filename)
        } else {
            save(filename)
        }
    }

    private fun save(filename: String) {
        if (record.isEmpty() || filename == "") return
        val strRecord: List<List<String>> = record.map {
            listOf(
                it.time.toString(),
                if (it.direction) "0" else "1",
                it.power.toString()
            )
        }
        CSVFile(activity.filesDir.path, filename).write(strRecord)
        initRecorder()
    }

    private fun initRecorder() {
        record = mutableListOf()
        lastRecord = UFORecord(0, true, 0)
        time = 0
        updateRecordTime()
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