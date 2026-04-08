package com.example.coin_budgetra

data class Challenge(
    var name: String,
    var description: String,
    var category: String,
    var startDate: String,
    var endDate: String,
    var budgetMax: Int,
    var amountSaved: Int
)