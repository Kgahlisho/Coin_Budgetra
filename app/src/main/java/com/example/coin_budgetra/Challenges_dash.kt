package com.example.coin_budgetra


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Challenges_dash : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChallengeAdapter
    private val challengeList = mutableListOf<Challenge>()

    private val addChallengeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult



        val name        = data.getStringExtra("challengeName")      ?: "Unnamed Challenge"
        val desc        = data.getStringExtra("challengeDesc")      ?: ""
        val category    = data.getStringExtra("challengeCategory")  ?: ""
        val startDate   = data.getStringExtra("challengeStartDate") ?: ""
        val endDate     = data.getStringExtra("challengeEndDate")   ?: ""
        val budgetMax   = data.getIntExtra("challengeBudgetMax", 0)
        val amountSaved = data.getIntExtra("challengeAmtSaved",  0)
        val isEdit      = data.getBooleanExtra("isEdit", false)
        val position    = data.getIntExtra("position", -1)

        if (isEdit && position >= 0 && position < challengeList.size) {
            challengeList[position].apply {
                this.name        = name
                this.description = desc
                this.category    = category
                this.startDate   = startDate
                this.endDate     = endDate
                this.budgetMax   = budgetMax
                this.amountSaved = amountSaved
            }

            adapter.refreshList()
        } else {
            challengeList.add(
                Challenge(
                    name        = name,
                    description = desc,
                    category    = category.ifEmpty { "General" },
                    startDate   = startDate,
                    endDate     = endDate,
                    budgetMax   = budgetMax,
                    amountSaved = amountSaved
                )
            )
            adapter.notifyItemInserted(challengeList.size - 1)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_challenge_dash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerChallenge)

        adapter = ChallengeAdapter(challengeList) { challenge, position ->
            val intent = Intent(this, Add_challenge::class.java).apply {
                putExtra("isEdit",      true)
                putExtra("position",    position)
                putExtra("name",        challenge.name)
                putExtra("description", challenge.description)
                putExtra("category",    challenge.category)
                putExtra("startDate",   challenge.startDate)
                putExtra("endDate",     challenge.endDate)
                putExtra("budgetMax",   challenge.budgetMax)
                putExtra("amountSaved", challenge.amountSaved)
            }
            addChallengeLauncher.launch(intent)
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnBackFromChallengeDash).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreateChallengeGoal).setOnClickListener {
            addChallengeLauncher.launch(Intent(this, Add_challenge::class.java))
        }
    }
}



//from the dashboard --> challenges dash --> create a new challenge
        //challenges dash is going to have all the challenges created by the user meaning the active challenges
        // also the dash will be consisted with the same flow as the personal goals where user can manipulate ,delete,edit and create a challenege
