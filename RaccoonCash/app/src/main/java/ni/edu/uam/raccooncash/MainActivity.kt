package ni.edu.uam.raccooncash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.ui.accounts.AccountsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.AddAccountScreen
import ni.edu.uam.raccooncash.ui.settings.SettingsScreen
import ni.edu.uam.raccooncash.ui.theme.RaccoonCashTheme
import ni.edu.uam.raccooncash.ui.transactions.AddTransactionScreen
import ni.edu.uam.raccooncash.ui.transactions.TransactionsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            RaccoonCashTheme(darkTheme = isDarkTheme) {
                val accountsViewModel: AccountsViewModel = viewModel()
                val transactionsViewModel: TransactionsViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("accounts_list") }
                var editingTransaction by remember { mutableStateOf<ni.edu.uam.raccooncash.data.model.TransactionResponse?>(null) }
                var editingAccount by remember { mutableStateOf<ni.edu.uam.raccooncash.data.model.AccountResponse?>(null) }
                var selectedAccountDetails by remember { mutableStateOf<ni.edu.uam.raccooncash.data.model.AccountResponse?>(null) }

                when (currentScreen) {
                    "accounts_list" -> AccountsScreen(
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
                    "account_details" -> selectedAccountDetails?.let { account ->
                        ni.edu.uam.raccooncash.ui.account_details.AccountDetailsScreen(
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
                            onBack = { currentScreen = "accounts_list" }
                        )
                    }
                    "add_account" -> AddAccountScreen(
                        viewModel = accountsViewModel,
                        accountToEdit = editingAccount,
                        onBack = { currentScreen = "accounts_list" }
                    )
                    "add_transaction" -> AddTransactionScreen(
                        viewModel = transactionsViewModel,
                        transactionToEdit = editingTransaction,
                        onBack = { 
                            currentScreen = "accounts_list"
                            accountsViewModel.loadAccounts() // Recargar datos al volver
                        }
                    )
                    "settings" -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it },
                        onBack = { currentScreen = "accounts_list" }
                    )
                }
            }
        }
    }
}
