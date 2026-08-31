package com.rememberflash.app.presentation.essay.capture.components

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rememberflash.app.presentation.essay.capture.util.ImageUtils
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private enum class DragHandle {
    NONE, INSIDE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

@Composable
fun ImageCropperDialog(
    imageUri: Uri,
    onCropConfirmed: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(true) }
    var isProcessingCrop by remember { mutableStateOf(false) }

    // Retângulo normalizado (0.0f a 1.0f relativo à imagem)
    var cropLeft by remember { mutableFloatStateOf(0.05f) }
    var cropTop by remember { mutableFloatStateOf(0.05f) }
    var cropRight by remember { mutableFloatStateOf(0.95f) }
    var cropBottom by remember { mutableFloatStateOf(0.95f) }

    var currentDragHandle by remember { mutableStateOf(DragHandle.NONE) }

    LaunchedEffect(imageUri) {
        isLoadingImage = true
        bitmap = ImageUtils.loadRotatedBitmap(context, imageUri)
        isLoadingImage = false
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessingCrop) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessingCrop,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (isLoadingImage || bitmap == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Carregando imagem...", color = Color.White)
                }
            } else {
                val currentBmp = bitmap!!

                Column(modifier = Modifier.fillMaxSize()) {
                    // Barra superior com título e instrução
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isProcessingCrop
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                        }

                        Text(
                            text = "Ajustar Enquadramento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        TextButton(
                            onClick = {
                                cropLeft = 0f
                                cropTop = 0f
                                cropRight = 1f
                                cropBottom = 1f
                            },
                            enabled = !isProcessingCrop
                        ) {
                            Icon(
                                Icons.Default.CropFree,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Toda", color = Color.White)
                        }
                    }

                    // Canvas de Recorte Interativo
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        val composeBitmap = remember(currentBmp) { currentBmp.asImageBitmap() }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(currentBmp) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            val (imgX, imgY, imgW, imgH) = calculateImageBounds(
                                                size.width.toFloat(),
                                                size.height.toFloat(),
                                                currentBmp.width.toFloat(),
                                                currentBmp.height.toFloat()
                                            )
                                            if (imgW <= 0 || imgH <= 0) return@detectDragGestures

                                            val rectPxLeft = imgX + cropLeft * imgW
                                            val rectPxTop = imgY + cropTop * imgH
                                            val rectPxRight = imgX + cropRight * imgW
                                            val rectPxBottom = imgY + cropBottom * imgH

                                            val handleTouchRadius = 48.dp.toPx()

                                            currentDragHandle = when {
                                                isNear(startOffset, Offset(rectPxLeft, rectPxTop), handleTouchRadius) -> DragHandle.TOP_LEFT
                                                isNear(startOffset, Offset(rectPxRight, rectPxTop), handleTouchRadius) -> DragHandle.TOP_RIGHT
                                                isNear(startOffset, Offset(rectPxLeft, rectPxBottom), handleTouchRadius) -> DragHandle.BOTTOM_LEFT
                                                isNear(startOffset, Offset(rectPxRight, rectPxBottom), handleTouchRadius) -> DragHandle.BOTTOM_RIGHT
                                                startOffset.x in rectPxLeft..rectPxRight && startOffset.y in rectPxTop..rectPxBottom -> DragHandle.INSIDE
                                                else -> DragHandle.NONE
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val (_, _, imgW, imgH) = calculateImageBounds(
                                                size.width.toFloat(),
                                                size.height.toFloat(),
                                                currentBmp.width.toFloat(),
                                                currentBmp.height.toFloat()
                                            )
                                            if (imgW <= 0 || imgH <= 0) return@detectDragGestures

                                            val deltaNormX = dragAmount.x / imgW
                                            val deltaNormY = dragAmount.y / imgH

                                            val minBoxSize = 0.1f

                                            when (currentDragHandle) {
                                                DragHandle.TOP_LEFT -> {
                                                    cropLeft = (cropLeft + deltaNormX).coerceIn(0f, cropRight - minBoxSize)
                                                    cropTop = (cropTop + deltaNormY).coerceIn(0f, cropBottom - minBoxSize)
                                                }
                                                DragHandle.TOP_RIGHT -> {
                                                    cropRight = (cropRight + deltaNormX).coerceIn(cropLeft + minBoxSize, 1f)
                                                    cropTop = (cropTop + deltaNormY).coerceIn(0f, cropBottom - minBoxSize)
                                                }
                                                DragHandle.BOTTOM_LEFT -> {
                                                    cropLeft = (cropLeft + deltaNormX).coerceIn(0f, cropRight - minBoxSize)
                                                    cropBottom = (cropBottom + deltaNormY).coerceIn(cropTop + minBoxSize, 1f)
                                                }
                                                DragHandle.BOTTOM_RIGHT -> {
                                                    cropRight = (cropRight + deltaNormX).coerceIn(cropLeft + minBoxSize, 1f)
                                                    cropBottom = (cropBottom + deltaNormY).coerceIn(cropTop + minBoxSize, 1f)
                                                }
                                                DragHandle.INSIDE -> {
                                                    val curWidth = cropRight - cropLeft
                                                    val curHeight = cropBottom - cropTop

                                                    val newLeft = (cropLeft + deltaNormX).coerceIn(0f, 1f - curWidth)
                                                    val newTop = (cropTop + deltaNormY).coerceIn(0f, 1f - curHeight)

                                                    cropLeft = newLeft
                                                    cropTop = newTop
                                                    cropRight = newLeft + curWidth
                                                    cropBottom = newTop + curHeight
                                                }
                                                DragHandle.NONE -> {}
                                            }
                                        },
                                        onDragEnd = {
                                            currentDragHandle = DragHandle.NONE
                                        },
                                        onDragCancel = {
                                            currentDragHandle = DragHandle.NONE
                                        }
                                    )
                                }
                        ) {
                            val (imgX, imgY, imgW, imgH) = calculateImageBounds(
                                size.width,
                                size.height,
                                currentBmp.width.toFloat(),
                                currentBmp.height.toFloat()
                            )

                            // 1. Desenhar a imagem centralizada
                            drawImage(
                                image = composeBitmap,
                                dstOffset = IntOffset(imgX.toInt(), imgY.toInt()),
                                dstSize = IntSize(imgW.toInt(), imgH.toInt())
                            )

                            // 2. Coordenadas do quadro de corte em pixels
                            val rectPxLeft = imgX + cropLeft * imgW
                            val rectPxTop = imgY + cropTop * imgH
                            val rectPxRight = imgX + cropRight * imgW
                            val rectPxBottom = imgY + cropBottom * imgH
                            val cropWidth = rectPxRight - rectPxLeft
                            val cropHeight = rectPxBottom - rectPxTop

                            // 3. Desenhar overlay escuro ao redor do quadro
                            val overlayColor = Color(0x99000000)
                            // Top
                            drawRect(overlayColor, Offset(imgX, imgY), Size(imgW, rectPxTop - imgY))
                            // Bottom
                            drawRect(overlayColor, Offset(imgX, rectPxBottom), Size(imgW, (imgY + imgH) - rectPxBottom))
                            // Left
                            drawRect(overlayColor, Offset(imgX, rectPxTop), Size(rectPxLeft - imgX, cropHeight))
                            // Right
                            drawRect(overlayColor, Offset(rectPxRight, rectPxTop), Size((imgX + imgW) - rectPxRight, cropHeight))

                            // 4. Borda do retângulo de corte
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(rectPxLeft, rectPxTop),
                                size = Size(cropWidth, cropHeight),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // 5. Linhas de grade (terços)
                            val gridColor = Color(0x66FFFFFF)
                            val oneThirdX = rectPxLeft + cropWidth / 3f
                            val twoThirdsX = rectPxLeft + 2 * cropWidth / 3f
                            val oneThirdY = rectPxTop + cropHeight / 3f
                            val twoThirdsY = rectPxTop + 2 * cropHeight / 3f

                            drawLine(gridColor, Offset(oneThirdX, rectPxTop), Offset(oneThirdX, rectPxBottom), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(twoThirdsX, rectPxTop), Offset(twoThirdsX, rectPxBottom), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(rectPxLeft, oneThirdY), Offset(rectPxRight, oneThirdY), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(rectPxLeft, twoThirdsY), Offset(rectPxRight, twoThirdsY), strokeWidth = 1.dp.toPx())

                            // 6. Cantos reforçados (Handles)
                            val handleLen = 22.dp.toPx()
                            val handleStroke = 4.dp.toPx()
                            val handleColor = Color(0xFF38BDF8) // Azul claro destaque

                            drawCornerHandle(rectPxLeft, rectPxTop, handleLen, handleStroke, handleColor, true, true)
                            drawCornerHandle(rectPxRight, rectPxTop, handleLen, handleStroke, handleColor, false, true)
                            drawCornerHandle(rectPxLeft, rectPxBottom, handleLen, handleStroke, handleColor, true, false)
                            drawCornerHandle(rectPxRight, rectPxBottom, handleLen, handleStroke, handleColor, false, false)
                        }
                    }

                    // Barra Inferior de Ações
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                enabled = !isProcessingCrop,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Tirar Outra")
                            }

                            Button(
                                onClick = {
                                    isProcessingCrop = true
                                    coroutineScope.launch {
                                        val normRect = RectF(cropLeft, cropTop, cropRight, cropBottom)
                                        val croppedUri = ImageUtils.cropAndSaveBitmap(
                                            context,
                                            currentBmp,
                                            normRect
                                        )
                                        isProcessingCrop = false
                                        if (croppedUri != null) {
                                            onCropConfirmed(croppedUri)
                                        } else {
                                            // Fallback para URI original
                                            onCropConfirmed(imageUri)
                                        }
                                    }
                                },
                                enabled = !isProcessingCrop,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isProcessingCrop) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cortando...")
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Extrair Texto (OCR)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCornerHandle(
    x: Float,
    y: Float,
    length: Float,
    stroke: Float,
    color: Color,
    isLeft: Boolean,
    isTop: Boolean
) {
    val dirX = if (isLeft) 1f else -1f
    val dirY = if (isTop) 1f else -1f

    drawLine(
        color = color,
        start = Offset(x, y),
        end = Offset(x + dirX * length, y),
        strokeWidth = stroke
    )
    drawLine(
        color = color,
        start = Offset(x, y),
        end = Offset(x, y + dirY * length),
        strokeWidth = stroke
    )
}

private fun isNear(p1: Offset, p2: Offset, radius: Float): Boolean {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    return (dx * dx + dy * dy) <= (radius * radius)
}

private data class ImageBounds(val x: Float, val y: Float, val width: Float, val height: Float)

private fun calculateImageBounds(
    canvasWidth: Float,
    canvasHeight: Float,
    bmpWidth: Float,
    bmpHeight: Float
): ImageBounds {
    if (bmpWidth <= 0 || bmpHeight <= 0 || canvasWidth <= 0 || canvasHeight <= 0) {
        return ImageBounds(0f, 0f, 0f, 0f)
    }

    val canvasRatio = canvasWidth / canvasHeight
    val bmpRatio = bmpWidth / bmpHeight

    return if (bmpRatio > canvasRatio) {
        val displayWidth = canvasWidth
        val displayHeight = canvasWidth / bmpRatio
        val offsetY = (canvasHeight - displayHeight) / 2f
        ImageBounds(0f, offsetY, displayWidth, displayHeight)
    } else {
        val displayHeight = canvasHeight
        val displayWidth = canvasHeight * bmpRatio
        val offsetX = (canvasWidth - displayWidth) / 2f
        ImageBounds(offsetX, 0f, displayWidth, displayHeight)
    }
}
