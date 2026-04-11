package com.example.coin_budgetra

import android.app.AlertDialog
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class ExpenseAdapter(
    private val expenses: MutableList<Expense>,
    private val onEditClicked: (expense: Expense, position: Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName:      TextView = itemView.findViewById(R.id.txtExpenseName)
        val txtCategory:  TextView = itemView.findViewById(R.id.txtExpenseCategory)
        val txtBudget:    TextView = itemView.findViewById(R.id.txtExpenseBudget)
        val txtProgress:  TextView = itemView.findViewById(R.id.txtExpenseProgress)
        val txtDocuments: TextView = itemView.findViewById(R.id.txtExpenseDocuments)
        val btnAdd:       Button   = itemView.findViewById(R.id.btnExpenseAdd)
        val btnEdit:      Button   = itemView.findViewById(R.id.btnExpenseEdit)
        val btnDelete:    Button   = itemView.findViewById(R.id.btnExpenseDelete)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }


    override fun getItemCount(): Int = expenses.size
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.txtName.text      = expense.name
        holder.txtCategory.text  = expense.category.ifEmpty { "General" }
        holder.txtBudget.text    = "R${expense.spendingLimit}"
        bindProgress(holder, expense)
        holder.txtDocuments.text = if (expense.documentUris.isEmpty()) "None"
        else "${expense.documentUris.size} Document${if (expense.documentUris.size == 1) "" else "s"}"

        holder.btnAdd.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val exp = expenses[pos]

            // If already at the limit, nothing to add
            if (exp.amountAdded >= exp.spendingLimit) {
                Toast.makeText(
                    holder.itemView.context,
                    "Budget limit already reached for \"${exp.name}\"",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Show a simple input dialog
            val input = EditText(holder.itemView.context).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                hint = "Amount to add (R)"
            }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Add money to \"${exp.name}\"")
                .setMessage("Remaining: R${exp.spendingLimit - exp.amountAdded}")
                .setView(input)
                .setPositiveButton("Add") { dialog, _ ->
                    val toAdd = input.text.toString().trim().toIntOrNull()
                    when {
                        toAdd == null || toAdd <= 0 ->
                            Toast.makeText(holder.itemView.context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        exp.amountAdded + toAdd > exp.spendingLimit -> {
                            val remaining = exp.spendingLimit - exp.amountAdded
                            Toast.makeText(
                                holder.itemView.context,
                                "Only R$remaining remaining before the limit",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            exp.amountAdded += toAdd
                            notifyItemChanged(pos)
                            Toast.makeText(
                                holder.itemView.context,
                                "R$toAdd added to \"${exp.name}\"",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }


        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onEditClicked(expenses[pos], pos)
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete expense?")
                .setMessage("Are you sure you want to delete \"${expense.name}\"? This cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    if (pos < expenses.size) {
                        expenses.removeAt(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, expenses.size)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun bindProgress(holder: ExpenseViewHolder, expense: Expense) {
        val added = expense.amountAdded
        val limit = expense.spendingLimit
        holder.txtProgress.text = "R$added / R$limit"

        val colour = when {
            limit <= 0          -> Color.parseColor("#888888")
            added >= limit      -> Color.parseColor("#B71C1C") // at/over limit — red
            added.toFloat() / limit >= 0.75f -> Color.parseColor("#E65100") // ≥75% — orange
            else                -> Color.parseColor("#1565C0") // healthy — blue
        }
        holder.txtProgress.setTextColor(colour)
    }
    fun refreshList() {
        notifyDataSetChanged()
    }
}