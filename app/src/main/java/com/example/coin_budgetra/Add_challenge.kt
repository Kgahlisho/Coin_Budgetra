package com.example.coin_budgetra

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
        val inputCategory =
            findViewById<EditText>(R.id.editChallengeCategory)
        val inputMin      =
            findViewById<EditText>(R.id.editChallengeMinAmount)
        val inputMax      =
            findViewById<EditText>(R.id.editChallengeMaxAmount)
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

        btnStartDate.text =
            dateFormat.format(selectedStartDate.time)
        btnEndDate.text   =
            dateFormat.format(selectedEndDate.time)

        if (isEdit) {
            screenTitle.text = "Edit Challenge"
            inputName.setText(intent.getStringExtra("name"))
            inputDesc.setText(intent.getStringExtra("description"))
            inputCategory.setText(intent.getStringExtra("category"))
            inputMin.setText(intent.getIntExtra("minAmount", 0).toString())
            inputMax.setText(intent.getIntExtra("maxAmount", 0).toString())

            val startStr = intent.getStringExtra("startDate") ?: dateFormat.format(selectedStartDate.time)
            val endStr   = intent.getStringExtra("endDate")   ?: dateFormat.format(selectedEndDate.time)
            btnStartDate.text = startStr
            btnEndDate.text   = endStr

            try {
                selectedStartDate.time = dateFormat.parse(startStr) ?: selectedStartDate.time
                selectedEndDate.time   = dateFormat.parse(endStr)   ?: selectedEndDate.time
            } catch (_: Exception) { }
        }

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

        btnSave.setOnClickListener {
            val name     = inputName.text.toString().trim()
            val desc     = inputDesc.text.toString().trim()
            val category = inputCategory.text.toString().trim()
            val minStr   = inputMin.text.toString().trim()
            val maxStr   = inputMax.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "The name of the Challenge is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Both minimum and maximum amounts are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minAmount = try { minStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Minimum amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val maxAmount = try { maxStr.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(this, "Maximum amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minAmount < 0 || maxAmount < 0) {
                Toast.makeText(this, "Amounts cannot be negative", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minAmount > maxAmount) {
                Toast.makeText(this, "Minimum amount cannot exceed the maximum amount", Toast.LENGTH_SHORT).show()
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
                putExtra("challengeMinAmount", minAmount)
                putExtra("challengeMaxAmount", maxAmount)
                putExtra("isEdit",           isEdit)
                if (isEdit) putExtra("position", position)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}