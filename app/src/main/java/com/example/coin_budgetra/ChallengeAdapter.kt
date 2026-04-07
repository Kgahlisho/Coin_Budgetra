package com.example.coin_budgetra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView


class ChallengeAdapter (
    private val challenges: MutableList<Challenge>,
    private val onEditClicked: (challenge: Challenge, position: Int) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView        = itemView.findViewById(R.id.txtChallengeName)
        val txtDescription: TextView = itemView.findViewById(R.id.txtChallengeDescription)
        val txtCategory: TextView    = itemView.findViewById(R.id.txtChallengeCategory)
        val txtDates: TextView       = itemView.findViewById(R.id.txtChallengeDates)
        val txtAmounts: TextView     = itemView.findViewById(R.id.txtChallengeAmounts)
        val btnEdit: Button          = itemView.findViewById(R.id.btnChallengeEdit)
        val btnDelete: Button        = itemView.findViewById(R.id.btnChallengeDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun getItemCount(): Int = challenges.size

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        holder.txtName.text        = challenge.name
        holder.txtDescription.text = challenge.description.ifEmpty { "No description" }
        holder.txtCategory.text    = "Category: ${challenge.category.ifEmpty { "General" }}"
        holder.txtDates.text       = "📅  ${challenge.startDate}  →  ${challenge.endDate}"
        holder.txtAmounts.text     = "Min: R${challenge.minAmount}   |   Max: R${challenge.maxAmount}"

        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onEditClicked(challenges[pos], pos)
            }
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete challenge ?")
                .setMessage("Are you sure you want to delete \"${challenge.name}\"? This cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    if (pos < challenges.size) {
                        challenges.removeAt(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, challenges.size)
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