package com.example.coin_budgetra

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Add_goal : AppCompatActivity() {

    private val defaultCategories = mutableListOf(
        "General",
        "Home",
        "Personal",
        "Maintanance",
        "Food & Grocery",
        "Entertainment",
        "Travel",
        "Education",
        "Health",
        "+ Add new"
    )
    private lateinit var spinnerAdapter : ArrayAdapter<String>


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
        val spinnerCategory    = findViewById<Spinner>(R.id.spinnerGoalCategory)
        val inputInitial     = findViewById<EditText>(R.id.editTextInitialAmount)
        val saveBtn          = findViewById<Button>(R.id.button19)

        val isEdit   = intent.getBooleanExtra("isEdit", false)
       // val position = intent.getIntExtra("position", -1)

        val isCompleted: Boolean = false

        spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,defaultCategories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        spinnerCategory.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, View: android.view.View?, pos: Int, id: Long){
                if (defaultCategories[pos] == "+ Add new..")
                    showAddCategoryDialog(spinnerCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}


        }

        if (isEdit) {
            screenTitle.text = "Edit Goal"
            inputName.setText(intent.getStringExtra("name"))
            inputDescription.setText(intent.getStringExtra("description"))
            selectOrAddCategory(spinnerCategory, intent.getStringExtra("category") ?: "")
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
            val category    = spinnerCategory.selectedItem?.toString()
                ?.takeIf{ it != "+ Add new"} ?: "General"
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

            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("goalName",         name)
                putExtra("goalAmount",       targetStr)
                putExtra("goalDescription",  description)
                putExtra("goalCategory",     category)
                putExtra("goalInitialSaved", initialAmount)
                putExtra("isEdit",           isEdit)
                if (isEdit) putExtra("goalId", intent.getIntExtra("goalId", -1))

            })

            finish()
        }
    }

    private fun showAddCategoryDialog(spinner: Spinner) {
        val input = EditText(this).apply{
            hint = "Enter new category name"
            textSize = 14f
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)

            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->

                val newCat = input.text.toString().trim()
                if (newCat.isNotEmpty()) {
                    val pos = defaultCategories.size - 1   // insert before "➕ Add new…"
                    defaultCategories.add(pos, newCat)
                    spinnerAdapter.notifyDataSetChanged()
                    spinner.setSelection(pos)

                } else {
                    spinner.setSelection(0)
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> spinner.setSelection(0) }
            .show()
    }


    private fun selectOrAddCategory(spinner: Spinner, category: String) {

        if (category.isEmpty()) return

        val idx = defaultCategories.indexOf(category)
        if (idx >= 0) { spinner.setSelection(idx) }
        else {

            val pos = defaultCategories.size - 1
            defaultCategories.add(pos, category)
            spinnerAdapter.notifyDataSetChanged()
            spinner.setSelection(pos)
        }
    }
}