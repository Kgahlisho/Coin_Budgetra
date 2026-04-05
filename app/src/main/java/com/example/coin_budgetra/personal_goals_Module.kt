package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class personal_goals_Module : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter
    private val goalsList = mutableListOf<Goal>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_goals_module)

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        recyclerView = this.findViewById<RecyclerView>(R.id.recyclerGoals)
        //val goalsList = mutableListOf<Goal>()
        adapter = GoalAdapter(goalsList)



        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        this.findViewById<Button>(R.id.button16).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }

        this.findViewById<Button>(R.id.buttonAddGoal).setOnClickListener {
            startActivityForResult(Intent(this, Add_goal::class.java), 1)
        }

        this.findViewById<Button>(R.id.button18).setOnClickListener {
            startActivity(Intent(this, Dashboard_Module::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            val name = data.getStringExtra("goalName") ?:"Unnamed Goal"
            val amountStr = data.getStringExtra("goalAmount") ?: "0"

            val amount = try{
                amountStr.toInt()
            }catch (e: Exception){
                Toast.makeText(this,"Invalid amount" , Toast.LENGTH_SHORT).show()
                return
            }

            val newGoal = Goal(
                name = name,
                description = "No description",
                category = "General",
                targetAmount = amountStr.toInt()
            )
            goalsList.add(newGoal)
            adapter.notifyItemInserted(goalsList.size - 1)
        }
    }
}
