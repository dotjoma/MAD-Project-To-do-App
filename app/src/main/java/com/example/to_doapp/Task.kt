package com.example.to_doapp

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val due_date: String,
    val is_done: Boolean,
    val category_id: Int,
    val category_name: String? = null,
    val user_id: Int
)