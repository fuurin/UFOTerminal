package com.komatsu.ufoterminal

import android.widget.SeekBar

class UFOPlayerSeekBarListener(
    private val player: UFOPlayer
): SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        player.updatePlayTime(p1)
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        player.updatePlayTime(p0!!.progress)
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        player.updatePlayTime(p0!!.progress)
    }
}