package com.example.coin_budgetra

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(

    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
    val userId: Int,

    var name: String,
    var description: String,
    var category: String,
    var startDate: String,
    var endDate: String,
    var budgetMax: Int,
    var amountSaved: Int
)