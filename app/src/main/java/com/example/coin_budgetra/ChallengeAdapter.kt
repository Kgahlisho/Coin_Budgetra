package com.example.coin_budgetra

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class ChallengeAdapter(
    private val challenges: MutableList<Challenge>,
    private val onEditClicked: (challenge: Challenge, position: Int) -> Unit,
private val onTotalChanged: () -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView        = itemView.findViewById(R.id.txtChallengeName)
        val txtDescription: TextView = itemView.findViewById(R.id.txtChallengeDescription)
        val txtCategory: TextView    = itemView.findViewById(R.id.txtChallengeCategory)
        val txtDates: TextView       = itemView.findViewById(R.id.txtChallengeDates)
        val txtDaysLeft: TextView    = itemView.findViewById(R.id.txtChallengeDaysLeft)
        val txtAmounts: TextView     = itemView.findViewById(R.id.txtChallengeAmounts)
        val txtStatus: TextView      = itemView.findViewById(R.id.txtChallengeStatus)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressChallenge)
        val txtProgress: TextView    = itemView.findViewById(R.id.txtChallengeProgress)
        val btnEdit: Button          = itemView.findViewById(R.id.btnChallengeEdit)
        val btnDelete: Button        = itemView.findViewById(R.id.btnChallengeDelete)
        val editAddAmount: EditText   = itemView.findViewById(R.id.editAddAmount)
        val btnAddMoney: Button       = itemView.findViewById(R.id.btnAddMoney)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }


    override fun getItemCount(): Int = challenges.size

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }


        val endCal = Calendar.getInstance()
        try { endCal.time = dateFormat.parse(challenge.endDate) ?: Date() } catch (_: Exception) {}

        val msPerDay = 86_400_000L
        val daysLeft = ((endCal.timeInMillis - today.timeInMillis) / msPerDay).toInt()

        val isComplete = challenge.amountSaved >= challenge.budgetMax
        val isExpired  = daysLeft < 0
        val statusText: String
        val statusColor: Int

        when {
            isComplete -> { statusText = "Completed";  statusColor = Color.parseColor("#2E7D32") }
            isExpired  -> { statusText = "Expired";   statusColor = Color.parseColor("#B71C1C") }
            else       -> { statusText = "Active";    statusColor = Color.parseColor("#1565C0") }
        }

        val progressPct = if (challenge.budgetMax > 0)
            ((challenge.amountSaved.toFloat() / challenge.budgetMax) * 100).toInt().coerceIn(0, 100)
        else 0

        holder.txtName.text        = challenge.name
        holder.txtDescription.text = challenge.description.ifEmpty { "No description" }
        holder.txtCategory.text    = "Category: ${challenge.category.ifEmpty { "General" }}"
        holder.txtDates.text       = "${challenge.startDate}  →  ${challenge.endDate}"


        holder.txtDaysLeft.text = when {
            isComplete    -> "Challenge completed!"
            daysLeft == 0 -> "Last day today!"
            daysLeft > 0  -> "$daysLeft day${if (daysLeft == 1) "" 
            else 
                "s"} remaining"
            else
                -> "Ended ${-daysLeft} " +
                    "day${if (-daysLeft == 1) "" 
                    else 
                        "s"} ago"
        }

        holder.txtAmounts.text  = "Added: R${challenge.amountSaved}   |   Budget Max: R${challenge.budgetMax}"
        holder.txtStatus.text   = statusText
        holder.txtStatus.setTextColor(statusColor)
        holder.progressBar.progress = progressPct
        holder.txtProgress.text = "${progressPct}%  (R${challenge.amountSaved} / R${challenge.budgetMax})"

        val addMoneyVisibility = if (isComplete) View.GONE else View.VISIBLE

        holder.editAddAmount.visibility = addMoneyVisibility
        holder.btnAddMoney.visibility   = addMoneyVisibility
        holder.editAddAmount.setText("")  // clear recycled input
        holder.btnAddMoney.setOnClickListener {

            val pos = holder.adapterPosition

            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val input = holder.editAddAmount.text.toString().trim()
                val toAdd = input.toIntOrNull()
                val ch    = challenges[pos]

            if (input.isEmpty() || toAdd == null || toAdd <= 0) {
                Toast.makeText(holder.itemView.context, "Please enter a valid amount to add", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

            val newTotal = ch.amountSaved + toAdd

            if (newTotal > ch.budgetMax) {

                val remaining = ch.budgetMax - ch.amountSaved
                Toast.makeText(holder.itemView.context, "Only R$remaining remaining to reach the budget max", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

            ch.amountSaved = newTotal
            holder.editAddAmount.setText("")
            holder.editAddAmount.clearFocus()

            val imm = holder.itemView.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager

            imm.hideSoftInputFromWindow(holder.editAddAmount.windowToken, 0)
            notifyItemChanged(pos)
        onTotalChanged()
        }



        holder.btnEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) onEditClicked(challenges[pos], pos)
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete challenge?")
                .setMessage("""Are you sure you want to delete \"${challenge.name}"? This cannot be undone.""")
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

    fun refreshList() { notifyDataSetChanged() }
}