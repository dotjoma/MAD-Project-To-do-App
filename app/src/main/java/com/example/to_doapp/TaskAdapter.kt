package com.example.to_doapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onTaskClick: (Task) -> Unit,
    private val onTaskStatusChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)
        val cbTaskStatus: MaterialCheckBox = view.findViewById(R.id.cbTaskStatus)
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
        holder.tvCategory.text = task.category_id.toString()
        holder.tvRemarks.text = task.description
        holder.cbTaskStatus.isChecked = task.is_done

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        holder.cbTaskStatus.setOnCheckedChangeListener { _, isChecked ->
            onTaskStatusChange(task, isChecked)
        }
    }

    override fun getItemCount() = tasks.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
} 