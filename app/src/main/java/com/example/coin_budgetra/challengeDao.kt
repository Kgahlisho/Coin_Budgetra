package com.example.coin_budgetra

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChallengeDao {

    @Insert
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    @Query("SELECT * FROM challenges WHERE userId = :userId")
    suspend fun getChallengesForUser(userId: Int): List<Challenge>

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallengeById(id: Int): Challenge?

    @Query("SELECT SUM(amountSaved) FROM challenges WHERE userId = :userId")
    suspend fun getTotalSavedForUser(userId: Int): Int?
}