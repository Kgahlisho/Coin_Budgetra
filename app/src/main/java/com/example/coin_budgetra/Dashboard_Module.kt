package com.example.coin_budgetra

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Dashboard_Module : AppCompatActivity() {

    private lateinit var dao: ExpenseDao
    private lateinit var goalDao: GoalDao

    private lateinit var challengeDao: ChallengeDao
    private fun updateDashboardTotals() {
        val userId = UserSession.currentUser?.id?: return

      //  val totalGoals = GoalRepository.goals.sumOf { it.savedAmount }
        lifecycleScope.launch ( Dispatchers.IO){

            val totalExpenses = dao.getTotalSpentForUser(userId) ?: 0
            val totalGoals = goalDao.getTotalSavedForUser(userId)?: 0
            val totalChallenges = challengeDao.getTotalSavedForUser(userId)?: 0
            val net = totalGoals + totalChallenges - totalExpenses

            withContext(Dispatchers.Main){

                findViewById<TextView>(R.id.txtDashboardGoals).text = "Goals saved : R$totalGoals"

                findViewById<TextView>(R.id.txtDashboardChallenges).text = "Challenges Saved : R$totalChallenges"
                findViewById<TextView>(R.id.txtDashboardExpenses).text = "Expenses: R$totalExpenses"

                findViewById<TextView>(R.id.txtNetBalance).text = "Net Balance : R%d".format(net)
            }
        }
     }

    override fun onResume() {
        super.onResume()
        updateDashboardTotals()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //we use the logged-in users name to display in the dashbooard cardview

        goalDao = UserDatabase.getDatabase(this).goalDao()
        dao= UserDatabase.getDatabase(this).expenseDao()
        challengeDao = UserDatabase.getDatabase(this).challengeDao()

        val user = UserSession.currentUser
        if (user != null){
            findViewById<TextView>(R.id.textView13).text = "Welcome back ${user.name} ${user.surname}"
        }

        updateDashboardTotals()

        val btnExpense = this.findViewById<Button>(R.id.button5)
        btnExpense.setOnClickListener {
            val intent = Intent(this, Expense_Module::class.java)
            startActivity(intent)
        }

        val btnFinanace = this.findViewById<Button>(R.id.button6)
        btnFinanace.setOnClickListener {
            val intent = Intent(this, Finance_Module::class.java)
            startActivity(intent)
        }


        val btnPersonalGoals = this.findViewById<Button>(R.id.button7)
        btnPersonalGoals.setOnClickListener {
            val intent = Intent(this, personal_goals_Module::class.java)
            startActivity(intent)
        }

        val btnChallenge = this.findViewById<Button>(R.id.button8)
        btnChallenge.setOnClickListener {
            val intent = Intent(this, Challenges_Module::class.java)
            startActivity(intent)
        }




        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out ? ")
                .setPositiveButton("Yes") { _, _ ->
                    UserSession.currentUser = null
                    val intent = Intent(this, Login_module::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}