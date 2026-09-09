package com.rememberflash.app.presentation.discipline

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.Topic
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
    val tabs = listOf("Subpastas & Tópicos", "Flashcards", "Simulados e Questões")

    // Topic Dialog states
    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var topicNameInput by remember { mutableStateOf("") }
    var topicDescInput by remember { mutableStateOf("") }
    var topicFormError by remember { mutableStateOf<String?>(null) }
    var topicToDelete by remember { mutableStateOf<Topic?>(null) }

    // Dialog state (CRUD Flashcard)
    var showFlashcardFormDialog by remember { mutableStateOf(false) }
    var editingFlashcard by remember { mutableStateOf<Flashcard?>(null) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var selectedTopicIdForCard by remember { mutableStateOf<Long?>(null) }
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
            selectedTopicIdForCard = editingFlashcard?.topicId ?: uiState.selectedTopicId
            flashcardFormError = null
            highlightFrontError = false
            highlightBackError = false
        }
    }

    val isSyncing by viewModel.isSyncing.collectAsState()
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
                        Text(
                            text = uiState.discipline?.name ?: "Carregando...",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.topics.isNotEmpty()) {
                            Text(
                                text = "${uiState.discipline?.completedTopics ?: 0}/${uiState.topics.size} submatérias estudadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Sincronizando com a nuvem...", android.widget.Toast.LENGTH_SHORT).show()
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
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
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
                            // TAB 0: SUBPASTAS & TÓPICOS
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Barra de Progresso da Disciplina
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        val total = uiState.topics.size
                                        val completed = uiState.topics.count { it.isCompleted }
                                        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                                        val pct = (progress * 100).toInt()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Progresso do Conteúdo",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "$pct%",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (pct >= 100) SuccessGreen else MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp),
                                            color = if (pct >= 100) SuccessGreen else MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = if (total > 0) "$completed de $total tópicos concluídos" else "Nenhum subtópico cadastrado ainda",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Header com botão de adicionar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Subpastas da Disciplina (${uiState.topics.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )

                                    FilledTonalButton(
                                        onClick = {
                                            topicNameInput = ""
                                            topicDescInput = ""
                                            topicFormError = null
                                            showCreateTopicDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nova Subpasta")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.topics.isEmpty()) {
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
                                                imageVector = Icons.Default.FolderSpecial,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Nenhuma subpasta/tópico cadastrado.\n\nAo cadastrar um edital via PDF, os tópicos são extraídos automaticamente, ou você pode criar manualmente usando o botão acima!",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(uiState.topics, key = { it.id }) { topic ->
                                            TopicItemCard(
                                                topic = topic,
                                                onToggleCompletion = { isDone ->
                                                    viewModel.toggleTopicCompletion(topic.id, isDone)
                                                },
                                                onOpenFlashcards = {
                                                    viewModel.selectTopic(topic.id)
                                                    selectedTab = 1
                                                },
                                                onGenerateQuestions = {
                                                    questionThemeInput = topic.name
                                                    showQuestionGenDialog = true
                                                },
                                                onDelete = {
                                                    topicToDelete = topic
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB 1: FLASHCARDS TAB
                            val activeFilterTopic = uiState.topics.find { it.id == uiState.selectedTopicId }
                            val displayedFlashcards = if (uiState.selectedTopicId != null) {
                                uiState.flashcards.filter { it.topicId == uiState.selectedTopicId }
                            } else {
                                uiState.flashcards
                            }

                            Column(modifier = Modifier.fillMaxSize()) {
                                // Banner de filtro por tópico caso ativo
                                if (activeFilterTopic != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Pasta: ${activeFilterTopic.name}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            TextButton(
                                                onClick = { viewModel.selectTopic(null) }
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Limpar filtro", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ver Todos")
                                            }
                                        }
                                    }
                                }

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

                                if (displayedFlashcards.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (activeFilterTopic != null)
                                                "Nenhum flashcard nesta subpasta ainda.\nToque no botão Manual para adicionar!"
                                            else
                                                "Nenhum flashcard cadastrado nesta disciplina.\nCrie manualmente ou envie um PDF de estudos!",
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
                                        items(displayedFlashcards, key = { it.id }) { card ->
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
                        2 -> {
                            // TAB 2: QUESTIONS TAB
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
                                            text = "Gere questões focadas na sua banca organizadora ou em subtópicos específicos da matéria.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                        )

                                        Button(
                                            onClick = {
                                                questionThemeInput = ""
                                                showQuestionGenDialog = true
                                            },
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

    // Modal de Criação de Nova Subpasta / Tópico
    if (showCreateTopicDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTopicDialog = false },
            title = { Text("Nova Subpasta / Tópico", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (topicFormError != null) {
                        Text(
                            text = topicFormError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = topicNameInput,
                        onValueChange = { topicNameInput = it },
                        label = { Text("Nome da Subpasta/Tópico") },
                        placeholder = { Text("Ex: Modelagem de dados e SQL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = topicDescInput,
                        onValueChange = { topicDescInput = it },
                        label = { Text("Descrição / Ementa (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicNameInput.isBlank()) {
                            topicFormError = "O nome do tópico é obrigatório."
                            return@Button
                        }
                        viewModel.createTopic(topicNameInput, topicDescInput) {
                            showCreateTopicDialog = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTopicDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Exclusão de Tópico
    if (topicToDelete != null) {
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = { Text("Excluir Subpasta", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja excluir o tópico \"${topicToDelete?.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        topicToDelete?.let { viewModel.deleteTopic(it.id) }
                        topicToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
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
                        text = "Tema ou Subpasta da Matéria:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Se existirem tópicos, podemos permitir selecionar ou digitar
                    if (uiState.topics.isNotEmpty()) {
                        var showTopicDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = questionThemeInput.ifBlank { "Selecionar da lista ou digitar abaixo" },
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTopicDropdown) }
                            )
                            DropdownMenu(
                                expanded = showTopicDropdown,
                                onDismissRequest = { showTopicDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Geral (Toda a matéria)") },
                                    onClick = {
                                        questionThemeInput = ""
                                        showTopicDropdown = false
                                    }
                                )
                                uiState.topics.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            questionThemeInput = t.name
                                            showTopicDropdown = false
                                        }
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showTopicDropdown = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = questionThemeInput,
                        onValueChange = { questionThemeInput = it },
                        label = { Text("Tema Específico (Personalizado)") },
                        placeholder = { Text("Ex: Transações ACID, Normalização") },
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
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {}
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.isGeneratingFlashcards) "Gerando Flashcards com IA..." else "Gerando Questões com IA...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.isGeneratingFlashcards) "O Gemini está analisando seu PDF e criando os cartões estruturados." else "Montando prompt cognitivo e requisitando lote à IA.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Modal de Erro / Avisos da IA
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

                    // Seleção opcional de Subpasta / Tópico para o Flashcard
                    if (uiState.topics.isNotEmpty()) {
                        var showCardTopicMenu by remember { mutableStateOf(false) }
                        val selectedTopicName = uiState.topics.find { it.id == selectedTopicIdForCard }?.name ?: "Sem subpasta (Geral)"

                        Text("Subpasta / Tópico:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedTopicName,
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCardTopicMenu) }
                            )
                            DropdownMenu(
                                expanded = showCardTopicMenu,
                                onDismissRequest = { showCardTopicMenu = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sem subpasta (Geral)") },
                                    onClick = {
                                        selectedTopicIdForCard = null
                                        showCardTopicMenu = false
                                    }
                                )
                                uiState.topics.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            selectedTopicIdForCard = t.id
                                            showCardTopicMenu = false
                                        }
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showCardTopicMenu = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = frontText,
                        onValueChange = {
                            frontText = it
                            if (it.isNotBlank()) highlightFrontError = false
                        },
                        label = { Text("Frente (Pergunta/Conceito)") },
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
                        label = { Text("Verso (Resposta/Explicação)") },
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
                        // Validação Local
                        if (frontText.isBlank() || backText.isBlank()) {
                            highlightFrontError = frontText.isBlank()
                            highlightBackError = backText.isBlank()
                            flashcardFormError = "A frente e o verso do cartão são obrigatórios."
                            return@Button
                        }

                        scope.launch {
                            val result = if (editingFlashcard == null) {
                                viewModel.createManualFlashcard(frontText, backText, selectedTopicIdForCard)
                            } else {
                                viewModel.updateManualFlashcard(editingFlashcard!!, frontText, backText, selectedTopicIdForCard)
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

    // Modal de Confirmação de Exclusão de Flashcard
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

@Composable
private fun TopicItemCard(
    topic: Topic,
    onToggleCompletion: (Boolean) -> Unit,
    onOpenFlashcards: () -> Unit,
    onGenerateQuestions: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (topic.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = topic.isCompleted,
                    onCheckedChange = onToggleCompletion,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (topic.isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (topic.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            ),
                            color = if (topic.isCompleted)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!topic.description.isNullOrBlank()) {
                        Text(
                            text = topic.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contadores de conteúdo e Ações
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${topic.flashcardsCount} cards",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${topic.questionsCount} questões",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenFlashcards,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Style,
                                    contentDescription = "Ver Flashcards do tópico",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onGenerateQuestions,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Gerar questões do tópico",
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir tópico",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
