package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity(),
        UFOConnectionFragment.ConnectionFragmentListener,
        UFOMainFragment.MainFragmentListener,
        UFORecordListFragment.RecordListFragmentListener {

    private val fm = supportFragmentManager
    private val mainFragment: UFOMainFragment = UFOMainFragment()
    private val playerFragment: UFOPlayerFragment = UFOPlayerFragment()

    // 無駄な !! を防ぐため，「あとで初期化するから待ってて」のlateinitを使用
    private lateinit var controller: UFOController
    private lateinit var recorder: UFORecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScreen()
    }

    override fun onConnect(gatt: BluetoothGatt) {
        recorder = UFORecorder(this, mainFragment)
        controller = UFOController(gatt, recorder)
        replaceFragment(mainFragment)
    }

    override fun onDisconnectConfirm() {
        controller.pause()
    }

    override fun onDisconnectStart() {
        if (mainFragment.isVisible) mainFragment.stopAll()
        if (playerFragment.isVisible) playerFragment.stopAll()
        initScreen()
    }

    override fun onDisconnectCancel() {
        if (mainFragment.isVisible) mainFragment.start()
        if (playerFragment.isVisible) playerFragment.start()
    }

    override fun onUFOMainFragmentViewCreated() {
        if (controller == null || recorder == null) "NO controller or recorder!".log()
        mainFragment.ready(controller, recorder)
    }

    override fun onOpenRecordList() {
        mainFragment.stopAll()
        replaceFragment(UFORecordListFragment())
    }

    override fun onOpenPlayer(fileName: String) {
        playerFragment.initPlayer(this, fileName, controller)
        replaceFragment(playerFragment)
    }

    private fun initScreen() {
        fm.backStackEntryCount.loop { fm.popBackStack() }
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