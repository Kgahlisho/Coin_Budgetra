package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class personal_goals_Module : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private val goalsList = mutableListOf<Goal>()
    private lateinit var dao: GoalDao

    private val addGoalLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK)
            return@registerForActivityResult

        val data = result.data ?: return@registerForActivityResult

        val name = data.getStringExtra("goalName") ?: "Unnamed Goal"
        val amountStr = data.getStringExtra("goalAmount") ?: "0"
        val description = data.getStringExtra("goalDescription") ?: ""
        val category = data.getStringExtra("goalCategory") ?: ""
        val isEdit = data.getBooleanExtra("isEdit", false)
        val goalId = data.getIntExtra("goalId", -1)

        //val position = data.getIntExtra("position", -1)

        val amount = amountStr.toIntOrNull() ?: run {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        val userId = UserSession.currentUser?.id ?: return@registerForActivityResult




        if (isEdit && goalId >= 0) {
            val existing = goalsList.find { it.id == goalId } ?: return@registerForActivityResult
            val updated = existing.copy(
                name = name,
                description = description,
                category = category,
                targetAmount = amount
            )
            lifecycleScope.launch(Dispatchers.IO) {

                dao.updateGoal(updated)
                withContext(Dispatchers.Main) {
                    val idx = goalsList.indexOfFirst { it.id == goalId }
                    if (idx >= 0) goalsList[idx] = updated
                    adapter.refreshList()
                    updateTotalSavings()
                }
            }
        } else {
            val initialSaved = data.getIntExtra("goalInitialSaved", 0)
            val newGoal = Goal(
                userId = userId,
                name = name,
                description = description,
                category = category.ifEmpty { "General" },
                targetAmount = amount,
                savedAmount = initialSaved
            )
            lifecycleScope.launch (Dispatchers.IO){
                dao.insertGoal(newGoal)
                val goals = dao.getGoalsForUser(userId)
                withContext(Dispatchers.Main) {
                    goalsList.clear()
                    goalsList.addAll(goals)
                    adapter.refreshList()
                    updateTotalSavings()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_goals_module)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
dao = UserDatabase.getDatabase(this).goalDao()
        recyclerView = findViewById(R.id.recyclerGoals)


        adapter = GoalAdapter(goalsList, { goal, _->
            val intent = Intent(this, Add_goal::class.java).apply {
                putExtra("isEdit", true)
                putExtra("goalId", goal.id)
                putExtra("name", goal.name)
                putExtra("description", goal.description)
                putExtra("category", goal.category)
                putExtra("target", goal.targetAmount)
                putExtra("saved", goal.savedAmount)
            }
            addGoalLauncher.launch(intent)
        }, {
            updateTotalSavings()
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Filter buttons
        findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            adapter.applyFilter("All"); updateFilterButtons("All")
        }
        findViewById<Button>(R.id.btnFilterActive).setOnClickListener {
            adapter.applyFilter("Active"); updateFilterButtons("Active")
        }
        findViewById<Button>(R.id.btnFilterCompleted).setOnClickListener {
            adapter.applyFilter("Completed"); updateFilterButtons("Completed")
        }
        findViewById<Button>(R.id.button16).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }
        findViewById<Button>(R.id.buttonAddGoal).setOnClickListener {
            addGoalLauncher.launch(Intent(this, Add_goal::class.java))
        }

        loadGoals()
    }

    override fun onResume(){
        super.onResume()
        loadGoals()
    }

    private fun loadGoals(){
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch (Dispatchers.IO){
            val goals = dao.getGoalsForUser(userId)
            withContext(Dispatchers.Main){
                goalsList.clear()
                goalsList.addAll(goals)
                adapter.refreshList()
                updateTotalSavings()
            }
        }
    }
        private fun updateFilterButtons(active: String) {
            val btnAll = findViewById<Button>(R.id.btnFilterAll)
            val btnActive = findViewById<Button>(R.id.btnFilterActive)
            val btnCompleted = findViewById<Button>(R.id.btnFilterCompleted)
            listOf(btnAll, btnActive, btnCompleted).forEach { btn ->
                btn.alpha = 0.5f
            }
            when (active) {
                "All" -> btnAll.alpha = 1.0f
                "Active" -> btnActive.alpha = 1.0f
                "Completed" -> btnCompleted.alpha = 1.0f
            }
        }

    private fun updateTotalSavings(){
        val total = goalsList.sumOf{
            it.savedAmount
        }

        val txtTotal = findViewById<TextView>(R.id.txtTotalSavings)
        txtTotal.text = "Total Saved: R %,d".format(total)
    }
    }

/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)




        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            val name = data.getStringExtra("goalName") ?:"Unnamed Goal"
            val amountStr = data.getStringExtra("goalAmount") ?: "0"

            val amount = try{
                amountStr.toInt()
            }catch (e: Exception){
                Toast.makeText(this,"Invalid amount" , Toast.LENGTH_SHORT).show()
                return
            }

            val newGoal = Goal(
                name = name,
                description = "No description",
                category = "General",
                targetAmount = amountStr.toInt()
            )
            goalsList.add(newGoal)
            adapter.notifyItemInserted(goalsList.size - 1)
        }
    }
}
*/