package ni.edu.uam.raccooncash.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.PresupuestoSolicitud
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository

class BudgetsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _budgets = MutableStateFlow<List<PresupuestoRespuesta>>(emptyList())
    val budgets: StateFlow<List<PresupuestoRespuesta>> = _budgets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _budgets.value = repository.getBudgets()
            } catch (e: Exception) {
                _error.value = "Error al cargar presupuestos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBudget(
        nombre: String,
        monto: Double,
        tipoPeriodo: TipoPeriodoPresupuesto,
        valorPeriodo: Int,
        fechaInicio: String,
        color: String,
        esGasto: Boolean,
        incluirTodasLasTransacciones: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = PresupuestoSolicitud(
                    nombre = nombre,
                    monto = monto,
                    tipoPeriodo = tipoPeriodo,
                    valorPeriodo = valorPeriodo,
                    fechaInicio = fechaInicio,
                    color = color,
                    esGasto = esGasto,
                    incluirTodasLasTransacciones = incluirTodasLasTransacciones
                )
                repository.createBudget(request)
                _operationSuccess.value = true
                loadBudgets()
            } catch (e: Exception) {
                _error.value = "Error al crear presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBudget(
        id: Long,
        nombre: String,
        monto: Double,
        tipoPeriodo: TipoPeriodoPresupuesto,
        valorPeriodo: Int,
        fechaInicio: String,
        color: String,
        esGasto: Boolean,
        incluirTodasLasTransacciones: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = PresupuestoSolicitud(
                    nombre = nombre,
                    monto = monto,
                    tipoPeriodo = tipoPeriodo,
                    valorPeriodo = valorPeriodo,
                    fechaInicio = fechaInicio,
                    color = color,
                    esGasto = esGasto,
                    incluirTodasLasTransacciones = incluirTodasLasTransacciones
                )
                repository.updateBudget(id, request)
                _operationSuccess.value = true
                loadBudgets()
            } catch (e: Exception) {
                _error.value = "Error al actualizar presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteBudget(id)
                if (response.isSuccessful) {
                    _operationSuccess.value = true
                    loadBudgets()
                } else {
                    _error.value = "Error al eliminar: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _operationSuccess.value = false
    }
}
