package com.example.to_doapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
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
    private var categories = mutableListOf<Category>()

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://itkcowilyxyizuupfskw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0a2Nvd2lseXh5aXp1dXBmc2t3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc3NDAzNzIsImV4cCI6MjA2MzMxNjM3Mn0.HFUPpTpG-fDBvtjeJj6IWwUnq4w7LFr-JRVYEbkxcCM"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_task)

        initializeViews()
        loadCategories()
        setupDueDatePicker()
        setupClickListeners()

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

    @OptIn(UnstableApi::class) private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPref = this@ManageTaskActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)

                categories = supabase.postgrest["categories"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<Category>().toMutableList()

                withContext(Dispatchers.Main) {
                    val categoryNames = categories.map { it.name }.toMutableList()
                    categoryNames.add("Add Category")
                    val adapter = ArrayAdapter(this@ManageTaskActivity, android.R.layout.simple_dropdown_item_1line, categoryNames)
                    spinnerCategory.setAdapter(adapter)

                    if (categoryNames.size > 1) {
                        spinnerCategory.setText(categoryNames[0], false)
                    }
                }
            } catch (e: Exception) {
                Log.d("CATEGORIES", "Error loading categories: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Error loading categories", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

        spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = spinnerCategory.adapter.getItem(position).toString()
            if (selectedItem == "Add Category") {
                showAddCategoryDialog()
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etCategoryName = dialogView.findViewById<TextInputEditText>(R.id.etCategoryName)

        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = etCategoryName.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    createCategory(categoryName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun createCategory(categoryName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPref = this@ManageTaskActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)

                val newCategory = Category(
                    name = categoryName,
                    user_id = userId
                )

                supabase.postgrest["categories"]
                    .insert(newCategory)

                categories = supabase.postgrest["categories"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<Category>()
                    .toMutableList()

                withContext(Dispatchers.Main) {
                    val categoryNames = categories.map { it.name }.toMutableList()
                    categoryNames.add("Add Category")
                    val adapter = ArrayAdapter(
                        this@ManageTaskActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                    )
                    spinnerCategory.setAdapter(adapter)
                    spinnerCategory.setText(categoryName, false)
                    Toast.makeText(
                        this@ManageTaskActivity,
                        "Category added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CREATE_CATEGORY", "Error creating category", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManageTaskActivity,
                        "Error creating category: ${e.message?.take(50)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadTaskDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First ensure categories are loaded
                if (categories.isEmpty()) {
                    val sharedPref = this@ManageTaskActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    val userId = sharedPref.getInt("userId", -1)
                    
                    categories = supabase.postgrest["categories"]
                        .select {
                            filter {
                                eq("user_id", userId)
                            }
                        }
                        .decodeList<Category>().toMutableList()
                }

                // Then load task details
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
                    
                    // Find and set the category
                    val category = categories.find { it.id == task.category_id }
                    if (category != null) {
                        spinnerCategory.setText(category.name, false)
                    } else {
                        Toast.makeText(this@ManageTaskActivity, "Category not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Error loading task details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun saveTask() {
        val title = etTitle.text.toString()
        val description = etDescription.text.toString()
        val dueDate = etDueDate.text.toString()
        val categoryName = spinnerCategory.text.toString()

        if (title.isBlank() || description.isBlank() || dueDate.isBlank() || categoryName.isBlank() || categoryName == "Add Category") {
            Toast.makeText(this, "Please fill all fields and select a valid category", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPref = this@ManageTaskActivity.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)
                val category = categories.find { it.name == categoryName }

                if (category == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ManageTaskActivity, "Invalid category selected", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                if (taskId != null) {
                    supabase.postgrest["tasks"]
                        .update({
                            set("title", title)
                            set("description", description)
                            set("due_date", dueDate)
                            set("is_done", false)
                            set("category_id", category.id)
                            set("user_id", userId)
                        }) {
                            filter {
                                eq("id", taskId!!)
                            }
                        }
                } else {
                    supabase.postgrest["tasks"]
                        .insert(
                            Task(
                                title = title,
                                description = description,
                                due_date = dueDate,
                                category_id = category.id!!,
                                user_id = userId,
                                is_done = false
                            )
                        )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Task saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.d("SAVE_TASK", "Error saving task: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageTaskActivity, "Error saving task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 