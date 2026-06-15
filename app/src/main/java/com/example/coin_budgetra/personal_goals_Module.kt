package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
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
    private val goalsList = mutableListOf<Goal>()      // master list
    private val displayList = mutableListOf<Goal>()    // what adapter shows
    private lateinit var dao: GoalDao

    // Track active filter
    private var activeFilter: FilterOption = FilterOption.NONE
    private var activeCategoryFilter: String? = null

    enum class FilterOption {
        NONE,
        CATEGORY,
        AMOUNT_HIGHEST,
        AMOUNT_LOWEST,
        PROGRESS_HIGHEST,
        PROGRESS_LOWEST
    }

    private val addGoalLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult

        val name        = data.getStringExtra("goalName") ?: "Unnamed Goal"
        val amountStr   = data.getStringExtra("goalAmount") ?: "0"
        val description = data.getStringExtra("goalDescription") ?: ""
        val category    = data.getStringExtra("goalCategory") ?: ""
        val isEdit      = data.getBooleanExtra("isEdit", false)
        val goalId      = data.getIntExtra("goalId", -1)
        val amount      = amountStr.toIntOrNull() ?: run { Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show(); return@registerForActivityResult }
        val userId      = UserSession.currentUser?.id ?: return@registerForActivityResult

        if (isEdit && goalId >= 0) {
            val existing = goalsList.find { it.id == goalId } ?: return@registerForActivityResult
            val updated  = existing.copy(name = name, description = description, category = category, targetAmount = amount)
            lifecycleScope.launch(Dispatchers.IO) {
                dao.updateGoal(updated)
                try { FirebaseRepository.saveGoal(updated) } catch (e: Exception) { e.printStackTrace() }
                withContext(Dispatchers.Main) {
                    val idx = goalsList.indexOfFirst { it.id == goalId }
                    if (idx >= 0) goalsList[idx] = updated
                    applyFilter(activeFilter, activeCategoryFilter)
                    updateTotalSavings()
                }
            }
        } else {
            val initialSaved = data.getIntExtra("goalInitialSaved", 0)
            val newGoal = Goal(userId = userId, name = name, description = description, category = category.ifEmpty { "General" }, targetAmount = amount, savedAmount = initialSaved)
            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertGoal(newGoal)
                val goals = dao.getGoalsForUser(userId)
                try { goals.lastOrNull { it.name == name && it.userId == userId }?.let { FirebaseRepository.saveGoal(it) } } catch (e: Exception) { e.printStackTrace() }
                withContext(Dispatchers.Main) {
                    goalsList.clear()
                    goalsList.addAll(goals)
                    applyFilter(activeFilter, activeCategoryFilter)
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); insets
        }

        dao = UserDatabase.getDatabase(this).goalDao()
        recyclerView = findViewById(R.id.recyclerGoals)

        adapter = GoalAdapter(displayList, { goal, _ ->
            addGoalLauncher.launch(Intent(this, Add_goal::class.java).apply {
                putExtra("isEdit", true)
                putExtra("goalId", goal.id)
                putExtra("name", goal.name)
                putExtra("description", goal.description)
                putExtra("category", goal.category)
                putExtra("target", goal.targetAmount)
            })
        }, { updatedGoal ->
            updateTotalSavings()
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Keep original buttons for backward compatibility, but now they use the new filter system
        findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            applyFilter(FilterOption.NONE, null)
            updateFilterButtons("All")
        }
        findViewById<Button>(R.id.btnFilterActive).setOnClickListener {
            filterByStatus("Active")
            updateFilterButtons("Active")
        }
        findViewById<Button>(R.id.btnFilterCompleted).setOnClickListener {
            filterByStatus("Completed")
            updateFilterButtons("Completed")
        }

        // Add the new filter button
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { anchor ->
            showFilterPopup(anchor)
        }

        findViewById<Button>(R.id.button16).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }
        findViewById<Button>(R.id.buttonAddGoal).setOnClickListener {
            addGoalLauncher.launch(Intent(this, Add_goal::class.java))
        }

        loadGoals()
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
    }

    private fun loadGoals() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val goals = dao.getGoalsForUser(userId)
            withContext(Dispatchers.Main) {
                goalsList.clear()
                goalsList.addAll(goals)
                applyFilter(activeFilter, activeCategoryFilter)
                updateTotalSavings()
            }
        }
    }

    // New filter popup similar to expense module
    private fun showFilterPopup(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor, Gravity.END)

        // Sorting options
        popup.menu.add(0, 0, 0, "💰 Highest Amount First")
        popup.menu.add(0, 1, 1, "💰 Lowest Amount First")
        popup.menu.add(0, 2, 2, "📈 Highest Progress First")
        popup.menu.add(0, 3, 3, "📉 Lowest Progress First")

        // Category submenu
        val categories = goalsList.map { it.category.ifEmpty { "General" } }.distinct().sorted()
        val categoryGroup = popup.menu.addSubMenu("🏷 Filter by Category")
        categoryGroup.add(1, 900, 0, "All Categories")
        categories.forEachIndexed { i, cat ->
            categoryGroup.add(1, 901 + i, i + 1, cat)
        }

        popup.menu.add(0, 99, 99, "✖ Clear Filter")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0    -> applyFilter(FilterOption.AMOUNT_HIGHEST, null)
                1    -> applyFilter(FilterOption.AMOUNT_LOWEST, null)
                2    -> applyFilter(FilterOption.PROGRESS_HIGHEST, null)
                3    -> applyFilter(FilterOption.PROGRESS_LOWEST, null)
                99   -> applyFilter(FilterOption.NONE, null)
                else -> {
                    when (item.itemId) {
                        900 -> applyFilter(FilterOption.NONE, null)  // All Categories
                        else -> {
                            val chosenCategory = item.title?.toString() ?: return@setOnMenuItemClickListener true
                            applyFilter(FilterOption.CATEGORY, chosenCategory)
                        }
                    }
                }
            }
            true
        }
        popup.show()
    }

    // Filter by status (Active/Completed)
    private fun filterByStatus(status: String) {
        val filtered = when (status) {
            "Active" -> goalsList.filter { !it.isCompleted }
            "Completed" -> goalsList.filter { it.isCompleted }
            else -> goalsList.toList()
        }

        // Apply any existing sorting filter to the filtered list
        val sorted = applySorting(filtered)
        pushToAdapter(sorted)
        updateFilterLabel("Status: $status")
    }

    // Apply filter based on options
    private fun applyFilter(option: FilterOption, category: String?) {
        activeFilter = option
        activeCategoryFilter = category

        var filtered = goalsList.toList()

        // Apply category filter first
        if (option == FilterOption.CATEGORY && category != null) {
            filtered = filtered.filter {
                it.category.equals(category, ignoreCase = true) ||
                        (it.category.isEmpty() && category == "General")
            }
        }

        // Apply sorting
        val sorted = applySorting(filtered)
        pushToAdapter(sorted)

        // Update filter label
        val labelText = when (option) {
            FilterOption.AMOUNT_HIGHEST -> "💰 Highest amount"
            FilterOption.AMOUNT_LOWEST -> "💰 Lowest amount"
            FilterOption.PROGRESS_HIGHEST -> "📈 Best progress"
            FilterOption.PROGRESS_LOWEST -> "📉 Lowest progress"
            FilterOption.CATEGORY -> "Category: $category"
            FilterOption.NONE -> ""
        }
        updateFilterLabel(labelText)

        // Reset status buttons visual state when using advanced filter
        if (option != FilterOption.NONE) {
            updateFilterButtons(null)
        }
    }

    private fun applySorting(goals: List<Goal>): List<Goal> {
        return when (activeFilter) {
            FilterOption.AMOUNT_HIGHEST -> goals.sortedByDescending { it.targetAmount }
            FilterOption.AMOUNT_LOWEST -> goals.sortedBy { it.targetAmount }
            FilterOption.PROGRESS_HIGHEST -> goals.sortedByDescending {
                if (it.targetAmount > 0) it.savedAmount.toFloat() / it.targetAmount else 0f
            }
            FilterOption.PROGRESS_LOWEST -> goals.sortedBy {
                if (it.targetAmount > 0) it.savedAmount.toFloat() / it.targetAmount else 0f
            }
            else -> goals
        }
    }

    private fun pushToAdapter(list: List<Goal>) {
        displayList.clear()
        displayList.addAll(list)
        adapter.refreshList()
        updateTotalSavings()
    }

    private fun updateFilterLabel(text: String) {
        val label = findViewById<TextView>(R.id.txtActiveFilter)
        label.text = text
        label.visibility = if (text.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun updateFilterButtons(active: String?) {
        val btnAll = findViewById<Button>(R.id.btnFilterAll)
        val btnActive = findViewById<Button>(R.id.btnFilterActive)
        val btnCompleted = findViewById<Button>(R.id.btnFilterCompleted)

        if (active == null) {
            // Reset all to half opacity when using advanced filter
            listOf(btnAll, btnActive, btnCompleted).forEach { it.alpha = 0.5f }
        } else {
            listOf(btnAll, btnActive, btnCompleted).forEach { it.alpha = 0.5f }
            when (active) {
                "All" -> btnAll.alpha = 1f
                "Active" -> btnActive.alpha = 1f
                "Completed" -> btnCompleted.alpha = 1f
            }
        }
    }

    private fun updateTotalSavings() {
        val total = displayList.sumOf { it.savedAmount }
        findViewById<TextView>(R.id.txtTotalSavings).text = "Total Saved: R %,d".format(total)
    }
}