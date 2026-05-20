package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class Signup_module : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nameInput            = findViewById<EditText>(R.id.editTextText3)
        val surnameInput         = findViewById<EditText>(R.id.editTextText4)
        val phoneInput           = findViewById<EditText>(R.id.editTextText5)
        val emailInput           = findViewById<EditText>(R.id.editTextText6)
        val passwordInput        = findViewById<EditText>(R.id.editTextText7)
        val confirmPasswordInput = findViewById<EditText>(R.id.editTextText8)

        findViewById<TextView>(R.id.txtGoToLogin).setOnClickListener {
            startActivity(Intent(this, Login_module::class.java))
        }

        findViewById<Button>(R.id.button4).setOnClickListener {
            val name            = nameInput.text.toString().trim()
            val surname         = surnameInput.text.toString().trim()
            val phone           = phoneInput.text.toString().trim()
            val email           = emailInput.text.toString().trim()
            val password        = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (name.isEmpty()) { nameInput.error = "Name is required"; nameInput.requestFocus(); return@setOnClickListener }
            if (surname.isEmpty()) { surnameInput.error = "Surname is required"; surnameInput.requestFocus(); return@setOnClickListener }
            if (phone.isEmpty()) { phoneInput.error = "Phone number is required"; phoneInput.requestFocus(); return@setOnClickListener }
            if (phone.length < 10) { phoneInput.error = "Enter a valid cell phone number"; phoneInput.requestFocus(); return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInput.error = "Enter a valid email address"; emailInput.requestFocus(); return@setOnClickListener }
            if (password.length < 8) { passwordInput.error = "Password must be at least 8 characters"; passwordInput.requestFocus(); return@setOnClickListener }
            if (password != confirmPassword) { confirmPasswordInput.error = "Passwords do not match"; confirmPasswordInput.requestFocus(); return@setOnClickListener }

            val db  = UserDatabase.getDatabase(this)
            val dao = db.userDao()

            lifecycleScope.launch(Dispatchers.IO) {
                val existingUser = dao.getUserByEmail(email)
                if (existingUser != null) {
                    runOnUiThread { emailInput.error = "Email is already registered"; emailInput.requestFocus() }
                } else {
                    dao.insertUser(User(name = name, surname = surname, phone = phone, email = email, password = password))
                    val savedUser = dao.getUserByEmail(email)
                    if (savedUser != null) {
                        try { FirebaseRepository.saveUser(savedUser) } catch (e: Exception) { e.printStackTrace() }
                    }
                    runOnUiThread {
                        Toast.makeText(this@Signup_module, "Sign up complete! Please login.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Signup_module, Login_module::class.java))
                        finish()
                    }
                }
            }
        }
    }
}