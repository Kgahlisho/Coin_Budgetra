package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class Expense_Module : AppCompatActivity() {

    private lateinit var adapter: ExpenseAdapter
    private lateinit var recyclerExpenses: RecyclerView
    private val expenseList = mutableListOf<Expense>()
    private lateinit var dao: ExpenseDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = UserDatabase.getDatabase(this).expenseDao()

        recyclerExpenses = findViewById(R.id.recyclerExpenses)


        adapter = ExpenseAdapter(expenseList, { expense, _ ->
            val intent = Intent(this, Create_Expense::class.java)
            intent.putExtra("expenseId", expense.id)
            startActivity(intent)
        }, {
            updateTotalExpenses()
        })

        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        recyclerExpenses.adapter = adapter


        findViewById<Button>(R.id.button9).setOnClickListener {
            startActivity(Intent(this, Create_Expense::class.java))
        }

        findViewById<Button>(R.id.button15).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }
        loadExpenses()
    }


    override fun onResume() {
        super.onResume()
        adapter.refreshList()
        loadExpenses()
    }

    private fun loadExpenses() {

    val userId = UserSession.currentUser?.id ?:return

        lifecycleScope.launch(Dispatchers.IO){
        val expenses = dao.getExpensesForUser(userId)
        withContext(Dispatchers.Main){
            expenseList.clear()
            expenseList.addAll(expenses)
            adapter.refreshList()
            updateTotalExpenses()
        }

    }


    }

    private fun updateTotalExpenses() {
        val total = expenseList.sumOf {
            it.amountAdded
        }
        val budget = expenseList.sumOf {
            it.spendingLimit
        }
        findViewById<TextView>(R.id.txtTotalExpenses).text =
            "Spent: R$total  |  Budget: R$budget "
    }

}