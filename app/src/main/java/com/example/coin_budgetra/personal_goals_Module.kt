package com.example.coin_budgetra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        val btnBackdash = this.findViewById<Button>(R.id.button16)

        btnBackdash.setOnClickListener{
        val intent = Intent(this, Dashboard_Module::class.java)
        startActivity(intent);

        }



        val btnCancell = this.findViewById<Button>(R.id.button18)

        btnCancell.setOnClickListener {
            val intent = Intent(this, Dashboard_Module::class.java)
            startActivity(intent);
        }


    }


override fun onActivityResult(requestCode: Int , resultCode:Int ,data: Intent?){
    super.onActivityResult(requestCode,resultCode,data)


if (requestCode ==1 && resultCode == Activity.RESULT_OK && data !=null) {

    val name = data.getStringExtra("goalName")
    val amount = data.getStringExtra("goalAmount")

    val txtGoalName = this.findViewById<TextView>(R.id.txtGoalName)
    val txtGoalTarget = this.findViewById<TextView>(R.id.txtGoalTarget)
    val progressBar = this.findViewById<ProgressBar>(R.id.progressGoal)
    val txtProgress = this.findViewById<TextView>(R.id.txtProgress)

    txtGoalName.text = name
    txtGoalTarget.text = "Target: R$amount"

    progressBar.progress = 0
    txtProgress.text = "R0 saved"
}
}
}
