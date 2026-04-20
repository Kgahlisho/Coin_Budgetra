package com.example.coin_budgetra


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class Challenges_dash : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChallengeAdapter
    private val challengeList = mutableListOf<Challenge>()
    private lateinit var dao: ChallengeDao

    private val addChallengeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult



        val name        = data.getStringExtra("challengeName")      ?: "Unnamed Challenge"
        val desc        = data.getStringExtra("challengeDesc")      ?: ""
        val category    = data.getStringExtra("challengeCategory")  ?: ""
        val startDate   = data.getStringExtra("challengeStartDate") ?: ""
        val endDate     = data.getStringExtra("challengeEndDate")   ?: ""
        val budgetMax   = data.getIntExtra("challengeBudgetMax", 0)
        val amountSaved = data.getIntExtra("challengeAmtSaved",  0)
        val isEdit      = data.getBooleanExtra("isEdit", false)
        val challengeId = data.getIntExtra("challengeId", -1)

        val userId = UserSession.currentUser?.id?:
            return@registerForActivityResult


        if (isEdit && challengeId >= 0) {
            val existing = challengeList.find{
                it.id == challengeId}?: return@registerForActivityResult

            val updated = existing.copy(
                name        = name,
                description = desc,
                category    = category,
                startDate   = startDate,
                endDate     = endDate,
                budgetMax   = budgetMax,
                amountSaved = amountSaved)
            lifecycleScope.launch (Dispatchers.IO){
                dao.updateChallenge(updated)
                withContext(Dispatchers.Main){
                    val idx = challengeList.indexOfFirst {
                        it.id == challengeId}
                    if (idx >= 0) challengeList[idx] = updated
                    adapter.refreshList()
                    updatedTotalSaved()
                    }
                }


        } else {
            val newChallenge = Challenge(
                userId = userId,
                name = name,
                description = desc,
                category = category.ifEmpty { "General" },
                startDate = startDate,
                endDate = endDate,
                budgetMax = budgetMax,
                amountSaved = amountSaved
            )
            lifecycleScope.launch (Dispatchers.IO) {
                dao.insertChallenge(newChallenge)
                val challenges = dao.getChallengesForUser(userId)
                withContext(Dispatchers.Main) {
                    challengeList.clear()
                    challengeList.addAll(challenges)
                    adapter.refreshList()
                    updatedTotalSaved()
                }
            }
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

        dao = UserDatabase.getDatabase(this).challengeDao()
        recyclerView = findViewById(R.id.recyclerChallenge)

        adapter = ChallengeAdapter(challengeList , { challenge, _ ->
            val intent = Intent(this, Add_challenge::class.java).apply {
                putExtra("isEdit", true)
                putExtra("challengeId",challenge.id)
                putExtra("name", challenge.name)
                putExtra("description", challenge.description)
                putExtra("category", challenge.category)
                putExtra("startDate", challenge.startDate)
                putExtra("endDate", challenge.endDate)
                putExtra("budgetMax", challenge.budgetMax)
                putExtra("amountSaved", challenge.amountSaved)
            }

            addChallengeLauncher.launch(intent)
        },{
            updatedTotalSaved()
        })



        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnBackFromChallengeDash).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreateChallengeGoal).setOnClickListener {
            addChallengeLauncher.launch(Intent(this, Add_challenge::class.java))
        }
        loadChallenges()

    }

    override fun onResume(){
        super.onResume()
        loadChallenges()
    }

    private fun loadChallenges() {
        val userId = UserSession.currentUser?.id ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val challenges = dao.getChallengesForUser(userId)
            withContext(Dispatchers.Main) {
                challengeList.clear()
                challengeList.addAll(challenges)
                adapter.refreshList()
                updatedTotalSaved()
            }
        }
    }
    private fun updatedTotalSaved(){
        val total = challengeList.sumOf{
            it.amountSaved
        }
        val txtTotal = findViewById<TextView>(R.id.txtTotalSaved)
        txtTotal.text = "Total Accumilated : R$total"
    }
}



//from the dashboard --> challenges dash --> create a new challenge
        //challenges dash is going to have all the challenges created by the user meaning the active challenges
        // also the dash will be consisted with the same flow as the personal goals where user can manipulate ,delete,edit and create a challenege
