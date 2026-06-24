package ni.edu.uam.raccooncash.data.remote

import ni.edu.uam.raccooncash.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("accounts")
    suspend fun getAccounts(): List<AccountResponse>

    @GET("accounts/{id}")
    suspend fun getAccountById(@Path("id") id: Long): AccountResponse

    @POST("accounts")
    suspend fun createAccount(@Body request: AccountRequest): AccountResponse

    @PUT("accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Long, @Body request: AccountRequest): AccountResponse

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: Long): Response<Unit>

    // Transactions
    @GET("transactions")
    suspend fun getTransactions(): List<TransactionResponse>

    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionResponse

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Long, @Body request: TransactionRequest): TransactionResponse

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Long): Response<Unit>

    // Categories
    @GET("categories")
    suspend fun getCategories(): List<CategoryResponse>

    @POST("categories")
    suspend fun createCategory(@Body category: ni.edu.uam.raccooncash.data.model.CategoryRequest): CategoryResponse

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: ni.edu.uam.raccooncash.data.model.CategoryRequest): CategoryResponse

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<Unit>

    // Saving Goals
    @GET("saving-goals")
    suspend fun getSavingGoals(): List<SavingGoalResponse>

    @POST("saving-goals")
    suspend fun createSavingGoal(@Body request: SavingGoalRequest): SavingGoalResponse

    @PUT("saving-goals/{id}")
    suspend fun updateSavingGoal(@Path("id") id: Long, @Body request: SavingGoalRequest): SavingGoalResponse

    @DELETE("saving-goals/{id}")
    suspend fun deleteSavingGoal(@Path("id") id: Long): Response<Unit>

    @GET("saving-goals/{id}/transactions")
    suspend fun getSavingGoalTransactions(@Path("id") id: Long): List<TransactionResponse>

    // Budgets
    @GET("presupuestos")
    suspend fun getBudgets(): List<PresupuestoRespuesta>

    @GET("presupuestos/{id}/categories")
    suspend fun getBudgetCategoryLimits(@Path("id") id: Long): List<BudgetCategoryLimitResponse>

    @POST("presupuestos/{id}/categories")
    suspend fun createBudgetCategoryLimit(
        @Path("id") id: Long,
        @Body request: BudgetCategoryLimitRequest
    ): BudgetCategoryLimitResponse

    @PUT("presupuestos/{budgetId}/categories/{limitId}")
    suspend fun updateBudgetCategoryLimit(
        @Path("budgetId") budgetId: Long,
        @Path("limitId") limitId: Long,
        @Body request: BudgetCategoryLimitRequest
    ): BudgetCategoryLimitResponse

    @DELETE("presupuestos/{budgetId}/categories/{limitId}")
    suspend fun deleteBudgetCategoryLimit(
        @Path("budgetId") budgetId: Long,
        @Path("limitId") limitId: Long
    ): Response<Unit>

    @POST("presupuestos")
    suspend fun createBudget(@Body request: PresupuestoSolicitud): PresupuestoRespuesta

    @PUT("presupuestos/{id}")
    suspend fun updateBudget(@Path("id") id: Long, @Body request: PresupuestoSolicitud): PresupuestoRespuesta

    @DELETE("presupuestos/{id}")
    suspend fun deleteBudget(@Path("id") id: Long): Response<Unit>
}
