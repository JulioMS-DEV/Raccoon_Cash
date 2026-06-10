package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CurrencyOption(val code: String, val symbol: String, val country: String)

val currencies = listOf(
    CurrencyOption("NIO", "C$", "Nicaragua"),
    CurrencyOption("USD", "$", "United States"),
    CurrencyOption("EUR", "€", "Euro Member"),
    CurrencyOption("JPY", "¥", "Japan"),
    CurrencyOption("GBP", "£", "United Kingdom"),
    CurrencyOption("AUD", "$", "Australia")
)

val accountColors = listOf(
    Color(0xFF7E57C2), // Purple
    Color(0xFF66BB6A), // Green
    Color(0xFF26A69A), // Teal
    Color(0xFF26C6DA), // Cyan
    Color(0xFF42A5F5), // Blue
    Color(0xFF3F51B5)  // Indigo
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountsViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(accountColors[0]) }
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }
    
    val success by viewModel.addAccountSuccess.collectAsState()
    val isDark = isSystemInDarkTheme()
    
    // Theme-aware colors
    val cardBg = if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isDark) Color.White else Color.Black

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
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        // Safer color conversion to hex
                        val argb = selectedColor.toArgb()
                        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
                        
                        viewModel.createAccount(
                            name = name,
                            balance = balance.toDoubleOrNull() ?: 0.0,
                            currency = selectedCurrency.symbol,
                            color = colorHex
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFFB39DDB) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Crear cuenta", 
                    color = if (isDark) Color.Black else Color.White, 
                    fontWeight = FontWeight.Bold
                )
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
                text = "Añadir cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Name Input
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre", fontSize = 24.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = selectedColor,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedTextColor = selectedColor,
                    unfocusedTextColor = selectedColor
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                accountColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 0.dp,
                                color = if (isDark) Color.White else Color.Black,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Balance Input (Editable)
            Text("Presupuesto inicial", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = balance,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) balance = it },
                label = { Text("A partir de ${selectedCurrency.symbol}") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                prefix = { Text("${selectedCurrency.symbol} ", fontWeight = FontWeight.Bold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Precision Decimal info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Text(" Precisión decimal", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Surface(
                    color = cardBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "2 decimales",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currency Grid
            Text("Divisa", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currencies) { currency ->
                    CurrencyCard(
                        currency = currency,
                        isSelected = selectedCurrency == currency,
                        onClick = { selectedCurrency = currency },
                        cardBg = cardBg,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyCard(
    currency: CurrencyOption, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    cardBg: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else cardBg
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(currency.code, color = Color.Gray, fontSize = 10.sp)
            Text(currency.symbol, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(currency.country, color = Color.Gray, fontSize = 8.sp)
        }
    }
}
