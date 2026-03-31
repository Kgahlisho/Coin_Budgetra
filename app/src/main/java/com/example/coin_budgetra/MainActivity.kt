package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val  signupBtn = this.findViewById<Button>(R.id.signup_btn);
        val loginBtn = this.findViewById<Button>(R.id.button2);

        signupBtn.setOnClickListener{
            val intent = Intent(this, Signup_module::class.java)
            startActivity(intent);
        }

        loginBtn.setOnClickListener{
            val intent = Intent(this, Login_module::class.java)
            startActivity(intent);
        }
    }
}