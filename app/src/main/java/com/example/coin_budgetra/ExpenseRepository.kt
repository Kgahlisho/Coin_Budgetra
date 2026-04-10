package com.example.coin_budgetra

//This object is used as a shared expense list used by both activities refernecing instead of a static
object ExpenseRepository {
    val expenses : MutableList<Expense> = mutableListOf()
}