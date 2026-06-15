package com.example.coin_budgetra

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import android.widget.FrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Dashboard_Module : AppCompatActivity() {

    private lateinit var dao:          ExpenseDao
    private lateinit var goalDao:      GoalDao
    private lateinit var challengeDao: ChallengeDao

    private var expenses:   List<Expense>   = emptyList()
    private var goals:      List<Goal>      = emptyList()
    private var challenges: List<Challenge> = emptyList()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(s.left, s.top, s.right, s.bottom); insets
        }

        dao          = UserDatabase.getDatabase(this).expenseDao()
        goalDao      = UserDatabase.getDatabase(this).goalDao()
        challengeDao = UserDatabase.getDatabase(this).challengeDao()

        // Welcome text
        val user = UserSession.currentUser
        if (user != null) {
            findViewById<TextView>(R.id.textView13).text =
                "Welcome back ${user.name} ${user.surname}"
        }

        // Nav cards
        findViewById<CardView>(R.id.cardTotalBudget).setOnClickListener {
            startActivity(Intent(this, Expense_Module::class.java))
        }
        findViewById<CardView>(R.id.cardGoals).setOnClickListener {
            startActivity(Intent(this, personal_goals_Module::class.java))
        }
        findViewById<CardView>(R.id.cardChallenges).setOnClickListener {
            startActivity(Intent(this, Challenges_dash::class.java))
        }

        // Achievements card header — tap to open full Achievements page
        findViewById<TextView>(R.id.textView12).apply {
            text = "Achievements  ›"
            isClickable = true
            isFocusable = true
            paintFlags  = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            setTextColor(android.graphics.Color.parseColor("#1565C0"))
            setOnClickListener {
                startActivity(Intent(this@Dashboard_Module, Achievements_Module::class.java))
            }
        }

        // Nav buttons
        findViewById<Button>(R.id.button5).setOnClickListener {
            startActivity(Intent(this, Expense_Module::class.java))
        }
        findViewById<Button>(R.id.button6).setOnClickListener {
            startActivity(Intent(this, Finance_Module::class.java))
        }
        findViewById<Button>(R.id.button7).setOnClickListener {
            startActivity(Intent(this, personal_goals_Module::class.java))
        }
        findViewById<Button>(R.id.button8).setOnClickListener {
            startActivity(Intent(this, Challenges_Module::class.java))
        }

        // Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    UserSession.currentUser = null
                    val intent = Intent(this, Login_module::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        updateDashboardTotals()
        loadChartData()
    }

    // ── Dashboard totals ──────────────────────────────────────────────────────

    private fun updateDashboardTotals() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val totalExpenses   = dao.getTotalSpentForUser(userId)        ?: 0
            val totalGoals      = goalDao.getTotalSavedForUser(userId)     ?: 0
            val totalChallenges = challengeDao.getTotalSavedForUser(userId) ?: 0
            val totalBudget     = dao.getTotalBudgetForUser(userId)        ?: 0
            val net             = totalGoals + totalChallenges - totalExpenses
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.txtFinTotalExpenses).text  = "R$totalExpenses"
                findViewById<TextView>(R.id.txtFinBudget).text         = "R$totalBudget"
                findViewById<TextView>(R.id.txtFinGoalSaved).text      = "R$totalGoals"
                findViewById<TextView>(R.id.txtFinChallengeSaved).text = "R$totalChallenges"
                val netView = findViewById<TextView>(R.id.txtFinNetBalance)
                netView.text = "R$net"
                netView.setTextColor(
                    if (net >= 0) android.graphics.Color.parseColor("#2E7D32")
                    else          android.graphics.Color.parseColor("#B71C1C")
                )
            }
        }
    }

    // ── Achievements (dashboard mini-card) ────────────────────────────────────

    private fun loadAchievements() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val completedGoals      = goalDao.getCompletedGoals(userId)
            val completedExpenses   = dao.getCompletedExpenses(userId)
            val completedChallenges = challengeDao.getCompletedChallenges(userId)
            withContext(Dispatchers.Main) {
                showAchievements(completedGoals, completedExpenses, completedChallenges)
            }
        }
    }

    private fun showAchievements(
        goals:      List<Goal>,
        expenses:   List<Expense>,
        challenges: List<Challenge>
    ) {
        val container = findViewById<LinearLayout>(R.id.achievementContainer)
        container.removeAllViews()

        val total = goals.size + expenses.size + challenges.size

        if (total == 0) {
            container.addView(TextView(this).apply {
                text     = "No achievements yet!"
                textSize = 11f
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, 8, 0, 4)
            })
            return
        }


        // Build flat list: Goals first, then Challenges, then Expenses
        val items = mutableListOf<Triple<String, String, Class<*>>>()
        goals.forEach      { items.add(Triple("🏆 ${it.name}", "Goal completed",      personal_goals_Module::class.java)) }
        challenges.forEach { items.add(Triple("🥇 ${it.name}", "Challenge completed", Challenges_dash::class.java)) }
        expenses.forEach   { items.add(Triple("✅ ${it.name}", "Expense covered",     Expense_Module::class.java)) }

        // Show up to 4 compact clickable rows
        items.take(4).forEach { (title, sub, dest) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 6, 0, 6)
                isClickable = true
                isFocusable = true
                setOnClickListener { startActivity(Intent(this@Dashboard_Module, dest)) }
            }
            row.addView(TextView(this).apply {
                text     = title
                textSize = 11f
                setTextColor(android.graphics.Color.parseColor("#212121"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text     = sub
                textSize = 10f
                setTextColor(android.graphics.Color.parseColor("#777777"))
            })
            container.addView(row)
        }

        // "See all X →" link when more than 4 achievements
        if (total > 4) {
            container.addView(TextView(this).apply {
                text     = "See all $total  →"
                textSize = 11f
                setTextColor(android.graphics.Color.parseColor("#1565C0"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 0)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    startActivity(Intent(this@Dashboard_Module, Achievements_Module::class.java))
                }
            })
        }
    }

    // ── Chart ─────────────────────────────────────────────────────────────────

    private fun loadChartData() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            expenses   = dao.getExpensesForUser(userId)
            goals      = goalDao.getGoalsForUser(userId)
            challenges = challengeDao.getChallengesForUser(userId)
            withContext(Dispatchers.Main) { renderDashboardChart() }
        }
    }

    private fun renderDashboardChart() {
        val container = findViewById<FrameLayout>(R.id.dashboardChartContainer)
        container.removeAllViews()
        val chart = FinanceBarChart(this)
        chart.setBars(listOf(
            FinanceBarChart.Bar("Spent",      expenses.sumOf   { it.amountAdded }.toFloat(),   android.graphics.Color.parseColor("#E53935")),
            FinanceBarChart.Bar("Budget",     expenses.sumOf   { it.spendingLimit }.toFloat(), android.graphics.Color.parseColor("#1565C0")),
            FinanceBarChart.Bar("Goals",      goals.sumOf      { it.savedAmount }.toFloat(),   android.graphics.Color.parseColor("#2E7D32")),
            FinanceBarChart.Bar("Challenges", challenges.sumOf { it.amountSaved }.toFloat(),   android.graphics.Color.parseColor("#F57F17"))
        ))
        container.addView(chart, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 500))
    }
}