package com.example.coin_budgetra

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FinanceBarChart(context: Context) : View(context) {

    data class Bar(val label: String, val value: Float, val color: Int)

    private var bars: List<Bar> = emptyList()
    private val paintBar   = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize  = 28f
        color     = Color.parseColor("#555555")
        textAlign = Paint.Align.CENTER
    }
    private val paintValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize  = 26f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val paintGrid  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
    }

    fun setBars(data: List<Bar>) { bars = data; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) return

        val padLeft   = 16f
        val padRight  = 16f
        val padTop    = 40f
        val padBottom = 56f
        val chartH    = height - padTop - padBottom
        val chartW    = width  - padLeft - padRight
        val maxVal    = bars.maxOf { it.value }.coerceAtLeast(1f)
        val barW      = (chartW / bars.size) * 0.55f
        val gap       = chartW / bars.size

        // grid lines
        for (i in 0..4) {
            val y = padTop + chartH - (i / 4f) * chartH
            canvas.drawLine(padLeft, y, width - padRight, y, paintGrid)
        }

        bars.forEachIndexed { i, bar ->
            val cx    = padLeft + gap * i + gap / 2f
            val barH  = (bar.value / maxVal) * chartH
            val left  = cx - barW / 2f
            val top   = padTop + chartH - barH
            val right = cx + barW / 2f
            val bot   = padTop + chartH

            paintBar.color = bar.color
            val rect = RectF(left, top, right, bot)
            canvas.drawRoundRect(rect, 12f, 12f, paintBar)

            paintValue.color = bar.color
            canvas.drawText("R${bar.value.toInt()}", cx, top - 8f, paintValue)
            canvas.drawText(bar.label, cx, height - padBottom + 36f, paintText)
        }
    }
}

