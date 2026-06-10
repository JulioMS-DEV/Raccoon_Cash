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
}
