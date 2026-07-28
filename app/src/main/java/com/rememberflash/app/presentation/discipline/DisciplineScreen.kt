package com.rememberflash.app.presentation.discipline

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.presentation.theme.AccentOrange
import com.rememberflash.app.presentation.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineScreen(
    viewModel: DisciplineViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResolveQuestions: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Flashcards", "Simulados e Questões")

    // Dialog state (CRUD Flashcard)
    var showFlashcardFormDialog by remember { mutableStateOf(false) }
    var editingFlashcard by remember { mutableStateOf<Flashcard?>(null) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var flashcardFormError by remember { mutableStateOf<String?>(null) }
    var highlightFrontError by remember { mutableStateOf(false) }
    var highlightBackError by remember { mutableStateOf(false) }
    var flashcardToDelete by remember { mutableStateOf<Flashcard?>(null) }

    // PDF Import UI state
    var showPdfImportDialog by remember { mutableStateOf(false) }
    var pdfQuantitySelection by remember { mutableStateOf("10") }
    var customPdfQuantity by remember { mutableStateOf("") }

    // Question Gen UI state
    var showQuestionGenDialog by remember { mutableStateOf(false) }
    var questionQuantitySelection by remember { mutableStateOf("5") }
    var questionThemeInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // PDF Launcher contract
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var size = 0L
            var name = "documento.pdf"
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                    if (nameIndex != -1) name = it.getString(nameIndex)
                }
            }

            // Validação de Tamanho (10MB = 10 * 1024 * 1024 bytes)
            if (size > 10 * 1024 * 1024) {
                viewModel.setPdfError("O arquivo deve ser um PDF de até 10MB. Tente dividir o documento ou escolher um arquivo menor.")
            } else {
                viewModel.onPdfSelected(uri, name, size)
            }
        }
    }

    LaunchedEffect(uiState.isQuestionsGeneratedSuccess) {
        if (uiState.isQuestionsGeneratedSuccess) {
            viewModel.resetQuestionsSuccess()
            onNavigateToResolveQuestions(viewModel.disciplineId)
        }
    }

    LaunchedEffect(showFlashcardFormDialog) {
        if (showFlashcardFormDialog) {
            frontText = editingFlashcard?.front ?: ""
            backText = editingFlashcard?.back ?: ""
            flashcardFormError = null
            highlightFrontError = false
            highlightBackError = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.discipline?.name ?: "Carregando...",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTab) {
                        0 -> {
                            // FLASHCARDS TAB
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Buttons Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            editingFlashcard = null
                                            showFlashcardFormDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Manual")
                                    }

                                    Button(
                                        onClick = { showPdfImportDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gerar via PDF")
                                    }
                                }

                                if (uiState.flashcards.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Nenhum flashcard cadastrado nesta disciplina.\nCrie manualmente ou envie um PDF de estudos!",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(32.dp)
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(uiState.flashcards, key = { it.id }) { card ->
                                            com.rememberflash.app.presentation.flashcard.FlashcardListItem(
                                                card = card,
                                                onEdit = {
                                                    editingFlashcard = card
                                                    showFlashcardFormDialog = true
                                                },
                                                onDelete = {
                                                    flashcardToDelete = card
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // QUESTIONS TAB
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Geração de Questões Cognitivas",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Gere questões focadas especificamente na sua banca e nos parâmetros da sua IA de forma dinâmica.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                        )

                                        Button(
                                            onClick = { showQuestionGenDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Gerar Novo Lote de Questões")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                if (uiState.questions.isNotEmpty()) {
                                    Text(
                                        text = "Lote de Questões Ativo",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Text(
                                        text = "Existe um lote de ${uiState.questions.size} questões pronto para estudo.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .padding(top = 4.dp, bottom = 16.dp)
                                    )

                                    Button(
                                        onClick = { onNavigateToResolveQuestions(viewModel.disciplineId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(55.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Iniciar Simulado", fontWeight = FontWeight.Bold)
                                    }

                                    // Lista de Tentativas Anteriores
                                    if (uiState.attempts.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = "Histórico de Tentativas:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        uiState.attempts.forEachIndexed { index, attempt ->
                                            val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(attempt.createdAt))
                                            val pct = (attempt.score.toFloat() / attempt.totalQuestions.toFloat() * 100).toInt()
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Tentativa #${uiState.attempts.size - index}",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = dateFormatted,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                    Text(
                                                        text = "${attempt.score} / ${attempt.totalQuestions} acertos ($pct%)",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (pct >= 70) SuccessGreen else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Nenhuma questão gerada.\nToque no botão acima para criar o seu simulado.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Upload PDF (RF007)
    if (showPdfImportDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isGeneratingFlashcards) {
                    showPdfImportDialog = false
                    viewModel.clearPdf()
                }
            },
            title = { Text("Geração de Flashcards via PDF") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quantidade de Flashcards a gerar:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var showPdfMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (pdfQuantitySelection == "x") "Outro (Digitar)" else "$pdfQuantitySelection flashcards",
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPdfMenu) }
                        )
                        DropdownMenu(
                            expanded = showPdfMenu,
                            onDismissRequest = { showPdfMenu = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            listOf("5", "10", "15", "20", "x").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(if (option == "x") "Outro" else "$option flashcards") },
                                    onClick = {
                                        pdfQuantitySelection = option
                                        showPdfMenu = false
                                    }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showPdfMenu = true }
                        )
                    }

                    if (pdfQuantitySelection == "x") {
                        OutlinedTextField(
                            value = customPdfQuantity,
                            onValueChange = { customPdfQuantity = it },
                            label = { Text("Digite a quantidade") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selecione o arquivo PDF:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (uiState.pdfName.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(uiState.pdfName, fontWeight = FontWeight.Bold, maxLines = 1)
                                    val sizeMb = uiState.pdfSize / (1024f * 1024f)
                                    Text(String.format("%.2f MB", sizeMb), style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = viewModel::clearPdf, enabled = !uiState.isGeneratingFlashcards) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar PDF")
                        }
                    }

                    if (uiState.pdfError != null) {
                        Text(
                            text = uiState.pdfError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = if (pdfQuantitySelection == "x") {
                            customPdfQuantity.toIntOrNull() ?: 10
                        } else {
                            pdfQuantitySelection.toIntOrNull() ?: 10
                        }
                        viewModel.generateFlashcardsFromPdf(qty)
                    },
                    enabled = uiState.pdfUri != null && !uiState.isGeneratingFlashcards,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isGeneratingFlashcards) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Gerar Flashcards")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPdfImportDialog = false
                        viewModel.clearPdf()
                    },
                    enabled = !uiState.isGeneratingFlashcards
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Geração de Questões (RF008)
    if (showQuestionGenDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isGeneratingQuestions) showQuestionGenDialog = false },
            title = { Text("Geração de Questões") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tema Específico (Opcional):",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = questionThemeInput,
                        onValueChange = { questionThemeInput = it },
                        label = { Text("Ex: Atos administrativos, Crise de 29") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Quantidade de Questões:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var showQtyMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "$questionQuantitySelection questões",
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showQtyMenu) }
                        )
                        DropdownMenu(
                            expanded = showQtyMenu,
                            onDismissRequest = { showQtyMenu = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            listOf("5", "10", "15", "20").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option questões") },
                                    onClick = {
                                        questionQuantitySelection = option
                                        showQtyMenu = false
                                    }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showQtyMenu = true }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = questionQuantitySelection.toIntOrNull() ?: 5
                        viewModel.generateQuestionsIA(qty, questionThemeInput)
                        showQuestionGenDialog = false
                    },
                    enabled = !uiState.isGeneratingQuestions,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Gerar Questões")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuestionGenDialog = false },
                    enabled = !uiState.isGeneratingQuestions
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de carregamento geral para processos longos de IA
    if (uiState.isGeneratingFlashcards || uiState.isGeneratingQuestions) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = if (uiState.isGeneratingFlashcards) "Extraindo do PDF..." else "Elaborando Questões...",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.isGeneratingFlashcards) "O Gemini está analisando seu PDF e criando os cartões estruturados." else "Montando prompt cognitivo e requisitando lote à IA.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Modal de Erro / Avisos da IA (RF007 A2/A3, RF008 A1/A2/A3)
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Aviso do Sistema", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                Button(onClick = viewModel::clearError) {
                    Text("OK")
                }
            }
        )
    }

    // Modal de Criação / Edição de Flashcard Manual (RF006)
    if (showFlashcardFormDialog) {
        AlertDialog(
            onDismissRequest = { showFlashcardFormDialog = false },
            title = {
                Text(
                    text = if (editingFlashcard == null) "Novo Flashcard" else "Editar Flashcard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (flashcardFormError != null) {
                        Text(
                            text = flashcardFormError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = frontText,
                        onValueChange = {
                            frontText = it
                            if (it.isNotBlank()) highlightFrontError = false
                        },
                        label = { Text("Frente (Pergunta/Título)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = highlightFrontError,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = backText,
                        onValueChange = {
                            backText = it
                            if (it.isNotBlank()) highlightBackError = false
                        },
                        label = { Text("Verso (Resposta/Descrição)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        isError = highlightBackError,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validação Local (RF006 A2)
                        if (frontText.isBlank() || backText.isBlank()) {
                            highlightFrontError = frontText.isBlank()
                            highlightBackError = backText.isBlank()
                            flashcardFormError = "A frente e o verso do cartão são obrigatórios."
                            return@Button
                        }

                        scope.launch {
                            val result = if (editingFlashcard == null) {
                                viewModel.createManualFlashcard(frontText, backText)
                            } else {
                                viewModel.updateManualFlashcard(editingFlashcard!!, frontText, backText)
                            }

                            when (result) {
                                is Result.Success -> {
                                    snackbarHostState.showSnackbar(
                                        if (editingFlashcard == null) "Flashcard salvo com sucesso!" else "Flashcard atualizado!"
                                    )
                                    showFlashcardFormDialog = false
                                }
                                is Result.Error -> {
                                    flashcardFormError = result.message
                                }
                                else -> {}
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlashcardFormDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmação de Exclusão (RF006 A3)
    if (flashcardToDelete != null) {
        AlertDialog(
            onDismissRequest = { flashcardToDelete = null },
            title = { Text("Excluir Flashcard", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja excluir este cartão?") },
            confirmButton = {
                Button(
                    onClick = {
                        val card = flashcardToDelete!!
                        flashcardToDelete = null
                        scope.launch {
                            when (val result = viewModel.deleteManualFlashcard(card.id)) {
                                is Result.Success -> {
                                    snackbarHostState.showSnackbar("Flashcard excluído")
                                }
                                is Result.Error -> {
                                    snackbarHostState.showSnackbar("Erro ao excluir: ${result.message}")
                                }
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { flashcardToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
