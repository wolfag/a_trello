package com.example.a_trello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a_trello.adapter.MemberListItemsAdapter
import com.example.a_trello.databinding.DialogListBinding
import com.example.a_trello.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: MemberListItemsAdapter? = null
    private var binding: DialogListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding?.root!!)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setupRecycleView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

    private fun setupRecycleView() {
        binding?.tvTitle?.text = title
        if (list.size > 0) {
            adapter = MemberListItemsAdapter(context, list)
            binding?.rvList?.apply {
                adapter = adapter
                layoutManager = LinearLayoutManager(context)
            }

            adapter!!.setOnClickListener(object : MemberListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}