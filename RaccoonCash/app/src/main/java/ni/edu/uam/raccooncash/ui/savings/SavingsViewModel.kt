package ni.edu.uam.raccooncash.ui.savings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.SavingGoalRequest
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.model.TransactionRequest
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SavingsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private companion object {
        const val LogTag = "SavingsFlow"
    }

    private val _savingGoals = MutableStateFlow<List<SavingGoalResponse>>(emptyList())
    val savingGoals: StateFlow<List<SavingGoalResponse>> = _savingGoals.asStateFlow()

    private val _currentGoalTransactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val currentGoalTransactions: StateFlow<List<TransactionResponse>> = _currentGoalTransactions.asStateFlow()

    private val _addGoalSuccess = MutableStateFlow(false)
    val addGoalSuccess: StateFlow<Boolean> = _addGoalSuccess.asStateFlow()
    
    private val _addTransactionSuccess = MutableStateFlow(false)
    val addTransactionSuccess: StateFlow<Boolean> = _addTransactionSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSavingGoals()
    }

    fun loadSavingGoals() {
        viewModelScope.launch {
            loadSavingGoalsSuspend()
        }
    }

    fun clearSessionData() {
        _savingGoals.value = emptyList()
        _currentGoalTransactions.value = emptyList()
        _addGoalSuccess.value = false
        _addTransactionSuccess.value = false
        _isLoading.value = false
        _error.value = null
    }

    private suspend fun loadSavingGoalsSuspend() {
        _isLoading.value = true
        try {
            _savingGoals.value = repository.getSavingGoals()
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al cargar metas: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun loadGoalTransactions(goalId: Long) {
        viewModelScope.launch {
            loadGoalTransactionsSuspend(goalId)
        }
    }

    private suspend fun loadGoalTransactionsSuspend(goalId: Long) {
        _isLoading.value = true
        try {
            _currentGoalTransactions.value = repository.getSavingGoalTransactions(goalId)
                .map { it.withSavingGoalId(goalId) }
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al cargar transacciones: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun addSavingGoal(name: String, targetAmount: Double, deadline: String, color: String, icon: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _addGoalSuccess.value = false
            _error.value = null
            try {
                Log.d(LogTag, "addSavingGoal input name='$name', targetAmount=$targetAmount, deadline=$deadline, color=$color, icon=$icon")
                val request = SavingGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline,
                    color = color,
                    icon = icon
                )
                Log.d(LogTag, "addSavingGoal request=$request")
                val savedGoal = repository.createSavingGoal(request)
                Log.d(LogTag, "addSavingGoal saved id=${savedGoal.id}")
                loadSavingGoalsSuspend()
                _addGoalSuccess.value = true
            } catch (e: HttpException) {
                _error.value = httpErrorMessage(e, "Error al crear meta")
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al crear meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSavingGoal(id: Long, name: String, targetAmount: Double, deadline: String, color: String, icon: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _addGoalSuccess.value = false // Reset before start
            _error.value = null
            try {
                Log.d(LogTag, "updateSavingGoal input id=$id, name='$name', targetAmount=$targetAmount, deadline=$deadline, color=$color, icon=$icon")
                val request = SavingGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline,
                    color = color,
                    icon = icon
                )
                Log.d(LogTag, "updateSavingGoal request=$request")
                repository.updateSavingGoal(id, request)
                loadSavingGoalsSuspend()
                _addGoalSuccess.value = true // Trigger navigation
            } catch (e: HttpException) {
                _error.value = httpErrorMessage(e, "Error al actualizar meta")
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al actualizar meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSavingGoal(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSavingGoal(id)
                _addGoalSuccess.value = true
                loadSavingGoalsSuspend()
            } catch (e: Exception) {
                _error.value = "Error al eliminar meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTransactionToGoal(
        goalId: Long,
        accountId: Long,
        amount: Double,
        description: String,
        notes: String?,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = "TRANSFER",
                    accountId = accountId,
                    description = description,
                    notes = notes,
                    date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    savingGoalId = goalId
                )
                Log.d(LogTag, "addTransactionToGoal request=$request")
                val savedTransaction = repository.createTransaction(request).withSavingGoalId(goalId)
                Log.d(LogTag, "addTransactionToGoal saved id=${savedTransaction.id}, savingGoalId=${savedTransaction.savingGoalId}")
                _currentGoalTransactions.value = listOf(savedTransaction) + _currentGoalTransactions.value.filter { it.id != savedTransaction.id }
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: HttpException) {
                _error.value = httpErrorMessage(e, "Error al registrar ahorro")
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al registrar ahorro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGoalTransaction(
        transactionId: Long,
        goalId: Long,
        accountId: Long,
        amount: Double,
        description: String,
        notes: String?,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = "TRANSFER",
                    accountId = accountId,
                    description = description,
                    notes = notes,
                    date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    savingGoalId = goalId
                )
                val updatedTransaction = repository.updateTransaction(transactionId, request).withSavingGoalId(goalId)
                _currentGoalTransactions.value = _currentGoalTransactions.value.map {
                    if (it.id == updatedTransaction.id) updatedTransaction else it
                }
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: HttpException) {
                _error.value = httpErrorMessage(e, "Error al actualizar ahorro")
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al actualizar ahorro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(transactionId: Long, goalId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                repository.deleteTransaction(transactionId)
                // Clear the current list immediately to give instant feedback
                _currentGoalTransactions.value = _currentGoalTransactions.value.filter { it.id != transactionId }
                
                // Then fetch from server to be sure
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: HttpException) {
                _error.value = httpErrorMessage(e, "Error al eliminar transacción")
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al eliminar transacción: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun resetSuccess() {
        _addGoalSuccess.value = false
        _addTransactionSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    private fun TransactionResponse.withSavingGoalId(goalId: Long): TransactionResponse {
        return if (savingGoalId == goalId) this else copy(savingGoalId = goalId)
    }

    private fun httpErrorMessage(error: HttpException, fallback: String): String {
        val message = error.response()?.errorBody()?.string()?.let { body ->
            runCatching { JSONObject(body).optString("message") }.getOrNull()
                ?.takeIf { it.isNotBlank() }
        }
        return message ?: "$fallback (${error.code()})"
    }
}
