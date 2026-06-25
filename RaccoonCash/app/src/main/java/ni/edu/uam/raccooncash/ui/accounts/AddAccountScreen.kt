package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse

val accountColors = listOf(
    Color(0xFF7E57C2), // Purple
    Color(0xFF2196F3), // Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF4DB6AC), // Teal
    Color(0xFF81C784), // Light Green
    Color(0xFFD4E157), // Lime
    Color(0xFFFFA726), // Orange
    Color(0xFFEF5350), // Red
    Color(0xFFEC407A), // Pink
    Color(0xFFAB47BC)  // Light Purple
)

data class CurrencyInfo(val code: String, val symbol: String, val country: String)
val availableCurrencies = listOf(
    CurrencyInfo("NIO", "C$", "Nicaragua"),
    CurrencyInfo("USD", "$", "United States"),
    CurrencyInfo("EUR", "€", "Euro Member"),
    CurrencyInfo("JPY", "¥", "Japan"),
    CurrencyInfo("GBP", "£", "United Kingdom"),
    CurrencyInfo("AUD", "$", "Australia")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountsViewModel,
    accountToEdit: AccountResponse? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var balance by remember { mutableStateOf(accountToEdit?.initialBalance?.toString() ?: "") }
    
    val initialColor = if (accountToEdit?.color != null) {
        try { Color(android.graphics.Color.parseColor(accountToEdit.color)) } catch (e: Exception) { accountColors[0] }
    } else {
        accountColors[0]
    }
    var selectedColor by remember { mutableStateOf(initialColor) }
    
    val initialCurrency = availableCurrencies.find { it.symbol == accountToEdit?.currency } ?: availableCurrencies[0]
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    
    var decimalPrecision by remember { mutableIntStateOf(accountToEdit?.decimalPrecision ?: 2) }
    var searchQuery by remember { mutableStateOf("") }
    
    val success by viewModel.addAccountSuccess.collectAsState()
    
    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Info */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color(0xFFB39DDB),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (name.isNotEmpty()) {
                        val argb = selectedColor.toArgb()
                        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
                        
                        if (accountToEdit != null) {
                            viewModel.updateAccount(
                                id = accountToEdit.id,
                                name = name,
                                balance = balance.toDoubleOrNull() ?: 0.0,
                                currency = selectedCurrency.symbol,
                                color = colorHex,
                                precision = decimalPrecision
                            )
                        } else {
                            viewModel.createAccount(
                                name = name,
                                balance = balance.toDoubleOrNull() ?: 0.0,
                                currency = selectedCurrency.symbol,
                                color = colorHex,
                                precision = decimalPrecision
                            )
                        }
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (name.isEmpty()) "Asignar nombre" else if (accountToEdit != null) "Actualizar cuenta" else "Crear cuenta",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (accountToEdit != null) "Editar cuenta" else "Añadir cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Name Input
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre", fontSize = 28.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color Picker
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) // Placeholder for palette icon
                    }
                }
                items(accountColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 4.dp else 0.dp,
                                color = Color(0xFFB39DDB),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Balance Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.Gray.copy(alpha = 0.1f),
                border = borderStroke()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("A partir de ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "${selectedCurrency.symbol}${if (balance.isEmpty()) "0" else balance}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Gray
                    )
                    // Input invisible mapping to text for style
                    TextField(
                        value = balance,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) balance = it },
                        modifier = Modifier.width(1.dp).height(1.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Decimal Precision
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.Gray.copy(alpha = 0.1f),
                border = borderStroke()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(".00", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Precisión decimal", modifier = Modifier.weight(1f))
                    
                    Surface(
                        onClick = { decimalPrecision = if (decimalPrecision == 2) 0 else 2 },
                        color = Color(0xFF90CAF9).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "$decimalPrecision decimales",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFF90CAF9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Currency Grid
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar divisas...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.1f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.1f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(availableCurrencies.filter { it.code.contains(searchQuery, ignoreCase = true) }) { curr ->
                    CurrencyGridItem(
                        currency = curr,
                        isSelected = selectedCurrency == curr,
                        onClick = { selectedCurrency = curr }
                    )
                }
            }
        }
    }
}

@Composable
fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))

@Composable
fun CurrencyGridItem(
    currency: CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Gray.copy(alpha = 0.1f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(currency.code, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Text(currency.symbol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(currency.country, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
