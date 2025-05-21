package com.example.to_doapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.material.button.MaterialButton
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

class ProfileActivity : AppCompatActivity() {
    private lateinit var tvUsername: MaterialTextView
    private lateinit var tvEmail: MaterialTextView
    private lateinit var btnLogout: MaterialButton

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
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initializeViews() {
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        val username = sharedPref.getString("username", "")
        val email = sharedPref.getString("email", "")

        tvUsername.text = username
        tvEmail.text = email
    }

    @OptIn(UnstableApi::class) private fun logout() {
        // Clear user session
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "")

        Log.d("LOGOUT", "User: $username initiate a logout request.")

        sharedPref.edit().clear().apply()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 