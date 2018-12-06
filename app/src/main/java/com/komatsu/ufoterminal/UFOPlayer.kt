package com.komatsu.ufoterminal

import java.util.*

class UFOPlayer(
        file: CSVFile,
        private val controller: UFOController,
        private val listener: PlayerListener?) {

    val record = file.read {
        UFORecord(
                it[0].toInt(), it[1] === "0", it[2].toByte()
        )
    } as List<UFORecord>

    private var time: Int = 0
    private var currentId: Int = 0
    private var currentRecord: UFORecord = record[currentId]
    private var timer: Timer? = null

    interface PlayerListener {
        fun onUpdateTime(time: Float)
        fun onPlayFinished()
    }

    fun updatePlayTime(time: Int) {
        this.time = time
        currentRecord = record.find { time <= it.time } ?: record.last()
        currentId = record.indexOf(currentRecord)
    }

    fun start() {
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

    fun forward(plus: Float) {
        updatePlayTime(time + plus.toUnitPeriods())
    }

    fun backward(minus: Float) {
        updatePlayTime(time - minus.toUnitPeriods())
    }

    private fun stopPlay() {
        if (timer == null) return // ?.演算子が効かないのでnullチェック
        timer?.cancel()
        timer = null
    }

    private fun playTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                time++

                if (time == currentRecord.time) {
                    controller.updateRotation(currentRecord.power.toInt(), currentRecord.direction)
                    currentId++
                    currentRecord = record[currentId]
                }

                if (currentId + 1 == record.size) {
                    Timer().schedule(playFinishTask(), 1000)
                }

                listener?.onUpdateTime(time.toSecond())
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