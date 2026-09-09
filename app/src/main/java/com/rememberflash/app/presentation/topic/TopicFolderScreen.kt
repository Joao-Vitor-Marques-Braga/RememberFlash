package com.rememberflash.app.presentation.topic

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.presentation.theme.AccentOrange
import com.rememberflash.app.presentation.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFolderScreen(
    viewModel: TopicFolderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResolveQuestions: (Long, Long) -> Unit, // disciplineId, topicId
    onNavigateToStudyDeck: (Long) -> Unit // disciplineId
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Flashcards (${uiState.flashcards.size})", "Questões (${uiState.questions.size})")

    // Flashcard Form State
    var showFlashcardFormDialog by remember { mutableStateOf(false) }
    var editingFlashcard by remember { mutableStateOf<Flashcard?>(null) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var flashcardFormError by remember { mutableStateOf<String?>(null) }
    var cardToDelete by remember { mutableStateOf<Flashcard?>(null) }

    // PDF Import State
    var showPdfImportDialog by remember { mutableStateOf(false) }
    var pdfQuantitySelection by remember { mutableStateOf("10") }
    var customPdfQuantity by remember { mutableStateOf("") }

    // Question Gen State
    var showQuestionGenDialog by remember { mutableStateOf(false) }
    var questionQuantitySelection by remember { mutableStateOf("5") }

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

            if (size > 10 * 1024 * 1024) {
                viewModel.setPdfError("O arquivo deve ser um PDF de até 10MB.")
            } else {
                viewModel.onPdfSelected(uri, name, size)
            }
        }
    }

    LaunchedEffect(uiState.isQuestionsGeneratedSuccess) {
        if (uiState.isQuestionsGeneratedSuccess) {
            viewModel.resetQuestionsSuccess()
            onNavigateToResolveQuestions(viewModel.disciplineId, viewModel.topicId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    LaunchedEffect(showFlashcardFormDialog) {
        if (showFlashcardFormDialog) {
            frontText = editingFlashcard?.front ?: ""
            backText = editingFlashcard?.back ?: ""
            flashcardFormError = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.topic?.name ?: "Pasta do Tópico",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Matéria: ${uiState.discipline?.name ?: "Carregando..."}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão Sincronizar
                    IconButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Sincronizando...", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.syncNow { msg ->
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
                        )
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
            // Status do Tópico (Concluído / Pendente)
            Surface(
                color = if (uiState.topic?.isCompleted == true) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (uiState.topic?.isCompleted == true) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (uiState.topic?.isCompleted == true) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (uiState.topic?.isCompleted == true) "Tópico Estudado e Concluído" else "Tópico Pendente de Estudo",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.topic?.isCompleted == true) SuccessGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Marque quando terminar de estudar este conteúdo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.topic?.isCompleted == true,
                        onCheckedChange = { isDone ->
                            viewModel.toggleTopicCompletion(isDone)
                        }
                    )
                }
            }

            // Tabs (Flashcards / Questões)
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
                            // TAB FLASHCARDS DO TÓPICO
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Ações de Flashcard
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            editingFlashcard = null
                                            showFlashcardFormDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
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
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Via PDF")
                                    }

                                    if (uiState.flashcards.isNotEmpty()) {
                                        Button(
                                            onClick = { onNavigateToStudyDeck(viewModel.disciplineId) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Estudar")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.flashcards.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Nenhum flashcard criado nesta pasta de tópico.\n\nCrie manualmente ou importe uma aula em PDF para gerar cards automáticos com IA!",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(uiState.flashcards, key = { it.id }) { card ->
                                            TopicFlashcardItemCard(
                                                flashcard = card,
                                                onEdit = {
                                                    editingFlashcard = card
                                                    showFlashcardFormDialog = true
                                                },
                                                onDelete = {
                                                    cardToDelete = card
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB QUESTÕES DO TÓPICO
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Ações de Questões
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { showQuestionGenDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gerar Questões IA")
                                    }

                                    if (uiState.questions.isNotEmpty()) {
                                        Button(
                                            onClick = { onNavigateToResolveQuestions(viewModel.disciplineId, viewModel.topicId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Resolver (${uiState.questions.size})")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.questions.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Quiz,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Nenhuma questão gerada para este tópico ainda.\n\nToque em 'Gerar Questões IA' para gerar um simulado focado especificamente neste assunto!",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(uiState.questions, key = { it.id }) { question ->
                                            TopicQuestionItemCard(question = question)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Formulário Flashcard Manual
    if (showFlashcardFormDialog) {
        AlertDialog(
            onDismissRequest = { showFlashcardFormDialog = false },
            title = {
                Text(if (editingFlashcard == null) "Novo Flashcard no Tópico" else "Editar Flashcard")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pasta: ${uiState.topic?.name ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = frontText,
                        onValueChange = {
                            frontText = it
                            flashcardFormError = null
                        },
                        label = { Text("Frente (Pergunta ou Conceito)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = backText,
                        onValueChange = {
                            backText = it
                            flashcardFormError = null
                        },
                        label = { Text("Verso (Resposta ou Explicação)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    flashcardFormError?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (frontText.isBlank()) {
                            flashcardFormError = "Preencha a frente do flashcard"
                            return@Button
                        }
                        if (backText.isBlank()) {
                            flashcardFormError = "Preencha o verso do flashcard"
                            return@Button
                        }

                        if (editingFlashcard != null) {
                            val updated = editingFlashcard!!.copy(
                                front = frontText.trim(),
                                back = backText.trim()
                            )
                            viewModel.updateFlashcard(updated) {
                                showFlashcardFormDialog = false
                            }
                        } else {
                            viewModel.createFlashcard(frontText.trim(), backText.trim()) {
                                showFlashcardFormDialog = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
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

    // Modal Importar Flashcards via PDF
    if (showPdfImportDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isGeneratingFlashcards) {
                    showPdfImportDialog = false
                    viewModel.clearPdfSelection()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extrair Cards via PDF")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "A IA analisará o PDF selecionado e criará cards salvos diretamente nesta pasta (${uiState.topic?.name ?: ""}).",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Card do Arquivo Selecionado
                    if (uiState.pdfUri != null) {
                        val sizeInMb = "%.2f MB".format(uiState.pdfSize / (1024f * 1024f))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uiState.pdfName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = sizeInMb, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(
                                    onClick = { viewModel.clearPdfSelection() },
                                    enabled = !uiState.isGeneratingFlashcards
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar Arquivo PDF (até 10MB)")
                        }
                    }

                    // Seletor de Quantidade
                    Text("Quantidade de Cards a Gerar:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5", "10", "20", "Outro").forEach { opt ->
                            FilterChip(
                                selected = pdfQuantitySelection == opt,
                                onClick = { pdfQuantitySelection = opt },
                                label = { Text(opt) }
                            )
                        }
                    }

                    if (pdfQuantitySelection == "Outro") {
                        OutlinedTextField(
                            value = customPdfQuantity,
                            onValueChange = { customPdfQuantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Quantidade personalizada") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (uiState.isGeneratingFlashcards) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("A IA está analisando o PDF...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = if (pdfQuantitySelection == "Outro") {
                            customPdfQuantity.toIntOrNull() ?: 10
                        } else {
                            pdfQuantitySelection.toIntOrNull() ?: 10
                        }
                        viewModel.generateFlashcardsFromPdf(qty)
                    },
                    enabled = uiState.pdfUri != null && !uiState.isGeneratingFlashcards,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Gerar Cards")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPdfImportDialog = false
                        viewModel.clearPdfSelection()
                    },
                    enabled = !uiState.isGeneratingFlashcards
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal Gerar Questões por IA
    if (showQuestionGenDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isGeneratingQuestions) {
                    showQuestionGenDialog = false
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar Questões do Tópico")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "A IA criará um simulado exclusivo sobre o tópico '${uiState.topic?.name ?: ""}'.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("Quantidade de Questões:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5", "10", "15", "20").forEach { opt ->
                            FilterChip(
                                selected = questionQuantitySelection == opt,
                                onClick = { questionQuantitySelection = opt },
                                label = { Text(opt) }
                            )
                        }
                    }

                    if (uiState.isGeneratingQuestions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Criando questões com IA...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = questionQuantitySelection.toIntOrNull() ?: 5
                        viewModel.generateQuestionsIA(qty)
                    },
                    enabled = !uiState.isGeneratingQuestions,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Gerar e Iniciar")
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

    // Modal Confirmar Exclusão de Card
    if (cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Excluir Flashcard") },
            text = { Text("Tem certeza de que deseja remover este flashcard da pasta do tópico?") },
            confirmButton = {
                Button(
                    onClick = {
                        cardToDelete?.let { viewModel.deleteFlashcard(it.id) }
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TopicFlashcardItemCard(
    flashcard: Flashcard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isFlipped) AccentOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isFlipped) "VERSO (RESPOSTA)" else "FRENTE (PERGUNTA)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isFlipped) AccentOrange else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFlipped) flashcard.back else flashcard.front,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Toque para virar o card ↺",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TopicQuestionItemCard(question: Question) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = question.statement,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                question.options.forEachIndexed { idx, opt ->
                    val isCorrect = idx == question.correctIndex
                    Surface(
                        color = if (isCorrect) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .border(
                                width = if (isCorrect) 1.dp else 0.dp,
                                color = if (isCorrect) SuccessGreen else androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = "${('A' + idx)}) $opt",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (!question.explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explicação: ${question.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toque para ver as alternativas e gabarito",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
