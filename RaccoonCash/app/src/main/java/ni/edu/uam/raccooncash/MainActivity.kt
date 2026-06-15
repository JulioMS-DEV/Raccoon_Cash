package ni.edu.uam.raccooncash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.account_details.AccountDetailsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.AddAccountScreen
import ni.edu.uam.raccooncash.ui.settings.SettingsScreen
import ni.edu.uam.raccooncash.ui.theme.RaccoonCashTheme
import ni.edu.uam.raccooncash.ui.transactions.AddTransactionScreen
import ni.edu.uam.raccooncash.ui.transactions.TransactionsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            RaccoonCashTheme(darkTheme = isDarkTheme) {
                val accountsViewModel: AccountsViewModel = viewModel()
                val transactionsViewModel: TransactionsViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("inicio") }
                var editingTransaction by remember { mutableStateOf<TransactionResponse?>(null) }
                var editingAccount by remember { mutableStateOf<AccountResponse?>(null) }
                var selectedAccountDetails by remember { mutableStateOf<AccountResponse?>(null) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0F111A),
                            contentColor = Color.White
                        ) {
                            val items = listOf(
                                Triple("inicio", "Inicio", Icons.Default.Home),
                                Triple("transacciones", "Transacciones", Icons.AutoMirrored.Filled.List),
                                Triple("ahorro", "Ahorro", Icons.Default.Star)
                            )
                            items.forEach { (screen, label, icon) ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.Gray,
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color(0xFF2C313F)
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            "inicio" -> AccountsScreen(
                                viewModel = accountsViewModel,
                                onAddAccountClick = { 
                                    editingAccount = null
                                    currentScreen = "add_account" 
                                },
                                onAddTransactionClick = { 
                                    editingTransaction = null
                                    currentScreen = "add_transaction" 
                                },
                                onTransactionClick = { transaction ->
                                    editingTransaction = transaction
                                    currentScreen = "add_transaction"
                                },
                                onAccountClick = { account ->
                                    selectedAccountDetails = account
                                    currentScreen = "account_details"
                                },
                                onSettingsClick = { currentScreen = "settings" }
                            )
                            "transacciones" -> TransactionsTabScreen(
                                viewModel = accountsViewModel,
                                onTransactionClick = { transaction ->
                                    editingTransaction = transaction
                                    currentScreen = "add_transaction"
                                }
                            )
                            "ahorro" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pantalla de Ahorro (Próximamente)", color = Color.White)
                            }
                            "account_details" -> selectedAccountDetails?.let { account ->
                                AccountDetailsScreen(
                                    account = account,
                                    viewModel = accountsViewModel,
                                    onEditAccount = {
                                        editingAccount = account
                                        currentScreen = "add_account"
                                    },
                                    onTransactionClick = { transaction ->
                                        editingTransaction = transaction
                                        currentScreen = "add_transaction"
                                    },
                                    onBack = { currentScreen = "inicio" }
                                )
                            }
                            "add_account" -> AddAccountScreen(
                                viewModel = accountsViewModel,
                                accountToEdit = editingAccount,
                                onBack = { currentScreen = "inicio" }
                            )
                            "add_transaction" -> AddTransactionScreen(
                                viewModel = transactionsViewModel,
                                transactionToEdit = editingTransaction,
                                onBack = { 
                                    currentScreen = "inicio"
                                    accountsViewModel.loadAccounts()
                                }
                            )
                            "settings" -> SettingsScreen(
                                onBack = { currentScreen = "inicio" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsTabScreen(
    viewModel: AccountsViewModel,
    onTransactionClick: (TransactionResponse) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val allAccounts by viewModel.accounts.collectAsState()
    
    val months = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
    var selectedMonthIndex by remember { mutableIntStateOf(java.time.LocalDate.now().monthValue - 1) }

    val filteredTransactions = transactions.filter {
        val date = java.time.LocalDateTime.parse(it.date, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        date.monthValue == selectedMonthIndex + 1 && date.year == java.time.LocalDate.now().year
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F111A))) {
        Text(
            "Transacciones",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedMonthIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMonthIndex]),
                    color = Color.White
                )
            }
        ) {
            months.forEachIndexed { index, month ->
                Tab(
                    selected = selectedMonthIndex == index,
                    onClick = { selectedMonthIndex = index },
                    text = { Text(month, color = if (selectedMonthIndex == index) Color.White else Color.Gray) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFF1E222D), RoundedCornerShape(12.dp)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gastos", color = Color.Gray, fontSize = 12.sp)
                Text("C$${String.format(Locale.getDefault(), "%.2f", totalExpense)}", color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ingresos", color = Color.Gray, fontSize = 12.sp)
                Text("C$${String.format(Locale.getDefault(), "%.2f", totalIncome)}", color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", color = Color.Gray, fontSize = 12.sp)
                val total = totalIncome - totalExpense
                Text("C$${String.format(Locale.getDefault(), "%.2f", total)}", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val groupedByDay = filteredTransactions.sortedByDescending { it.date }.groupBy {
                java.time.LocalDateTime.parse(it.date, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
            }

            groupedByDay.forEach { (date, dailyTrans) ->
                item {
                    val dateLabel = when (date) {
                        java.time.LocalDate.now() -> "Hoy, ${date.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                        java.time.LocalDate.now().minusDays(1) -> "Ayer, ${date.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es")))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dateLabel.replaceFirstChar { it.uppercase(Locale.getDefault()) }, color = Color.Gray, fontSize = 14.sp)
                        val daySum = dailyTrans.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
                        Text("C$${String.format(Locale.getDefault(), "%.2f", daySum)}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                items(dailyTrans) { transaction ->
                    ni.edu.uam.raccooncash.ui.accounts.TransactionItem(
                        transaction = transaction,
                        allCategories = allCategories,
                        allAccounts = allAccounts,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}
