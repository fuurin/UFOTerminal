package com.komatsu.ufoterminal

import java.lang.Integer.min
import java.util.*
import java.util.concurrent.CancellationException

class UFOPlayer(
        file: CSVFile,
        private val controller: UFOController,
        private val listener: PlayerListener?) {

    val record = file.readAll {
        UFORecord(
                it[0].toInt(), it[1] === "0", it[2].toByte()
        )
    } as List<UFORecord>

    var playContinuously: Boolean = false

    private var time: Int = 0
    private var currentId: Int = 0
    private var currentRecord: UFORecord = record[currentId]
    private var timer: Timer? = null

    interface PlayerListener {
        fun onUpdateTime(time: Int)
        fun onPlayFinished()
    }

    fun updatePlayTime(time: Int) {
        currentRecord = record.find { time <= it.time } ?: record.last()
        this.time = min(time, currentRecord.time)
        currentId = record.indexOf(currentRecord)
        listener?.onUpdateTime(this.time)
    }

    fun start() {
        controller.start()
        timer = Timer()
        timer!!.scheduleAtFixedRate(playTask(), 0, UNIT_PERIOD.toLong())
    }

    fun pause() {
        stopPlay()
        controller.stop()
    }

    fun end() {
        pause()
        updatePlayTime(0)
    }

    fun forward(plus: Int) {
        updatePlayTime(time + plus)
    }

    fun backward(minus: Int) {
        updatePlayTime(time - minus)
    }

    private fun stopPlay() {
        try { timer?.cancel() } catch (e: CancellationException) {}
        timer = null
    }

    private fun playTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                time++

                if (time == currentRecord.time) {
                    controller.updateRotation(currentRecord.power.toInt(), currentRecord.direction)

                    if (++currentId >= record.size) {
                        if (playContinuously)
                            updatePlayTime(0)
                        else {
                            stopPlay()
                            Timer().schedule(playFinishTask(), 1000)
                        }
                    } else {
                        currentRecord = record[currentId]
                    }
                }

                listener?.onUpdateTime(time)
            }
        }
    }

    private fun playFinishTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                controller.stop()
                listener?.onPlayFinished()
            }
        }
    }
}