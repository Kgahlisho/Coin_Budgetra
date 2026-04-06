package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class personal_goals_Module : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private val goalsList = mutableListOf<Goal>()

    private val addGoalLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult

        val name = data.getStringExtra("goalName") ?: "Unnamed Goal"
        val amountStr = data.getStringExtra("goalAmount") ?: "0"
        val description = data.getStringExtra("goalDescription") ?: ""
        val category = data.getStringExtra("goalCategory") ?: ""
        val isEdit = data.getBooleanExtra("isEdit", false)
        val position = data.getIntExtra("position", -1)

        val amount = try {
            amountStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }



        if (isEdit && position >= 0 && position < goalsList.size) {
            goalsList[position].apply {
                this.name = name
                this.description = description
                this.category = category
                this.targetAmount = amount
            }
        } else {
            val initialSaved = data.getIntExtra("goalInitialSaved", 0)
            goalsList.add(
                Goal(
                    name = name,
                    description = description,
                    category = if (category.isNotEmpty()) category else "General",
                    targetAmount = amount,
                    savedAmount = initialSaved
                )
            )
        }
        adapter.refreshList()
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

        recyclerView = findViewById(R.id.recyclerGoals)

        adapter = GoalAdapter(goalsList) { goal, position ->
            val intent = Intent(this, Add_goal::class.java).apply {
                putExtra("isEdit", true)
                putExtra("position", position)
                putExtra("name", goal.name)
                putExtra("description", goal.description)
                putExtra("category", goal.category)
                putExtra("target", goal.targetAmount)
                putExtra("saved", goal.savedAmount)
            }
            addGoalLauncher.launch(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Filter buttons
        findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            adapter.applyFilter("All")
            updateFilterButtons("All")
        }
        findViewById<Button>(R.id.btnFilterActive).setOnClickListener {
            adapter.applyFilter("Active")
            updateFilterButtons("Active")
        }
        findViewById<Button>(R.id.btnFilterCompleted).setOnClickListener {
            adapter.applyFilter("Completed")
            updateFilterButtons("Completed")
        }
            findViewById<Button>(R.id.button16).setOnClickListener {
                startActivity(Intent(this, Dashboard_Module::class.java))
            }

            // Add new goal
            findViewById<Button>(R.id.buttonAddGoal).setOnClickListener {
                addGoalLauncher.launch(Intent(this, Add_goal::class.java))
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