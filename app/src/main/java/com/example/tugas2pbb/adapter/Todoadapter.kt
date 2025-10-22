package com.example.tugas2pbb.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tugas2pbb.databinding.ItemTodoBinding
import com.example.tugas2pbb.entity.Todo

class Todoadapter (
    private val dataset: MutableList<Todo>,
    private val todoItemEvents: Todoadapter.TodoItemEvents
): RecyclerView.Adapter<Todoadapter.CustomViewHolder>() {

    interface TodoItemEvents {
        fun onTodoItemEdit(todo: Todo): Unit
        fun onTodoItemDelete(todo: Todo): Unit
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CustomViewHolder,
        index: Int
    ) {
        val data = dataset[index]
        holder.bindData(data)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    inner class CustomViewHolder(
        val view: ItemTodoBinding
    ): RecyclerView.ViewHolder(view.root) {

        fun bindData(data: Todo){
            view.title.text = data.title
            view.description.text = data.description

            view.root.setOnClickListener {
                todoItemEvents.onTodoItemEdit(data)
            }

            view.root.setOnLongClickListener {
                todoItemEvents.onTodoItemDelete(data)
                true
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(data: List<Todo>){
        dataset.clear()
        dataset.addAll(data)
        notifyDataSetChanged()
    }

}
