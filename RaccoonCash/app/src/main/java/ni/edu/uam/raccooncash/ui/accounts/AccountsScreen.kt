package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(),
    onAddAccountClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (TransactionResponse) -> Unit = {},
    onAccountClick: (AccountResponse) -> Unit = {},
    onSettingsClick: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Agrupar transacciones por fecha
    val groupedTransactions = remember(transactions) {
        transactions.sortedByDescending { it.date }.groupBy {
            try {
                LocalDateTime.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }

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
            RaccAddFloatingActionButton(
                onClick = onAddTransactionClick,
                contentDescription = "Nueva Transacción"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(accounts) { account ->
                            val transactionCount = transactions.count { it.accountId == account.id || it.toAccountId == account.id }
                            AccountCard(
                                account = account,
                                transactionCount = transactionCount,
                                onClick = { onAccountClick(account) }
                            )
                        }
                        item {
                            AddAccountCard(onClick = onAddAccountClick)
                        }
                    }
                }
            }

            item {
                BalanceTrendChart(transactions = transactions)
            }

            if (transactions.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay transacciones registradas.", color = Color.Gray)
                    }
                }
            } else {
                groupedTransactions.forEach { (date, dailyTransactions) ->
                    val dailySum = dailyTransactions.sumOf { 
                        if (it.type == "INCOME") it.amount else if (it.type == "EXPENSE") -it.amount else 0.0 
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateLabel = when (date) {
                                LocalDate.now() -> "Hoy, ${date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                                LocalDate.now().minusDays(1) -> "Ayer, ${date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                                else -> date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es")))
                            }
                            Text(
                                text = dateLabel.replaceFirstChar { it.uppercase() },
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            
                            if (dailySum != 0.0) {
                                Text(
                                    text = "${if (dailySum > 0) "+" else ""}C$${String.format(Locale.getDefault(), "%.2f", dailySum)}",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    items(dailyTransactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            allCategories = allCategories,
                            allAccounts = accounts,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceTrendChart(transactions: List<TransactionResponse>) {
    val sortedTransactions = remember(transactions) {
        transactions.sortedBy { it.date }
    }

    val balanceHistory = remember(sortedTransactions) {
        var current = 0.0
        sortedTransactions.map {
            current += if (it.type == "INCOME") it.amount else if (it.type == "EXPENSE") -it.amount else 0.0
            Pair(it.date, current)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tendencia de Balance", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (balanceHistory.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay suficientes datos", color = Color.Gray)
                }
            } else {
                val balances = balanceHistory.map { it.second }
                val maxBalance = balances.maxOrNull() ?: 1.0
                val minBalance = balances.minOrNull() ?: 0.0
                val range = (maxBalance - minBalance).coerceAtLeast(1.0)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val labelPadding = 40.dp.toPx()
                    val bottomPadding = 30.dp.toPx()
                    val chartWidth = size.width - labelPadding
                    val chartHeight = size.height - bottomPadding
                    val spacing = chartWidth / (balanceHistory.size - 1)

                    // Draw Y-axis labels (Money)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    val yLabels = 4
                    for (i in 0..yLabels) {
                        val labelValue = minBalance + (range * i / yLabels)
                        val y = chartHeight - (chartHeight * i / yLabels)
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format(Locale.getDefault(), "%.0f", labelValue),
                            labelPadding - 10f,
                            y + 5f,
                            textPaint
                        )
                    }

                    val points = balanceHistory.mapIndexed { index, pair ->
                        val balance = pair.second
                        val x = labelPadding + index * spacing
                        val y = chartHeight - ((balance - minBalance) / range * chartHeight).toFloat()
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    val strokePath = Path().apply {
                        points.forEachIndexed { index, offset ->
                            if (index == 0) moveTo(offset.x, offset.y)
                            else lineTo(offset.x, offset.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(size.width, chartHeight)
                        lineTo(labelPadding, chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = Color(0xFF90CAF9),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw X-axis labels (Date)
                    val datePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
                    val xLabelCount = minOf(balanceHistory.size, 5)
                    val step = (balanceHistory.size - 1) / (xLabelCount - 1).coerceAtLeast(1)

                    for (i in 0 until xLabelCount) {
                        val index = i * step
                        if (index < balanceHistory.size) {
                            val pair = balanceHistory[index]
                            val date = java.time.LocalDateTime.parse(pair.first, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            val x = labelPadding + index * spacing
                            drawContext.canvas.nativeCanvas.drawText(
                                date.format(dateFormatter),
                                x,
                                size.height - 5f,
                                datePaint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: AccountResponse,
    transactionCount: Int,
    onClick: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)),
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
                        text = "${account.currency}${String.format("%.${account.decimalPrecision ?: 2}f", account.currentBalance)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "$transactionCount transacciones",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun AddAccountCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cuenta",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionResponse,
    allCategories: List<ni.edu.uam.raccooncash.data.model.CategoryResponse> = emptyList(),
    allAccounts: List<AccountResponse> = emptyList(),
    onClick: () -> Unit
) {
    val isSaving = transaction.description.startsWith("Ahorro para", ignoreCase = true)
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER" || isSaving
    
    val category = transaction.category ?: allCategories.find { it.id == transaction.categoryId }
    val parentCategory = if (category?.parentCategoryId != null && category.parentCategoryId != 0L) {
        allCategories.find { it.id == category.parentCategoryId }
    } else null

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
            val emoji = if (parentCategory != null) {
                getEmojiForCategory(parentCategory.name, parentCategory.icon)
            } else {
                getEmojiForCategory(transaction.categoryName ?: category?.name, category?.icon)
            }
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.description, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val accountColor = try {
                    val colorHex = transaction.account?.color ?: allAccounts.find { it.id == transaction.accountId }?.color ?: "#7E57C2"
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    Color.Gray
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accountColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.accountName ?: transaction.account?.name ?: "Cuenta desconocida",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                if (parentCategory != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${getEmojiForCategory(category?.name, category?.icon)} ${transaction.categoryName ?: category?.name ?: ""}",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color(0xFFEF9A9A).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$prefix C$ ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
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

fun getEmojiForCategory(categoryName: String?, serverIcon: String?): String {
    // Mapeo basado en el identificador de icono del servidor
    val iconMapping = mapOf(
        "utensils" to "🍴",
        "shopping-bag" to "🛍️",
        "car" to "🚕",
        "gamepad" to "🎮",
        "receipt" to "🧾",
        "home" to "🏠",
        "heart-pulse" to "🏥",
        "pill" to "💊",
        "graduation-cap" to "🎓",
        "plane" to "✈️",
        "gift" to "🎁",
        "sparkles" to "✨",
        "briefcase" to "💼",
        "house" to "🏠",
        "laptop" to "💻",
        "money-bill" to "💵",
        "bus" to "🚌",
        "hamburger" to "🍔",
        "shopping-cart" to "🛒",
        "lightbulb" to "💡",
        "wrench" to "🔧"
    )

    val normalizedServerIcon = serverIcon?.trim()
    if (!normalizedServerIcon.isNullOrEmpty()) {
        iconMapping[normalizedServerIcon.lowercase()]?.let { return it }
        if (normalizedServerIcon.isCustomEmojiValue()) return normalizedServerIcon
    }

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
        "salud", "medicamento", "medicamentos" -> "💊"
        "educación" -> "📚"
        "hogar", "alquiler" -> "🏠"
        "facturas", "servicios" -> "🧾"
        "ahorro" -> "💰"
        "ingreso" -> "💵"
        "tecnología" -> "💻"
        "suscripciones" -> "📺"
        "deudas" -> "💳"
        "mascotas" -> "🐶"
        "emergencias" -> "🚨"
        "impuestos" -> "🏛️"
        "otros" -> "📝"
        "corrección de saldo" -> "⚖️"
        else -> "📝"
    }
}

private fun String.isCustomEmojiValue(): Boolean {
    return any { char ->
        val type = Character.getType(char)
        type == Character.SURROGATE.toInt() ||
            type == Character.OTHER_SYMBOL.toInt() ||
            type == Character.NON_SPACING_MARK.toInt() ||
            char == '\uFE0F' ||
            char == '\u200D'
    }
}

@Composable
fun AccountChip(
    account: ni.edu.uam.raccooncash.data.model.AccountResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accountColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, accountColor) else null,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accountColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
