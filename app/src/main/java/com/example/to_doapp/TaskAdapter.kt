package com.example.to_doapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: MutableList<Task> = mutableListOf(),
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.tvTaskTitle.text = task.title
        holder.tvDueDate.text = task.due_date
        holder.tvCategory.text = task.category_name
        holder.tvRemarks.text = task.description
        holder.tvStatus.text = if (task.is_done) "Completed" else "Not completed"

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount() = tasks.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    fun updateTask(updatedTask: Task) {
        val position = tasks.indexOfFirst { it.id == updatedTask.id }
        if (position != -1) {
            tasks[position] = updatedTask
            notifyItemChanged(position)
        }
    }
} 