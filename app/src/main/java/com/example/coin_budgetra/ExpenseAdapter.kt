package com.example.coin_budgetra


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView


class ExpenseAdapter(
    private val expenses: MutableList<Expense>,
    private val onEditClicked: (expense: Expense, position: Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {



    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName:      TextView = itemView.findViewById(R.id.txtExpenseName)
        val txtCategory:  TextView = itemView.findViewById(R.id.txtExpenseCategory)
        val txtBudget:    TextView = itemView.findViewById(R.id.txtExpenseBudget)
        val txtDocuments: TextView = itemView.findViewById(R.id.txtExpenseDocuments)
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
        holder.txtDocuments.text = if (expense.documentUris.isEmpty()) "None"
        else "${expense.documentUris.size} Document${if (expense.documentUris.size == 1) "" else "s"}"

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
    fun refreshList() {
        notifyDataSetChanged()
    }
}