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
import ni.edu.uam.raccooncash.ui.theme.RaccoonCashTheme
import ni.edu.uam.raccooncash.ui.transactions.AddTransactionScreen
import ni.edu.uam.raccooncash.ui.transactions.TransactionsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RaccoonCashTheme {
                val accountsViewModel: AccountsViewModel = viewModel()
                val transactionsViewModel: TransactionsViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("accounts_list") }
                var editingTransaction by remember { mutableStateOf<ni.edu.uam.raccooncash.data.model.TransactionResponse?>(null) }

                when (currentScreen) {
                    "accounts_list" -> AccountsScreen(
                        viewModel = accountsViewModel,
                        onAddAccountClick = { currentScreen = "add_account" },
                        onAddTransactionClick = { 
                            editingTransaction = null
                            currentScreen = "add_transaction" 
                        },
                        onTransactionClick = { transaction ->
                            editingTransaction = transaction
                            currentScreen = "add_transaction"
                        }
                    )
                    "add_account" -> AddAccountScreen(
                        viewModel = accountsViewModel,
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
                }
            }
        }
    }
}
