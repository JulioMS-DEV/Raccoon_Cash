package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(),
    onAddAccountClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (ni.edu.uam.raccooncash.data.model.TransactionResponse) -> Unit = {},
    onSettingsClick: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio", fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                    IconButton(onClick = { viewModel.loadAccounts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onAddAccountClick,
                    containerColor = Color(0xFFC5CAE9),
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Cuenta")
                }
                FloatingActionButton(
                    onClick = onAddTransactionClick,
                    containerColor = Color(0xFFE1D5F9), // Light purple from image
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Transacción")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Accounts List
            item {
                if (isLoading && accounts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (error != null && accounts.isEmpty()) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(accounts) { account ->
                            AccountCard(account = account)
                        }
                    }
                }
            }

            // Placeholder for Graph (Based on image)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gráfico de Balance", color = Color.Gray)
                        // This is where the line graph would go
                    }
                }
            }

            // Placeholder for Transactions Header
            item {
                Text(
                    text = "Transacciones recientes",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Real Transactions List
            if (transactions.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No hay transacciones registradas.",
                            color = Color.Gray
                        )
                        Text(
                            "(O el servidor dio un error al cargarlas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(transactions.sortedByDescending { it.date }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: ni.edu.uam.raccooncash.data.model.TransactionResponse,
    onClick: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER"
    
    val amountColor = when {
        isExpense -> Color(0xFFEF9A9A)
        isTransfer -> Color(0xFF90CAF9)
        else -> Color(0xFFA5D6A7)
    }
    
    val prefix = when {
        isExpense -> "-"
        isTransfer -> ""
        else -> "+"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(amountColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getEmojiForCategory(transaction.categoryName ?: transaction.category?.name),
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.description, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                text = transaction.accountName ?: transaction.account?.name ?: "Cuenta desconocida",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$prefix C$${String.format("%.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (isTransfer) {
                Text(
                    text = "→ ${transaction.destinationAccountName ?: transaction.toAccountName ?: transaction.toAccount?.name ?: ""}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
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

@Composable
fun AccountCard(account: AccountResponse) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)), // Dark card color
        border = androidx.compose.foundation.BorderStroke(1.dp, accountColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(accountColor, CircleShape)
                )
            }
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "▲",
                        color = accountColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "${account.currency}${String.format("%.0f", account.currentBalance)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Actualizado",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun TransactionItemPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Transacción", color = Color.White, fontWeight = FontWeight.Medium)
            Text("Categoría", color = Color.Gray, fontSize = 12.sp)
        }
        Text("- C$0", color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold)
    }
}
