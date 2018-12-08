package com.komatsu.ufoterminal

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_connection.*


class UFOConnectionFragment : Fragment(),
        BleConnector.BleConnectorListener {

    private lateinit var listener: ConnectionFragmentListener
    private lateinit var connector: BleConnector

    companion object {
        const val DEVISE_NAME = "UFOSA"
    }

    interface ConnectionFragmentListener {
        fun onConnect(gatt: BluetoothGatt)
        fun onDisconnectConfirm()
        fun onDisconnectStart()
        fun onDisconnectCancel()
        fun onConfirmBleDisableFinished()
    }

    fun confirmBleDisable() {
        connector.disconnect()
        if (!connector.bleIsEnabled()) {
            listener.onConfirmBleDisableFinished()
            return
        }
        AlertDialog.Builder(context)
                .setTitle(R.string.confirm_ble_off_title)
                .setMessage(R.string.confirm_ble_off_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ ->
                    connector.disableBle()
                    listener.onConfirmBleDisableFinished()
                }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> listener.onConfirmBleDisableFinished()}
                .create().show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is ConnectionFragmentListener)
            throw RuntimeException("$context must implement ConnectionFragmentListener")
        super.onAttach(context)
        listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connector = BleConnector(activity ?: return, DEVISE_NAME, this)
        connectionButton.setOnClickListener { connection((it as ToggleButton).isChecked) }
    }

    override fun onStartConnect() {
        connectionStateText.setText(R.string.connecting)
        connectionButton.isChecked = true
    }

    override fun onConnect(gatt: BluetoothGatt) {
        connectionStateText.text = getString(R.string.connected)
        Handler().run { connectionButton.isEnabled = true }
        connectionButton.isChecked = true
        listener.onConnect(gatt)
    }

    override fun onTimeout() {
        connectionStateText.text = getString(R.string.timeout)
        connectionButton.isEnabled = true
        connectionButton.isChecked = false
    }

    private fun connection(checked: Boolean) {
        if (checked) connect() else disconnect()
    }

    private fun connect() {
        connectionButton.isEnabled = false
        connector.connect()
    }

    private fun disconnect(confirm: Boolean = true) {
        if (!connector.isConnected) return
        if (confirm) {
            disconnectConfirm()
            AlertDialog.Builder(activity)
                    .setTitle(R.string.confirm_disconnect_title)
                    .setMessage(R.string.confirm_disconnect_message)
                    .setPositiveButton(R.string.confirm_ok) { _, _ -> disconnectStart() }
                    .setNegativeButton(R.string.confirm_cancel) { _, _ -> disconnectCancel() }
                    .create().show()
        } else disconnectStart()
    }

    private fun disconnectConfirm() {
        connectionButton.isChecked = true
        listener.onDisconnectConfirm()
    }

    private fun disconnectStart() {
        listener.onDisconnectStart() // connector.disconnectよりも先に実行すること！
        connectionStateText.text = getString(R.string.disconnected)
        connectionButton.isChecked = false
        connector.disconnect()
    }

    private fun disconnectCancel() {
        listener.onDisconnectCancel()
    }
}
