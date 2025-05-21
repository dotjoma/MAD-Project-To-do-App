package com.example.to_doapp

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int? = 0,
    val name: String,
    val user_id: Int
) 