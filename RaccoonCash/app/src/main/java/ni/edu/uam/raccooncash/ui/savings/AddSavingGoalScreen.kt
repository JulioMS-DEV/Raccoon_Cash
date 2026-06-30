package ni.edu.uam.raccooncash.ui.savings

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.ui.accounts.accountColors
import ni.edu.uam.raccooncash.ui.components.EmojiPickerDialog
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private object AddGoalPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF8B5CF6)
    val Sky = Color(0xFF74C7EC)
    val Mint = Color(0xFF7EDC8D)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val Border = Color.White.copy(alpha = 0.08f)
}

private val goalEmojiShortcuts = listOf("💰", "✈️", "💻", "🏠", "🚗", "🎓", "🛡️", "🎁")
private const val AddSavingGoalLogTag = "SavingsFlow"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingGoalScreen(
    viewModel: SavingsViewModel,
    goalToEdit: SavingGoalResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val success by viewModel.addGoalSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember(goalToEdit?.id) { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmount by remember(goalToEdit?.id) { mutableStateOf(formatEditableMoney(goalToEdit?.targetAmount)) }
    var selectedColor by remember(goalToEdit?.id) { mutableStateOf(parseGoalEditColor(goalToEdit?.color) ?: accountColors.first()) }
    var selectedEmoji by remember(goalToEdit?.id) { mutableStateOf(goalToEdit?.icon ?: "💰") }
    var deadline by remember(goalToEdit?.id) { mutableStateOf(parseGoalEditDeadline(goalToEdit?.deadline)) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val targetAmountValue = parseMoneyInput(targetAmount)
    val canSave = name.isNotBlank() && targetAmountValue != null && targetAmountValue > 0.0 && !isLoading
    val isEditing = goalToEdit != null
    val amountPreview = targetAmountValue?.let { formatCurrencyAmount(it) } ?: formatCurrencyAmount(0.0)
    val deadlineLabel = formatGoalEditDate(deadline)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> deadline = LocalDate.of(year, month + 1, dayOfMonth) },
        deadline.year,
        deadline.monthValue - 1,
        deadline.dayOfMonth
    )

    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun showLocalError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun saveGoal() {
        if (isLoading) return
        val trimmedName = name.trim()
        val amount = parseMoneyInput(targetAmount)
        Log.d(AddSavingGoalLogTag, "saveGoal rawAmount='$targetAmount', parsedAmount=$amount, name='$trimmedName'")

        when {
            trimmedName.isBlank() -> {
                showLocalError("Ingresa el nombre de la meta.")
                return
            }
            targetAmount.isBlank() -> {
                showLocalError("Ingresa el monto objetivo de la meta.")
                return
            }
            amount == null -> {
                showLocalError("Ingresa un monto válido, por ejemplo 500 o 500.50.")
                return
            }
            amount <= 0.0 -> {
                showLocalError("El monto objetivo debe ser mayor a cero.")
                return
            }
        }

        val colorHex = selectedColor.toGoalHex()
        val deadlineText = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE)

        if (isEditing) {
            viewModel.updateSavingGoal(
                id = goalToEdit!!.id,
                name = trimmedName,
                targetAmount = amount,
                deadline = deadlineText,
                color = colorHex,
                icon = selectedEmoji
            )
        } else {
            viewModel.addSavingGoal(
                name = trimmedName,
                targetAmount = amount,
                deadline = deadlineText,
                color = colorHex,
                icon = selectedEmoji
            )
        }
    }

    if (showDeleteConfirmation && goalToEdit != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar objetivo?") },
            text = { Text("Esta acción no se puede deshacer y se perderán todos los datos asociados a este ahorro.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteSavingGoal(goalToEdit.id)
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFFF7A85))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = AddGoalPalette.ElevatedCard,
            titleContentColor = AddGoalPalette.TextPrimary,
            textContentColor = AddGoalPalette.TextSecondary
        )
    }

    Scaffold(
        containerColor = AddGoalPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddGoalTopBar(
                title = if (isEditing) "Editar meta" else "Nueva meta",
                goalColor = selectedColor,
                showDelete = isEditing,
                onBack = onBack,
                onDelete = { showDeleteConfirmation = true }
            )
        },
        bottomBar = {
            AddGoalSaveBar(
                enabled = canSave,
                isLoading = isLoading,
                goalColor = selectedColor,
                text = when {
                    isLoading -> "Guardando..."
                    name.isBlank() -> "Completa el nombre"
                    targetAmountValue == null || targetAmountValue <= 0.0 -> "Completa el monto"
                    isEditing -> "Guardar cambios"
                    else -> "Crear meta"
                },
                onClick = ::saveGoal
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AddGoalPalette.Background,
                            AddGoalPalette.BackgroundAlt,
                            AddGoalPalette.Background
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalTypeCard(goalColor = selectedColor)

            GoalNameCard(
                name = name,
                goalColor = selectedColor,
                onNameChange = { name = it }
            )

            GoalIconPickerCard(
                selectedEmoji = selectedEmoji,
                goalColor = selectedColor,
                onOpenPicker = { showEmojiPicker = true },
                onEmojiSelected = { selectedEmoji = it }
            )

            GoalColorPickerCard(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            GoalAmountCard(
                amount = targetAmount,
                amountPreview = amountPreview,
                goalColor = selectedColor,
                isError = targetAmount.isNotBlank() && targetAmountValue == null,
                onAmountChange = { if (isPotentialMoneyInput(it)) targetAmount = it }
            )

            GoalDateCard(
                deadlineLabel = deadlineLabel,
                goalColor = selectedColor,
                onClick = { datePickerDialog.show() }
            )

            GoalPreviewCard(
                name = name.ifBlank { "Nombre de la meta" },
                icon = selectedEmoji,
                amount = amountPreview,
                deadline = deadlineLabel,
                goalColor = selectedColor
            )
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            title = "Elige un emoji para la meta",
            onEmojiSelected = {
                selectedEmoji = it
                showEmojiPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalTopBar(
    title: String,
    goalColor: Color,
    showDelete: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Meta de ahorro", color = AddGoalPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(title, color = AddGoalPalette.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        navigationIcon = {
            AddGoalHeaderButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = AddGoalPalette.TextPrimary,
                borderColor = goalColor.copy(alpha = 0.35f),
                onClick = onBack,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        actions = {
            if (showDelete) {
                AddGoalHeaderButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFFF7A85),
                    borderColor = Color(0xFFFF7A85).copy(alpha = 0.32f),
                    onClick = onDelete,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AddGoalPalette.Background)
    )
}

@Composable
private fun AddGoalHeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(AddGoalPalette.ElevatedCard)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun GoalTypeCard(goalColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = AddGoalPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(goalColor.copy(alpha = 0.18f), AddGoalPalette.Card)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(goalColor.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.42f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Savings, contentDescription = null, tint = goalColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Meta de ahorro", color = AddGoalPalette.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("Define un objetivo y empieza a ahorrar", color = AddGoalPalette.TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun GoalNameCard(
    name: String,
    goalColor: Color,
    onNameChange: (String) -> Unit
) {
    PremiumGoalSectionCard(borderColor = goalColor.copy(alpha = 0.28f)) {
        Text("Nombre de la meta", color = AddGoalPalette.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Ej. Viaje, Laptop, Emergencia...", color = AddGoalPalette.TextSecondary.copy(alpha = 0.72f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = AddGoalPalette.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            colors = premiumGoalTextFieldColors(goalColor)
        )
    }
}

@Composable
private fun GoalIconPickerCard(
    selectedEmoji: String,
    goalColor: Color,
    onOpenPicker: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    PremiumGoalSectionCard(borderColor = goalColor.copy(alpha = 0.34f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(goalColor.copy(alpha = 0.18f))
                    .border(BorderStroke(2.dp, goalColor), CircleShape)
                    .clickable { onOpenPicker() },
                contentAlignment = Alignment.Center
            ) {
                Text(selectedEmoji, fontSize = 42.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Icono de la meta", color = AddGoalPalette.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Text("Toca el círculo para ver más opciones", color = AddGoalPalette.TextSecondary, fontSize = 13.sp)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 4.dp)) {
            items(goalEmojiShortcuts) { emoji ->
                val selected = emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (selected) goalColor.copy(alpha = 0.20f) else AddGoalPalette.ElevatedCard)
                        .border(
                            BorderStroke(if (selected) 2.dp else 1.dp, if (selected) goalColor else AddGoalPalette.Border),
                            CircleShape
                        )
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun GoalColorPickerCard(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    PremiumGoalSectionCard(borderColor = selectedColor.copy(alpha = 0.30f)) {
        Text("Color de la meta", color = AddGoalPalette.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
        Text("Este color se aplicará al progreso, bordes y botón", color = AddGoalPalette.TextSecondary, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)) {
            items(accountColors) { color ->
                val selected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(BorderStroke(if (selected) 3.dp else 1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.18f)), CircleShape)
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = "Color seleccionado", tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalAmountCard(
    amount: String,
    amountPreview: String,
    goalColor: Color,
    isError: Boolean,
    onAmountChange: (String) -> Unit
) {
    val amountFontSize = when {
        amount.length > 12 -> 30.sp
        amount.length > 8 -> 34.sp
        else -> 40.sp
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = AddGoalPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, if (isError) Color(0xFFFF7A85) else goalColor.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(goalColor.copy(alpha = 0.18f), AddGoalPalette.ElevatedCard, AddGoalPalette.Card)))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Objetivo", color = AddGoalPalette.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("C$", color = goalColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = {
                        Text("0.00", color = AddGoalPalette.TextSecondary.copy(alpha = 0.55f), fontSize = amountFontSize, fontWeight = FontWeight.ExtraBold)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(
                        color = AddGoalPalette.TextPrimary,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    ),
                    colors = premiumGoalTextFieldColors(goalColor)
                )
            }
            Text(
                text = if (isError) "Ingresa un monto válido, por ejemplo 500 o 500.50." else "Vista previa: $amountPreview",
                color = if (isError) Color(0xFFFF7A85) else AddGoalPalette.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GoalDateCard(
    deadlineLabel: String,
    goalColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = AddGoalPalette.ElevatedCard,
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.28f)),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(goalColor.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.38f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = goalColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Fecha objetivo", color = AddGoalPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(deadlineLabel, color = AddGoalPalette.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Toca para cambiar la fecha", color = AddGoalPalette.TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AddGoalPalette.TextSecondary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun GoalPreviewCard(
    name: String,
    icon: String,
    amount: String,
    deadline: String,
    goalColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AddGoalPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.36f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = goalColor, modifier = Modifier.size(18.dp))
                Text("Vista previa", color = AddGoalPalette.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(goalColor.copy(alpha = 0.18f), CircleShape)
                        .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.44f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 30.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(name, color = AddGoalPalette.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(deadline, color = AddGoalPalette.TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(amount, color = goalColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PremiumGoalSectionCard(
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = AddGoalPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun AddGoalSaveBar(
    enabled: Boolean,
    isLoading: Boolean,
    goalColor: Color,
    text: String,
    onClick: () -> Unit
) {
    val active = enabled && !isLoading
    Surface(color = AddGoalPalette.Background.copy(alpha = 0.96f), tonalElevation = 8.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .height(62.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        if (active) listOf(AddGoalPalette.LavenderDeep, goalColor, AddGoalPalette.Lavender)
                        else listOf(AddGoalPalette.ElevatedCard, AddGoalPalette.Card)
                    )
                )
                .border(BorderStroke(1.dp, if (active) Color.White.copy(alpha = 0.20f) else AddGoalPalette.Border), RoundedCornerShape(999.dp))
                .clickable(enabled = !isLoading) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = AddGoalPalette.TextPrimary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (active) AddGoalPalette.TextPrimary else AddGoalPalette.TextSecondary, modifier = Modifier.size(19.dp))
                    Text(text, color = if (active) AddGoalPalette.TextPrimary else AddGoalPalette.TextSecondary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun premiumGoalTextFieldColors(goalColor: Color) = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = goalColor,
    focusedTextColor = AddGoalPalette.TextPrimary,
    unfocusedTextColor = AddGoalPalette.TextPrimary,
    focusedPlaceholderColor = AddGoalPalette.TextSecondary,
    unfocusedPlaceholderColor = AddGoalPalette.TextSecondary
)

private fun parseGoalEditColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"
    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: Exception) {
        null
    }
}

private fun parseGoalEditDeadline(deadline: String?): LocalDate {
    return try {
        deadline?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } ?: LocalDate.now().plusYears(1)
    } catch (e: Exception) {
        LocalDate.now().plusYears(1)
    }
}

private fun formatGoalEditDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es")) else it.toString() }
}

private fun Color.toGoalHex(): String {
    return String.format("#%06X", 0xFFFFFF and toArgb())
}
