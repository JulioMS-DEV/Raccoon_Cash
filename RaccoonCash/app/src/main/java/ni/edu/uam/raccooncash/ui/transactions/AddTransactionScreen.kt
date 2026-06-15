package ni.edu.uam.raccooncash.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel,
    transactionToEdit: TransactionResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
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

    // Date and Time State
    val initialDateTime = if (transactionToEdit?.date != null) {
        try { LocalDateTime.parse(transactionToEdit.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME) } catch (e: Exception) { LocalDateTime.now() }
    } else {
        LocalDateTime.now()
    }
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showSubcategorySheet by remember { mutableStateOf(false) }
    var parentCategoryForSub by remember { mutableStateOf<CategoryResponse?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var initialParentIdForNewCategory by remember { mutableStateOf<Long?>(null) }
    var categoryToEdit by remember { mutableStateOf<CategoryResponse?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val tabs = listOf("Gasto", "Ingreso", "Transferir")

    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

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
                        onClick = { 
                            selectedTab = index
                            // Al cambiar de pestaña, resetear la categoría seleccionada si no es transferencia
                            if (index != 2) {
                                selectedCategoryId = null
                            }
                        },
                        text = { Text(label) }
                    )
                }
            }

            // Amount Input
            val selectedAccount = accounts.find { it.id == selectedAccountId }
            val currencySymbol = selectedAccount?.currency ?: "C$"

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = currencySymbol,
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

            // Date & Time Picker Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateText = when (selectedDate) {
                            LocalDate.now() -> "Hoy"
                            LocalDate.now().minusDays(1) -> "Ayer"
                            else -> selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        }
                        Text(text = dateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp)) // Placeholder for time icon
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedTime.format(DateTimeFormatter.ofPattern("HH : mm")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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
                label = { Text("Título") },
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
                            Text(getEmojiForCategory(selectedCategory?.name ?: transactionToEdit?.categoryName, selectedCategory?.icon), fontSize = 24.sp)
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
                    val finalDateTime = LocalDateTime.of(selectedDate, selectedTime)
                    
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
                                notes = notes,
                                dateTime = finalDateTime
                            )
                        } else {
                            viewModel.createTransaction(
                                amount = amountDouble,
                                type = type,
                                accountId = selectedAccountId!!,
                                toAccountId = if (type == "TRANSFER") selectedToAccountId else null,
                                categoryId = if (type != "TRANSFER") selectedCategoryId else null,
                                description = title,
                                notes = notes,
                                dateTime = finalDateTime
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
                    val isCorrectType = if (selectedTab == 0) it.type == "EXPENSE" else it.type == "INCOME"
                    isCorrectType && (it.parentCategoryId == null || it.parentCategoryId == 0L)
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
                                val subs = categories.filter { it.parentCategoryId == category.id }
                                if (subs.isNotEmpty()) {
                                    parentCategoryForSub = category
                                    showSubcategorySheet = true
                                    showCategorySheet = false
                                } else {
                                    selectedCategoryId = category.id
                                    showCategorySheet = false
                                }
                            },
                            onLongClick = {
                                categoryToEdit = category
                                showCategorySheet = false
                            }
                        )
                    }
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                     initialParentIdForNewCategory = null
                                    showAddCategoryDialog = true 
                                    showCategorySheet = false
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Añadir", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showSubcategorySheet && parentCategoryForSub != null) {
        SubcategoryPickerSheet(
            parentCategory = parentCategoryForSub!!,
            subcategories = categories.filter { it.parentCategoryId == parentCategoryForSub!!.id },
            onDismiss = { showSubcategorySheet = false },
            onSubcategorySelected = { subcategoryId ->
                selectedCategoryId = subcategoryId
                showSubcategorySheet = false
            },
            onAddSubcategory = {
                initialParentIdForNewCategory = parentCategoryForSub!!.id
                showAddCategoryDialog = true
                showSubcategorySheet = false
            },
            onEditParent = {
                categoryToEdit = parentCategoryForSub
                showSubcategorySheet = false
            }
        )
    }

    if (showAddCategoryDialog) {
        CategoryEditorDialog(
            categories = categories,
            initialParentId = initialParentIdForNewCategory,
            onDismiss = { 
                showAddCategoryDialog = false
                if (initialParentIdForNewCategory != null) {
                    showSubcategorySheet = true
                } else {
                    showCategorySheet = true
                }
            },
            onConfirm = { name, type, icon, color, parentId ->
                viewModel.createCategory(name, type, icon, parentId)
                showAddCategoryDialog = false
                if (parentId != null) {
                    // Si se creó una subcategoría, refrescamos el padre para que aparezca en el selector
                    parentCategoryForSub = categories.find { it.id == parentId }
                    showSubcategorySheet = true
                } else {
                    showCategorySheet = true
                }
            }
        )
    }

    if (categoryToEdit != null) {
        CategoryEditorDialog(
            categories = categories,
            category = categoryToEdit,
            onDismiss = { 
                categoryToEdit = null
                showCategorySheet = true
            },
            onConfirm = { name, type, icon, color, parentId ->
                viewModel.updateCategory(categoryToEdit!!.id, name, type, icon, color, parentId)
                categoryToEdit = null
                showCategorySheet = true
            },
            onDelete = {
                viewModel.deleteCategory(categoryToEdit!!.id)
                categoryToEdit = null
                showCategorySheet = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorDialog(
    categories: List<CategoryResponse>,
    category: CategoryResponse? = null,
    initialParentId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Long?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedType by remember { mutableStateOf(category?.type ?: "EXPENSE") }
    var selectedEmoji by remember { mutableStateOf(category?.icon ?: "📝") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#7E57C2") }
    var selectedParentId by remember { mutableStateOf<Long?>(category?.parentCategoryId ?: initialParentId) }
    var isSubcategory by remember { mutableStateOf(category?.parentCategoryId != null || initialParentId != null) }
    
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showParentPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val colors = listOf("#7E57C2", "#66BB6A", "#26A69A", "#29B6F6", "#42A5F5", "#FFA726", "#EF5350")
    
    if (showDeleteConfirmation) {
        // ... (existing AlertDialog code remains same, I'll just include the start and end)
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Color(0xFF1E222D),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(48.dp)) },
            title = { 
                Text(
                    "¿Eliminar ${if (category?.parentCategoryId != null) "subcategoría" else "categoría"}?", 
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(category?.name ?: "", color = Color(0xFFE1D5F9), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Eliminar la etiqueta de esta categoría de todas las transacciones asociadas. Las transacciones no se eliminarán.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onDelete?.invoke()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Borrar", color = Color.Black)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Cancelar", color = Color(0xFFEF9A9A))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A),
        dragHandle = null
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
                Text(
                    text = if (category == null) "Añadir categoría" else "Editar categoría",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (category != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Type Toggle (Gasto / Ingreso)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (selectedType == "EXPENSE") Color(0xFF2C313F) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedType = "EXPENSE" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gasto", color = if (selectedType == "EXPENSE") Color.White else Color.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (selectedType == "INCOME") Color(0xFF2C313F) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedType = "INCOME" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ingreso", color = if (selectedType == "INCOME") Color.White else Color.Gray)
                    }
                }
            }

            // Icon and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF2C313F), CircleShape)
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedEmoji, fontSize = 40.sp)
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre", color = Color.Gray, fontSize = 24.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE1D5F9),
                        focusedIndicatorColor = Color(0xFFE1D5F9),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done)
                )
            }

            // Color Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF2C313F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) // Palette icon placeholder
                }
                colors.forEach { colorStr ->
                    val color = Color(android.graphics.Color.parseColor(colorStr))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = colorStr }
                            .then(
                                if (selectedColor == colorStr) Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape) else Modifier
                            )
                    )
                }
            }

            // Main Category / Subcategory Toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
            ) {
                ListItem(
                    headlineContent = { Text("Categoría principal", color = Color.White, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.Menu, null, tint = Color.Gray) }, // Grid icon placeholder
                    trailingContent = { if (!isSubcategory) Icon(Icons.Default.Check, null, tint = Color(0xFFE1D5F9)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { 
                        isSubcategory = false
                        selectedParentId = null
                    }
                )
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Subcategoría", color = Color.White, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray) }, // Nested icon placeholder
                    trailingContent = { 
                        if (isSubcategory) {
                            Text(
                                categories.find { it.id == selectedParentId }?.name ?: "Seleccionar",
                                color = Color(0xFFE1D5F9)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { 
                        isSubcategory = true
                        showParentPicker = true
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirm(name, selectedType, selectedEmoji, selectedColor, selectedParentId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5A9D4)),
                shape = RoundedCornerShape(28.dp),
                enabled = name.isNotBlank() && (!isSubcategory || selectedParentId != null)
            ) {
                Text(if (category == null) "Asignar nombre" else "Guardar cambios", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { 
                selectedEmoji = it
                showEmojiPicker = false
            }
        )
    }

    if (showParentPicker) {
        ParentCategoryPickerDialog(
            categories = categories.filter { it.type == selectedType && it.parentCategoryId == null && it.id != category?.id },
            onDismiss = { showParentPicker = false },
            onParentSelected = {
                selectedParentId = it.id
                showParentPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf(
        "🍴", "🥦", "🛍️", "🚕", "🍿", "🎁", "🌸", "✈️", "💼", "💊", "📚", "🏠", "💰", "💵", "🍺", "☕", "🎮", "🏋️", "🐶", "⛽",
        "🍎", "🍔", "🍕", "🍦", "🚲", "🚗", "🚂", "🚢", "🏢", "💻", "📱", "📷", "⚽", "🏀", "🎸", "🎨", "🎬", "👗", "👞",
        "🌈", "☀️", "🌙", "⭐", "🔥", "💧", "❄️", "🌱", "🍀", "🍓", "🍉", "🍇", "🥕", "🍦", "🍩", "🍪", "🍺", "🍷", "🍹",
        "🚀", "🚁", "🚜", "⚓", "🏠", "🏖️", "⛰️", "🏗️", "⌚", "⏰", "📻", "📺", "🔋", "🔌", "🔨", "🔧", "🔩", "🏹", "🛡️", "🔑"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Elige Un Icono", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
            
            Surface(
                onClick = { /* Acción para seleccionar emoji de teclado */ },
                color = Color(0xFF1E222D),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Seleccione un emoji de su elección como icono", color = Color.White, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
                }
            }

            Surface(
                onClick = { /* Acción para temas */ },
                color = Color(0xFF1E222D),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, null, tint = Color.White) // Placeholder for theme icon
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cambiar el tema y el estilo de los iconos", color = Color.White, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.heightIn(max = 400.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 32.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCategoryPickerDialog(
    categories: List<CategoryResponse>,
    onDismiss: () -> Unit,
    onParentSelected: (CategoryResponse) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Selecciona Una Categoría", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Seleccione la categoría principal para esta subcategoría", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 400.dp).padding(top = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryGridItem(category, onClick = { onParentSelected(category) })
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryPickerSheet(
    parentCategory: CategoryResponse,
    subcategories: List<CategoryResponse>,
    onDismiss: () -> Unit,
    onSubcategorySelected: (Long) -> Unit,
    onAddSubcategory: () -> Unit,
    onEditParent: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Seleccionar Subcategoría",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = onEditParent,
                    color = Color(0xFF2C313F),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // "Ninguno" Item
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubcategorySelected(parentCategory.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF2C313F), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ninguno", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Subcategories
                items(subcategories) { sub ->
                    val subColor = try {
                        Color(android.graphics.Color.parseColor(sub.color ?: "#7E57C2"))
                    } catch (e: Exception) {
                        Color(0xFF7E57C2)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubcategorySelected(sub.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(subColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getEmojiForCategory(sub.name, sub.icon), fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sub.name, color = Color.White, fontSize = 12.sp)
                    }
                }

                // "+" Add Button
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onAddSubcategory() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Transparent, RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoryGridItem(category: CategoryResponse, onClick: () -> Unit) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(categoryColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(getEmojiForCategory(category.name, category.icon), fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(category.name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun AccountChip(
    account: AccountResponse,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryIconItem(
    category: CategoryResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    categoryColor.copy(alpha = if (isSelected) 1f else 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .then(
                    if (isSelected) Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(getEmojiForCategory(category.name, category.icon), fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
