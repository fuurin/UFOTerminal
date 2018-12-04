package com.komatsu.ufoterminal

import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_connection.*


class UFOConnectionFragment : Fragment(),
        BleConnector.BleConnectorListener{

    interface ConnectionFragmentListener {
        fun onConnect(gatt: BluetoothGatt)
        fun onDisconnectConfirm()
        fun onDisconnectStart()
        fun onDisconnectCancel()
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
        connector = BleConnector(activity?: return, this)
        connectionButton.setOnClickListener { connection((it as ToggleButton).isChecked) }
    }

    override fun onStartConnect() {
        connectionStateText.setText(R.string.connecting)
        connectionButton.isChecked = true
    }

    override fun onConnect(gatt: BluetoothGatt) {
        connectionStateText.text = getString(R.string.connected)
        connectionButton.isChecked = true
        onConnect(gatt)
    }

    override fun onTimeout() {
        connectionStateText.text = getString(R.string.timeout)
        connectionButton.isChecked = false
    }

    private val deviceName = "UFOSA"

    lateinit private var listener: ConnectionFragmentListener
    lateinit private var connector: BleConnector
    lateinit private var controller: UFOController

    private fun connection(checked: Boolean) {
        if (checked) connect() else disconnect()
    }

    private fun connect() {
        connector.connect(deviceName)
    }

    private fun disconnect(confirm: Boolean = true) {
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
        controller.pause()
        listener.onDisconnectConfirm()
    }

    private fun disconnectStart() {
        connector.disconnect()
        connectionStateText.text = getString(R.string.disconnected)
        connectionButton.isChecked = false
        listener.onDisconnectStart()
    }

    private fun disconnectCancel() {
        connectionButton.isChecked = true
        listener.onDisconnectCancel()
    }
}
