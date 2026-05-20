package com.example.coin_budgetra

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private var expenses : List<Expense> = emptyList()
    private var goals : List<Goal> = emptyList()
    private var challenges : List<Challenge> = emptyList()


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
            exportStatement() }
        loadData()
        }

    private fun loadData() {

    val userId = UserSession.currentUser?.id ?:return
        lifecycleScope.launch(Dispatchers.IO) {
            expenses = expenseDao.getExpensesForUser(userId)
            goals = goalDao.getGoalsForUser(userId)
            challenges = challengeDao.getChallengesForUser(userId)
            withContext(Dispatchers.Main) {
                renderAll()
            }
        }
        }
    private fun renderAll()
    {
        renderSummaryCards()
        renderBarChart()
        renderExpenseTable()
        renderGoalTable()
        renderChallengeTable()
    }

    private fun renderSummaryCards() {
        val totalExpenses   = expenses.sumOf { it.amountAdded }
        val totalBudget     = expenses.sumOf { it.spendingLimit }
        val totalGoalSaved  = goals.sumOf { it.savedAmount }
        val totalGoalTarget = goals.sumOf { it.targetAmount }
        val totalChallenge  = challenges.sumOf { it.amountSaved }
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

        val totalExpenses  = expenses.sumOf { it.amountAdded }.toFloat()
        val totalBudget    = expenses.sumOf { it.spendingLimit }.toFloat()
        val totalGoals     = goals.sumOf { it.savedAmount }.toFloat()
        val totalChallenge = challenges.sumOf { it.amountSaved }.toFloat()

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
        if (expenses.isEmpty()) { addEmptyRow(table, "No expenses recorded"); return }

        addTableHeader(table, listOf("Name", "Category", "Spent", "Budget"))
        expenses.forEach { e ->
            addTableRow(table, listOf(
                e.name, e.category,
                "R${e.amountAdded}", "R${e.spendingLimit}"
            ), if (e.amountAdded >= e.spendingLimit) Color.parseColor("#FFEBEE") else Color.TRANSPARENT)
        }
    }

    /* ── Goal table ── */
    private fun renderGoalTable() {
        val table = findViewById<TableLayout>(R.id.tableGoals)
        table.removeAllViews()
        if (goals.isEmpty()) { addEmptyRow(table, "No goals recorded"); return }

        addTableHeader(table, listOf("Name", "Category", "Saved", "Target", "%"))
        goals.forEach { g ->
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
        if (challenges.isEmpty()) { addEmptyRow(table, "No challenges recorded"); return }

        addTableHeader(table, listOf("Name", "Saved", "Max", "End Date"))
        challenges.forEach { c ->
            addTableRow(table, listOf(
                c.name, "R${c.amountSaved}",
                "R${c.budgetMax}", c.endDate
            ), if (c.amountSaved >= c.budgetMax) Color.parseColor("#E8F5E9") else Color.TRANSPARENT)
        }
    }

    /* ── Table helpers ── */
    private fun addTableHeader(table: TableLayout, cols: List<String>) {
        val row = TableRow(this)
        row.setBackgroundColor(Color.parseColor("#1565C0"))
        cols.forEach { col ->
            val tv = TextView(this).apply {
                text    = col
                setPadding(16, 12, 16, 12)
                setTextColor(Color.WHITE)
                textSize = 12f
                //isFakeBoldText = true
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

        // divider
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
        sb.appendLine()

        sb.appendLine("── SUMMARY ──────────────────────────────────")
        sb.appendLine("Total Spent         : R${expenses.sumOf { it.amountAdded }}")
        sb.appendLine("Total Budget        : R${expenses.sumOf { it.spendingLimit }}")
        sb.appendLine("Total Goals Saved   : R${goals.sumOf { it.savedAmount }}")
        sb.appendLine("Total Challenges    : R${challenges.sumOf { it.amountSaved }}")
        sb.appendLine("Net Balance         : R${goals.sumOf { it.savedAmount } + challenges.sumOf { it.amountSaved } - expenses.sumOf { it.amountAdded }}")
        sb.appendLine()

        if (expenses.isNotEmpty()) {
            sb.appendLine("── EXPENSES ─────────────────────────────────")
            expenses.forEach { e ->
                sb.appendLine("  ${e.name.padEnd(24)} | ${e.category.padEnd(18)} | Spent: R${e.amountAdded} / R${e.spendingLimit}")
            }
            sb.appendLine()
        }

        if (goals.isNotEmpty()) {
            sb.appendLine("── SAVINGS GOALS ────────────────────────────")
            goals.forEach { g ->
                val pct = if (g.targetAmount > 0) (g.savedAmount * 100) / g.targetAmount else 0
                sb.appendLine("  ${g.name.padEnd(24)} | Saved: R${g.savedAmount} / R${g.targetAmount} ($pct%)")
            }
            sb.appendLine()
        }

        if (challenges.isNotEmpty()) {
            sb.appendLine("── CHALLENGES ───────────────────────────────")
            challenges.forEach { c ->
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