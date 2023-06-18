package com.example.a_trello.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.databinding.ItemBoardBinding
import com.example.a_trello.models.Board

class BoardItemsAdapter(private val context: Context, private var list: ArrayList<Board>) :
    RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(var itemBinding: ItemBoardBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        Glide.with(context).load(model.image).centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(holder.itemBinding.ivBoardImage)

        holder.itemBinding.tvName.text = model.name
        holder.itemBinding.tvCreatedBy.text =
            "Created By : ${model.createdBy}"

        holder.itemBinding.root.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BoardItemsAdapter.ViewHolder {
        return ViewHolder(
            ItemBoardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }
}