package com.komatsu.ufoterminal


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_player.*


class UFOPlayerFragment : Fragment(),
        UFOPlayer.PlayerListener {

    private lateinit var file: CSVFile
    private lateinit var player: UFOPlayer
    private lateinit var playerSeekBarListener: UFOPlayerSeekBarListener


    fun initPlayer(activity: Activity, fileName: String, controller: UFOController) {
        file = CSVFile(activity.filesDir.path, fileName)
        player = UFOPlayer(file, controller, this)
        playerSeekBarListener = UFOPlayerSeekBarListener(player)
    }

    fun start() {
        if (this::player.isInitialized && playButton.isChecked) player.start()
    }

    fun stopAll() {
        if (this::player.isInitialized) player.end()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::player.isInitialized) player.end()
    }

    override fun onUpdateTime(time: Int) {
        playTimeText.text = time.recordTimeFormat()
        playTimeSeekBar.progress = time
    }

    override fun onPlayFinished() {
        playButton.isChecked = false
    }

    private fun initView() {
        playTitleText.text = file.title
        playCreatedTime.text = file.created
        playLength.text = player.record.last().time.recordTimeFormat()
        playContinuouslyButton.isChecked = false
        playButton.isChecked = false
        playTimeText.text = 0.recordTimeFormat()
        playTimeSeekBar.max = player.record.last().time
    }

    private fun attachEvents() {
        playContinuouslyButton.setOnClickListener { player.playContinuously = (this as ToggleButton).isChecked }
        playButton.setOnClickListener { playing((it as ToggleButton).isChecked) }
        playTimeSeekBar.setOnSeekBarChangeListener(playerSeekBarListener)
        back1Button.setOnClickListener { player.backward(10) }
        forward1Button.setOnClickListener { player.forward(10) }
        forward5Button.setOnClickListener { player.forward(50) }
        forward10Button.setOnClickListener { player.forward(100) }
        sendRecordButton.setOnClickListener { openMailer() }
    }

    private fun playing(checked: Boolean) {
        player.apply {
            if (checked) {
                if (playTimeSeekBar.progress == playTimeSeekBar.max) end()
                start()
            } else pause()
        }
    }

    private fun openMailer() {
        // 参考： http://kit-lab.hatenablog.jp/entry/2017/01/08/011428
        val uri = FileProvider.getUriForFile(activity!!, "${activity!!.applicationContext.packageName}.provider", file.file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.play_send_record_title))
        intent.putExtra(Intent.EXTRA_TEXT, file.title)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, null))
    }
}
