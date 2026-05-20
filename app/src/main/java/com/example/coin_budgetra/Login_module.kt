package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Login_module : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput    = findViewById<EditText>(R.id.editTextText)
        val passwordInput = findViewById<EditText>(R.id.editTextText2)
        val db            = UserDatabase.getDatabase(this)
        val dao           = db.userDao()

        findViewById<Button>(R.id.button).setOnClickListener {
            val email    = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) { emailInput.error = "Email is required"; emailInput.requestFocus(); return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInput.error = "Enter a valid email"; emailInput.requestFocus(); return@setOnClickListener }
            if (password.isEmpty()) { passwordInput.error = "Password is required"; passwordInput.requestFocus(); return@setOnClickListener }
            if (password.length < 8) { passwordInput.error = "Password must be at least 8 characters"; passwordInput.requestFocus(); return@setOnClickListener }

            CoroutineScope(Dispatchers.IO).launch {
                val user = dao.login(email, password)
                if (user == null) {
                    runOnUiThread { passwordInput.error = "Invalid email or password"; passwordInput.requestFocus() }
                } else {
                    // Pull any cloud data into local Room
                    try {
                        FirebaseRepository.syncFromFirebase(
                            userId       = user.id,
                            expenseDao   = db.expenseDao(),
                            goalDao      = db.goalDao(),
                            challengeDao = db.challengeDao()
                        )
                    } catch (e: Exception) { e.printStackTrace() }

                    runOnUiThread {
                        Toast.makeText(this@Login_module, "Login successful!", Toast.LENGTH_SHORT).show()
                        UserSession.currentUser = user
                        startActivity(Intent(this@Login_module, Dashboard_Module::class.java))
                        finish()
                    }
                }
            }
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            startActivity(Intent(this, Signup_module::class.java)); finish()
        }
    }
}