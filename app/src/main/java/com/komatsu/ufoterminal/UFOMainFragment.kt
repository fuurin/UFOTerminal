package com.komatsu.ufoterminal

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main.*


class UFOMainFragment : Fragment(),
        UFORecorder.OnRecordTimeChangedListener {

    interface MainFragmentListener {
        fun onMainFragmentViewCreated()
        fun onOpenRecordList()
    }

    fun ready(controller: UFOController, recorder: UFORecorder) {
        this.controller = controller
        this.recorder = recorder
        recordButton.isChecked = false
        recordPauseButton.isChecked = false
        controlButton.isChecked = true
        controlRandomPowerButton.isChecked = false
        seekBarController = UFOSeekBarController(controller)
        activateEvents()
        control()
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
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        super.onAttach(context)
        listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener.onMainFragmentViewCreated()
    }

    override fun onRecordTimeChanged(time: Float) {
        recordTimeText.text = String.format("%.1f", time)
    }

    override fun onRecordStart() {
        controller.updateRotation(controlPowerSeekBar.progress)
    }

    override fun onRecordStop() {
        controlButton?.isChecked = false // なぜかDisconnect時にnullになる
        controlRandomPowerButton?.isChecked = false
        controller.stop()
    }

    private lateinit var listener: MainFragmentListener
    private lateinit var controller: UFOController
    private lateinit var recorder: UFORecorder
    private lateinit var seekBarController: UFOSeekBarController

    private fun activateEvents() {
        recordButton.setOnClickListener { record() }
        recordPauseButton.setOnClickListener { recordPause() }
        recordListButton.setOnClickListener { openRecordList() }
        controlButton.setOnClickListener { control() }
        controlRandomPowerButton.setOnClickListener { randomPower() }
        controlPowerSeekBar.setOnSeekBarChangeListener(seekBarController)
        controlRandomDirectionButton.setOnClickListener { controller.rotateRandomly() }
        controlLeftButton.setOnClickListener { controller.rotateLeft() }
        controlRightButton.setOnClickListener { controller.rotateRight() }
    }

    private fun record() {
        if (recordButton.isChecked) recorder.start()
        else recorder.end()
    }

    private fun recordPause() {
        if (recordPauseButton.isChecked) recorder.pause()
        else recorder.start()
    }

    private fun openRecordList() {
        listener.onOpenRecordList()
    }

    private fun control() {
        if (controlButton.isChecked) controller.start(controlPowerSeekBar.progress)
        else controller.stop()
    }

    private fun randomPower() {
        if (controlRandomPowerButton.isChecked) controller.startRandomPower()
        else controller.stopRandomPower(controlPowerSeekBar.progress)
    }
}
