package com.example.coin_budgetra

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Expense_Module : AppCompatActivity() {

    private lateinit var adapter: ExpenseAdapter
    private lateinit var recyclerExpenses: RecyclerView
    private val expenseList      = mutableListOf<Expense>()   // master list (never sorted/filtered in-place)
    private val displayList      = mutableListOf<Expense>()   // what the adapter sees
    private lateinit var dao: ExpenseDao

    // Track active filter/sort so we can re-apply after data reload
    private var activeFilter: FilterOption = FilterOption.NONE

    enum class FilterOption {
        NONE,
        CATEGORY,
        DATE_NEWEST,
        DATE_OLDEST,
        AMOUNT_HIGHEST,
        AMOUNT_LOWEST
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = UserDatabase.getDatabase(this).expenseDao()
        recyclerExpenses = findViewById(R.id.recyclerExpenses)

        adapter = ExpenseAdapter(displayList, { expense, _ ->
            val intent = Intent(this, Create_Expense::class.java)
            intent.putExtra("expenseId", expense.id)
            startActivity(intent)
        }, {
            updateTotalExpenses()
        })

        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        recyclerExpenses.adapter = adapter

        // ── Filter button ──────────────────────────────────────────────────
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { anchor ->
            showFilterPopup(anchor)
        }

        // ── Nav buttons ────────────────────────────────────────────────────
        findViewById<Button>(R.id.button9).setOnClickListener {
            startActivity(Intent(this, Create_Expense::class.java))
        }
        findViewById<Button>(R.id.button15).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }

        loadExpenses()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    // ── Data loading ────────────────────────────────────────────────────────

    private fun loadExpenses() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val expenses = dao.getExpensesForUser(userId)
            withContext(Dispatchers.Main) {
                expenseList.clear()
                expenseList.addAll(expenses)
                applyFilter(activeFilter)       // re-apply whatever the user last chose
            }
        }
    }

    // ── Filter popup ────────────────────────────────────────────────────────

    private fun showFilterPopup(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor, Gravity.END)

        // Dynamically build category sub-options from actual data
        val categories = expenseList.map { it.category.ifEmpty { "General" } }
            .distinct().sorted()

        popup.menu.add(0, 0, 0, "⬆ Highest Amount First")
        popup.menu.add(0, 1, 1, "⬇ Lowest Amount First")
        popup.menu.add(0, 2, 2, "🕐 Newest First")
        popup.menu.add(0, 3, 3, "🕐 Oldest First")

        // Category group
        val categoryGroup = popup.menu.addSubMenu("🏷 Filter by Category")
        categoryGroup.add(1, 900, 0, "All Categories")
        categories.forEachIndexed { i, cat ->
            categoryGroup.add(1, 900 + i + 1, i + 1, cat)
        }

        popup.menu.add(0, 99, 99, "✖ Clear Filter")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0    -> applyFilter(FilterOption.AMOUNT_HIGHEST)
                1    -> applyFilter(FilterOption.AMOUNT_LOWEST)
                2    -> applyFilter(FilterOption.DATE_NEWEST)
                3    -> applyFilter(FilterOption.DATE_OLDEST)
                99   -> applyFilter(FilterOption.NONE)
                else -> {
                    if (item.itemId == 900) {
                        // "All Categories" — just sort by amount highest
                        applyFilter(FilterOption.NONE)
                    } else {
                        // A specific category was chosen
                        val chosenCategory = item.title?.toString() ?: return@setOnMenuItemClickListener true
                        applyCategoryFilter(chosenCategory)
                    }
                }
            }
            true
        }
        popup.show()
    }

    // ── Sorting / filtering logic ────────────────────────────────────────────

    private fun applyFilter(option: FilterOption) {
        activeFilter = option
        val filtered: List<Expense> = when (option) {
            FilterOption.NONE           -> expenseList.toList()
            FilterOption.AMOUNT_HIGHEST -> expenseList.sortedByDescending { it.amountAdded }
            FilterOption.AMOUNT_LOWEST  -> expenseList.sortedBy { it.amountAdded }
            FilterOption.DATE_NEWEST    -> expenseList.sortedByDescending { it.startDate }
            FilterOption.DATE_OLDEST    -> expenseList.sortedBy { it.startDate }
            FilterOption.CATEGORY       -> expenseList.sortedBy { it.category.lowercase() }
        }
        pushToAdapter(filtered)
        updateFilterLabel(option, null)
    }

    private fun applyCategoryFilter(category: String) {
        activeFilter = FilterOption.CATEGORY
        val filtered = expenseList
            .filter { it.category.equals(category, ignoreCase = true) || (it.category.isEmpty() && category == "General") }
            .sortedBy { it.name.lowercase() }
        pushToAdapter(filtered)
        updateFilterLabel(FilterOption.CATEGORY, category)
    }

    private fun pushToAdapter(list: List<Expense>) {
        displayList.clear()
        displayList.addAll(list)
        adapter.refreshList()
        updateTotalExpenses()
    }

    // Show an active-filter chip/label next to the filter button
    private fun updateFilterLabel(option: FilterOption, categoryName: String?) {
        val label = findViewById<TextView>(R.id.txtActiveFilter)
        val text = when (option) {
            FilterOption.NONE           -> ""
            FilterOption.AMOUNT_HIGHEST -> "↑ Highest first"
            FilterOption.AMOUNT_LOWEST  -> "↓ Lowest first"
            FilterOption.DATE_NEWEST    -> "Newest first"
            FilterOption.DATE_OLDEST    -> "Oldest first"
            FilterOption.CATEGORY       -> "Category: $categoryName"
        }
        label.text = text
        label.visibility = if (text.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    // ── Totals ───────────────────────────────────────────────────────────────

    private fun updateTotalExpenses() {
        val total  = displayList.sumOf { it.amountAdded }
        val budget = displayList.sumOf { it.spendingLimit }
        findViewById<TextView>(R.id.txtTotalExpenses).text =
            "Spent: R$total  |  Budget: R$budget"
    }
}