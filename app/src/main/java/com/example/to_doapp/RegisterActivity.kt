package com.example.to_doapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

class RegisterActivity: AppCompatActivity() {
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    
    // Password requirement TextViews
    private lateinit var tvLengthRequirement: TextView
    private lateinit var tvUppercaseRequirement: TextView
    private lateinit var tvLowercaseRequirement: TextView
    private lateinit var tvSpecialRequirement: TextView

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
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)

        // Initialize password requirement TextViews
        tvLengthRequirement = findViewById(R.id.tvLengthRequirement)
        tvUppercaseRequirement = findViewById(R.id.tvUppercaseRequirement)
        tvLowercaseRequirement = findViewById(R.id.tvLowercaseRequirement)
        tvSpecialRequirement = findViewById(R.id.tvSpecialRequirement)

        // Set up text change listeners
        etUsername.addTextChangedListener(TextValidator(etUsername))
        etPassword.addTextChangedListener(TextValidator(etPassword))
        etConfirmPassword.addTextChangedListener(TextValidator(etConfirmPassword))

        // Register button click listener
        findViewById<View>(R.id.btnRegister).setOnClickListener { Register() }

        // Login text click listener
        findViewById<View>(R.id.tvLogin).setOnClickListener { finish() }
    }

    private fun updatePasswordRequirements(password: String) {
        // Length requirement
        if (password.length >= 8) {
            tvLengthRequirement.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvLengthRequirement.setTextColor(ContextCompat.getColor(this, R.color.white_alpha_60))
        }

        // Uppercase requirement
        if (Pattern.compile("[A-Z]").matcher(password).find()) {
            tvUppercaseRequirement.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvUppercaseRequirement.setTextColor(ContextCompat.getColor(this, R.color.white_alpha_60))
        }

        // Lowercase requirement
        if (Pattern.compile("[a-z]").matcher(password).find()) {
            tvLowercaseRequirement.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvLowercaseRequirement.setTextColor(ContextCompat.getColor(this, R.color.white_alpha_60))
        }

        // Special character requirement
        if (Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            tvSpecialRequirement.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvSpecialRequirement.setTextColor(ContextCompat.getColor(this, R.color.white_alpha_60))
        }
    }

    @OptIn(UnstableApi::class) private fun Register() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Reset errors
        usernameInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        var isValid = true

        // Validate username
        if (username.isEmpty()) {
            usernameInputLayout.error = "Username is required"
            isValid = false
        } else if (username.length < 4) {
            usernameInputLayout.error = "Username must be at least 4 characters"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (!isPasswordValid(password)) {
            passwordInputLayout.error = "Password doesn't meet requirements"
            isValid = false
        }

        // Validate password confirmation
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords don't match"
            isValid = false
        }

        if (isValid) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isTaken = isUsernameTaken(username)
                    if (isTaken) {
                        Log.d("USERNAME_RESPONSE", "Username is taken.")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                            etUsername.error = "Choose another one"
                            etUsername.requestFocus()
                        }
                        return@launch
                    }

                    val isInserted = insertUser(username, password)
                    withContext(Dispatchers.Main) {
                        if (isInserted) {
                            Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_LONG).show()
                            Log.d("REGISTRATION", "Registration successful for $username")
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("REGISTRATION", "Failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!Pattern.compile("[A-Z]").matcher(password).find()) return false
        if (!Pattern.compile("[a-z]").matcher(password).find()) return false
        if (!Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) return false
        return true
    }

    private inner class TextValidator(private val editText: TextInputEditText) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            when (editText.id) {
                R.id.etUsername -> validateUsername()
                R.id.etPassword -> {
                    validatePassword()
                    updatePasswordRequirements(editText.text.toString())
                }
                R.id.etConfirmPassword -> validateConfirmPassword()
            }
        }

        private fun validateUsername() {
            val username = etUsername.text.toString().trim()
            usernameInputLayout.error = when {
                username.isEmpty() -> null
                username.length < 4 -> "Username too short"
                else -> null
            }
        }

        private fun validatePassword() {
            val password = etPassword.text.toString().trim()
            passwordInputLayout.error = when {
                password.isEmpty() -> null
                !isPasswordValid(password) -> "Weak password"
                else -> null
            }
        }

        private fun validateConfirmPassword() {
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            confirmPasswordInputLayout.error = when {
                confirmPassword.isEmpty() -> null
                password != confirmPassword -> "Passwords don't match"
                else -> null
            }
        }
    }

    @OptIn(UnstableApi::class) suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val response = supabase.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<User>()

            response.isNotEmpty()
        } catch (e: Exception) {
            Log.d("USERNAME_CHECK", "Error checking username: ${e.message}")
            false
        }
    }

    @OptIn(UnstableApi::class) suspend fun insertUser(username: String, password: String): Boolean {
        return try {
            val userData = mapOf(
                "username" to username,
                "password" to password
            )

            supabase.postgrest["users"]
                .insert(userData)

            true
        } catch (e: Exception) {
            Log.d("INSERT_USER", "Error inserting user: ${e.message}")
            false
        }
    }
}