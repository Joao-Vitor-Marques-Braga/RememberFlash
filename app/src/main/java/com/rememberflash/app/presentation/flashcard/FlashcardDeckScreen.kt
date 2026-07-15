package com.rememberflash.app.presentation.flashcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun FlashcardDeckScreen(
    viewModel: FlashcardDeckViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog state
    var showFormDialog by remember { mutableStateOf(false) }
    var editingFlashcard by remember { mutableStateOf<Flashcard?>(null) }
    
    // Form fields
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var highlightFrontError by remember { mutableStateOf(false) }
    var highlightBackError by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    var flashcardToDelete by remember { mutableStateOf<Flashcard?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showFormDialog) {
        if (showFormDialog) {
            frontText = editingFlashcard?.front ?: ""
            backText = editingFlashcard?.back ?: ""
            formError = null
            highlightFrontError = false
            highlightBackError = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.discipline?.name?.let { "Flashcards - $it" } ?: "Carregando...",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingFlashcard = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Flashcard")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.flashcards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nenhum flashcard cadastrado nesta disciplina.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toque no botão '+' abaixo para criar o seu primeiro cartão.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.flashcards,
                        key = { it.id }
                    ) { card ->
                        FlashcardListItem(
                            card = card,
                            onEdit = {
                                editingFlashcard = card
                                showFormDialog = true
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

    // Modal de Criação / Edição de Flashcard
    if (showFormDialog) {
        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            title = {
                Text(
                    text = if (editingFlashcard == null) "Novo Flashcard" else "Editar Flashcard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (formError != null) {
                        Text(
                            text = formError!!,
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validação Local (A2)
                        if (frontText.isBlank() || backText.isBlank()) {
                            highlightFrontError = frontText.isBlank()
                            highlightBackError = backText.isBlank()
                            formError = "A frente e o verso do cartão são obrigatórios."
                            return@Button
                        }

                        scope.launch {
                            val result = if (editingFlashcard == null) {
                                viewModel.createFlashcard(frontText, backText)
                            } else {
                                viewModel.updateFlashcard(editingFlashcard!!, frontText, backText)
                            }

                            when (result) {
                                is Result.Success -> {
                                    snackbarHostState.showSnackbar(
                                        if (editingFlashcard == null) "Flashcard salvo com sucesso!" else "Flashcard atualizado!"
                                    )
                                    showFormDialog = false
                                }
                                is Result.Error -> {
                                    formError = result.message
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
                TextButton(onClick = { showFormDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmação de Exclusão (A3)
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
                            when (val result = viewModel.deleteFlashcard(card.id)) {
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
fun FlashcardListItem(
    card: Flashcard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FRENTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Indicador de Sincronização
                if (card.isSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sincronizado",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Pendente de sincronização",
                        tint = AccentOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.front,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "VERSO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.back,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
