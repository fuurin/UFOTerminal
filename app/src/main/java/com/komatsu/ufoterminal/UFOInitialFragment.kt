package com.komatsu.ufoterminal


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_initial.*

class UFOInitialFragment : Fragment() {

    lateinit var listener: InitialFragmentListener

    companion object {
        const val DEMO_MODE = true
    }

    interface InitialFragmentListener {
        fun onStartDemo()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_initial, container, false)
    }

    override fun onAttach(context: Context) {
        if (context !is InitialFragmentListener)
            throw RuntimeException("$context must implement InitialFragmentListener")
        super.onAttach(context)
        listener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialImage.setOnLongClickListener { listener.onStartDemo(); true }
    }
}
