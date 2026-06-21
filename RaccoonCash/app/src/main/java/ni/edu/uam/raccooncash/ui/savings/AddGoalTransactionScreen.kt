package ni.edu.uam.raccooncash.ui.savings

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountChip
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalTransactionScreen(
    goal: SavingGoalResponse,
    savingsViewModel: SavingsViewModel,
    accountsViewModel: AccountsViewModel,
    transactionToEdit: ni.edu.uam.raccooncash.data.model.TransactionResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by accountsViewModel.accounts.collectAsState()
    val isLoading by savingsViewModel.isLoading.collectAsState()
    val success by savingsViewModel.addTransactionSuccess.collectAsState()

    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(transactionToEdit?.description ?: "Ahorro para ${goal.name}") }
    var notes by remember { mutableStateOf(transactionToEdit?.notes ?: "") }
    var selectedAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.accountId) }
    
    val initialDate = if (transactionToEdit?.date != null) {
        try { LocalDateTime.parse(transactionToEdit.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate() } catch (e: Exception) { LocalDate.now() }
    } else LocalDate.now()
    var selectedDate by remember { mutableStateOf(initialDate) }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    LaunchedEffect(success) {
        if (success) {
            accountsViewModel.loadAccounts()
            savingsViewModel.resetSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionToEdit != null) "Editar ahorro" else "Agregar transacción") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (transactionToEdit != null) {
                        IconButton(onClick = { 
                            savingsViewModel.deleteTransaction(transactionToEdit.id, goal.id)
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
                color = Color(0xFFD1C4E9),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (amount.isNotEmpty() && selectedAccountId != null) {
                        if (transactionToEdit != null) {
                            savingsViewModel.updateGoalTransaction(
                                transactionId = transactionToEdit.id,
                                goalId = goal.id,
                                accountId = selectedAccountId!!,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                description = title,
                                notes = notes,
                                dateTime = LocalDateTime.of(selectedDate, LocalTime.now())
                            )
                        } else {
                            savingsViewModel.addTransactionToGoal(
                                goalId = goal.id,
                                accountId = selectedAccountId!!,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                description = title,
                                notes = notes,
                                dateTime = LocalDateTime.of(selectedDate, LocalTime.now())
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
                            "Guardar movimiento",
                            color = Color.Black,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.currency,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) amount = it },
                    placeholder = { Text("0", fontSize = 40.sp, color = Color.Gray) },
                    modifier = Modifier.widthIn(max = 200.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Date Selection
            Surface(
                onClick = { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                color = Color.Gray.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    val dateText = when (selectedDate) {
                        LocalDate.now() -> "Hoy"
                        else -> selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es")))
                    }
                    Text(dateText, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                }
            }

            // Account Selection (Chips)
            Text("Debitar de la cuenta:", color = Color.Gray, fontSize = 14.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    AccountChip(
                        account = account,
                        isSelected = selectedAccountId == account.id,
                        onClick = { selectedAccountId = account.id }
                    )
                }
            }

            // Selected Goal (Fixed)
            Text("Hacia la meta:", color = Color.Gray, fontSize = 14.sp)
            val goalColor = try {
                Color(android.graphics.Color.parseColor(goal.color))
            } catch (e: Exception) {
                Color(0xFF7E57C2)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Gray.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, goalColor)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.icon ?: "💰", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(goal.name, fontWeight = FontWeight.Bold)
                }
            }

            // Title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Título", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Notes
            TextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notas", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
