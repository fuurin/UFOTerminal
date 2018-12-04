package com.komatsu.ufoterminal

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Handler
import android.provider.Settings
import android.util.Log

class BleConnector(
        private val activity: Activity,
        private val deviceName: String,
        private val listener: BleConnectorListener,
        var scanTimeOutPeriod: Long = 10000,
        private val forceActiveDevice: Boolean = true
) {

    var isScanning = false
    var isConnected = false
    private lateinit var scanCallback: ScanCallback // scan start/stopは同じ参照のコールバックである必要がある

    private val btManager: BluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val btAdapter: BluetoothAdapter = btManager.adapter
    private val bleScanner: BluetoothLeScanner = btAdapter.bluetoothLeScanner
    private val scanStopHandler: Handler = Handler()

    private var blGatt: BluetoothGatt? = null

    interface BleConnectorListener {
        fun onConnect(gatt: BluetoothGatt)
        fun onStartConnect()
        fun onTimeout()
    }

    init {
        forceActiveDevice()
    }

    fun connect() {
        forceActiveDevice()
        scanStopHandler.postDelayed(scanFailedCallback, scanTimeOutPeriod)
        listener.onStartConnect()
        startScan()
    }

    fun disconnect() {
        if (isScanning) stopScan()
        if (blGatt != null) {
            (blGatt as BluetoothGatt).apply { disconnect(); close(); }
            blGatt = null
            isConnected = false
        }
    }

    private fun forceActiveDevice() {
        if (forceActiveDevice) {
            forceLocationSourceOn()
            forceBluetoothOn()
        }
    }

    private fun bleIsEnabled(): Boolean {
        return btAdapter.isEnabled
    }

    private fun forceBluetoothOn() {
        if (bleIsEnabled()) return
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, 0)
    }

    private fun locationSourceIsEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE)
        return (locationManager as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun forceLocationSourceOn() {
        if (locationSourceIsEnabled()) return
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivityForResult(intent, 0)
    }

    private fun startScan() {
        if (isScanning) return
        Log.v("DEBUG", "START SCAN")
        scanCallback = scanCallbacks()
        bleScanner.startScan(scanCallback)
        isScanning = true
    }

    private fun stopScan() {
        if (!isScanning) return
        Log.v("DEBUG", "STOP SCAN")
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private val scanFailedCallback = Runnable {
        if (isScanning) {
            listener.onTimeout()
            stopScan()
        }
    }

    // start/stopが同じオブジェクトである必要があるため，例外的にvalの形で持つ．
    // そうしないと無限にscanを続ける．
    private fun scanCallbacks(): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                // ここにたどり着かなければ，設定からアプリの現在位置取得権限を手動で許可する．
                "GET_SCAN_RESULT".log()

                val device = (result ?: return).device ?: return

                if (device.name != null) device.name.log()
                if (deviceName != device.name) return

                stopScan()
                scanStopHandler.removeCallbacksAndMessages(null) // TODO: なぜか一度接続した後切断してもう一度つなぐと10秒ほど待たされる．

                device.connectGatt(activity, true, gattCallbacks()).connect()
            }
        }
    }

    private fun gattCallbacks(): BluetoothGattCallback {
        return object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    blGatt = gatt ?: return
                    gatt.discoverServices()
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnect()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (gatt == null) return
                isConnected = true
                listener.onConnect(gatt)
            }
        }
    }
}