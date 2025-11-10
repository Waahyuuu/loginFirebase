package com.example.tugas2pbb.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tugas2pbb.databinding.ItemTodoBinding
import com.example.tugas2pbb.entity.Todo

class Todoadapter(
    private val dataset: MutableList<Todo>,
    private val todoItemEvents: TodoItemEvents
) : RecyclerView.Adapter<Todoadapter.ViewHolder>() {

    interface TodoItemEvents {
        fun onTodoItemEdit(todo: Todo)
        fun onTodoItemDelete(todo: Todo)
    }

    inner class ViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            binding.title.text = todo.title
            binding.description.text = todo.description

            binding.root.setOnClickListener {
                todoItemEvents.onTodoItemEdit(todo)
            }

            binding.root.setOnLongClickListener {
                todoItemEvents.onTodoItemDelete(todo)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(newData: List<Todo>) {
        dataset.clear()
        dataset.addAll(newData)
        notifyDataSetChanged()
    }
}
