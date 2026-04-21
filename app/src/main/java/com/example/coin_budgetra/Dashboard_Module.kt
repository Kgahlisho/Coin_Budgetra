package com.example.coin_budgetra

import android.app.AlertDialog
import android.content.Intent
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
            tv.text = "No achievements yet — keep going!"
            tv.textSize = 11f
            tv.setTextColor(android.graphics.Color.GRAY)
            tv.setPadding(0, 8, 0, 8)
            container.addView(tv)
            return
        }

        goals.forEach {
            val tv = TextView(this)
            tv.text = "Goal, ${it.name} reached !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)


            val drawable = getDrawable(R.drawable.check)
         val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check,0,0,0)
           drawable?.setBounds(0,0,40,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)
        
        }
        expenses.forEach {
            val tv = TextView(this)
            tv.text = "Expense , ${it.name} covered !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)

            val drawable = getDrawable(R.drawable.check)
            val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check,0,0,0)
            drawable?.setBounds(0,0,40,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)

        }
        challenges.forEach {
            val tv = TextView(this)
            tv.text = "Challenge , ${it.name} Complete !"
            tv.textSize = 12f
            tv.setPadding(0, 8, 0, 8)

            val drawable = getDrawable(R.drawable.check)
            val size = (16 * resources.displayMetrics.density).toInt()
//         tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check,0,0,0)
            drawable?.setBounds(0,0,40,0)
            tv.setCompoundDrawables(drawable,null,null,null)
            tv.compoundDrawablePadding = 12
            tv.gravity = android.view.Gravity.CENTER_VERTICAL
            container.addView(tv)

        }
    }
}