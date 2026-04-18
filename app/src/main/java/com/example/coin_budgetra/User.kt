package com.example.coin_budgetra

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

//we create a user table for the database here
@Entity(tableName = "users",
    indices = [Index(value = ["email"],unique = true)]
)


data class User (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name : String,
    val surname : String,
    val phone : String,
    val email : String,
    val password : String

)