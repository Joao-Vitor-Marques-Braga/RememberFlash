package com.rememberflash.app.presentation.contest.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.presentation.theme.AccentOrange
import com.rememberflash.app.presentation.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailScreen(
    viewModel: ContestDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditContest: (Long) -> Unit,
    onNavigateToFlashcards: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingDisciplineId by remember { mutableStateOf<Long?>(null) }
    var disciplineNameInput by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingDisciplineId by remember { mutableStateOf<Long?>(null) }
    var deletingDisciplineName by remember { mutableStateOf("") }

    var expandedMenuDisciplineId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Concurso") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    uiState.contest?.let { contest ->
                        IconButton(onClick = { onNavigateToEditContest(contest.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Concurso")
                        }
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
                    editingDisciplineId = null
                    disciplineNameInput = ""
                    viewModel.clearDisciplineNameError()
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Disciplina")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            if (uiState.isLoading && uiState.contest == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                // Info do Concurso
                uiState.contest?.let { contest ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = contest.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (contest.organizerName.isNotBlank()) {
                                Text(
                                    text = "Banca: ${contest.organizerName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            Text(
                                text = "Formato de Prova: ${contest.questionType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Header das Disciplinas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disciplinas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${uiState.disciplines.size} cadastradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.disciplines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma disciplina cadastrada.\nToque no botão + para adicionar.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.disciplines, key = { it.id }) { discipline ->
                            DisciplineItemCard(
                                discipline = discipline,
                                isMenuExpanded = expandedMenuDisciplineId == discipline.id,
                                onMenuClick = {
                                    expandedMenuDisciplineId = if (expandedMenuDisciplineId == discipline.id) null else discipline.id
                                },
                                onDismissMenu = { expandedMenuDisciplineId = null },
                                onEditClick = {
                                    editingDisciplineId = discipline.id
                                    disciplineNameInput = discipline.name
                                    viewModel.clearDisciplineNameError()
                                    expandedMenuDisciplineId = null
                                    showAddEditDialog = true
                                },
                                onDeleteClick = {
                                    deletingDisciplineId = discipline.id
                                    deletingDisciplineName = discipline.name
                                    expandedMenuDisciplineId = null
                                    showDeleteDialog = true
                                },
                                onClick = {
                                    onNavigateToFlashcards(discipline.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Criação / Edição
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddEditDialog = false
                viewModel.clearDisciplineNameError()
            },
            title = {
                Text(
                    text = if (editingDisciplineId == null) "Nova Disciplina" else "Editar Disciplina",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = disciplineNameInput,
                        onValueChange = {
                            disciplineNameInput = it
                            viewModel.validateDisciplineName(it, editingDisciplineId)
                        },
                        label = { Text("Nome da Disciplina") },
                        isError = uiState.disciplineNameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    uiState.disciplineNameError?.let { errorText ->
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveDiscipline(disciplineNameInput, editingDisciplineId) {
                            showAddEditDialog = false
                            disciplineNameInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddEditDialog = false
                        viewModel.clearDisciplineNameError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Alerta de Confirmação de Exclusão (Inibição / Soft Delete)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Excluir Disciplina",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Tem certeza? A disciplina \"$deletingDisciplineName\" e seus materiais associados (flashcards, questões) serão ocultados dos estudos, mas não serão apagados permanentemente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletingDisciplineId?.let { id ->
                            viewModel.deleteDiscipline(id) {
                                showDeleteDialog = false
                                deletingDisciplineId = null
                                deletingDisciplineName = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DisciplineItemCard(
    discipline: Discipline,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = discipline.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (discipline.isSynced) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Sincronizado",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Pendente de sincronização",
                            tint = AccentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (discipline.totalTopics > 0) {
                    Text(
                        text = "Progresso: ${discipline.completedTopics}/${discipline.totalTopics} tópicos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opções da disciplina",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = onEditClick
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}
