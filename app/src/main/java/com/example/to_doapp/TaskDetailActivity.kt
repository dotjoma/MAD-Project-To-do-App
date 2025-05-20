package com.example.to_doapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var tvTitle: MaterialTextView
    private lateinit var tvDescription: MaterialTextView
    private lateinit var tvDueDate: MaterialTextView
    private lateinit var tvCategory: MaterialTextView
    private lateinit var cbTaskStatus: MaterialCheckBox
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var taskId: Int = -1
    private var task: Task? = null

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://itkcowilyxyizuupfskw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0a2Nvd2lseXh5aXp1dXBmc2t3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc3NDAzNzIsImV4cCI6MjA2MzMxNjM3Mn0.HFUPpTpG-fDBvtjeJj6IWwUnq4w7LFr-JRVYEbkxcCM"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        initializeViews()
        setupClickListeners()

        taskId = intent.getIntExtra("taskId", -1)
        if (taskId != -1) {
            loadTaskDetails()
        } else {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvCategory = findViewById(R.id.tvCategory)
        cbTaskStatus = findViewById(R.id.cbTaskStatus)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        cbTaskStatus.setOnCheckedChangeListener { _, isChecked ->
            updateTaskStatus(isChecked)
        }

        btnEdit.setOnClickListener {
            // Navigate to ManageTaskActivity for editing
            val intent = Intent(this, ManageTaskActivity::class.java)
            intent.putExtra("taskId", taskId)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            deleteTask()
        }
    }

    private fun loadTaskDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                task = supabase.postgrest["tasks"]
                    .select {
                        filter {
                            eq("id", taskId)
                        }
                    }
                    .decodeSingle<Task>()

                withContext(Dispatchers.Main) {
                    task?.let { displayTaskDetails(it) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error loading task details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayTaskDetails(task: Task) {
        tvTitle.text = task.title
        tvDescription.text = task.description
        tvDueDate.text = task.due_date
        tvCategory.text = task.category_id.toString()
        cbTaskStatus.isChecked = task.is_done
    }

    private fun updateTaskStatus(isCompleted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.postgrest["tasks"]
                    .update({
                        "is_done" to isCompleted
                    }) {
                        filter {
                            eq("id", taskId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TaskDetailActivity,
                        if (isCompleted) "Task marked as completed" else "Task marked as incomplete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error updating task status", Toast.LENGTH_SHORT).show()
                    cbTaskStatus.isChecked = !isCompleted // Revert the checkbox state
                }
            }
        }
    }

    private fun deleteTask() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.postgrest["tasks"]
                    .delete {
                        filter {
                            eq("id", taskId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 