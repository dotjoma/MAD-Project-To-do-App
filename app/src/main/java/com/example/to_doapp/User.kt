package com.example.to_doapp

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val username: String? = null,
    val password: String? = null
)