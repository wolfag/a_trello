package com.example.a_trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.databinding.ItemCardSelectedMemberBinding
import com.example.a_trello.models.SelectedMembers

class CardMemberListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMemberListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(var binding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCardSelectedMemberBinding.inflate(
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

        if (position == list.size - 1 && assignMembers) {
            holder.binding.ivAddMember.visibility = View.VISIBLE
            holder.binding.ivSelectedMemberImage.visibility = View.GONE
        } else {
            holder.binding.ivAddMember.visibility = View.GONE
            holder.binding.ivSelectedMemberImage.visibility = View.VISIBLE

            Glide.with(context).load(model.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.ivSelectedMemberImage)
        }
        holder.binding.root.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick()
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }
}