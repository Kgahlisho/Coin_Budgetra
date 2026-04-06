package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

        val screenTitle    = findViewById<TextView>(R.id.textView32)
        val inputName        = findViewById<EditText>(R.id.editTextText17)
        val inputTarget      = findViewById<EditText>(R.id.editTextText18)
        val inputDescription = findViewById<EditText>(R.id.editTextDescription)
        val inputCategory    = findViewById<EditText>(R.id.editTextCategory)
        val inputInitial     = findViewById<EditText>(R.id.editTextInitialAmount)
        val saveBtn          = findViewById<Button>(R.id.button19)

        val isEdit   = intent.getBooleanExtra("isEdit", false)
        val position = intent.getIntExtra("position", -1)

        if (isEdit) {
            screenTitle.text = "Edit Goal"
            inputName.setText(intent.getStringExtra("name"))
            inputDescription.setText(intent.getStringExtra("description"))
            inputCategory.setText(intent.getStringExtra("category"))
            inputTarget.setText(intent.getIntExtra("target", 0).toString())
            // Hide initial deposit field when editing (user uses "Add Money" on the card instead)
            inputInitial.visibility = android.view.View.GONE
            findViewById<TextView>(R.id.labelInitialAmount).visibility = android.view.View.GONE
        }

        findViewById<Button>(R.id.button33).setOnClickListener { finish() }

        saveBtn.setOnClickListener {
            val name        = inputName.text.toString().trim()
            val targetStr   = inputTarget.text.toString().trim()
            val description = inputDescription.text.toString().trim()
            val category    = inputCategory.text.toString().trim()
            val initialStr  = if (isEdit) "0" else inputInitial.text.toString().trim().ifEmpty { "0" }

            if (name.isEmpty() || targetStr.isEmpty()) {
                Toast.makeText(this, "Goal name and target amount are required", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val targetAmount = try { targetStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Target amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val initialAmount = try { initialStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Initial amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (initialAmount > targetAmount) {
                Toast.makeText(this, "Initial amount cannot exceed the target", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = Intent().apply {
                putExtra("goalName",         name)
                putExtra("goalAmount",       targetStr)
                putExtra("goalDescription",  description)
                putExtra("goalCategory",     category)
                putExtra("goalInitialSaved", initialAmount)
                putExtra("isEdit",           isEdit)
                if (isEdit) putExtra("position", position)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}