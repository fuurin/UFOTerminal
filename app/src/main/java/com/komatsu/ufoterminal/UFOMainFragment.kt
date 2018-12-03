package com.komatsu.ufoterminal

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_main.*


class UFOMainFragment : Fragment(),
        UFORecorder.OnRecordTimeChangedListener {

    interface MainFragmentListener {
        fun onUFOMainFragmentViewCreated()
        fun onOpenRecordList()
    }

    fun ready(controller: UFOController, recorder: UFORecorder) {
        this.controller = controller
        this.recorder = recorder
        initView()
        attachEvents()
        control(controlButton.isChecked)
    }

    fun stopAll() {
        controller.stop() // 本当はrecorder.stopでstopできる
        recorder.stop()
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

    override fun onRecordTimeChanged(time: Float) {
        recordTimeText.text = time.timeFormat()
    }

    override fun onRecordStart() {
        controller.updateRotation(currentPower())
    }

    override fun onRecordStop() {
        controlButton?.isChecked = false // なぜかDisconnect時にnullになる
        controlRandomPowerButton?.isChecked = false
        controller.stop()
    }

    private lateinit var listener: MainFragmentListener
    private lateinit var controller: UFOController
    private lateinit var recorder: UFORecorder
    private lateinit var controllerSeekBarListener: UFOControllerSeekBarListener

    private fun initView() {
        recordButton.isChecked = false
        recordPauseButton.isChecked = false
        controlButton.isChecked = true
        controlRandomPowerButton.isChecked = false
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
        recorder.apply { if (checked) pause() else start(controller.direction, currentPower()) }
    }

    private fun openRecordList() {
        listener.onOpenRecordList()
    }

    private fun control(checked: Boolean) {
        controller.apply { if (checked) start(currentPower()) else stop() }
    }

    private fun randomPower(checked: Boolean) {
        controller.apply { if (checked) startRandomPower() else stopRandomPower(currentPower()) }
    }

    private fun currentPower(): Int {
        return controlPowerSeekBar.progress
    }
}
