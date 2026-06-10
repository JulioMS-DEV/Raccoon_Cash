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
}
