package ni.edu.uam.raccooncash.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.AccountRequest
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository

class AccountsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addAccountSuccess = MutableStateFlow(false)
    val addAccountSuccess: StateFlow<Boolean> = _addAccountSuccess.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _accounts.value = repository.getAccounts()
                _transactions.value = repository.getTransactions()
            } catch (e: Exception) {
                _error.value = "No se pudo conectar con la API. Revisa que el backend esté encendido."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAccount(name: String, balance: Double, currency: String, color: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _addAccountSuccess.value = false
            try {
                val request = AccountRequest(
                    name = name,
                    type = "BANK", // Defaulting to BANK for now as per logic
                    initialBalance = balance,
                    currency = currency,
                    color = color
                )
                repository.createAccount(request)
                _addAccountSuccess.value = true
                loadAccounts()
            } catch (e: Exception) {
                _error.value = "Error al crear la cuenta."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _addAccountSuccess.value = false
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAccount(id)
                loadAccounts()
            } catch (e: Exception) {
                _error.value = "Error al eliminar la cuenta."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
