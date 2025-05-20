package com.example.to_doapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.to_doapp.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var createdAtTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        usernameTextView = findViewById(R.id.usernameTextView)
        createdAtTextView = findViewById(R.id.createdAtTextView)
        logoutButton = findViewById(R.id.logoutButton)

        // TODO: Fetch user information (username, created_at) from Supabase and display it
        // For now, displaying dummy data
        usernameTextView.text = "Username: Loading..."
        createdAtTextView.text = "Member since: Loading..."

        logoutButton.setOnClickListener {
            // TODO: Implement Supabase logout logic here
            Toast.makeText(this, "Logout functionality not yet implemented", Toast.LENGTH_SHORT).show()
            // On successful logout, navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
            startActivity(intent)
            finish()
        }
    }
} 