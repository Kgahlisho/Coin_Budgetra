package com.example.coin_budgetra

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity (tableName = "goals")

data class Goal(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val userId: Int,
        var name: String,
        var description: String,
        var category: String,
        var targetAmount: Int,
        var savedAmount: Int = 0

){
// Derived — no extra field needed, always accurate
val isCompleted: Boolean get() = targetAmount > 0 && savedAmount >= targetAmount
}