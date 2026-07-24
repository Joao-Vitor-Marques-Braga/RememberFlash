package com.rememberflash.app.data.local.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object LocalPdfExtractor {
    
    fun extractText(context: Context, uri: Uri): String {
        return try {
            // Inicializa recursos do PDFBox
            PDFBoxResourceLoader.init(context)
            
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            
            document.close()
            inputStream.close()
            
            text.trim()
        } catch (e: Exception) {
            android.util.Log.e("LocalPdfExtractor", "Falha ao extrair texto local do PDF com PDFBox", e)
            ""
        }
    }
}
