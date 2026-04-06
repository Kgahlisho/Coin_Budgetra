package com.example.coin_budgetra

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(
    private val goals: MutableList<Goal>,
    private val onEditClicked: (goal: Goal, position: Int) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

     var currentFilter: String = "All"

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

    // Call this after adding or editing a goal so the filter stays applied
    fun refreshList() {
        applyFilter(currentFilter)
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView            = itemView.findViewById(R.id.txtGoalName)
        val txtStatus: TextView          = itemView.findViewById(R.id.txtStatus)
        val txtDescription: TextView     = itemView.findViewById(R.id.txtDescription)
        val txtCategory: TextView        = itemView.findViewById(R.id.txtCategory)
        val txtTarget: TextView          = itemView.findViewById(R.id.txtTarget)
        val txtSaved: TextView           = itemView.findViewById(R.id.txtSaved)
        val progressBar: ProgressBar     = itemView.findViewById(R.id.progressGoal)
        val layoutAddMoney: LinearLayout = itemView.findViewById(R.id.layoutAddMoney)
        val inputAdd: EditText           = itemView.findViewById(R.id.inputAddAmount)
        val btnAdd: Button               = itemView.findViewById(R.id.btnAddMoney)
        val btnEdit: Button              = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button            = itemView.findViewById(R.id.btnDelete)
        val txtCompleted: TextView       = itemView.findViewById(R.id.txtCompleted)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }


    override fun getItemCount(): Int = filteredGoals.size

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = filteredGoals[position]
        val completed = goal.isCompleted


        holder.txtName.text        = goal.name
        holder.txtDescription.text = goal.description.ifEmpty { "No description" }
        holder.txtCategory.text    = "Category: " + goal.category.ifEmpty { "General" }
        holder.txtTarget.text      = "Target: R${goal.targetAmount}"
        holder.txtSaved.text       = "Saved: R${goal.savedAmount}"


        val progress = if (goal.targetAmount > 0) (goal.savedAmount * 100) / goal.targetAmount else 0
        holder.progressBar.progress = progress

        if (completed) {
            holder.txtStatus.text = "Completed"
            holder.txtStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        } else {
            holder.txtStatus.text = "Active"
            holder.txtStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#1565C0"))
        }

        holder.itemView.setBackgroundColor(
            if (completed) Color.parseColor("#F1F8E9") else Color.TRANSPARENT
        )

       /* if (completed) {
            holder.card.setCardBackgroundColor(Color.parseColor("#F1F8E9")) // light green
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
        }*/

        if (completed) {
            holder.layoutAddMoney.visibility = View.GONE
            holder.txtCompleted.visibility   = View.VISIBLE
        } else {
            holder.layoutAddMoney.visibility = View.VISIBLE
            holder.txtCompleted.visibility   = View.GONE
        }

        holder.btnAdd.setOnClickListener {
            val input = holder.inputAdd.text.toString()
            if (input.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amountToAdd = try { input.toInt() } catch (e: NumberFormatException) {
                Toast.makeText(holder.itemView.context, "Invalid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            goal.savedAmount += amountToAdd
            if (goal.savedAmount > goal.targetAmount) goal.savedAmount = goal.targetAmount
            holder.inputAdd.text.clear()

            notifyItemChanged(holder.adapterPosition)
        }



        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"${goal.name}\"?")
                .setPositiveButton("Delete") { dialog, _ ->
                    val realIndex = goals.indexOf(filteredGoals[pos])
                    if (realIndex >= 0) goals.removeAt(realIndex)
                    refreshList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }


        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                  val realIndex = goals.indexOf(filteredGoals[pos])
                onEditClicked(goal, realIndex)
            }
        }



        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"${goal.name}\"?")
                .setPositiveButton("Delete") { dialog, _ ->
                    val realIndex = goals.indexOf(filteredGoals[pos])
                    if (realIndex >= 0) goals.removeAt(realIndex)
                    refreshList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}