package com.rememberflash.app.presentation.essay.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.presentation.essay.capture.components.ImageCropperDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayCaptureScreen(
    viewModel: EssayCaptureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var rawImageUriForCrop by remember { mutableStateOf<Uri?>(null) }

    fun generateTempUri(ctx: Context): Uri? {
        return try {
            val imagesDir = File(ctx.cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val tempFile = File.createTempFile("essay_cap_", ".jpg", imagesDir)
            FileProvider.getUriForFile(
                ctx,
                "${ctx.packageName}.provider",
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                rawImageUriForCrop = uri
            }
        }
    }

    fun launchCameraDirectly() {
        val uri = generateTempUri(context)
        if (uri != null) {
            tempPhotoUri = uri
            try {
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                viewModel.setError("Não foi possível abrir a câmera: ${e.localizedMessage ?: "Aplicativo de câmera não encontrado."}")
            }
        } else {
            viewModel.setError("Erro ao preparar o arquivo seguro para armazenamento da foto.")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraDirectly()
        } else {
            viewModel.setError("A permissão de acesso à câmera é necessária para fotografar a folha de redação.")
        }
    }

    fun requestAndLaunchCamera() {
        val permission = Manifest.permission.CAMERA
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraDirectly()
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            rawImageUriForCrop = uri
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onPdfSelected(context, uri)
        }
    }

    // Navega para os resultados quando a avaliação der certo
    LaunchedEffect(uiState.successEssayId) {
        uiState.successEssayId?.let { essayId ->
            onNavigateToResult(essayId)
        }
    }

    // Modal de seleção e corte de quadro da imagem para OCR
    rawImageUriForCrop?.let { rawUri ->
        ImageCropperDialog(
            imageUri = rawUri,
            onCropConfirmed = { croppedUri ->
                rawImageUriForCrop = null
                viewModel.onImageSelected(croppedUri)
            },
            onDismiss = {
                rawImageUriForCrop = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submeter Redação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.method == SubmissionMethod.NONE) {
                // Seleção de método (Escrita vs Câmera)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Como deseja enviar sua redação?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Card Câmera
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onMethodSelected(SubmissionMethod.CAMERA) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Escanear Folha Manuscrita",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tire uma foto ou suba da galeria para extrair com OCR local",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card Digitação
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onMethodSelected(SubmissionMethod.TYPING) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Digitar Redação",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Insira o texto livremente pelo teclado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // Formulário de Submissão
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Seleção do Concurso Pai
                    Text(
                        text = "Concurso Vinculado",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (uiState.availableContests.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Nenhum concurso cadastrado. Cadastre um concurso primeiro para definir a banca examinadora e critérios de correção.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        var isContestMenuExpanded by remember { mutableStateOf(false) }
                        val selectedContest = uiState.availableContests.firstOrNull { it.id == uiState.selectedContestId }
                            ?: uiState.availableContests.first()

                        ExposedDropdownMenuBox(
                            expanded = isContestMenuExpanded,
                            onExpandedChange = { isContestMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${selectedContest.title} (Banca: ${selectedContest.organizerName.ifBlank { "Geral" }})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Concurso e Banca Examinadora") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isContestMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = isContestMenuExpanded,
                                onDismissRequest = { isContestMenuExpanded = false }
                            ) {
                                uiState.availableContests.forEach { contest ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(contest.title, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Banca: ${contest.organizerName.ifBlank { "Geral" }} • Rigor: ${contest.aiRigor}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onContestSelected(contest.id)
                                            isContestMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Tema
                    OutlinedTextField(
                        value = uiState.theme,
                        onValueChange = viewModel::onThemeChanged,
                        label = { Text("Tema da Redação (Ex: Segurança Pública no Brasil)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (uiState.method == SubmissionMethod.CAMERA) {
                        // Painel da Foto
                        Text(
                            text = "Folha de Redação Manuscrita",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (uiState.imageUri == null && uiState.pdfUri == null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { requestAndLaunchCamera() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tirar Foto")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Galeria")
                                    }

                                    Button(
                                        onClick = { pdfLauncher.launch("application/pdf") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("PDF")
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (uiState.pdfUri != null) "PDF carregado com sucesso!" else "Imagem carregada com sucesso!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = {
                                        viewModel.onMethodSelected(SubmissionMethod.NONE)
                                    }
                                ) {
                                    Text("Trocar")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (uiState.isOcrLoading) {
                        // Loader OCR / Transcrição
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Transcrevendo caligrafia manuscrita com Inteligência Artificial...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (uiState.text.isNotBlank() || uiState.method == SubmissionMethod.TYPING) {
                        // Campo de Revisão / Digitação Livre
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.method == SubmissionMethod.CAMERA) "Revisão do Texto (OCR)" else "Texto da Redação",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // No modo digitação, botão rápido para também poder extrair texto via foto se desejar
                            if (uiState.method == SubmissionMethod.TYPING) {
                                TextButton(
                                    onClick = { requestAndLaunchCamera() }
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Escanear Foto", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.text,
                            onValueChange = viewModel::onTextChanged,
                            placeholder = { Text("Escreva ou revise aqui o texto da sua redação. Mínimo de 150 caracteres para avaliação...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 250.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botão de Avaliar
                        Button(
                            onClick = viewModel::onEvaluateClicked,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isEvaluating,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isEvaluating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Avaliando redação com IA...")
                            } else {
                                Text("Avaliar Redação")
                            }
                        }
                    }
                }
            }

            // Exibição de Alertas de Erro (como Heurística de tamanho A3 ou OCR ilegível A2)
            uiState.error?.let { errorMessage ->
                AlertDialog(
                    onDismissRequest = viewModel::clearError,
                    title = { Text("Aviso do Sistema") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("Entendido")
                        }
                    }
                )
            }
        }
    }
}
