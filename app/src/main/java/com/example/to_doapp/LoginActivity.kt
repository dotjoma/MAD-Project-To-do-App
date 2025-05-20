package com.example.to_doapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
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

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://itkcowilyxyizuupfskw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0a2Nvd2lseXh5aXp1dXBmc2t3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc3NDAzNzIsImV4cCI6MjA2MzMxNjM3Mn0.HFUPpTpG-fDBvtjeJj6IWwUnq4w7LFr-JRVYEbkxcCM"
    ) {
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
        })
    }

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        signupText = findViewById(R.id.signupText)

        loginButton.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = loginUser(username, password)

                    withContext(Dispatchers.Main) {
                        if (user != null) {
                            saveLoginState(this@LoginActivity, user)

                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            Log.d("Login", "Login successful for $username")

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Log.d("LOGIN", "Login failed. Please check your credentials.")
                            Toast.makeText(this@LoginActivity, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("LOGIN", "Failed: ${e.message}")
                }
            }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    @OptIn(UnstableApi::class) suspend fun loginUser(username: String, password: String): User? {
        return try {
            val response = supabase.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<User>()

            Log.d("LOGIN_RESPONSE", "Response size: ${response.size}")

            if (response.isNotEmpty()) {
                val user = response.first()
                Log.d("USER_DATA", """
                    User found:
                    ID: ${user.id}
                    Username: ${user.username}
                """.trimIndent())

                if (user.password == password) {
                    Log.d("LOGIN_STATUS", "Password match successful")
                    user
                } else {
                    Log.d("LOGIN_STATUS", "Password match failed")
                    null
                }
            } else {
                Log.d("LOGIN_STATUS", "No user found with username: $username")
                null
            }
        } catch (e: Exception) {
            Log.d("LOGIN_USER", "Login error: ${e.message}")
            null
        }
    }

    fun saveLoginState(context: Context, user: User) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            putString("username", user.username)
            user.id?.let { putInt("userId", it) }
            apply()
        }
    }
} 