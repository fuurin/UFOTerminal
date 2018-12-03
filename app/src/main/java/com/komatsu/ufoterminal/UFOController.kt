package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import android.support.v4.view.ViewCompat.setRotation
import android.support.v7.widget.RecyclerView.EdgeEffectFactory.DIRECTION_LEFT
import android.support.v7.widget.RecyclerView.EdgeEffectFactory.DIRECTION_RIGHT
import java.util.*


//    正回転(右回り)が0x020101～0x020164
//    逆回転(左回り)が0x020181～0x0201e4
//    停止が0x020100、0x020180
class UFOController(
        private val gatt: BluetoothGatt,
        private val listener: OnUpdateRotationListener? = null
) {

    interface OnUpdateRotationListener {
        fun onUpdateRotation(power: Byte, direction: Boolean)
    }

    fun updateRotation(power: Int, direction: Boolean) {
        if (!isActive || power > MAX_POWER || power < 0) return
        if (this.power == power && this.direction == direction && power != 0) return
        if (!setRotation(power, direction)) return

        this.power = power
        this.direction = direction

        listener?.onUpdateRotation(power.toByte(), direction)
    }

    fun updateRotation(power: Int) {
        updateRotation(power, direction)
    }


    fun start(initPower: Int) {
        if (characteristic == null) return
        isActive = true
        updateRotation(initPower)
    }

    fun start() {
        start(power)
    }

    fun stop() {
        if (characteristic == null) return
        stopRandomDir()
        updateRotation(0, direction)
        isActive = false
    }

    fun startRandomPower(period: Long) {
        powerTimer = Timer()
        powerTimer!!.scheduleAtFixedRate(randomPowerTask(), 0, period)
    }

    fun startRandomPower() {
        startRandomPower(CHANGE_POWER_PERIOD)
    }

    fun stopRandomPower() {
        if (powerTimer == null) return
        powerTimer!!.cancel()
        powerTimer!!.purge()
        powerTimer = null
    }

    fun stopRandomPower(newPower: Int) {
        stopRandomPower()
        updateRotation(newPower)
    }

    fun rotateRandomly() {
        rotateRandomly(CHANGE_DIR_PERIOD)
    }

    fun rotateRight() {
        stopRandomDir()
        updateRotation(power, DIRECTION_RIGHT)
    }

    fun rotateLeft() {
        stopRandomDir()
        updateRotation(power, DIRECTION_LEFT)
    }


    // TODO UUIDは自動でとれないのか？

    // 対象のサービスUUID
    private val SERVICE_UUID = "40ee1111-63ec-4b7f-8ce7-712efd55b90e"

    // 対象のキャラクタリスティックUUID
    private val CHARACTERISTIC_UUID = "40ee2222-63ec-4b7f-8ce7-712efd55b90e"

    // UFOSAの操作コードの先頭値
    private val MACHINE_CODE = 2.toByte()

    // VORZE製品の操作コードの真ん中の値
    private val VORZE_CODE = 1.toByte()

    // 逆回転の開始される値
    private val ADDITION_TO_REVERSE = 0x80

    // ローターの最大回転スピード
    private val MAX_POWER = 100

    // 回転スピードを変更する可能性のある機会の時間間隔(ミリ秒)
    private val CHANGE_POWER_PERIOD: Long = 500

    // 右方向
    private val DIRECTION_RIGHT = true

    // 左方向
    private val DIRECTION_LEFT = false

    // 回転方向を変更する可能性のある機会の時間間隔(ミリ秒)
    private val CHANGE_DIR_PERIOD: Long = 1000

    // 回転方向が変更される確率(%)
    private val CHANGE_DIR_POSSIBILITY = 50

    private val service = gatt.getService(UUID.fromString(SERVICE_UUID))
    private val characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

    private var isActive = false
    private var power = 0
    private var powerTimer: Timer? = null
    private var direction = DIRECTION_RIGHT
    private var directionTimer: Timer? = null
    private val rnd = Random()

    private fun setRotation(power: Int, direction: Boolean): Boolean {
        if (characteristic == null) return false
        val newPower = power or if (direction == DIRECTION_RIGHT) 0 else ADDITION_TO_REVERSE
        characteristic.value = byteArrayOf(MACHINE_CODE, VORZE_CODE, newPower.toByte())
        return gatt.writeCharacteristic(characteristic)
    }

    private fun randomPowerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                val newPower = rnd.nextInt(101)
                if (newPower != power) updateRotation(newPower, direction)
            }
        }
    }

    private fun rotateRandomTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                val newDirection = rnd.nextInt(100 / CHANGE_DIR_POSSIBILITY) == 0
                if (newDirection != direction) updateRotation(power, newDirection)
            }
        }
    }

    private fun rotateRandomly(period: Long) {
        directionTimer = Timer()
        directionTimer!!.scheduleAtFixedRate(rotateRandomTask(), 0, period)
    }

    private fun stopRandomDir() {
        if (directionTimer == null) return
        directionTimer!!.cancel()
        directionTimer!!.purge()
        directionTimer = null
    }
}