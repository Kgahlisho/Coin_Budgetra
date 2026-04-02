package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Expense_Module : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCreateExpense = this.findViewById<Button>(R.id.button9)

        btnCreateExpense.setOnClickListener {
            val intent = Intent(this, Create_Expense::class.java)
            startActivity(intent);
        }

        val btnBack = this.findViewById<Button>(R.id.button15)
        btnBack.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);
        }


    }
}