package com.komatsu.ufoterminal


import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
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
        controller.start(50)
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

    override fun onUpdateTime(time: Float) {
        playTimeText.text = time.timeFormat()
    }

    override fun onPlayFinished() {
        playButton.isChecked = false
    }

    override fun onDetach() {
        super.onDetach()
        if (this::player.isInitialized) player.end()
    }

    private fun initView() {
        playCreatedTime.text = file.created
        playTitleText.text = file.title
        playButton.isChecked = false
        playTimeText.text = 0f.timeFormat()
        playTimeSeekBar.max = player.record.last().time
    }

    private fun attachEvents() {
        playButton.setOnClickListener { playing((it as ToggleButton).isChecked) }
        playTimeSeekBar.setOnSeekBarChangeListener(playerSeekBarListener)
        back1Button.setOnClickListener { player.backward(1.0f) }
        forward1Button.setOnClickListener { player.forward(1.0f) }
        forward5Button.setOnClickListener { player.forward(5.0f) }
        forward10Button.setOnClickListener { player.forward(10.0f) }
    }

    private fun playing(checked: Boolean) {
        player.apply { if (checked) start() else pause() }
    }
}
