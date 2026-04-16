package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Expense_Module : AppCompatActivity() {

    private lateinit var adapter: ExpenseAdapter
    private lateinit var recyclerExpenses: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerExpenses = findViewById(R.id.recyclerExpenses)

        adapter = ExpenseAdapter(ExpenseRepository.expenses, {expense , position ->
            val intent = Intent(this, Create_Expense::class.java)
            intent.putExtra("position", position)
            startActivity(intent)
        },{
            updateTotalExpenses()
        })

        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        recyclerExpenses.adapter = adapter


        val btnCreateExpense = findViewById<Button>(R.id.button9)
        btnCreateExpense.setOnClickListener {
            startActivity(Intent(this, Create_Expense::class.java))
        }

        val btnBack = findViewById<Button>(R.id.button15)
        btnBack.setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }
        updateTotalExpenses()
    }

    override fun onResume() {
        super.onResume()
        adapter.refreshList()
        updateTotalExpenses()
    }
    
    private fun updateTotalExpenses() {
        val total = ExpenseRepository.expenses.sumOf {
            it.amountAdded
        }
        val txtTotal = findViewById<TextView>(R.id.txtTotalExpenses)
        txtTotal.text = "Spent: R$total  |  Budget: R${ExpenseRepository.expenses.sumOf { it.spendingLimit }}"}
}