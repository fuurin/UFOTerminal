package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
        BleConnector.BleConnectorListener,
        UFOMainFragment.MainFragmentListener,
        UFORecordListFragment.RecordListFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFragment()
        connectionButton.setOnClickListener { connection() }
        bleConnector = BleConnector(this, this)
    }

    override fun onStartConnect() {
        connectionStateText.setText(R.string.connecting)
        connectionButton.isChecked = true
    }

    override fun onConnect(gatt: BluetoothGatt) {
        connectionStateText.text = getString(R.string.connected)
        connectionButton.isChecked = true
        recorder = UFORecorder(this, mainFragment)
        controller = UFOController(gatt, recorder)
        replaceFragment(mainFragment)
    }

    override fun onTimeout() {
        connectionStateText.text = getString(R.string.timeout)
        connectionButton.isChecked = false
    }

    override fun onDisconnect() {
        connectionStateText.text = getString(R.string.disconnected)
        connectionButton.isChecked = false
        mainFragment.stopAll()
        initFragment()
    }

    override fun onDisconnectCancel() {
        connectionButton.isChecked = true
    }

    override fun onMainFragmentViewCreated() {
        mainFragment.ready(controller, recorder)
    }

    override fun onOpenRecordList() {
        mainFragment.stopAll()
        replaceFragment(UFORecordListFragment())
    }

    override fun onOpenPlayer(fileName: String) {
        fileName.log()
    }

    private val deviceName = "UFOSA"
    private val fm = supportFragmentManager
    private val mainFragment: UFOMainFragment = UFOMainFragment()

    // 無駄な !! を防ぐため，「あとで初期化するから待ってて」のlateinitを使用
    private lateinit var bleConnector: BleConnector
    private lateinit var controller: UFOController
    private lateinit var recorder: UFORecorder

    private fun initFragment() {
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = fm.beginTransaction()
        transaction.add(R.id.mainScreen, UFOInitialFragment())
        transaction.commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.mainScreen, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun connection() {
        if (connectionButton.isChecked) bleConnector.connect(deviceName)
        else {
            controller.stop()
            bleConnector.disconnect(true)
        }
    }
}

/*
ConnectionFragmentがonAttachでConnectionListenerを実装した親Activityを受け取る
ConnectionFragmentがonCreateのでinitを呼び出す
initではBleManagerを生成
connectionButtonのonClickListenerにBleManagerのconnectionを呼び出す
connectionでは，BLEをOnにし，ボタンがOnになっていれば接続，そうでなければ切断する

BleManagerではgetSystemServiceでBLManagerを取得
BLManagerからBLAdapterをもらう
BLAdapterからBLScannerをもらう
Scanを始めるときは，scanCallback関数をBLScanner.startScanに渡す
scanCallback関数の中では，お目当てのDEVICE_NAMEのデバイスが見つかったらスキャンを終了し，
そのデバイスのGattに接続する．Gatt（Generic Attribute）とは，BLEデバイス内のデータの階層構造のこと
Gattに接続するとき，Gattに接続が完了したとき呼ばれるGattCallBacks関数を設定する
GattCallbacksの中ではonServicesDiscovered関数でListenerに対してonConnectを呼び出し，同時にgattを渡す

ConnectFragmentはonConnectで受け取ったgattをさらにonConnectでMainActivityに渡す．

MainActivityはonConnect関数内でUFOControllerをgattを渡して生成
UFOControllerをRecordFragmentとControlFragmentとControlDirectionFragmentに渡す

UFOControllerは受け取ったGattを使って色々やる．
まずはGattからgetServiceで対象となるSERVICEUUIDのServiceを受け取る
ServiceからgetCharacteristicで対象となるCHARACTERISTICUUIDのCharacteristicを受け取る
characteristicにsetValueをすることでCharacteristicの状態を変える
GattのwriteCharacteristicで状態を変えられたCharacteristicを渡すことでデバイスを動作させる

*/