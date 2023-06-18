package com.example.a_trello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a_trello.adapter.LabelColorListAdapter
import com.example.a_trello.databinding.DialogListBinding
import com.example.a_trello.databinding.ItemLabelColorBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListAdapter? = null
    private var binding: DialogListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) }

        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding?.tvTitle?.text = title
        binding?.rvList?.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListAdapter(context, list, mSelectedColor)
        binding?.rvList?.adapter = adapter

        adapter!!.onItemClickListener=object :LabelColorListAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }
}