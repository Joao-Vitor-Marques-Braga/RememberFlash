package com.rememberflash.app.data.local.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object LocalPdfExtractor {
    
    fun extractText(context: Context, uri: Uri, maxPages: Int = 50): String {
        var document: PDDocument? = null
        var inputStream: java.io.InputStream? = null
        return try {
            PDFBoxResourceLoader.init(context)
            
            inputStream = if (uri.scheme == "file") {
                java.io.File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            } ?: return ""
            
            document = PDDocument.load(inputStream)
            val totalPages = document.numberOfPages
            val stripper = PDFTextStripper().apply {
                startPage = 1
                endPage = if (totalPages > maxPages) maxPages else totalPages
            }
            val text = stripper.getText(document)
            
            sanitizePdfText(text)
        } catch (t: Throwable) {
            android.util.Log.e("LocalPdfExtractor", "Falha ao extrair texto local do PDF com PDFBox", t)
            ""
        } finally {
            try { document?.close() } catch (_: Throwable) {}
            try { inputStream?.close() } catch (_: Throwable) {}
        }
    }

    fun sanitizePdfText(rawText: String): String {
        return rawText
            // Remove múltiplos quebras de linha e espaços duplos
            .replace(Regex("\\r\\n|\\r|\\n"), " ")
            .replace(Regex("\\s+"), " ")
            // Remove padrões comuns de cabeçalho/rodapé repetitivos (ex: "Página X de Y")
            .replace(Regex("Página \\d+ de \\d+", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
