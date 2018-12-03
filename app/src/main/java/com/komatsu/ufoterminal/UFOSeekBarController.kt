package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import android.widget.SeekBar

class UFOSeekBarController(
        val controller: UFOController
): SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        controller.updateRotation(p1)
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        controller.updateRotation(p0!!.progress)
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        controller.updateRotation(p0!!.progress)
    }

}