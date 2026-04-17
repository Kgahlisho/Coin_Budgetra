package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Dashboard_Module : AppCompatActivity() {

    private fun  updateDashboardTotals()
    {
        val totalExpenses = ExpenseRepository.expenses.sumOf{ it.amountAdded }
        val totalGoals = GoalRepository.goals.sumOf { it.savedAmount }
        val totalChallenges = ChallengeRepository.challenges.sumOf { it.amountSaved }

        val net = totalGoals + totalChallenges - totalExpenses


    findViewById<TextView>(R.id.txtDashboardExpenses).text = "Expenses: R$totalExpenses"

        findViewById<TextView>(R.id.txtDashboardGoals).text = "Goals saved : R$totalGoals"

        findViewById<TextView>(R.id.txtDashboardChallenges).text = "Challenges Saved : R$totalChallenges"

        findViewById<TextView>(R.id.txtNetBalance).text = "Net Balance : R%d".format(net)
    }

    override fun onResume(){
        super.onResume()
        updateDashboardTotals()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_module)
        updateDashboardTotals()
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


        val btnPersonalGoals = this.findViewById<Button>(R.id.button7)
        btnPersonalGoals.setOnClickListener {
            val intent = Intent(this, personal_goals_Module::class.java)
            startActivity(intent);
        }

        val btnChallenge = this.findViewById<Button>(R.id.button8)
        btnChallenge.setOnClickListener {
            val intent = Intent(this, Challenges_Module::class.java)
            startActivity(intent);
        }
    }
}