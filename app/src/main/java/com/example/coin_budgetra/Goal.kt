package com.example.coin_budgetra

data class Goal(
        var name: String,
        var description: String,
        var category: String,
        var targetAmount: Int,
        var savedAmount: Int = 0

){
// Derived — no extra field needed, always accurate
val isCompleted: Boolean get() = targetAmount > 0 && savedAmount >= targetAmount
}