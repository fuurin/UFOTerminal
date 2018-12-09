package com.komatsu.ufoterminal

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Toast

class MainActivity : AppCompatActivity(),
        UFOInitialFragment.InitialFragmentListener,
        UFOConnectionFragment.ConnectionFragmentListener,
        UFOMainFragment.MainFragmentListener,
        UFORecordListFragment.RecordListFragmentListener {

    private val fm = supportFragmentManager
    private var connectionFragment = UFOConnectionFragment()
    private var mainFragment: UFOMainFragment = UFOMainFragment()
    private var playerFragment: UFOPlayerFragment = UFOPlayerFragment()

    // 無駄な !! を防ぐため，「あとで初期化するから待ってて」のlateinitを使用
    private lateinit var controller: UFOController
    private lateinit var recorder: UFORecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setConnectionFragment()
        initScreen()
    }

    override fun onConnect(gatt: BluetoothGatt?) {
        mainFragment = UFOMainFragment()
        recorder = UFORecorder(mainFragment)
        controller = UFOController(gatt, recorder)
        replaceFragment(mainFragment)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && fm.backStackEntryCount <= 1) connectionFragment.confirmBleDisable()
        return super.onKeyDown(keyCode, event)
    }

    override fun onStartDemo() {
        Toast.makeText(this, "start demo", Toast.LENGTH_LONG).show()
        onConnect(null)
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

    override fun onConfirmBleDisableFinished() {
        finish()
    }

    override fun onUFOMainFragmentViewCreated() {
        mainFragment.ready(controller, recorder)
    }

    override fun onOpenRecordList() {
        mainFragment.stopAll()
        replaceFragment(UFORecordListFragment())
    }

    override fun onOpenPlayer(fileName: String) {
        playerFragment = UFOPlayerFragment() // どう頑張っても再生ボタンとシークバーが初期化されないのでフラグメントごと作り直すことにした
        playerFragment.initPlayer(this, fileName, controller)
        replaceFragment(playerFragment)
    }

    private fun setConnectionFragment() {
        val transaction = fm.beginTransaction()
        transaction.add(R.id.connectionScreen, connectionFragment)
        transaction.commit()
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