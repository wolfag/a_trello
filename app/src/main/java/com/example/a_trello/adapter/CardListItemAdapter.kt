package com.example.a_trello.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_trello.activities.TaskListActivity
import com.example.a_trello.databinding.ItemCardBinding
import com.example.a_trello.models.Card
import com.example.a_trello.models.SelectedMembers

class CardListItemAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<CardListItemAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(var binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        if (model.labelColor.isNotEmpty()) {
            holder.binding.vLabelColor.visibility = View.VISIBLE
            holder.binding.vLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
        } else {
            holder.binding.vLabelColor.visibility = View.GONE
        }

        holder.binding.tvCardName.text = model.name

        if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
            for (i in context.mAssignedMembersDetailList.indices) {
                for (j in model.assignedTo) {
                    if (context.mAssignedMembersDetailList[i].id == j) {
                        val selectedMember = SelectedMembers(
                            context.mAssignedMembersDetailList[i].id,
                            context.mAssignedMembersDetailList[i].image
                        )

                        selectedMembersList.add(selectedMember)
                    }
                }
            }

            if (selectedMembersList.size > 0) {
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createBy) {
                    holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                } else {
                    holder.binding.rvCardSelectedMembersList.visibility = View.VISIBLE

                    holder.binding.rvCardSelectedMembersList.layoutManager =
                        GridLayoutManager(context, 4)
                    val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                    holder.binding.rvCardSelectedMembersList.adapter = adapter
                    adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener {
                        override fun onClick() {
                            if (onClickListener != null) {
                                onClickListener!!.onClick(holder.adapterPosition)
                            }
                        }
                    })
                }
            } else {
                holder.binding.rvCardSelectedMembersList.visibility = View.GONE
            }
        }

        holder.binding.root.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(holder.adapterPosition)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }
}