package com.komatsu.ufoterminal

import android.widget.SeekBar
import java.util.*
import java.util.concurrent.CancellationException

class UFOControllerSeekBarListener(
        private val controller: UFOController
): SeekBar.OnSeekBarChangeListener {

    var timer: Timer? = null

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (timer != null) stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(updateRotationTask(p1), 0, UNIT_PERIOD.toLong())
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    override fun onStopTrackingTouch(p0: SeekBar?) {}

    private fun stopTimer() {
        if (timer == null) return
        try { timer?.cancel() } catch (e: CancellationException) {}
        timer = null
    }

    private fun updateRotationTask(power: Int): TimerTask {
        return object : TimerTask() {
            override fun run() {
                if (controller.updateRotation(power)) stopTimer()
            }
        }
    }
}