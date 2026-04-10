package com.example.coin_budgetra

class Expense (
    val name: String,
    val description: String,
    val category: String,
    val startDate: String,
    val endDate: String,
    val spendingLimit: Int,
    var amountAdded: Int,
    val documentUris: MutableList<String> = mutableListOf()

)