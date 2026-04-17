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

        val emailInput = findViewById<EditText>(R.id.editTextText)
        val passwordInput = findViewById<EditText>(R.id.editTextText2)


        val CompleteLogin = this.findViewById<Button>(R.id.button);

        CompleteLogin.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()



            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                passwordInput.error = "Password must be at least 8 characters"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            val user = UserRepository.users.find {
                it.email == email && it.password == password
            }

            if (user == null) {
                passwordInput.error = "Invalid email or password"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            UserRepository.currentUser = user


            Toast.makeText(this, "Login successful! ", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, Dashboard_Module::class.java))
            finish()
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            startActivity(Intent(this, Signup_module::class.java))
            finish()
        }


    }
}