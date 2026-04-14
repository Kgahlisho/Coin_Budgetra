package com.example.coin_budgetra

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*


class Add_challenge : AppCompatActivity() {

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar   = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        setContentView(R.layout.activity_add_challenge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val screenTitle   =
            findViewById<TextView>(R.id.txtChallengeFormTitle)
        val inputName     =
            findViewById<EditText>(R.id.editChallengeName)
        val inputDesc     =
            findViewById<EditText>(R.id.editChallengeDescription)
        val spinnerCategory =
            findViewById<Spinner>(R.id.spinnerChallengeCategory)
        val inputBudgetMax      =
            findViewById<EditText>(R.id.editChallengeBudgetMax)
        val inputAmtSaved      =
            findViewById<EditText>(R.id.editChallengeAmountSaved)
        val btnStartDate  =
            findViewById<Button>(R.id.btnPickStartDate)
        val btnEndDate    =
            findViewById<Button>(R.id.btnPickEndDate)
        val btnSave       =
            findViewById<Button>(R.id.btnSaveChallenge)
        val btnBack       =
            findViewById<Button>(R.id.btnChallengeBack)

        val isEdit   =
            intent.getBooleanExtra("isEdit", false)
        val position =
            intent.getIntExtra("position", -1)


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



        btnStartDate.text =
            dateFormat.format(selectedStartDate.time)
        btnEndDate.text   =
            dateFormat.format(selectedEndDate.time)




        btnStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedStartDate.set(year, month, day)
                    btnStartDate.text = dateFormat.format(selectedStartDate.time)
                },
                selectedStartDate.get(Calendar.YEAR),
                selectedStartDate.get(Calendar.MONTH),
                selectedStartDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedEndDate.set(year, month, day)
                    btnEndDate.text = dateFormat.format(selectedEndDate.time)
                },
                selectedEndDate.get(Calendar.YEAR),
                selectedEndDate.get(Calendar.MONTH),
                selectedEndDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnBack.setOnClickListener { finish() }

        if (isEdit) {
            screenTitle.text = "Edit Challenge"
            inputName.setText(intent.getStringExtra("name"))
            inputDesc.setText(intent.getStringExtra("description"))
            selectOrAddCategory(spinnerCategory,intent.getStringExtra("category")?: "")
            inputBudgetMax.setText(intent.getIntExtra("budgetMax", 0).toString())
            inputAmtSaved.setText(intent.getIntExtra("amountSaved", 0).toString())

            val startStr = intent.getStringExtra("startDate") ?: dateFormat.format(selectedStartDate.time)
            val endStr   = intent.getStringExtra("endDate")   ?: dateFormat.format(selectedEndDate.time)
            btnStartDate.text = startStr
            btnEndDate.text   = endStr

            try {
                selectedStartDate.time = dateFormat.parse(startStr) ?: selectedStartDate.time
                selectedEndDate.time   = dateFormat.parse(endStr)   ?: selectedEndDate.time
            } catch (_: Exception) { }
        }

        btnSave.setOnClickListener {
            val name     = inputName.text.toString().trim()
            val desc     = inputDesc.text.toString().trim()
            val category = spinnerCategory.selectedItem?.toString()
                ?.takeIf { it != "+ Add new.." }?: "General"

            val maxStr   = inputBudgetMax.text.toString().trim()
            val savedStr = inputAmtSaved.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "The name of the Challenge is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (maxStr.isEmpty()) {
                Toast.makeText(this, "The Maximum Budget amount is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (savedStr.isEmpty()) {
                Toast.makeText(this, "Your own Added Amount is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budgetMax = try { maxStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Maximum Budget must be a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amountSaved = try { savedStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Amount added must be a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (budgetMax <= 0) {
                Toast.makeText(this, "The Maximum Budget must be greater than zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amountSaved < 0) {
                Toast.makeText(this, "Your added Amount cannot be smaller than 0. ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amountSaved > budgetMax) {
                Toast.makeText(this, "Amount added cannot exceed the budget maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedEndDate.before(selectedStartDate)) {
                Toast.makeText(this, "End date cannot be before the start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = Intent().apply {
                putExtra("challengeName",      name)
                putExtra("challengeDesc",      desc)
                putExtra("challengeCategory",  category)
                putExtra("challengeStartDate", btnStartDate.text.toString())
                putExtra("challengeEndDate",   btnEndDate.text.toString())
                putExtra("challengeBudgetMax", budgetMax)
                putExtra("challengeAmtSaved", amountSaved)
                putExtra("isEdit",           isEdit)
                if (isEdit) putExtra("position", position)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    private fun showAddCategoryDialog(spinner: Spinner) {
        val input = EditText(this).apply { hint = "Enter new category name"; textSize = 14f; setPadding(48, 32, 48, 32) }
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