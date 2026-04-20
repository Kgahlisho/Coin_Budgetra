package com.example.coin_budgetra



import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun getExpensesForUser(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("SELECT SUM(amountAdded) FROM expenses WHERE userId = :userId")
    suspend fun getTotalSpentForUser(userId: Int): Int?

    @Query("SELECT SUM(spendingLimit) FROM expenses WHERE userId = :userId")
    suspend fun getTotalBudgetForUser(userId: Int): Int?
}
