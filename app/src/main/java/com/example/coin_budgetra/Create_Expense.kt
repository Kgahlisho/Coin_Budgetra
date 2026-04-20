package com.example.coin_budgetra


import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*



class Create_Expense : AppCompatActivity() {

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate:   Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())



 //private val attachedUris: MutableList<String> = mutableListOf()

    private var editExpenseId: Int = -1
    private lateinit var dao : ExpenseDao

/*
    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            uris.forEach { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { }
                val uriStr = uri.toString()
                if (!attachedUris.contains(uriStr)) attachedUris.add(uriStr)
            }
            updateDocumentCountLabel()
        }
*/


    private lateinit var screenTitle:        TextView
    private lateinit var inputName:          EditText
    private lateinit var inputDesc:          EditText
    private lateinit var inputCategory:      Spinner
    private lateinit var btnStartDate:       Button
    private lateinit var btnEndDate:         Button
    private lateinit var inputSpendingLimit: EditText
    private lateinit var inputAddMoney:      EditText
    private lateinit var btnAttachDoc:       Button
    private lateinit var txtDocCount:        TextView
    private lateinit var btnSave:            Button
    private lateinit var btnCancel:          Button





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = UserDatabase.getDatabase(this).expenseDao()


        bindViews()
        setupCategorySpinner()
        setupDatePickers()
        setupButtons()
        handleEditIntent()
    }



    private fun bindViews() {
        screenTitle        = findViewById(R.id.txtExpenseFormTitle)
        inputName          = findViewById(R.id.editExpenseName)
        inputDesc          = findViewById(R.id.editExpenseDescription)
        inputCategory      = findViewById(R.id.spinnerExpenseCategory)
        btnStartDate       = findViewById(R.id.btnExpenseStartDate)
        btnEndDate         = findViewById(R.id.btnExpenseEndDate)
        inputSpendingLimit = findViewById(R.id.editExpenseSpendingLimit)
        inputAddMoney      = findViewById(R.id.editExpenseAddMoney)
        btnSave            = findViewById(R.id.btnSaveExpense)
        btnCancel          = findViewById(R.id.btnCancelExpense)
    }


    private fun setupCategorySpinner() {
        val categories = listOf(
            "General", "Home", "Personal", "Car Maintenance",
            "Food & Groceries", "Entertainment", "Travel", "Education", "Health"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputCategory.adapter = adapter
    }

    private fun setupDatePickers() {
        btnStartDate.text = dateFormat.format(selectedStartDate.time)
        btnEndDate.text   = dateFormat.format(selectedEndDate.time)

        btnStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                selectedStartDate.set(year, month, day)
                btnStartDate.text = dateFormat.format(selectedStartDate.time)
            }, selectedStartDate.get(Calendar.YEAR),
                selectedStartDate.get(Calendar.MONTH),
                selectedStartDate.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                selectedEndDate.set(year, month, day)
                btnEndDate.text = dateFormat.format(selectedEndDate.time)
            }, selectedEndDate.get(Calendar.YEAR),
                selectedEndDate.get(Calendar.MONTH),
                selectedEndDate.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupButtons() {
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveExpense() }
    }

    private fun handleEditIntent() {
        editExpenseId = intent.getIntExtra("expenseId", -1)
        if (editExpenseId < 0) return

        lifecycleScope.launch(Dispatchers.IO) {
            val expense = dao.getExpenseById(editExpenseId) ?: return@launch
            withContext(Dispatchers.Main) {
                screenTitle.text = "Edit Expense"
                btnSave.text     = "Update Expense"
                inputName.setText(expense.name)
                inputDesc.setText(expense.description)
                inputSpendingLimit.setText(expense.spendingLimit.toString())
                inputAddMoney.setText(expense.amountAdded.toString())

                val spinnerAdapter = inputCategory.adapter as ArrayAdapter<*>
                for (i in 0 until spinnerAdapter.count) {
                    if (spinnerAdapter.getItem(i).toString() == expense.category) {
                        inputCategory.setSelection(i)
                        break
                    }
                }

                try {
                    selectedStartDate.time = dateFormat.parse(expense.startDate) ?: selectedStartDate.time
                    selectedEndDate.time   = dateFormat.parse(expense.endDate)   ?: selectedEndDate.time
                } catch (_: Exception) { }
                btnStartDate.text = expense.startDate
                btnEndDate.text   = expense.endDate
            }
        }
    }


    private fun saveExpense() {
        val name = inputName.text.toString().trim()
        val desc = inputDesc.text.toString().trim()
        val category = inputCategory.selectedItem?.toString() ?: "General"
        val limitStr = inputSpendingLimit.text.toString().trim()
        val addStr = inputAddMoney.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Expense name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (limitStr.isEmpty()) {
            Toast.makeText(this, "Spending limit is required", Toast.LENGTH_SHORT).show()
            return
        }
        val limit = limitStr.toIntOrNull()
        if (limit == null || limit <= 0) {
            Toast.makeText(
                this,
                "Spending limit must be a valid positive number",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val added = if (addStr.isEmpty()) 0 else addStr.toIntOrNull() ?: run {
            Toast.makeText(this, "Amount added must be a valid number", Toast.LENGTH_SHORT).show()
            return
        }
        if (added < 0) {
            Toast.makeText(this, "Amount added cannot be negative", Toast.LENGTH_SHORT).show()
            return
        }
        if (added > limit) {
            Toast.makeText(
                this,
                "Amount added cannot exceed the spending limit",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (selectedEndDate.before(selectedStartDate)) {
            Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = UserSession.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = if (editExpenseId >= 0) editExpenseId else 0,
            userId = currentUser.id,
            name = name,
            description = desc,
            category = category,
            startDate = btnStartDate.text.toString(),
            endDate = btnEndDate.text.toString(),
            spendingLimit = limit,
            amountAdded = added,
            //documentUris  = attachedUris.toMutableList()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            if (editExpenseId >= 0) {
                dao.updateExpense(expense)
            } else {
                dao.insertExpense(expense)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@Create_Expense,
                    if (editExpenseId >= 0) "Expense updated!" else "Expense saved!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}

/*
    private fun updateDocumentCountLabel() {
        txtDocCount.text = if (attachedUris.isEmpty()) "No documents attached"
        else "${attachedUris.size} document${if (attachedUris.size == 1) "" else "s"} attached"
    }
}*/