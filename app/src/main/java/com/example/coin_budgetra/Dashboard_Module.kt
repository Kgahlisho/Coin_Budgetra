package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Dashboard_Module : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnExpense = this.findViewById<Button>(R.id.button5)
        btnExpense.setOnClickListener {
            val intent = Intent(this, Expense_Module::class.java)
            startActivity(intent);
        }

        val btnFinanace = this.findViewById<Button>(R.id.button6)
        btnFinanace.setOnClickListener {
            val intent = Intent(this, Finance_Module::class.java)
            startActivity(intent);
        }


        val btnOther = this.findViewById<Button>(R.id.button7)
        btnOther.setOnClickListener {
            val intent = Intent(this, Expense_Module::class.java)
            startActivity(intent);
        }

        val btnChallenge = this.findViewById<Button>(R.id.button8)
        btnChallenge.setOnClickListener {
            val intent = Intent(this, Challenges_Module::class.java)
            startActivity(intent);
        }
    }
}