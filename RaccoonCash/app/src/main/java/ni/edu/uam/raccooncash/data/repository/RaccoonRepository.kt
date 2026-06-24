package ni.edu.uam.raccooncash.data.repository

import ni.edu.uam.raccooncash.data.model.*
import ni.edu.uam.raccooncash.data.remote.RetrofitClient

class RaccoonRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getAccounts() = apiService.getAccounts()

    suspend fun getAccountById(id: Long) = apiService.getAccountById(id)

    suspend fun createAccount(request: AccountRequest) = apiService.createAccount(request)

    suspend fun updateAccount(id: Long, request: AccountRequest) = apiService.updateAccount(id, request)

    suspend fun deleteAccount(id: Long) = apiService.deleteAccount(id)

    // Transactions
    suspend fun getTransactions() = apiService.getTransactions()

    suspend fun createTransaction(request: TransactionRequest) = apiService.createTransaction(request)

    suspend fun updateTransaction(id: Long, request: TransactionRequest) = apiService.updateTransaction(id, request)

    suspend fun deleteTransaction(id: Long) = apiService.deleteTransaction(id)

    // Categories
    suspend fun getCategories() = apiService.getCategories()

    suspend fun createCategory(request: ni.edu.uam.raccooncash.data.model.CategoryRequest) = apiService.createCategory(request)

    suspend fun updateCategory(id: Long, request: ni.edu.uam.raccooncash.data.model.CategoryRequest) = apiService.updateCategory(id, request)

    suspend fun deleteCategory(id: Long) = apiService.deleteCategory(id)

    // Saving Goals
    suspend fun getSavingGoals() = apiService.getSavingGoals()

    suspend fun createSavingGoal(request: SavingGoalRequest) = apiService.createSavingGoal(request)

    suspend fun updateSavingGoal(id: Long, request: SavingGoalRequest) = apiService.updateSavingGoal(id, request)

    suspend fun deleteSavingGoal(id: Long) = apiService.deleteSavingGoal(id)

    suspend fun getSavingGoalTransactions(id: Long) = apiService.getSavingGoalTransactions(id)

    // Budgets
    suspend fun getBudgets() = apiService.getBudgets()

    suspend fun getBudgetCategoryLimits(id: Long) = apiService.getBudgetCategoryLimits(id)

    suspend fun createBudgetCategoryLimit(id: Long, request: BudgetCategoryLimitRequest) = apiService.createBudgetCategoryLimit(id, request)

    suspend fun updateBudgetCategoryLimit(budgetId: Long, limitId: Long, request: BudgetCategoryLimitRequest) = apiService.updateBudgetCategoryLimit(budgetId, limitId, request)

    suspend fun deleteBudgetCategoryLimit(budgetId: Long, limitId: Long) = apiService.deleteBudgetCategoryLimit(budgetId, limitId)

    suspend fun createBudget(request: PresupuestoSolicitud) = apiService.createBudget(request)

    suspend fun updateBudget(id: Long, request: PresupuestoSolicitud) = apiService.updateBudget(id, request)

    suspend fun deleteBudget(id: Long) = apiService.deleteBudget(id)
}
