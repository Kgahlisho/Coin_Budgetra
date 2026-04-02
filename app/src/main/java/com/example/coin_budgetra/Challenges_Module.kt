package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Challenges_Module : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_challenges_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCreateGoal = this.findViewById<Button>(R.id.button13)
        btnCreateGoal.setOnClickListener {
            val intent = Intent(this, Challenges_Module::class.java)
            startActivity(intent);
        }

        val btnCancel = this.findViewById<Button>(R.id.button14)
        btnCancel.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);
        }
    }
}