package com.example.coin_budgetra

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalAdapter(
    private val goals: MutableList<Goal>,
    private val onEditClicked: (goal: Goal, position: Int) -> Unit,
    private val onTotalChanged: (goal: Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    var currentFilter = "All"
    private var filteredGoals: MutableList<Goal> = goals.toMutableList()

    fun applyFilter(filter: String) {
        currentFilter = filter
        filteredGoals = when (filter) {
            "Active"    -> goals.filter { !it.isCompleted }.toMutableList()
            "Completed" -> goals.filter { it.isCompleted }.toMutableList()
            else        -> goals.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun refreshList() { applyFilter(currentFilter) }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dao: GoalDao             = UserDatabase.getDatabase(itemView.context).goalDao()
        val txtName: TextView        = itemView.findViewById(R.id.txtGoalName)
        val txtStatus: TextView      = itemView.findViewById(R.id.txtStatus)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        val txtCategory: TextView    = itemView.findViewById(R.id.txtCategory)
        val txtTarget: TextView      = itemView.findViewById(R.id.txtTarget)
        val txtSaved: TextView       = itemView.findViewById(R.id.txtSaved)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressGoal)
        val layoutAddMoney: LinearLayout = itemView.findViewById(R.id.layoutAddMoney)
        val inputAdd: EditText       = itemView.findViewById(R.id.inputAddAmount)
        val btnAdd: Button           = itemView.findViewById(R.id.btnAddMoney)
        val btnEdit: Button          = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button        = itemView.findViewById(R.id.btnDelete)
        val txtCompleted: TextView   = itemView.findViewById(R.id.txtCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GoalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false))

    override fun getItemCount() = filteredGoals.size

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal      = filteredGoals[position]
        val completed = goal.isCompleted

        holder.txtName.text        = goal.name
        holder.txtDescription.text = goal.description.ifEmpty { "No description" }
        holder.txtCategory.text    = "Category: " + goal.category.ifEmpty { "General" }
        holder.txtTarget.text      = "Target: R${goal.targetAmount}"
        holder.txtSaved.text       = "Saved: R${goal.savedAmount}"
        holder.progressBar.progress = if (goal.targetAmount > 0) (goal.savedAmount * 100) / goal.targetAmount else 0

        holder.txtStatus.text = if (completed) "Completed" else "Active"
        holder.txtStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
            Color.parseColor(if (completed) "#2E7D32" else "#1565C0"))
        holder.itemView.setBackgroundColor(if (completed) Color.parseColor("#F1F8E9") else Color.TRANSPARENT)
        holder.layoutAddMoney.visibility = if (completed) View.GONE else View.VISIBLE
        holder.txtCompleted.visibility   = if (completed) View.VISIBLE else View.GONE

        holder.btnAdd.setOnClickListener {
            val input = holder.inputAdd.text.toString()
            if (input.isEmpty()) { Toast.makeText(holder.itemView.context, "Enter an amount", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val amountToAdd = input.toIntOrNull() ?: run { Toast.makeText(holder.itemView.context, "Invalid number", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val newTotal = goal.savedAmount + amountToAdd
            if (newTotal > goal.targetAmount) {
                Toast.makeText(holder.itemView.context, "Only R${goal.targetAmount - goal.savedAmount} remaining to reach target.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = goal.copy(savedAmount = newTotal)
            CoroutineScope(Dispatchers.IO).launch {
                holder.dao.updateGoal(updated)
                try { FirebaseRepository.saveGoal(updated) } catch (e: Exception) { e.printStackTrace() }
                withContext(Dispatchers.Main) {
                    val idx = goals.indexOf(goal); if (idx >= 0) goals[idx] = updated
                    holder.inputAdd.text.clear(); refreshList(); onTotalChanged(updated)
                }
            }
        }

        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onEditClicked(goal, goals.indexOf(filteredGoals[pos]))
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"${goal.name}\"?")
                .setPositiveButton("Delete") { dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        holder.dao.deleteGoal(goal)
                        try { FirebaseRepository.deleteGoal(goal) } catch (e: Exception) { e.printStackTrace() }
                        withContext(Dispatchers.Main) {
                            val realIndex = goals.indexOf(filteredGoals[pos])
                            if (realIndex >= 0) goals.removeAt(realIndex)
                            refreshList(); onTotalChanged(goal)
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}