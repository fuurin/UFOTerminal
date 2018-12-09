package com.komatsu.ufoterminal

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_main.*


class UFOMainFragment : Fragment(),
        UFORecorder.OnRecordTimeChangedListener {

    private lateinit var listener: MainFragmentListener
    private lateinit var controller: UFOController
    private lateinit var controllerSeekBarListener: UFOControllerSeekBarListener
    private lateinit var recorder: UFORecorder

    interface MainFragmentListener {
        fun onUFOMainFragmentViewCreated()
        fun onOpenRecordList()
    }

    fun ready(controller: UFOController, recorder: UFORecorder) {
        this.controller = controller
        this.recorder = recorder
        initView()
        attachEvents()
        start()
    }

    fun start() {
        control(controlButton.isChecked)
    }

    fun stopAll() {
        controller.stop() // 本当はrecorder.stopでstopできる
        recorder.reset()
        initView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        if (context !is MainFragmentListener)
            throw RuntimeException("$context must implement MainFragmentListener")
        super.onAttach(context)
        listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener.onUFOMainFragmentViewCreated()
    }

    override fun onRecordTimeChanged(time: Int) {
        recordTimeText.text = time.recordTimeFormat()
    }

    override fun onRecordStart() {
        controller.updateRotation(currentPower())
    }

    override fun onRecordEndCancel() {
        recordButton.isChecked = true
        recordPauseButton.isChecked = true
    }

    override fun onRecordEnd() {
        controlButton?.isChecked = false // なぜかDisconnect時にnullになる
        controlRandomPowerButton?.isChecked = false
        controller.stop()
        openSaveDialog()
    }

    private fun initView() {
        recordButton.isChecked = false
        recordPauseButton.isChecked = false
        recordTimeText.text = 0.recordTimeFormat()
        controlButton.isChecked = true
        controlRandomPowerButton.isChecked = false
        controlPowerSeekBar.progress = 0
    }

    private fun attachEvents() {
        recordButton.setOnClickListener { record((it as ToggleButton).isChecked) }
        recordPauseButton.setOnClickListener { recordPause((it as ToggleButton).isChecked) }
        recordListButton.setOnClickListener { openRecordList() }
        controlButton.setOnClickListener { control((it as ToggleButton).isChecked) }
        controlRandomPowerButton.setOnClickListener { randomPower((it as ToggleButton).isChecked) }
        controllerSeekBarListener = UFOControllerSeekBarListener(controller)
        controlPowerSeekBar.setOnSeekBarChangeListener(controllerSeekBarListener)
        controlRandomDirectionButton.setOnClickListener { controller.startRandomDirection() }
        controlLeftButton.setOnClickListener { controller.startLeftDirection() }
        controlRightButton.setOnClickListener { controller.startRightDirection() }
    }

    private fun record(checked: Boolean) {
        recorder.apply { if (checked) start(controller.direction, currentPower()) else end() }
    }

    private fun recordPause(checked: Boolean) {
        recorder.apply {
            if (checked) pause()
            else if (recorder.isRecording) start(controller.direction, currentPower())
        }
    }

    private fun openSaveDialog() {
        val editView = EditText(activity)
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.record_save_title)
                .setMessage(R.string.record_save_message)
                .setView(editView.withMarginLayout())
                .setPositiveButton(R.string.record_save) { _, _ ->
                    val filename = editView.text.toString()
                    if (recorder.checkOverwrite(activity!!, filename)) openOverwriteConfirmDialog(filename)
                    else recorder.save(activity!!, filename)
                }
                .setNegativeButton(R.string.record_abandon) { _, _ -> openRecordAbandonConfirmDialog() }
                .setNeutralButton(R.string.record_cancel) { _, _ -> recorder.endCancel() }.create()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun openRecordAbandonConfirmDialog() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_abandon_title)
                .setMessage(R.string.confirm_record_abandon_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ ->
                    recordPauseButton.isChecked = false
                    recorder.initRecorder()
                }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> recorder.endCancel() }
                .create().show()
    }

    private fun openOverwriteConfirmDialog(filename: String) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_overwrite_title)
                .setMessage(R.string.confirm_record_overwrite_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ ->
                    val res = recorder.save(activity!!, filename)
                    val msg = if (res) "$filename${resources.getText(R.string.record_complete)}"
                              else resources.getText(R.string.record_failed)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> recorder.endCancel() }
                .create().show()
    }

    private fun openRecordList() {
        listener.onOpenRecordList()
    }

    private fun control(checked: Boolean) {
        controller.apply { if (checked) start(currentPower()) else pause() }
    }

    private fun randomPower(checked: Boolean) {
        controller.apply { if (checked) startRandomPower() else stopRandomPower(currentPower()) }
    }

    private fun currentPower(): Int {
        return controlPowerSeekBar.progress
    }
}
