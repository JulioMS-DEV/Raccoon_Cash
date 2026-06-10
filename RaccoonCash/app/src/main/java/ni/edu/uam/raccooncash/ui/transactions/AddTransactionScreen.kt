package ni.edu.uam.raccooncash.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel,
    transactionToEdit: TransactionResponse? = null,
    onBack: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.addTransactionSuccess.collectAsState()

    var selectedTab by remember { mutableIntStateOf(if (transactionToEdit?.type == "INCOME") 1 else if (transactionToEdit?.type == "TRANSFER") 2 else 0) }
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var notes by remember { mutableStateOf(transactionToEdit?.notes ?: "") }
    var selectedAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.accountId ?: transactionToEdit?.account?.id) }
    var selectedToAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.destinationAccountId ?: transactionToEdit?.toAccountId ?: transactionToEdit?.toAccount?.id) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(transactionToEdit?.categoryId ?: transactionToEdit?.category?.id) }

    var showCategorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val tabs = listOf("Gasto", "Ingreso", "Transferir")

    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionToEdit != null) "Editar transacción" else "Agregar transacción") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (transactionToEdit != null) {
                        IconButton(onClick = { viewModel.deleteTransaction(transactionToEdit.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }

            // Amount Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "C$",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    placeholder = { Text("0", style = MaterialTheme.typography.headlineLarge) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH : mm")),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Account Selection
            Text("Seleccionar cuenta", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    AccountChip(
                        account = account,
                        isSelected = selectedAccountId == account.id,
                        onClick = { selectedAccountId = account.id }
                    )
                }
            }

            if (selectedTab == 2) {
                Text("A la cuenta", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts.filter { it.id != selectedAccountId }) { account ->
                        AccountChip(
                            account = account,
                            isSelected = selectedToAccountId == account.id,
                            onClick = { selectedToAccountId = account.id }
                        )
                    }
                }
            }

            // Title and Notes
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Category Selection (Only for Income/Expense)
            if (selectedTab != 2) {
                val selectedCategory = categories.find { it.id == selectedCategoryId }
                
                Surface(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getEmojiForCategory(selectedCategory?.name ?: transactionToEdit?.categoryName), fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedCategory?.name ?: transactionToEdit?.categoryName ?: "Selecciona una categoría",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val isFormValid = selectedAccountId != null && 
                             amount.isNotBlank() && 
                             title.isNotBlank() && 
                             (if (selectedTab == 2) selectedToAccountId != null else selectedCategoryId != null)

            Button(
                onClick = {
                    val type = when (selectedTab) {
                        0 -> "EXPENSE"
                        1 -> "INCOME"
                        else -> "TRANSFER"
                    }
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (isFormValid) {
                        if (transactionToEdit != null) {
                            viewModel.updateTransaction(
                                id = transactionToEdit.id,
                                amount = amountDouble,
                                type = type,
                                accountId = selectedAccountId!!,
                                toAccountId = if (type == "TRANSFER") selectedToAccountId else null,
                                categoryId = if (type != "TRANSFER") selectedCategoryId else null,
                                description = title,
                                notes = notes
                            )
                        } else {
                            viewModel.createTransaction(
                                amount = amountDouble,
                                type = type,
                                accountId = selectedAccountId!!,
                                toAccountId = if (type == "TRANSFER") selectedToAccountId else null,
                                categoryId = if (type != "TRANSFER") selectedCategoryId else null,
                                description = title,
                                notes = notes
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isFormValid
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (transactionToEdit != null) "Actualizar" else "Guardar")
                }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Selecciona Una Categoría",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                val filteredCategories = categories.filter {
                    if (selectedTab == 0) it.type == "EXPENSE" else it.type == "INCOME"
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryIconItem(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = category.id
                                showCategorySheet = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CategoryIconItem(
    category: CategoryResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Gray.copy(alpha = 0.1f)
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(backgroundColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(getEmojiForCategory(category.name), fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun AccountChip(
    account: AccountResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = account.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun getEmojiForCategory(categoryName: String?): String {
    return when (categoryName?.lowercase()) {
        "comida", "restaurante" -> "🍴"
        "comestibles", "supermercado" -> "🥦"
        "compras" -> "🛍️"
        "transporte" -> "🚕"
        "entretenimiento" -> "🍿"
        "regalos" -> "🎁"
        "belleza" -> "🌸"
        "viajes" -> "✈️"
        "trabajo" -> "💼"
        "salud", "medicamentos" -> "💊"
        "educación" -> "📚"
        "hogar" -> "🏠"
        "ahorro" -> "💰"
        "ingreso" -> "💵"
        else -> "📝"
    }
}
