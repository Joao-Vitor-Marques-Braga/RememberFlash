package com.rememberflash.app.presentation.essay.capture.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    /**
     * Carrega um Bitmap a partir da Uri, corrigindo a rotação EXIF da câmera
     * e redimensionando se necessário para economizar memória.
     */
    suspend fun loadRotatedBitmap(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Obter dimensões sem carregar tudo na memória
                var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val origWidth = options.outWidth
                val origHeight = options.outHeight
                if (origWidth <= 0 || origHeight <= 0) return@withContext null

                var inSampleSize = 1
                val maxActualDim = max(origWidth, origHeight)
                while ((maxActualDim / inSampleSize) > maxDimension) {
                    inSampleSize *= 2
                }

                // 2. Decodificar com sample size apropriado
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                inputStream = context.contentResolver.openInputStream(uri)
                val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
                inputStream?.close()

                if (decodedBitmap == null) return@withContext null

                // 3. Checar rotação EXIF
                val rotationDegrees = getExifRotation(context, uri)
                if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    val rotatedBitmap = Bitmap.createBitmap(
                        decodedBitmap,
                        0,
                        0,
                        decodedBitmap.width,
                        decodedBitmap.height,
                        matrix,
                        true
                    )
                    if (rotatedBitmap != decodedBitmap) {
                        decodedBitmap.recycle()
                    }
                    rotatedBitmap
                } else {
                    decodedBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = ExifInterface(stream)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Recorta o bitmap usando coordenadas normalizadas (0.0f a 1.0f) e salva em cache temporário.
     */
    suspend fun cropAndSaveBitmap(
        context: Context,
        sourceBitmap: Bitmap,
        normalizedRect: RectF
    ): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val bmpWidth = sourceBitmap.width
                val bmpHeight = sourceBitmap.height

                val left = (normalizedRect.left * bmpWidth).toInt().coerceIn(0, bmpWidth - 1)
                val top = (normalizedRect.top * bmpHeight).toInt().coerceIn(0, bmpHeight - 1)
                val right = (normalizedRect.right * bmpWidth).toInt().coerceIn(left + 1, bmpWidth)
                val bottom = (normalizedRect.bottom * bmpHeight).toInt().coerceIn(top + 1, bmpHeight)

                val cropWidth = (right - left).coerceAtLeast(10)
                val cropHeight = (bottom - top).coerceAtLeast(10)

                val croppedBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    left,
                    top,
                    cropWidth,
                    cropHeight
                )

                val imagesDir = File(context.cacheDir, "images").apply {
                    if (!exists()) mkdirs()
                }
                val outputFile = File.createTempFile("cropped_essay_", ".jpg", imagesDir)
                FileOutputStream(outputFile).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                if (croppedBitmap != sourceBitmap) {
                    croppedBitmap.recycle()
                }

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    outputFile
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
