package com.example.coin_budgetra

import androidx.room.Entity
import androidx.room.PrimaryKey


//changing the data class in order to be saved in the database

@Entity(tableName = "expenses")//create the tablename for the tble

data class Expense (

    @PrimaryKey(autoGenerate = true)

    val id: Int = 0,
    val userId: Int, //every user will have a id


    val name: String,
    val description: String,
    val category: String,
    val startDate: String,
    val endDate: String,
    val spendingLimit: Int,
    var amountAdded: Int,
    // val documentUris: MutableList<String> = mutableListOf()

)