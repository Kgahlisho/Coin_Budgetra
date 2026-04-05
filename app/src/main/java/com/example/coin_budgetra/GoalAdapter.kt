package com.example.coin_budgetra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView


class GoalAdapter(private val goals: MutableList<Goal>) :
    RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtGoalName)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val txtTarget: TextView = itemView.findViewById(R.id.txtTarget)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressGoal)
        val inputAdd: EditText = itemView.findViewById(R.id.inputAddAmount)
        val btnAdd: Button = itemView.findViewById(R.id.btnAddMoney)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun getItemCount(): Int = goals.size

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]

        holder.txtName.text = goal.name
        holder.txtDescription.text = goal.description
        holder.txtCategory.text = goal.category
        holder.txtTarget.text = "Target: R${goal.targetAmount}"



        val progress = if (goal.targetAmount > 0) {
            (goal.savedAmount * 100) / goal.targetAmount
        } else 0
        holder.progressBar.progress = progress

        holder.btnAdd.setOnClickListener {
            val input = holder.inputAdd.text.toString()

            if (input.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val amountToAdd = try {
                input.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(holder.itemView.context, "Invalid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            goal.savedAmount += amountToAdd
            if (goal.savedAmount > goal.targetAmount) goal.savedAmount = goal.targetAmount

            val newProgress = if (goal.targetAmount > 0) {
                (goal.savedAmount * 100) / goal.targetAmount
            } else 0
            holder.progressBar.progress = newProgress
            holder.inputAdd.text.clear()
        }


        holder.btnDelete.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                goals.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
            }
        }
    }
}



