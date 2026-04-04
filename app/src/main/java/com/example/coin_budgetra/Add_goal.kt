package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Add_goal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_goal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val btnbackk = this.findViewById<Button>(R.id.button33)
            btnbackk.setOnClickListener {
            val intent = Intent (this, personal_goals_Module::class.java)
            startActivity(intent);
        }


        val goalName = this.findViewById<EditText>(R.id.editTextText17)
        val goalAmount = this.findViewById<EditText>(R.id.editTextText18)
        val saveBtn = this.findViewById<Button>(R.id.button19)

        saveBtn.setOnClickListener {
            val name = goalName.text.toString()
            val amount = goalAmount.text.toString()

            if (name.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }

            //sends back data
            val intent = Intent()
            intent.putExtra("goalName", name)
            intent.putExtra("goalAmount", amount)

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
