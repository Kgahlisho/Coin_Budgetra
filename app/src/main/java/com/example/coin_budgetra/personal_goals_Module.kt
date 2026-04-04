package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

var savedAmount =0
var targetAmount =0

class personal_goals_Module : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_goals_module)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val inputAddAmount = this.findViewById<EditText>(R.id.inputAddAmount)
        val buttonAddMoney = this.findViewById<Button>(R.id.buttonAddMoney)
        val progressBar = this.findViewById<ProgressBar>(R.id.progressGoal)
        val txtProgress = this.findViewById<TextView>(R.id.txtProgress)

        buttonAddMoney.setOnClickListener {
            if (targetAmount == 0){
                Toast.makeText(this, "Create a goal first" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val addValue = inputAddAmount.text.toString()

            if(addValue.isEmpty()){
                Toast.makeText(this, "Enter amount" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountToAdd = try {
                addValue.toInt()

            }catch(e:Exception){
                Toast.makeText(this, "Invalid number" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savedAmount += amountToAdd

            if (savedAmount > targetAmount){
                savedAmount = targetAmount
            }

            val progress = (savedAmount * 100) / targetAmount

            progressBar.progress = progress
            txtProgress.text = "R$savedAmount saved"

            inputAddAmount.text.clear()
        }



        val btnBackdash = this.findViewById<Button>(R.id.button16)

        btnBackdash.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);

        }

        val btnAddGoal = this.findViewById<Button>(R.id.buttonAddGoal)
        btnAddGoal.setOnClickListener {
            val intent = Intent(this, Add_goal::class.java)
            startActivityForResult(intent, 1)
        }


        val btnCancell = this.findViewById<Button>(R.id.button18)

        btnCancell.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            val name = data.getStringExtra("goalName")
            val amountStr = data.getStringExtra("goalAmount")

            targetAmount = amountStr!!.toInt()
            savedAmount = 0

            val txtGoalName = findViewById<TextView>(R.id.txtGoalName)
            val txtGoalTarget = findViewById<TextView>(R.id.txtGoalTarget)
            val progressBar = findViewById<ProgressBar>(R.id.progressGoal)
            val txtProgress = findViewById<TextView>(R.id.txtProgress)

            txtGoalName.text = name
            txtGoalTarget.text = "Target: R$amountStr"

            progressBar.progress = 0
            txtProgress.text = "R0 saved"
        }
    }
}