package ni.edu.uam.raccooncash.ui.budgets

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.ui.accounts.accountColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    viewModel: BudgetsViewModel,
    budgetToEdit: PresupuestoRespuesta? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var nombre by remember { mutableStateOf(budgetToEdit?.nombre ?: "") }
    var monto by remember { mutableStateOf(budgetToEdit?.monto?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var esGasto by remember { mutableStateOf(budgetToEdit?.esGasto ?: true) }
    var valorPeriodo by remember { mutableStateOf(budgetToEdit?.valorPeriodo?.toString() ?: "1") }
    var tipoPeriodo by remember { mutableStateOf(budgetToEdit?.tipoPeriodo ?: TipoPeriodoPresupuesto.MENSUAL) }
    
    val initialDate = if (budgetToEdit != null) {
        try { LocalDate.parse(budgetToEdit.fechaInicio, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { LocalDate.now() }
    } else LocalDate.now()
    var fechaInicio by remember { mutableStateOf(initialDate) }
    
    val initialColor = if (budgetToEdit != null) {
        try { Color(android.graphics.Color.parseColor(budgetToEdit.color)) } catch (e: Exception) { accountColors[0] }
    } else accountColors[0]
    var selectedColor by remember { mutableStateOf(initialColor) }
    
    var incluirTodasLasTransacciones by remember { mutableStateOf(budgetToEdit?.incluirTodasLasTransacciones ?: false) }
    
    val success by viewModel.operationSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaInicio = LocalDate.of(year, month + 1, dayOfMonth)
            },
            fechaInicio.year,
            fechaInicio.monthValue - 1,
            fechaInicio.dayOfMonth
        )
    }

    // Dynamic period description
    val periodLabel = when (tipoPeriodo) {
        TipoPeriodoPresupuesto.DIARIO -> if (valorPeriodo == "1") "día" else "días"
        TipoPeriodoPresupuesto.SEMANAL -> if (valorPeriodo == "1") "semana" else "semanas"
        TipoPeriodoPresupuesto.MENSUAL -> if (valorPeriodo == "1") "mes" else "meses"
        TipoPeriodoPresupuesto.ANUAL -> if (valorPeriodo == "1") "año" else "años"
        else -> "período"
    }
    
    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (budgetToEdit != null) "Editar presupuesto" else "Añadir presupuesto", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help info */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                    if (budgetToEdit != null) {
                        IconButton(onClick = { 
                            viewModel.deleteBudget(budgetToEdit.id)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = if (nombre.isNotEmpty() && monto.isNotEmpty() && !isLoading) Color(0xFFD1C4E9) else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (nombre.isNotEmpty() && monto.isNotEmpty() && !isLoading) {
                        val argb = selectedColor.toArgb()
                        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
                        
                        val finalMonto = monto.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val finalValorPeriodo = valorPeriodo.toIntOrNull() ?: 1

                        if (budgetToEdit != null) {
                            viewModel.updateBudget(
                                id = budgetToEdit.id,
                                nombre = nombre,
                                monto = finalMonto,
                                tipoPeriodo = tipoPeriodo,
                                valorPeriodo = finalValorPeriodo,
                                fechaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                color = colorHex,
                                esGasto = esGasto,
                                incluirTodasLasTransacciones = incluirTodasLasTransacciones
                            )
                        } else {
                            viewModel.createBudget(
                                nombre = nombre,
                                monto = finalMonto,
                                tipoPeriodo = tipoPeriodo,
                                valorPeriodo = finalValorPeriodo,
                                fechaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                color = colorHex,
                                esGasto = esGasto,
                                incluirTodasLasTransacciones = incluirTodasLasTransacciones
                            )
                        }
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text(
                            text = if (nombre.isEmpty() || monto.isEmpty()) "Completa los campos" else "Guardar presupuesto",
                            color = if (nombre.isNotEmpty() && monto.isNotEmpty()) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Expense/Saving Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(4.dp)
            ) {
                BudgetTab(
                    text = "Presupuesto De Gastos",
                    isSelected = esGasto,
                    color = Color(0xFFE57373),
                    modifier = Modifier.weight(1f),
                    onClick = { esGasto = true }
                )
                BudgetTab(
                    text = "Presupuesto De Ahorro",
                    isSelected = !esGasto,
                    color = Color(0xFF81C784),
                    modifier = Modifier.weight(1f),
                    onClick = { esGasto = false }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Central Card UI
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Name input
                    BasicTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (nombre.isEmpty()) {
                                Text(
                                    "Nombre",
                                    color = Color.Gray,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                    
                    Box(modifier = Modifier.width(100.dp).height(2.dp).background(Color.Gray.copy(alpha = 0.5f)))
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount and Period
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("C$", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        BasicTextField(
                            value = monto,
                            onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) monto = it },
                            modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 20.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (monto.isEmpty()) {
                                    Text("0", color = Color.Gray, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                }
                                innerTextField()
                            }
                        )
                        Text("/", fontSize = 28.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                        BasicTextField(
                            value = valorPeriodo,
                            onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) valorPeriodo = it },
                            modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 20.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = periodLabel,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { 
                                tipoPeriodo = when(tipoPeriodo) {
                                    TipoPeriodoPresupuesto.DIARIO -> TipoPeriodoPresupuesto.SEMANAL
                                    TipoPeriodoPresupuesto.SEMANAL -> TipoPeriodoPresupuesto.MENSUAL
                                    TipoPeriodoPresupuesto.MENSUAL -> TipoPeriodoPresupuesto.ANUAL
                                    else -> TipoPeriodoPresupuesto.DIARIO
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("comienzo", color = Color.Gray, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fechaInicio.format(DateTimeFormatter.ofPattern("d MMM", Locale("es"))),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Period Summary
                    val valP = valorPeriodo.toLongOrNull() ?: 1L
                    val endDate = remember(fechaInicio, valP, tipoPeriodo) {
                        when (tipoPeriodo) {
                            TipoPeriodoPresupuesto.DIARIO -> fechaInicio.plusDays(valP)
                            TipoPeriodoPresupuesto.SEMANAL -> fechaInicio.plusWeeks(valP)
                            TipoPeriodoPresupuesto.MENSUAL -> fechaInicio.plusMonths(valP)
                            TipoPeriodoPresupuesto.ANUAL -> fechaInicio.plusYears(valP)
                            else -> fechaInicio.plusMonths(valP)
                        }.minusDays(1)
                    }
                    val days = remember(fechaInicio, endDate) { ChronoUnit.DAYS.between(fechaInicio, endDate) + 1 }
                    val dailyAmount = remember(monto, days) { (monto.toDoubleOrNull() ?: 0.0) / days }
                    
                    Text(
                        text = "Período actual: ${fechaInicio.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))} – ${endDate.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "C$${String.format(Locale.getDefault(), "%.2f", dailyAmount)}/día para $days días",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Color Palette
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                    }
                }
                items(accountColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetTab(
    text: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF454B57) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
