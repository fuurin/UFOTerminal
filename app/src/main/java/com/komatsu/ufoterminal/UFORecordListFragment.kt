package com.komatsu.ufoterminal

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_record_list.*
import java.io.File


class UFORecordListFragment : Fragment(),
        UFORecordRecyclerAdapter.OnRecyclerListener {

    private lateinit var listener: RecordListFragmentListener
    private lateinit var filesDir: String

    interface RecordListFragmentListener {
        fun onOpenPlayer(fileName: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record_list, container, false)
    }

    override fun onAttach(context: Context) {
        if (context !is RecordListFragmentListener)
            throw RuntimeException("$context must implement RecordListFragmentListener")
        super.onAttach(context)
        listener = context
        filesDir = context.filesDir.path
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateRecordList()
        recordRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onRecordSelected(fileName: String) {
        listener.onOpenPlayer(fileName)
    }

    override fun onRecordRenameStart(fileName: String) {
        val editView = EditText(activity)
        editView.text = SpannableStringBuilder(fileName)
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder
                .setTitle(R.string.record_rename_title)
                .setMessage(R.string.record_rename_message)
                .setView(editView.withMarginLayout())
                .setPositiveButton(R.string.record_save) { _, _ -> checkOverwrite(fileName, editView.text.toString()) }
                .setNeutralButton(R.string.record_cancel) { _, _ -> }
        val dialog = dialogBuilder.create()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun openOverwriteConfirmDialog(oldName: String, newName: String) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_overwrite_title)
                .setMessage(R.string.confirm_record_overwrite_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ -> rename(oldName, newName) }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> }
                .create().show()
    }

    private fun checkOverwrite(oldName: String, newName: String) {
        if (File("${activity?.filesDir}/$newName.csv").isFile) {
            openOverwriteConfirmDialog(oldName, newName)
        } else {
            rename(oldName, newName)
        }
    }

    override fun onRecordDeleteStart(fileName: String) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirm_record_delete_title)
                .setMessage(R.string.confirm_record_delete_message)
                .setPositiveButton(R.string.confirm_ok) { _, _ -> delete(fileName) }
                .setNegativeButton(R.string.confirm_cancel) { _, _ -> }
                .create().show()
    }

    private fun csvFile(fileName: String): File {
        return CSVFile(filesDir, fileName).file
    }

    private fun csvRecords(): List<UFORecordFile> {
        return File(filesDir).listFiles()
                .filter { it.isFile && it.name.endsWith(".csv") }
                .sortedByDescending { it.created() }
                .map { UFORecordFile(it.name.removeSuffix(".csv"), it.created()) }
    }

    private fun updateRecordList() {
        recordRecyclerView.adapter = UFORecordRecyclerAdapter(activity, csvRecords(), this)
    }

    private fun rename(oldName: String, newName: String) {
        if (oldName === newName) return
        csvFile(oldName).renameTo(csvFile(newName))
        updateRecordList()
    }

    private fun delete(fileName: String) {
        csvFile(fileName).delete()
        updateRecordList()
    }
}
