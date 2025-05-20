package com.example.to_doapp

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class ManageTaskActivity : AppCompatActivity() {
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDueDate: TextInputEditText
    private lateinit var spinnerCategory: MaterialAutoCompleteTextView
    private lateinit var btnSave: MaterialButton

    private var taskId: Int? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
        setContentView(R.layout.activity_manage_task)

        initializeViews()
        setupCategorySpinner()
        setupDueDatePicker()
        setupClickListeners()

        // Check if we're editing an existing task
        taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
        if (taskId != null) {
            loadTaskDetails()
        }
    }

    private fun initializeViews() {
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDueDate = findViewById(R.id.etDueDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Work", "School", "Personal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(adapter)
    }

    private fun setupDueDatePicker() {
        etDueDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    etDueDate.setText(dateFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun loadTaskDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = supabase.postgrest["tasks"]
                    .select {
                        filter {
                            eq("id", taskId!!)
                        }
                    }
                    .decodeSingle<Task>()

                withContext(Dispatchers.Main) {
                    etTitle.setText(task.title)
                    etDescription.setText(task.description)
                    etDueDate.setText(task.due_date)
                    spinnerCategory.setText(task.category_id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Error loading task details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveTask() {
        val title = etTitle.text.toString()
        val description = etDescription.text.toString()
        val dueDate = etDueDate.text.toString()
        val category = spinnerCategory.text.toString()

        if (title.isBlank() || description.isBlank() || dueDate.isBlank() || category.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (taskId != null) {
                    supabase.postgrest["tasks"]
                        .update({
                            "title" to title
                            "description" to description
                            "due_date" to dueDate
                            "category_id" to category
                        }) {
                            filter {
                                eq("id", taskId!!)
                            }
                        }
                } else {
                    val sharedPref = this@ManageTaskActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    val userId = sharedPref.getInt("userId", -1)

                    supabase.postgrest["tasks"]
                        .insert({
                            "title" to title
                            "description" to description
                            "due_date" to dueDate
                            "category_id" to category
                            "is_done" to false
                            "user_id" to userId
                        })
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Task saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Error saving task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 