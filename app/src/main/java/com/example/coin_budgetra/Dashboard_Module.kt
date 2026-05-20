package com.example.coin_budgetra

import android.app.AlertDialog
//import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.FrameLayout
import android.content.Intent
import androidx.cardview.widget.CardView

class Dashboard_Module : AppCompatActivity() {

    private lateinit var dao: ExpenseDao
    private lateinit var goalDao: GoalDao
    private lateinit var challengeDao: ChallengeDao


    private var expenses : List<Expense> = emptyList()
    private var goals : List<Goal> = emptyList()
    private var challenges : List<Challenge> = emptyList()

    private fun updateDashboardTotals() {

        val userId = UserSession.currentUser?.id ?: return

        lifecycleScope.launch(Dispatchers.IO) {

            val totalExpenses =
                dao.getTotalSpentForUser(userId) ?: 0

            val totalGoals =
                goalDao.getTotalSavedForUser(userId) ?: 0

            val totalChallenges =
                challengeDao.getTotalSavedForUser(userId) ?: 0

            val totalBudget =
                dao.getTotalBudgetForUser(userId) ?: 0

            val net =
                totalGoals + totalChallenges - totalExpenses

            withContext(Dispatchers.Main) {

                // NEW CARD VALUES
                findViewById<TextView>(R.id.txtFinTotalExpenses).text =
                    "R$totalExpenses"

                findViewById<TextView>(R.id.txtFinBudget).text =
                    "R$totalBudget"

                findViewById<TextView>(R.id.txtFinGoalSaved).text =
                    "R$totalGoals"

                findViewById<TextView>(R.id.txtFinChallengeSaved).text =
                    "R$totalChallenges"

                val netView =
                    findViewById<TextView>(R.id.txtFinNetBalance)

                netView.text = "R$net"

                if (net >= 0) {
                    netView.setTextColor(
                        android.graphics.Color.parseColor("#2E7D32")
                    )
                } else {
                    netView.setTextColor(
                        android.graphics.Color.parseColor("#B71C1C")
                    )
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        updateDashboardTotals()
        loadAchievements()
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

        val totalBudgetCard = findViewById<CardView>(R.id.cardTotalBudget)
        val goalsCard = findViewById<CardView>(R.id.cardGoals)
        val challengesCard = findViewById<CardView>(R.id.cardChallenges)

        totalBudgetCard.setOnClickListener {
            startActivity(Intent(this, Expense_Module::class.java))
        }

        goalsCard.setOnClickListener {
            startActivity(Intent(this, personal_goals_Module::class.java))
        }

        challengesCard.setOnClickListener {
            startActivity(Intent(this, Challenges_dash::class.java))
        }

        goalDao = UserDatabase.getDatabase(this).goalDao()
        dao= UserDatabase.getDatabase(this).expenseDao()
        challengeDao = UserDatabase.getDatabase(this).challengeDao()

        loadChartData()



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

        lifecycleScope.launch(Dispatchers.IO) {

           // val email = UserSession.getUserEmail(this@Dashboard_Module)
           // val user = db.userDao().getUserByEmail(email!!)
            val userId = UserSession.currentUser?.id ?:
                return@launch

            val completedGoals = goalDao.getCompletedGoals(userId)
            val completedExpenses = dao.getCompletedExpenses(userId)
            val completedChallenges = challengeDao.getCompletedChallenges(userId)

            withContext(Dispatchers.Main){
                showAchievements(completedGoals,completedExpenses,completedChallenges)
            }
        }

    }

    private fun loadAchievements() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = UserSession.currentUser?.id ?: return@launch
            val completedGoals      = goalDao.getCompletedGoals(userId)
            val completedExpenses   = dao.getCompletedExpenses(userId)
            val completedChallenges = challengeDao.getCompletedChallenges(userId)
            withContext(Dispatchers.Main) {
                showAchievements(completedGoals, completedExpenses, completedChallenges)
            }
        }
    }

    private fun showAchievements(goals: List<Goal>, expenses: List<Expense>, challenges: List<Challenge>) {
        val container = findViewById<android.widget.LinearLayout>(R.id.achievementContainer)
        container.removeAllViews()

        if (goals.isEmpty() && expenses.isEmpty() && challenges.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No achievements yet !"
            tv.textSize = 11f
            tv.setTextColor(android.graphics.Color.GRAY)
            tv.setPadding(0, 15, 0, 8)
            container.addView(tv)
            return
        }

        goals.forEach {
            val tv = TextView(this)
            tv.text = " ${it.name} : Goal has been completed !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)


            val drawable = getDrawable(R.drawable.check)
            val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check.size,0,0,0)
           drawable?.setBounds(0,0,20,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)
        
        }
        expenses.forEach {
            val tv = TextView(this)
            tv.text = " Your , ${it.name} : expense has been covered !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)

            val drawable = getDrawable(R.drawable.check)
            val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check,0,0,0)
            drawable?.setBounds(0,0,20,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)

        }
        challenges.forEach {
            val tv = TextView(this)
            tv.text = "  Challenge ${it.name} has been Complete !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)

            val drawable = getDrawable(R.drawable.check)
            val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check,0,0,0)
            drawable?.setBounds(0,0,20,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)

        }
    }


    private fun loadChartData() {

        val userId = UserSession.currentUser?.id ?: return

        lifecycleScope.launch(Dispatchers.IO) {

            expenses = dao.getExpensesForUser(userId)
            goals = goalDao.getGoalsForUser(userId)
            challenges = challengeDao.getChallengesForUser(userId)

            withContext(Dispatchers.Main) {
                renderDashboardChart()
            }
        }
    }

    private fun renderDashboardChart() {

        val container =
            findViewById<FrameLayout>(R.id.dashboardChartContainer)

        container.removeAllViews()

        val totalExpenses =
            expenses.sumOf { it.amountAdded }.toFloat()

        val totalBudget =
            expenses.sumOf { it.spendingLimit }.toFloat()

        val totalGoals =
            goals.sumOf { it.savedAmount }.toFloat()

        val totalChallenges =
            challenges.sumOf { it.amountSaved }.toFloat()

        val chart = FinanceBarChart(this)

        chart.setBars(
            listOf(
                FinanceBarChart.Bar(
                    "Spent",
                    totalExpenses,
                    android.graphics.Color.parseColor("#E53935")
                ),

                FinanceBarChart.Bar(
                    "Budget",
                    totalBudget,
                    android.graphics.Color.parseColor("#1565C0")
                ),

                FinanceBarChart.Bar(
                    "Goals",
                    totalGoals,
                    android.graphics.Color.parseColor("#2E7D32")
                ),

                FinanceBarChart.Bar(
                    "Challenges",
                    totalChallenges,
                    android.graphics.Color.parseColor("#F57F17")
                )
            )
        )

        container.addView(
            chart,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                500
            )
        )
    }
}