class Finance_Module : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var goalDao : GoalDao
    private lateinit var challengeDao : ChallengeDao

    // Master lists
    private var allExpenses : List<Expense> = emptyList()
    private var allGoals : List<Goal> = emptyList()
    private var allChallenges : List<Challenge> = emptyList()

    // Filtered lists for display
    private var filteredExpenses: List<Expense> = emptyList()
    private var filteredGoals: List<Goal> = emptyList()
    private var filteredChallenges: List<Challenge> = emptyList()

    // Track active filters
    private var activeFilter: FilterOptions = FilterOptions.NONE
    private var activeCategoryFilter: String? = null
    private var activeSection: String = "expenses" // "expenses", "goals", "challenges"

    enum class FilterOptions {
        NONE,
        CATEGORY,
        AMOUNT_HIGHEST,
        AMOUNT_LOWEST,
        PROGRESS_HIGHEST,
        PROGRESS_LOWEST
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finance_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = UserDatabase.getDatabase(this)
        expenseDao = db.expenseDao()
        goalDao = db.goalDao()
        challengeDao = db.challengeDao()

        findViewById<Button>(R.id.button17).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.btnExportStatement).setOnClickListener {
            exportStatement()
        }

        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { anchor ->
            showFilterPopup(anchor)
        }

        loadData()
    }

    private fun loadData() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            allExpenses = expenseDao.getExpensesForUser(userId)
            allGoals = goalDao.getGoalsForUser(userId)
            allChallenges = challengeDao.getChallengesForUser(userId)

            // Initialize filtered lists with all data
            filteredExpenses = allExpenses.toList()
            filteredGoals = allGoals.toList()
            filteredChallenges = allChallenges.toList()

            withContext(Dispatchers.Main) {
                renderAll()
            }
        }
    }

    private fun showFilterPopup(anchor: View) {
        val popup = PopupMenu(this, anchor, Gravity.END)

        // Section selection submenu
        val sectionGroup = popup.menu.addSubMenu("📂 Select Section")
        sectionGroup.add(0, 100, 0, "Expenses")
        sectionGroup.add(0, 101, 1, "Savings Goals")
        sectionGroup.add(0, 102, 2, "Challenges")

        popup.menu.add(0, 1000 , 1000, "-----------------").isEnabled = false

        // Sorting options (dynamic based on selected section)
        val sortGroup = popup.menu.addSubMenu("Sort By")
        sortGroup.add(0, 0, 0, "Highest Amount First")
        sortGroup.add(0, 1, 1, "Lowest Amount First")

        popup.menu.add(0, 1000 , 1000, "-----------------").isEnabled = false

        // Category filter submenu
        updateCategorySubmenu(popup)

        popup.menu.add(0, 1000 , 1000, "-----------------").isEnabled = false
        popup.menu.add(0, 99, 99, "✖ Clear All Filters")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                100 -> {
                    activeSection = "expenses"
                    Toast.makeText(this, "Filtering Expenses", Toast.LENGTH_SHORT).show()
                    updateFilterLabel("Filtering: Expenses")
                    applyFilter(activeFilter, activeCategoryFilter)
                }
                101 -> {
                    activeSection = "goals"
                    Toast.makeText(this, "Filtering Savings Goals", Toast.LENGTH_SHORT).show()
                    updateFilterLabel("Filtering: Goals")
                    applyFilter(activeFilter, activeCategoryFilter)
                }
                102 -> {
                    activeSection = "challenges"
                    Toast.makeText(this, "Filtering Challenges", Toast.LENGTH_SHORT).show()
                    updateFilterLabel("Filtering: Challenges")
                    applyFilter(activeFilter, activeCategoryFilter)
                }
                0 -> applyFilter(FilterOptions.AMOUNT_HIGHEST, null)
                1 -> applyFilter(FilterOptions.AMOUNT_LOWEST, null)
                99 -> clearAllFilters()
                else -> {
                    // Category selection (IDs 200 and above)
                    if (item.itemId >= 200) {
                        val category = item.title?.toString()
                        if (category == "All Categories") {
                            applyFilter(FilterOptions.NONE, null)
                        } else {
                            applyFilter(FilterOptions.CATEGORY, category)
                        }
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun updateCategorySubmenu(popup: PopupMenu) {
        val categoryGroup = popup.menu.addSubMenu("🏷 Filter by Category")
        categoryGroup.add(0, 200, 0, "All Categories")

        val categories = when (activeSection) {
            "expenses" -> allExpenses.map { it.category.ifEmpty { "General" } }.distinct().sorted()
            "goals" -> allGoals.map { it.category.ifEmpty { "General" } }.distinct().sorted()
            "challenges" -> allChallenges.map { it.name.split(" ").firstOrNull() ?: "General" }.distinct().sorted()
            else -> emptyList()
        }

        categories.forEachIndexed { index, cat ->
            categoryGroup.add(0, 201 + index, index + 1, cat)
        }
    }

    private fun applyFilter(option: FilterOptions, category: String?) {
        activeFilter = option
        activeCategoryFilter = category

        when (activeSection) {
            "expenses" -> {
                var filtered = allExpenses.toList()

                // Apply category filter
                if (option == FilterOptions.CATEGORY && category != null) {
                    filtered = filtered.filter {
                        it.category.equals(category, ignoreCase = true) ||
                                (it.category.isEmpty() && category == "General")
                    }
                }

                // Apply sorting
                filteredExpenses = when (option) {
                    FilterOptions.AMOUNT_HIGHEST -> filtered.sortedByDescending { it.amountAdded }
                    FilterOptions.AMOUNT_LOWEST -> filtered.sortedBy { it.amountAdded }
                    else -> filtered
                }

                // Reset other sections to show all data
                filteredGoals = allGoals.toList()
                filteredChallenges = allChallenges.toList()
            }
            "goals" -> {
                var filtered = allGoals.toList()

                // Apply category filter
                if (option == FilterOptions.CATEGORY && category != null) {
                    filtered = filtered.filter {
                        it.category.equals(category, ignoreCase = true) ||
                                (it.category.isEmpty() && category == "General")
                    }
                }

                // Apply sorting based on option
                filteredGoals = when (option) {
                    FilterOptions.AMOUNT_HIGHEST -> filtered.sortedByDescending { it.targetAmount }
                    FilterOptions.AMOUNT_LOWEST -> filtered.sortedBy { it.targetAmount }
                    FilterOptions.PROGRESS_HIGHEST -> filtered.sortedByDescending {
                        if (it.targetAmount > 0) it.savedAmount.toFloat() / it.targetAmount else 0f
                    }
                    FilterOptions.PROGRESS_LOWEST -> filtered.sortedBy {
                        if (it.targetAmount > 0) it.savedAmount.toFloat() / it.targetAmount else 0f
                    }
                    else -> filtered
                }

                // Reset other sections
                filteredExpenses = allExpenses.toList()
                filteredChallenges = allChallenges.toList()
            }
            "challenges" -> {
                var filtered = allChallenges.toList()

                // Apply category-like filter (using name)
                if (option == FilterOptions.CATEGORY && category != null) {
                    filtered = filtered.filter {
                        it.name.contains(category, ignoreCase = true)
                    }
                }

                // Apply sorting
                filteredChallenges = when (option) {
                    FilterOptions.AMOUNT_HIGHEST -> filtered.sortedByDescending { it.amountSaved }
                    FilterOptions.AMOUNT_LOWEST -> filtered.sortedBy { it.amountSaved }
                    else -> filtered
                }

                // Reset other sections
                filteredExpenses = allExpenses.toList()
                filteredGoals = allGoals.toList()
            }
        }

        renderAll()

        // Update filter label
        val labelText = buildFilterLabel(option, category)
        updateFilterLabel(labelText)
    }

    private fun buildFilterLabel(option: FilterOptions, category: String?): String {
        val sectionName = when (activeSection) {
            "expenses" -> "Expenses"
            "goals" -> "Goals"
            "challenges" -> "Challenges"
            else -> ""
        }

        return when (option) {
            FilterOptions.AMOUNT_HIGHEST -> "$sectionName: Highest amount"
            FilterOptions.AMOUNT_LOWEST -> "$sectionName: Lowest amount"
            FilterOptions.PROGRESS_HIGHEST -> "$sectionName: Best progress"
            FilterOptions.PROGRESS_LOWEST -> "$sectionName: Lowest progress"
            FilterOptions.CATEGORY -> "$sectionName: $category"
            FilterOptions.NONE -> if (activeFilter != FilterOptions.NONE) sectionName else ""
        }
    }

    private fun clearAllFilters() {
        activeFilter = FilterOptions.NONE
        activeCategoryFilter = null
        filteredExpenses = allExpenses.toList()
        filteredGoals = allGoals.toList()
        filteredChallenges = allChallenges.toList()
        renderAll()
        updateFilterLabel("")
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show()
    }

    private fun updateFilterLabel(text: String) {
        // Filter label visibility is handled in XML - we can add a TextView for it
        // For now, we'll just show a toast when filter is applied
        if (text.isNotEmpty() && text != "Filtering: Expenses" && text != "Filtering: Goals" && text != "Filtering: Challenges") {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderAll() {
        renderSummaryCards()
        renderBarChart()
        renderExpenseTable()
        renderGoalTable()
        renderChallengeTable()
    }

    private fun renderSummaryCards() {
        val totalExpenses   = filteredExpenses.sumOf { it.amountAdded }
        val totalBudget     = filteredExpenses.sumOf { it.spendingLimit }
        val totalGoalSaved  = filteredGoals.sumOf { it.savedAmount }
        val totalGoalTarget = filteredGoals.sumOf { it.targetAmount }
        val totalChallenge  = filteredChallenges.sumOf { it.amountSaved }
        val netBalance      = totalGoalSaved + totalChallenge - totalExpenses

        findViewById<TextView>(R.id.txtFinTotalExpenses).text   = "R$totalExpenses"
        findViewById<TextView>(R.id.txtFinBudget).text          = "R$totalBudget"
        findViewById<TextView>(R.id.txtFinGoalSaved).text       = "R$totalGoalSaved / R$totalGoalTarget"
        findViewById<TextView>(R.id.txtFinChallengeSaved).text  = "R$totalChallenge"
        findViewById<TextView>(R.id.txtFinNetBalance).text      = "R$netBalance"
        val netView = findViewById<TextView>(R.id.txtFinNetBalance)
        netView.setTextColor(if (netBalance >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#B71C1C"))
    }

    private fun renderBarChart() {
        val container = findViewById<FrameLayout>(R.id.finChartContainer)
        container.removeAllViews()

        val totalExpenses  = filteredExpenses.sumOf { it.amountAdded }.toFloat()
        val totalBudget    = filteredExpenses.sumOf { it.spendingLimit }.toFloat()
        val totalGoals     = filteredGoals.sumOf { it.savedAmount }.toFloat()
        val totalChallenge = filteredChallenges.sumOf { it.amountSaved }.toFloat()

        val chart = FinanceBarChart(this)
        chart.setBars(listOf(
            FinanceBarChart.Bar("Spent",       totalExpenses,  Color.parseColor("#E53935")),
            FinanceBarChart.Bar("Budget",      totalBudget,    Color.parseColor("#1565C0")),
            FinanceBarChart.Bar("Goals",       totalGoals,     Color.parseColor("#2E7D32")),
            FinanceBarChart.Bar("Challenges",  totalChallenge, Color.parseColor("#F57F17"))
        ))
        container.addView(chart, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 500
        ))
    }

    private fun renderExpenseTable() {
        val table = findViewById<TableLayout>(R.id.tableExpenses)
        table.removeAllViews()
        if (filteredExpenses.isEmpty()) {
            addEmptyRow(table, "No expenses recorded")
            return
        }

        addTableHeader(table, listOf("Name", "Category", "Spent", "Budget"))
        filteredExpenses.forEach { e ->
            addTableRow(table, listOf(
                e.name, e.category,
                "R${e.amountAdded}", "R${e.spendingLimit}"
            ), if (e.amountAdded >= e.spendingLimit) Color.parseColor("#FFEBEE") else Color.TRANSPARENT)
        }
    }

    private fun renderGoalTable() {
        val table = findViewById<TableLayout>(R.id.tableGoals)
        table.removeAllViews()
        if (filteredGoals.isEmpty()) {
            addEmptyRow(table, "No goals recorded")
            return
        }

        addTableHeader(table, listOf("Name", "Category", "Saved", "Target", "%"))
        filteredGoals.forEach { g ->
            val pct = if (g.targetAmount > 0) (g.savedAmount * 100) / g.targetAmount else 0
            addTableRow(table, listOf(
                g.name, g.category,
                "R${g.savedAmount}", "R${g.targetAmount}", "$pct%"
            ), if (g.isCompleted) Color.parseColor("#E8F5E9") else Color.TRANSPARENT)
        }
    }

    private fun renderChallengeTable() {
        val table = findViewById<TableLayout>(R.id.tableChallenges)
        table.removeAllViews()
        if (filteredChallenges.isEmpty()) {
            addEmptyRow(table, "No challenges recorded")
            return
        }

        addTableHeader(table, listOf("Name", "Saved", "Max", "End Date"))
        filteredChallenges.forEach { c ->
            addTableRow(table, listOf(
                c.name, "R${c.amountSaved}",
                "R${c.budgetMax}", c.endDate
            ), if (c.amountSaved >= c.budgetMax) Color.parseColor("#E8F5E9") else Color.TRANSPARENT)
        }
    }

    private fun addTableHeader(table: TableLayout, cols: List<String>) {
        val row = TableRow(this)
        row.setBackgroundColor(Color.parseColor("#1565C0"))
        cols.forEach { col ->
            val tv = TextView(this).apply {
                text    = col
                setPadding(16, 12, 16, 12)
                setTextColor(Color.WHITE)
                textSize = 12f
            }
            row.addView(tv)
        }
        table.addView(row)
    }

    private fun addTableRow(table: TableLayout, cols: List<String>, bgColor: Int) {
        val row = TableRow(this)
        if (bgColor != Color.TRANSPARENT) row.setBackgroundColor(bgColor)
        cols.forEach { col ->
            val tv = TextView(this).apply {
                text     = col
                setPadding(16, 10, 16, 10)
                textSize = 11f
                setTextColor(Color.parseColor("#333333"))
            }
            row.addView(tv)
        }
        table.addView(row)

        val divider = View(this)
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"))
        table.addView(divider, TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT, 1
        ))
    }

    private fun addEmptyRow(table: TableLayout, msg: String) {
        val tv = TextView(this).apply {
            text      = msg
            setPadding(16, 16, 16, 16)
            textSize  = 12f
            setTextColor(Color.GRAY)
        }
        table.addView(tv)
    }

    private fun exportStatement() {
        val user = UserSession.currentUser
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val sb = StringBuilder()
        sb.appendLine("=== COIN BUDGETRA — FINANCIAL STATEMENT ===")
        sb.appendLine("User : ${user?.name} ${user?.surname}")
        sb.appendLine("Date : $date")

        // Add filter info to export
        if (activeFilter != FilterOptions.NONE) {
            sb.appendLine("Filter Applied: ${buildFilterLabel(activeFilter, activeCategoryFilter)}")
        }
        sb.appendLine()

        sb.appendLine("── SUMMARY ──────────────────────────────────")
        sb.appendLine("Total Spent         : R${filteredExpenses.sumOf { it.amountAdded }}")
        sb.appendLine("Total Budget        : R${filteredExpenses.sumOf { it.spendingLimit }}")
        sb.appendLine("Total Goals Saved   : R${filteredGoals.sumOf { it.savedAmount }}")
        sb.appendLine("Total Challenges    : R${filteredChallenges.sumOf { it.amountSaved }}")
        sb.appendLine("Net Balance         : R${filteredGoals.sumOf { it.savedAmount } + filteredChallenges.sumOf { it.amountSaved } - filteredExpenses.sumOf { it.amountAdded }}")
        sb.appendLine()

        if (filteredExpenses.isNotEmpty()) {
            sb.appendLine("── EXPENSES ─────────────────────────────────")
            filteredExpenses.forEach { e ->
                sb.appendLine("  ${e.name.padEnd(24)} | ${e.category.padEnd(18)} | Spent: R${e.amountAdded} / R${e.spendingLimit}")
            }
            sb.appendLine()
        }

        if (filteredGoals.isNotEmpty()) {
            sb.appendLine("── SAVINGS GOALS ────────────────────────────")
            filteredGoals.forEach { g ->
                val pct = if (g.targetAmount > 0) (g.savedAmount * 100) / g.targetAmount else 0
                sb.appendLine("  ${g.name.padEnd(24)} | Saved: R${g.savedAmount} / R${g.targetAmount} ($pct%)")
            }
            sb.appendLine()
        }

        if (filteredChallenges.isNotEmpty()) {
            sb.appendLine("── CHALLENGES ───────────────────────────────")
            filteredChallenges.forEach { c ->
                sb.appendLine("  ${c.name.padEnd(24)} | Saved: R${c.amountSaved} / R${c.budgetMax} | Ends: ${c.endDate}")
            }
            sb.appendLine()
        }

        sb.appendLine("═════════════════════════════════════════════")
        sb.appendLine("Generated by Coin Budgetra  •  $date")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type    = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Coin Budgetra Statement – $date")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Share Statement via…"))
    }
}