package com.example.a_trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.databinding.ActivityMembersBinding
import com.example.a_trello.databinding.ItemMemberBinding
import com.example.a_trello.models.User
import com.example.a_trello.utils.Constants

class MemberListItemsAdapter(private val context: Context, private val list: ArrayList<User>) :
    RecyclerView.Adapter<MemberListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(var binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemberBinding.inflate(
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

        Glide.with(context).load(model.image).centerCrop()
            .placeholder(R.drawable.ic_user_place_holder).into(holder.binding.ivMemberImage)

        holder.binding.tvMemberEmail.text = model.email
        holder.binding.tvMemberName.text = model.name

        holder.binding.ivSelectedMember.visibility = if (model.selected) View.VISIBLE else View.GONE

        holder.binding.root.setOnClickListener {
            if (onClickListener != null) {
                val action = if (model.selected) Constants.UN_SELECT else Constants.SELECT
                onClickListener!!.onClick(position, model, action)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }
}