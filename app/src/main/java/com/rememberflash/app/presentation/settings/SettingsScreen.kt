package com.rememberflash.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isApiKeyVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Configurações salvas com sucesso!")
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações de IA") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Personalize o comportamento da Inteligência Artificial Generativa para adequar as questões e redações ao seu perfil de estudos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dificuldade das Questões
            Text(
                text = "Dificuldade das Questões",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Dropdown simulado com campo editável
            var showDifficultyMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.difficulty,
                    onValueChange = viewModel::onDifficultyChanged,
                    label = { Text("Nível de Dificuldade") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDifficultyMenu)
                    }
                )
                DropdownMenu(
                    expanded = showDifficultyMenu,
                    onDismissRequest = { showDifficultyMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf("Fácil", "Médio", "Difícil").forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                viewModel.onDifficultyChanged(level)
                                showDifficultyMenu = false
                            }
                        )
                    }
                }
                // Invisible box to detect click on the textfield
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                        .clickable { showDifficultyMenu = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rigor da Correção
            Text(
                text = "Rigor na Correção da Redação",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            var showRigorMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.rigor,
                    onValueChange = viewModel::onRigorChanged,
                    label = { Text("Nível de Rigor") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRigorMenu)
                    }
                )
                DropdownMenu(
                    expanded = showRigorMenu,
                    onDismissRequest = { showRigorMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf("Flexível", "Padrão", "Rígido").forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                viewModel.onRigorChanged(level)
                                showRigorMenu = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                        .clickable { showRigorMenu = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tom do Tutor
            Text(
                text = "Tom do Tutor Interativo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    RadioButton(
                        selected = uiState.tone == "Direto/Objetivo",
                        onClick = { viewModel.onToneChanged("Direto/Objetivo") }
                    )
                    Text("Direto/Objetivo", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.tone == "Explicativo" || uiState.tone == "Explicativo/Detalhado",
                        onClick = { viewModel.onToneChanged("Explicativo/Detalhado") }
                    )
                    Text("Explicativo/Detalhado", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chave API
            Text(
                text = "Chave de API (Gemini)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = viewModel::onApiKeyChanged,
                label = { Text("Google AI Studio API Key") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (isApiKeyVisible) "Ocultar chave" else "Mostrar chave"
                    IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botão Salvar
            Button(
                onClick = viewModel::saveSettings,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Salvar Configurações", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal de Alerta de Conectividade (A1)
    if (uiState.error != null && uiState.error!!.contains("nuvem")) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Falha de Comunicação") },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                Button(onClick = viewModel::clearError) {
                    Text("OK")
                }
            }
        )
    }
}
