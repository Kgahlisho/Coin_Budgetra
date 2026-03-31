package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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


        val CompleteLogin = this.findViewById<Button>(R.id.button);

        CompleteLogin.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);
        }

        val GoSignup = this.findViewById<Button>(R.id.button3);

        GoSignup.setOnClickListener {
            val intent = Intent(this, Signup_module::class.java)
            startActivity(intent);
        }
    }
}