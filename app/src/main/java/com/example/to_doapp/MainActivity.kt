package com.example.to_doapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: FloatingActionButton

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
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        fabAddTask = findViewById(R.id.fabAddTask)

        setupRecyclerView()
        setupClickListeners()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Navigate to task detail
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("taskId", task.id)
                startActivity(intent)
            },
            onTaskStatusChange = { task, isCompleted ->
                updateTaskStatus(task, isCompleted)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupClickListeners() {
        fabAddTask.setOnClickListener {
            startActivity(Intent(this, ManageTaskActivity::class.java))
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = supabase.postgrest["tasks"]
                    .select {
                        // Add any filters here if needed
                    }
                    .decodeList<Task>()

                withContext(Dispatchers.Main) {
                    taskAdapter.updateTasks(tasks)
                }
            } catch (e: Exception) {
                Log.d("LOAD_TASKS", "Error loading tasks: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading tasks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.postgrest["tasks"]
                    .update({
                        "is_completed" to isCompleted
                    }) {
                        filter {
                            eq("id", task.id)
                        }
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        if (isCompleted) "Task marked as completed" else "Task marked as incomplete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.d("UPDATE_TASK", "Error updating task: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error updating task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks() // Reload tasks when returning to this activity
    }
} 