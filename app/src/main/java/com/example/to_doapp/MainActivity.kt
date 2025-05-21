package com.example.to_doapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import io.github.jan.supabase.postgrest.query.Order
import android.widget.ImageView

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

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        recyclerView = findViewById(R.id.recyclerView)
        fabAddTask = findViewById(R.id.fabAddTask)

        setupRecyclerView()
        
        setupClickListeners()
            Handler(Looper.getMainLooper()).postDelayed({
            loadTasks()
        }, 100)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Navigate to task detail
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("taskId", task.id)
                startActivity(intent)
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

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun loadTasks() {
        val sharedPref = this@MainActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)

        Log.d("LOAD_TASKS", "Current userId: $userId")

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LOAD_TASKS", "Fetching tasks for user: $userId")

                val tasks = supabase.postgrest["tasks"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            this@select.order("created_at", Order.DESCENDING)
                        }
                    }
                    .decodeList<Task>()

                Log.d("LOAD_TASKS", "Found ${tasks.size} tasks")

                val categories = supabase.postgrest["categories"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<Category>()

                val categoryMap = categories.associate { it.id to it.name }

                val tasksWithCategories = tasks.map { task ->
                    task.copy(category_name = categoryMap[task.category_id] ?: "Uncategorized")
                }

                withContext(Dispatchers.Main) {
                    taskAdapter.updateTasks(tasksWithCategories)
                }
            } catch (e: Exception) {
                Log.d("LOAD_TASKS", "Error loading tasks: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading tasks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class) private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.postgrest["tasks"]
                    .update({
                        set("is_done", isCompleted)
                    }) {
                        filter {
                            eq("id", task.id)
                        }
                    }

                // Update the task in the adapter
                withContext(Dispatchers.Main) {
                    val updatedTask = task.copy(is_done = isCompleted)
                    taskAdapter.updateTask(updatedTask)
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
        loadTasks()
    }
} 