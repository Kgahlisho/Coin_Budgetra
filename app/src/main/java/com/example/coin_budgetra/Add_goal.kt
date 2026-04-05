package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Add_goal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_goal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.button33).setOnClickListener {
            finish()
        }

        val goalName = findViewById<EditText>(R.id.editTextText17)
        val goalAmount = findViewById<EditText>(R.id.editTextText18)
        val saveBtn = findViewById<Button>(R.id.button19)


        saveBtn.setOnClickListener {
            val name = goalName.text.toString().trim()
            val amount = goalAmount.text.toString().trim()

            if (name.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            //sends back data
            val result = Intent().apply {
                putExtra("goalName", name)
                putExtra("goalAmount", amount)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}

