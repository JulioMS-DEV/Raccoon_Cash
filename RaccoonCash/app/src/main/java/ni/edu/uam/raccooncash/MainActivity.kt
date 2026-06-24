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
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.ui.account_details.AccountDetailsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.AddAccountScreen
import ni.edu.uam.raccooncash.ui.budgets.AddBudgetScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetDetailsScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetsScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetsViewModel
import ni.edu.uam.raccooncash.ui.savings.AddGoalTransactionScreen
import ni.edu.uam.raccooncash.ui.savings.AddSavingGoalScreen
import ni.edu.uam.raccooncash.ui.savings.SavingGoalDetailsScreen
import ni.edu.uam.raccooncash.ui.savings.SavingsScreen
import ni.edu.uam.raccooncash.ui.savings.SavingsViewModel
import ni.edu.uam.raccooncash.ui.settings.SettingsScreen
import ni.edu.uam.raccooncash.ui.theme.RaccoonCashTheme
import ni.edu.uam.raccooncash.ui.transactions.AddTransactionScreen
import ni.edu.uam.raccooncash.ui.transactions.TransactionsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
                val savingsViewModel: SavingsViewModel = viewModel()
                val budgetsViewModel: BudgetsViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("inicio") }
                var editingTransaction by remember { mutableStateOf<TransactionResponse?>(null) }
                var editingAccount by remember { mutableStateOf<AccountResponse?>(null) }
                var selectedAccountDetails by remember { mutableStateOf<AccountResponse?>(null) }
                var selectedSavingGoal by remember { mutableStateOf<SavingGoalResponse?>(null) }
                var editingSavingGoal by remember { mutableStateOf<SavingGoalResponse?>(null) }
                var editingGoalTransaction by remember { mutableStateOf<TransactionResponse?>(null) }
                var selectedBudget by remember { mutableStateOf<PresupuestoRespuesta?>(null) }
                var editingBudget by remember { mutableStateOf<PresupuestoRespuesta?>(null) }
                var budgetTransactionInitialType by remember { mutableStateOf<String?>(null) }
                var budgetTransactionInitialDescription by remember { mutableStateOf("") }
                var budgetTransactionInitialDate by remember { mutableStateOf<LocalDate?>(null) }
                var budgetTransactionInitialCategoryId by remember { mutableStateOf<Long?>(null) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0F111A),
                            contentColor = Color.White
                        ) {
                            val items = listOf(
                                Triple("inicio", "Inicio", Icons.Default.Home),
                                Triple("transacciones", "Transacciones", Icons.AutoMirrored.Filled.List),
                                Triple("presupuestos", "Presupuestos", Icons.Default.AccountBalanceWallet),
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
                            "ahorro" -> SavingsScreen(
                                viewModel = savingsViewModel,
                                onAddGoalClick = { 
                                    editingSavingGoal = null
                                    currentScreen = "add_saving_goal" 
                                },
                                onGoalClick = { goal ->
                                    selectedSavingGoal = goal
                                    currentScreen = "saving_goal_details"
                                }
                            )
                            "presupuestos" -> BudgetsScreen(
                                viewModel = budgetsViewModel,
                                onAddBudgetClick = {
                                    editingBudget = null
                                    currentScreen = "add_budget"
                                },
                                onBudgetClick = { budget ->
                                    selectedBudget = budget
                                    currentScreen = "budget_details"
                                }
                            )
                            "budget_details" -> selectedBudget?.let { budget ->
                                BudgetDetailsScreen(
                                    budgetId = budget.id,
                                    viewModel = budgetsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    onAddTransaction = { currentBudget, categoryId ->
                                        editingTransaction = null
                                        budgetTransactionInitialType = if (currentBudget.esGasto) "EXPENSE" else "INCOME"
                                        budgetTransactionInitialDescription = currentBudget.nombre
                                        budgetTransactionInitialDate = getDefaultTransactionDateForBudget(currentBudget)
                                        budgetTransactionInitialCategoryId = categoryId
                                        currentScreen = "add_budget_transaction"
                                    },
                                    onEditBudget = { currentBudget ->
                                        editingBudget = currentBudget
                                        currentScreen = "add_budget"
                                    },
                                    onTransactionClick = { transaction ->
                                        editingTransaction = transaction
                                        budgetTransactionInitialType = null
                                        budgetTransactionInitialDescription = ""
                                        budgetTransactionInitialDate = null
                                        budgetTransactionInitialCategoryId = null
                                        currentScreen = "add_budget_transaction"
                                    },
                                    onBack = { currentScreen = "presupuestos" }
                                )
                            }
                            "add_budget" -> AddBudgetScreen(
                                viewModel = budgetsViewModel,
                                accountsViewModel = accountsViewModel,
                                budgetToEdit = editingBudget,
                                onBack = {
                                    currentScreen = if (editingBudget != null && selectedBudget?.id == editingBudget?.id) {
                                        "budget_details"
                                    } else {
                                        "presupuestos"
                                    }
                                    budgetsViewModel.loadBudgets()
                                }
                            )
                            "saving_goal_details" -> selectedSavingGoal?.let { goal ->
                                SavingGoalDetailsScreen(
                                    goalId = goal.id,
                                    viewModel = savingsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    onAddTransaction = { 
                                        editingGoalTransaction = null
                                        currentScreen = "add_goal_transaction" 
                                    },
                                    onEditGoal = { 
                                        editingSavingGoal = goal
                                        currentScreen = "add_saving_goal" 
                                    },
                                    onTransactionClick = { transaction ->
                                        editingGoalTransaction = transaction
                                        currentScreen = "add_goal_transaction"
                                    },
                                    onBack = { currentScreen = "ahorro" }
                                )
                            }
                            "add_goal_transaction" -> selectedSavingGoal?.let { goal ->
                                AddGoalTransactionScreen(
                                    goal = goal,
                                    savingsViewModel = savingsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    transactionToEdit = editingGoalTransaction,
                                    onBack = { currentScreen = "saving_goal_details" }
                                )
                            }
                            "add_saving_goal" -> AddSavingGoalScreen(
                                viewModel = savingsViewModel,
                                goalToEdit = editingSavingGoal,
                                onBack = { 
                                    if (editingSavingGoal != null) {
                                        // Si estábamos editando, regresamos a los detalles de esa meta específica
                                        selectedSavingGoal = editingSavingGoal
                                        currentScreen = "saving_goal_details"
                                        savingsViewModel.loadSavingGoals()
                                        savingsViewModel.loadGoalTransactions(editingSavingGoal!!.id)
                                    } else {
                                        // Si era una meta nueva, regresamos a la lista general
                                        currentScreen = "ahorro"
                                        savingsViewModel.loadSavingGoals()
                                    }
                                }
                            )
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
                            "add_budget_transaction" -> selectedBudget?.let {
                                AddTransactionScreen(
                                    viewModel = transactionsViewModel,
                                    transactionToEdit = editingTransaction,
                                    initialType = budgetTransactionInitialType,
                                    initialDescription = budgetTransactionInitialDescription,
                                    initialDate = budgetTransactionInitialDate,
                                    initialCategoryId = budgetTransactionInitialCategoryId,
                                    onBack = {
                                        currentScreen = "budget_details"
                                        accountsViewModel.loadAccounts()
                                        budgetsViewModel.loadBudgets()
                                    }
                                )
                            }
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

private fun getDefaultTransactionDateForBudget(budget: PresupuestoRespuesta): LocalDate {
    val startDate = try {
        LocalDate.parse(budget.fechaInicio, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        return LocalDate.now()
    }
    val endDate = calculateBudgetEndDate(startDate, budget.tipoPeriodo, budget.valorPeriodo)
    val today = LocalDate.now()

    return if (today.isBefore(startDate) || today.isAfter(endDate)) startDate else today
}

private fun calculateBudgetEndDate(startDate: LocalDate, periodType: TipoPeriodoPresupuesto, periodValue: Int): LocalDate {
    val value = periodValue.toLong().coerceAtLeast(1)
    return when (periodType) {
        TipoPeriodoPresupuesto.DIARIO -> startDate.plusDays(value)
        TipoPeriodoPresupuesto.SEMANAL -> startDate.plusWeeks(value)
        TipoPeriodoPresupuesto.MENSUAL -> startDate.plusMonths(value)
        TipoPeriodoPresupuesto.ANUAL -> startDate.plusYears(value)
        TipoPeriodoPresupuesto.PERSONALIZADO -> startDate.plusDays(value)
    }.minusDays(1)
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
