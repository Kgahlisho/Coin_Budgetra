package com.example.coin_budgetra

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GoalDao {

    @Insert
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId")
    suspend fun getGoalsForUser(userId: Int): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Int): Goal?

    @Query("SELECT * FROM goals WHERE userId = :userId AND savedAmount >= targetAmount")
    suspend fun getCompletedGoals(userId: Int): List<Goal>

    @Query("SELECT SUM(savedAmount) FROM goals WHERE userId = :userId")
    suspend fun getTotalSavedForUser(userId: Int): Int?
}