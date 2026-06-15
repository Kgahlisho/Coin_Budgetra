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

class Challenges_dash : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChallengeAdapter
    private val allChallenges = mutableListOf<Challenge>()     // Master list
    private val displayList = mutableListOf<Challenge>()       // What adapter shows
    private lateinit var dao: ChallengeDao

    // Track active filter
    private var activeFilter: FilterOption = FilterOption.NONE

    enum class FilterOption {
        NONE,
        CATEGORY,
        AMOUNT_HIGHEST,
        AMOUNT_LOWEST,
        PROGRESS_HIGHEST,
        PROGRESS_LOWEST,
        DATE_NEWEST,
        DATE_OLDEST
    }

    private val addChallengeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult

        val name        = data.getStringExtra("challengeName")      ?: "Unnamed Challenge"
        val desc        = data.getStringExtra("challengeDesc")      ?: ""
        val category    = data.getStringExtra("challengeCategory")  ?: ""
        val startDate   = data.getStringExtra("challengeStartDate") ?: ""
        val endDate     = data.getStringExtra("challengeEndDate")   ?: ""
        val budgetMax   = data.getIntExtra("challengeBudgetMax", 0)
        val amountSaved = data.getIntExtra("challengeAmtSaved",  0)
        val isEdit      = data.getBooleanExtra("isEdit", false)
        val challengeId = data.getIntExtra("challengeId", -1)
        val userId      = UserSession.currentUser?.id ?: return@registerForActivityResult

        if (isEdit && challengeId >= 0) {
            val existing = allChallenges.find { it.id == challengeId } ?: return@registerForActivityResult
            val updated  = existing.copy(name = name, description = desc, category = category, startDate = startDate, endDate = endDate, budgetMax = budgetMax, amountSaved = amountSaved)
            lifecycleScope.launch(Dispatchers.IO) {
                dao.updateChallenge(updated)
                try { FirebaseRepository.saveChallenge(updated) } catch (e: Exception) { e.printStackTrace() }
                withContext(Dispatchers.Main) {
                    val idx = allChallenges.indexOfFirst { it.id == challengeId }
                    if (idx >= 0) allChallenges[idx] = updated
                    applyFilter(activeFilter)
                    updateTotalSaved()
                }
            }
        } else {
            val newChallenge = Challenge(userId = userId, name = name, description = desc, category = category.ifEmpty { "General" }, startDate = startDate, endDate = endDate, budgetMax = budgetMax, amountSaved = amountSaved)
            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertChallenge(newChallenge)
                val challenges = dao.getChallengesForUser(userId)
                try { challenges.lastOrNull { it.name == name && it.userId == userId }?.let { FirebaseRepository.saveChallenge(it) } } catch (e: Exception) { e.printStackTrace() }
                withContext(Dispatchers.Main) {
                    allChallenges.clear()
                    allChallenges.addAll(challenges)
                    applyFilter(activeFilter)
                    updateTotalSaved()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_challenge_dash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = UserDatabase.getDatabase(this).challengeDao()
        recyclerView = findViewById(R.id.recyclerChallenge)

        adapter = ChallengeAdapter(displayList, { challenge, _ ->
            addChallengeLauncher.launch(Intent(this, Add_challenge::class.java).apply {
                putExtra("isEdit", true)
                putExtra("challengeId", challenge.id)
                putExtra("name", challenge.name)
                putExtra("description", challenge.description)
                putExtra("category", challenge.category)
                putExtra("startDate", challenge.startDate)
                putExtra("endDate", challenge.endDate)
                putExtra("budgetMax", challenge.budgetMax)
                putExtra("amountSaved", challenge.amountSaved)
            })
        }, { _ ->
            updateTotalSaved()
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnBackFromChallengeDash).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreateChallengeGoal).setOnClickListener {
            addChallengeLauncher.launch(Intent(this, Add_challenge::class.java))
        }

        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { anchor ->
            showFilterPopup(anchor)
        }

        loadChallenges()
    }

    override fun onResume() {
        super.onResume()
        loadChallenges()
    }

    private fun loadChallenges() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val challenges = dao.getChallengesForUser(userId)
            withContext(Dispatchers.Main) {
                allChallenges.clear()
                allChallenges.addAll(challenges)
                applyFilter(activeFilter)
                updateTotalSaved()
            }
        }
    }

    private fun showFilterPopup(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor, Gravity.END)

        // Sorting options
        popup.menu.add(0, 0, 0, "Highest Amount")
        popup.menu.add(0, 1, 1, "Lowest Amount")
        popup.menu.add(0, 2, 2, "Most Progress")
        popup.menu.add(0, 3, 3, "Least Progress")
        popup.menu.add(0, 4, 4, "Earliest (Start Date)")
        popup.menu.add(0, 5, 5, "Oldest (Start Date)")

        // Category submenu
        val categories = allChallenges.map { it.category.ifEmpty { "General" } }.distinct().sorted()
        val categoryGroup = popup.menu.addSubMenu("🏷 Filter by Category")
        categoryGroup.add(1, 900, 0, "All Categories")
        categories.forEachIndexed { i, cat ->
            categoryGroup.add(1, 901 + i, i + 1, cat)
        }

        //popup.menu.addSeparator()
        popup.menu.add(0, 99, 99, "✖ Clear Filter")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0    -> applyFilter(FilterOption.AMOUNT_HIGHEST)
                1    -> applyFilter(FilterOption.AMOUNT_LOWEST)
                2    -> applyFilter(FilterOption.PROGRESS_HIGHEST)
                3    -> applyFilter(FilterOption.PROGRESS_LOWEST)
                4    -> applyFilter(FilterOption.DATE_NEWEST)
                5    -> applyFilter(FilterOption.DATE_OLDEST)
                99   -> applyFilter(FilterOption.NONE)
                else -> {
                    when (item.itemId) {
                        900 -> applyFilter(FilterOption.NONE)
                        else -> {
                            val chosenCategory = item.title?.toString() ?: return@setOnMenuItemClickListener true
                            filterByCategory(chosenCategory)
                        }
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun filterByCategory(category: String) {
        val filtered = allChallenges.filter {
            it.category.equals(category, ignoreCase = true) ||
                    (it.category.isEmpty() && category == "General")
        }
        val sorted = applySorting(filtered)
        pushToAdapter(sorted)
        updateFilterLabel("Category: $category")
        Toast.makeText(this, "Showing challenges in category: $category", Toast.LENGTH_SHORT).show()
    }

    private fun applyFilter(option: FilterOption) {
        activeFilter = option
        val filtered = allChallenges.toList()
        val sorted = applySorting(filtered)
        pushToAdapter(sorted)

        val labelText = when (option) {
            FilterOption.AMOUNT_HIGHEST -> "💰 Highest amount"
            FilterOption.AMOUNT_LOWEST -> "💰 Lowest amount"
            FilterOption.PROGRESS_HIGHEST -> "📈 Best progress"
            FilterOption.PROGRESS_LOWEST -> "📉 Lowest progress"
            FilterOption.DATE_NEWEST -> "🕐 Newest first"
            FilterOption.DATE_OLDEST -> "🕐 Oldest first"
            FilterOption.NONE -> ""
            else -> ""
        }
        updateFilterLabel(labelText)

        if (option != FilterOption.NONE) {
            Toast.makeText(this, labelText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applySorting(challenges: List<Challenge>): List<Challenge> {
        return when (activeFilter) {
            FilterOption.AMOUNT_HIGHEST -> challenges.sortedByDescending { it.budgetMax }
            FilterOption.AMOUNT_LOWEST -> challenges.sortedBy { it.budgetMax }
            FilterOption.PROGRESS_HIGHEST -> challenges.sortedByDescending {
                if (it.budgetMax > 0) it.amountSaved.toFloat() / it.budgetMax else 0f
            }
            FilterOption.PROGRESS_LOWEST -> challenges.sortedBy {
                if (it.budgetMax > 0) it.amountSaved.toFloat() / it.budgetMax else 0f
            }
            FilterOption.DATE_NEWEST -> challenges.sortedByDescending { it.startDate }
            FilterOption.DATE_OLDEST -> challenges.sortedBy { it.startDate }
            else -> challenges
        }
    }

    private fun pushToAdapter(list: List<Challenge>) {
        displayList.clear()
        displayList.addAll(list)
        adapter.refreshList()
        updateTotalSaved()
    }

    private fun updateFilterLabel(text: String) {
        val label = findViewById<TextView>(R.id.txtActiveFilter)
        label.text = text
        label.visibility = if (text.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun updateTotalSaved() {
        val total = displayList.sumOf { it.amountSaved }
        findViewById<TextView>(R.id.txtTotalSaved).text = "Total Accumulated: R$total"
    }
}