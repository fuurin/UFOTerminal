package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import java.util.*


//    正回転(右回り)が0x020101～0x020164
//    逆回転(左回り)が0x020181～0x0201e4
//    停止が0x020100、0x020180
class UFOController(
        private val gatt: BluetoothGatt,
        private val listener: OnUpdateRotationListener? = null
) {

    var power = 0
    var direction = true
    private var isActive = false

    private val service = gatt.getService(UUID.fromString(SERVICE_UUID))
    private val characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
    private val rnd = Random()

    private var powerTimer: Timer? = null
    private var directionTimer: Timer? = null

    companion object {
        // TODO UUIDは自動でとれないのか？

        // 対象のサービスUUID
        const val SERVICE_UUID = "40ee1111-63ec-4b7f-8ce7-712efd55b90e"

        // 対象のキャラクタリスティックUUID
        const val CHARACTERISTIC_UUID = "40ee2222-63ec-4b7f-8ce7-712efd55b90e"

        // UFOSAの操作コードの先頭値
        const val MACHINE_CODE = 2.toByte()

        // VORZE製品の操作コードの真ん中の値
        const val VORZE_CODE = 1.toByte()

        // 逆回転の開始される値
        const val ADDITION_TO_REVERSE = 0x80

        // ローターの最大回転スピード
        const val MAX_POWER = 100

        // 右回転
        const val DIRECTION_RIGHT = true

        // 左回転
        const val DIRECTION_LEFT = false

        // 回転スピードを変更する可能性のある機会の時間間隔(ミリ秒)
        const val CHANGE_POWER_PERIOD: Long = 500

        // 回転方向を変更する可能性のある機会の時間間隔(ミリ秒)
        const val CHANGE_DIR_PERIOD: Long = 1000

        // 回転方向が変更される確率(%)
        const val CHANGE_DIR_POSSIBILITY = 50
    }

    interface OnUpdateRotationListener {
        fun onUpdateRotation(power: Int, direction: Boolean)
    }

    fun updateRotation(power: Int, direction: Boolean): Boolean {
        if (!isActive || power > MAX_POWER || power < 0) return false
        if (this.power == power && this.direction == direction) return false
        return forceUpdateRotation(power, direction)
    }

    fun updateRotation(power: Int): Boolean {
        return updateRotation(power, direction)
    }

    fun updateRotation(direction: Boolean): Boolean {
        return updateRotation(power, direction)
    }

    fun start(initPower: Int=power, initDirection: Boolean=direction) {
        isActive = true
        updateRotation(initPower, initDirection)
    }

    fun pause() {
        if (!isActive) return // BT切断後pauseが呼ばれると無限ループするので止める．これとstart以外でisActiveはいじらない！
        isActive = false
        while(!forceUpdateRotation(0, direction)){} // 0が送れないことがあるので止まるまで止め続ける
    }

    fun stop() {
        pause()
        stopRandomPower()
        stopRandomDirection()
    }

    fun startRandomPower(period: Long=CHANGE_POWER_PERIOD) {
        powerTimer = Timer()
        powerTimer?.scheduleAtFixedRate(randomPowerTask(), 0, period)
    }

    fun stopRandomPower() {
        if (powerTimer == null) return // .?演算子がなんか効かないのでnullチェック
        powerTimer?.cancel()
        powerTimer = null // timerはnullで破棄しないと終わらない
    }

    fun stopRandomPower(newPower: Int) {
        stopRandomPower()
        updateRotation(newPower)
    }

    fun startRandomDirection(period: Long=CHANGE_DIR_PERIOD) {
        directionTimer = Timer()
        directionTimer!!.scheduleAtFixedRate(randomDirectionTask(), 0, period)
    }

    fun stopRandomDirection() {
        if (directionTimer == null) return
        directionTimer!!.cancel()
        directionTimer = null
    }

    fun startRightDirection() {
        stopRandomDirection()
        updateRotation(power, DIRECTION_RIGHT)
    }

    fun startLeftDirection() {
        stopRandomDirection()
        updateRotation(power, DIRECTION_LEFT)
    }

    private fun forceUpdateRotation(power: Int, direction: Boolean): Boolean {
        val newPower = power or if (direction == DIRECTION_RIGHT) 0 else ADDITION_TO_REVERSE
        characteristic.value = byteArrayOf(MACHINE_CODE, VORZE_CODE, newPower.toByte())
        if (!gatt.writeCharacteristic(characteristic)) return false

        this.power = power
        this.direction = direction

        listener?.onUpdateRotation(power, direction)
        return true
    }

    // TimerTaskは関数にして，新しいオブジェクトを作成して返すこと．同じTimerTaskは起動/cancelできない．
    private fun randomPowerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                val newPower = rnd.nextInt(101)
                if (newPower != power) updateRotation(newPower, direction)
            }
        }
    }

    private fun randomDirectionTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                val newDirection = rnd.nextInt(100 / CHANGE_DIR_POSSIBILITY) == 0
                if (newDirection != direction) updateRotation(power, newDirection)
            }
        }
    }
}