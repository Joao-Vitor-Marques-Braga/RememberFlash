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

    fun extractHeaderSnippet(rawText: String): String {
        return if (rawText.length > 15000) {
            rawText.substring(0, 15000)
        } else {
            rawText
        }
    }

    fun extractRulesSnippet(rawText: String): String {
        val keywords = listOf(
            "caneta", "transparente", "proibido", "aparelhos", "eletrônicos", 
            "celular", "relógio", "levar", "lápis", "detector", "comprovante", "documento"
        )
        // Divide o texto em sentenças ou linhas
        val sentences = rawText.split("\n", ". ")
        val matchingSentences = sentences.filter { sentence ->
            keywords.any { keyword -> sentence.contains(keyword, ignoreCase = true) }
        }
        val result = matchingSentences.take(50).joinToString("\n")
        return result.ifBlank {
            if (rawText.length > 5000) rawText.substring(0, 5000) else rawText
        }
    }

    fun extractSyllabusSnippet(rawText: String, cargo: String): String {
        val keywords = listOf("conteúdo programático", "objetos de avaliação", "anexo ii", "dos objetos")
        var index = -1
        for (keyword in keywords) {
            val found = rawText.indexOf(keyword, ignoreCase = true)
            if (found != -1) {
                index = found
                break
            }
        }
        
        if (index != -1) {
            val end = (index + 20000).coerceAtMost(rawText.length)
            return rawText.substring(index, end)
        }
        
        // Se não achar palavras-chave, extrai a metade final do edital, que geralmente contém as matérias
        return if (rawText.length > 20000) {
            rawText.substring(rawText.length - 20000)
        } else {
            rawText
        }
    }
}
