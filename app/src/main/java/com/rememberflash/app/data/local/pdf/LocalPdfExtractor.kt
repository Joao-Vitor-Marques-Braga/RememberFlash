package com.rememberflash.app.data.local.pdf

import android.content.Context
import android.net.Uri

object LocalPdfExtractor {
    
    fun extractText(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            val textBuilder = StringBuilder()
            var i = 0
            val len = bytes.size
            while (i < len - 4) {
                if (bytes[i] == '('.code.toByte()) {
                    val start = i + 1
                    var end = start
                    while (end < len && bytes[end] != ')'.code.toByte()) {
                        end++
                    }
                    if (end < len) {
                        val strBytes = bytes.copyOfRange(start, end)
                        val str = String(strBytes, Charsets.ISO_8859_1).trim()
                        // Filtra para manter somente blocos de texto razoavelmente legíveis
                        if (str.length > 2 && str.all { it.isLetterOrDigit() || it.isWhitespace() || it in ",.-;:_!?=+-/*@#()[]{}'\"" }) {
                            textBuilder.append(str).append(" ")
                        }
                    }
                    i = end + 1
                } else {
                    i++
                }
            }
            textBuilder.toString().trim()
        } catch (e: Exception) {
            android.util.Log.e("LocalPdfExtractor", "Falha ao extrair texto local do PDF", e)
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
