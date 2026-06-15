package com.example.coin_budgetra

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    // USER
    suspend fun saveUser(user: User) {
        val doc = mapOf(
            "id"      to user.id,
            "name"    to user.name,
            "surname" to user.surname,
            "phone"   to user.phone,
            "email"   to user.email
        )
        db.collection("users")
            .document(user.id.toString())
            .set(doc, SetOptions.merge())
            .await()
    }

    // EXPENSES
    suspend fun saveExpense(expense: Expense) {
        db.collection("users")
            .document(expense.userId.toString())
            .collection("expenses")
            .document(expense.id.toString())
            .set(expenseToMap(expense), SetOptions.merge())
            .await()
    }

    suspend fun deleteExpense(expense: Expense) {
        db.collection("users")
            .document(expense.userId.toString())
            .collection("expenses")
            .document(expense.id.toString())
            .delete().await()
    }

    private fun expenseToMap(e: Expense) = mapOf(
        "id" to e.id, "userId" to e.userId, "name" to e.name,
        "description" to e.description, "category" to e.category,
        "startDate" to e.startDate, "endDate" to e.endDate,
        "spendingLimit" to e.spendingLimit, "amountAdded" to e.amountAdded
    )

    // GOALS

    suspend fun saveGoal(goal: Goal) {
        db.collection("users")
            .document(goal.userId.toString())
            .collection("goals")
            .document(goal.id.toString())
            .set(goalToMap(goal), SetOptions.merge())
            .await()
    }

    suspend fun deleteGoal(goal: Goal) {
        db.collection("users")
            .document(goal.userId.toString())
            .collection("goals")
            .document(goal.id.toString())
            .delete().await()
    }

    private fun goalToMap(g: Goal) = mapOf(
        "id" to g.id, "userId" to g.userId, "name" to g.name,
        "description" to g.description, "category" to g.category,
        "targetAmount" to g.targetAmount, "savedAmount" to g.savedAmount
    )

    // CHALLENGES
    suspend fun saveChallenge(challenge: Challenge) {
        db.collection("users")
            .document(challenge.userId.toString())
            .collection("challenges")
            .document(challenge.id.toString())
            .set(challengeToMap(challenge), SetOptions.merge())
            .await()
    }

    suspend fun deleteChallenge(challenge: Challenge) {
        db.collection("users")
            .document(challenge.userId.toString())
            .collection("challenges")
            .document(challenge.id.toString())
            .delete().await()
    }

    private fun challengeToMap(c: Challenge) = mapOf(
        "id" to c.id, "userId" to c.userId, "name" to c.name,
        "description" to c.description, "category" to c.category,
        "startDate" to c.startDate, "endDate" to c.endDate,
        "budgetMax" to c.budgetMax, "amountSaved" to c.amountSaved
    )

    // SYNC ON LOGIN
    suspend fun syncFromFirebase(
        userId: Int,
        expenseDao: ExpenseDao,
        goalDao: GoalDao,
        challengeDao: ChallengeDao
    ) {
        val userRef = db.collection("users").document(userId.toString())

        // Expenses
        val localExpenseIds = expenseDao.getExpensesForUser(userId).map { it.id }.toSet()
        for (doc in userRef.collection("expenses").get().await().documents) {
            val id = (doc.getLong("id") ?: continue).toInt()
            if (id in localExpenseIds) continue
            expenseDao.insertExpense(Expense(
                id = id,
                userId = (doc.getLong("userId") ?: userId.toLong()).toInt(),
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "",
                startDate = doc.getString("startDate") ?: "",
                endDate = doc.getString("endDate") ?: "",
                spendingLimit = (doc.getLong("spendingLimit") ?: 0).toInt(),
                amountAdded = (doc.getLong("amountAdded") ?: 0).toInt()
            ))
        }

        // Goals
        val localGoalIds = goalDao.getGoalsForUser(userId).map { it.id }.toSet()
        for (doc in userRef.collection("goals").get().await().documents) {
            val id = (doc.getLong("id") ?: continue).toInt()
            if (id in localGoalIds) continue
            goalDao.insertGoal(Goal(
                id = id,
                userId = (doc.getLong("userId") ?: userId.toLong()).toInt(),
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "",
                targetAmount = (doc.getLong("targetAmount") ?: 0).toInt(),
                savedAmount = (doc.getLong("savedAmount") ?: 0).toInt()
            ))
        }

        // Challenges
        val localChallengeIds = challengeDao.getChallengesForUser(userId).map { it.id }.toSet()
        for (doc in userRef.collection("challenges").get().await().documents) {
            val id = (doc.getLong("id") ?: continue).toInt()
            if (id in localChallengeIds) continue
            challengeDao.insertChallenge(Challenge(
                id = id,
                userId = (doc.getLong("userId") ?: userId.toLong()).toInt(),
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "",
                startDate = doc.getString("startDate") ?: "",
                endDate = doc.getString("endDate") ?: "",
                budgetMax = (doc.getLong("budgetMax") ?: 0).toInt(),
                amountSaved = (doc.getLong("amountSaved") ?: 0).toInt()
            ))
        }
    }
}