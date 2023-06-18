package com.example.a_trello.adapter

import android.app.AlertDialog
import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_trello.activities.TaskListActivity
import com.example.a_trello.databinding.ItemTaskBinding
import com.example.a_trello.models.Card
import com.example.a_trello.models.Task
import java.util.Collections

class TaskListItemsAdapter(private val context: Context, private var list: ArrayList<Task>) :
    RecyclerView.Adapter<TaskListItemsAdapter.ViewHolder>() {

    private var mPositionDraggedForm = -1
    private var mPositionDraggedTo = -1

    inner class ViewHolder(var binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(
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
        val model = list[holder.adapterPosition]

        if (position == list.size - 1) {
            holder.binding.tvAddTaskList.visibility = View.VISIBLE
            holder.binding.llTaskItem.visibility = View.GONE
            holder.binding.tvAddCard.visibility = View.GONE
        } else {
            holder.binding.tvAddTaskList.visibility = View.GONE
            holder.binding.llTaskItem.visibility = View.VISIBLE
            holder.binding.tvAddCard.visibility = View.VISIBLE
        }

        holder.binding.tvTaskListTitle.text = model.title
        holder.binding.tvAddTaskList.setOnClickListener {
            holder.binding.tvAddTaskList.visibility = View.GONE
            holder.binding.cvAddTaskListName.visibility = View.VISIBLE
        }

        holder.binding.ibCloseListName.setOnClickListener {
            holder.binding.tvAddTaskList.visibility = View.VISIBLE
            holder.binding.cvAddTaskListName.visibility = View.GONE
        }

        holder.binding.ibDoneListName.setOnClickListener {
            val listName = holder.binding.etTaskListName.text.toString()

            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.createTaskList(listName)
                }
            } else {
                Toast.makeText(context, "Please enter list name", Toast.LENGTH_LONG).show()
            }
        }

        holder.binding.ibEditListName.setOnClickListener {
            holder.binding.etEditTaskListName.setText(model.title)
            holder.binding.llTitleView.visibility = View.GONE
            holder.binding.cvEditTaskListName.visibility = View.VISIBLE
        }

        holder.binding.ibCloseEditableView.setOnClickListener {
            holder.binding.llTitleView.visibility = View.VISIBLE
            holder.binding.cvEditTaskListName.visibility = View.GONE
        }

        holder.binding.ibDoneEditListName.setOnClickListener {
            val listName = holder.binding.etEditTaskListName.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateTaskList(holder.adapterPosition, listName, model)
                }
            } else {
                Toast.makeText(context, "Please enter list name", Toast.LENGTH_LONG).show()
            }
        }

        holder.binding.ibDeleteList.setOnClickListener {
            alertDialogForDeleteList(holder.adapterPosition, model.title)
        }

        holder.binding.tvAddCard.setOnClickListener {
            holder.binding.tvAddCard.visibility = View.GONE
            holder.binding.cvAddCard.visibility = View.VISIBLE

            holder.binding.ibCloseCardName.setOnClickListener {
                holder.binding.tvAddCard.visibility = View.VISIBLE
                holder.binding.cvAddCard.visibility = View.GONE
            }

            holder.binding.ibDoneCardName.setOnClickListener {
                val cardName = holder.binding.etCardName.text.toString()
                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(holder.adapterPosition, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please enter card detail", Toast.LENGTH_LONG).show()
                }
            }
        }
        holder.binding.rvCardList.layoutManager = LinearLayoutManager(context)
        holder.binding.rvCardList.setHasFixedSize(true)

        val adapter = CardListItemAdapter(context, model.cards)
        holder.binding.rvCardList.adapter = adapter

        adapter.setOnClickListener(object : CardListItemAdapter.OnClickListener {
            override fun onClick(cardPosition: Int) {
                if (context is TaskListActivity) {
                    context.cardDetails(holder.adapterPosition, cardPosition)
                }
            }
        })

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        holder.binding.rvCardList.addItemDecoration(dividerItemDecoration)
        val helper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val draggedPosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                if (mPositionDraggedForm == -1) {
                    mPositionDraggedForm = draggedPosition
                }
                mPositionDraggedTo = targetPosition

                Collections.swap(
                    list[holder.adapterPosition].cards,
                    draggedPosition,
                    targetPosition
                )
                adapter.notifyItemMoved(draggedPosition, targetPosition)

                return false // true if moved
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

            //            Finally when the dragging is completed than call the function to update the cards in the database and reset the global variables.)
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                if (mPositionDraggedForm != -1 && mPositionDraggedTo != -1 && mPositionDraggedForm != mPositionDraggedTo) {
                    (context as TaskListActivity).updateCardsInTaskList(
                        position,
                        list[holder.adapterPosition].cards
                    )

                    mPositionDraggedForm = -1
                    mPositionDraggedTo = -1
                }
            }
        })

        helper.attachToRecyclerView(holder.binding.rvCardList)
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()

        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}