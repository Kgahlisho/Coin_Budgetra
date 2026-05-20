package com.example.coin_budgetra

import android.app.AlertDialog
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseAdapter(
    private val expenses: MutableList<Expense>,
    private val onEditClicked: (expense: Expense, position: Int) -> Unit,
    private val onTotalChanged: () -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dao: ExpenseDao       = UserDatabase.getDatabase(itemView.context).expenseDao()
        val txtName: TextView     = itemView.findViewById(R.id.txtExpenseName)
        val txtCategory: TextView = itemView.findViewById(R.id.txtExpenseCategory)
        val txtBudget: TextView   = itemView.findViewById(R.id.txtExpenseBudget)
        val txtProgress: TextView = itemView.findViewById(R.id.txtExpenseProgress)
        val btnAdd: Button        = itemView.findViewById(R.id.btnExpenseAdd)
        val btnEdit: Button       = itemView.findViewById(R.id.btnExpenseEdit)
        val btnDelete: Button     = itemView.findViewById(R.id.btnExpenseDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ExpenseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false))

    override fun getItemCount() = expenses.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.txtName.text     = expense.name
        holder.txtCategory.text = expense.category.ifEmpty { "General" }
        holder.txtBudget.text   = "R${expense.spendingLimit}"
        bindProgress(holder, expense)

        holder.btnAdd.setOnClickListener {
            val pos = holder.adapterPosition; if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val exp = expenses[pos]
            if (exp.amountAdded >= exp.spendingLimit) { Toast.makeText(holder.itemView.context, "You have reached the budget limit for: \"${exp.name}\"", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val input = EditText(holder.itemView.context).apply { inputType = InputType.TYPE_CLASS_NUMBER; hint = "Amount to add (R): " }
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Add money to: \"${exp.name}\"").setMessage("Remaining: R${exp.spendingLimit - exp.amountAdded}").setView(input)
                .setPositiveButton("Add") { dialog, _ ->
                    val toAdd = input.text.toString().trim().toIntOrNull()
                    when {
                        toAdd == null || toAdd <= 0 -> Toast.makeText(holder.itemView.context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        exp.amountAdded + toAdd > exp.spendingLimit -> Toast.makeText(holder.itemView.context, "Only R${exp.spendingLimit - exp.amountAdded} remaining before the limit", Toast.LENGTH_SHORT).show()
                        else -> {
                            val updated = exp.copy(amountAdded = exp.amountAdded + toAdd)
                            CoroutineScope(Dispatchers.IO).launch {
                                holder.dao.updateExpense(updated)
                                try { FirebaseRepository.saveExpense(updated) } catch (e: Exception) { e.printStackTrace() }
                                withContext(Dispatchers.Main) { expenses[pos] = updated; notifyItemChanged(pos); onTotalChanged(); Toast.makeText(holder.itemView.context, "R$toAdd added to: \"${exp.name}\"", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
        }

        holder.btnEdit.setOnClickListener { val pos = holder.adapterPosition; if (pos != RecyclerView.NO_POSITION) onEditClicked(expenses[pos], pos) }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition; if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete expense?").setMessage("Are you sure you want to delete: \"${expense.name}\"? This cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        holder.dao.deleteExpense(expense)
                        try { FirebaseRepository.deleteExpense(expense) } catch (e: Exception) { e.printStackTrace() }
                        withContext(Dispatchers.Main) { if (pos < expenses.size) { expenses.removeAt(pos); notifyItemRemoved(pos); notifyItemRangeChanged(pos, expenses.size); onTotalChanged() } }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
        }
    }

    private fun bindProgress(holder: ExpenseViewHolder, expense: Expense) {
        val added = expense.amountAdded; val limit = expense.spendingLimit
        holder.txtProgress.text = "R$added / R$limit"
        holder.txtProgress.setTextColor(Color.parseColor(when {
            limit <= 0 -> "#888888"; added >= limit -> "#B71C1C"; added.toFloat() / limit >= 0.75f -> "#E65100"; else -> "#1565C0"
        }))
    }

    fun refreshList() { notifyDataSetChanged() }
}