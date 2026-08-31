package com.rememberflash.app.data.remote.gemini

import android.util.Log

object GeminiTokenTracker {
    @Volatile
    var lastInputTokens: Int = 0
    
    @Volatile
    var lastOutputTokens: Int = 0

    val lastTotalTokens: Int
        get() = lastInputTokens + lastOutputTokens

    // Métricas dedicadas para análise de viabilidade do OCR por IA
    @Volatile
    var lastOcrInputTokens: Int = 0

    @Volatile
    var lastOcrOutputTokens: Int = 0

    val lastOcrTotalTokens: Int
        get() = lastOcrInputTokens + lastOcrOutputTokens

    val lastOcrEstimatedCostUsd: Double
        get() = (lastOcrInputTokens * 0.075 / 1_000_000.0) + (lastOcrOutputTokens * 0.30 / 1_000_000.0)

    @Volatile
    var totalOcrCalls: Int = 0

    @Volatile
    var accumulatedOcrTokens: Long = 0

    @Volatile
    var accumulatedOcrCostUsd: Double = 0.0

    fun reset() {
        lastInputTokens = 0
        lastOutputTokens = 0
    }

    /**
     * Registra o consumo específico de uma extração de OCR via IA para cálculo de viabilidade.
     */
    fun recordOcrUsage(inputTokens: Int, outputTokens: Int) {
        lastOcrInputTokens = inputTokens
        lastOcrOutputTokens = outputTokens
        totalOcrCalls += 1
        val totalTokens = inputTokens + outputTokens
        accumulatedOcrTokens += totalTokens
        
        val costUsd = (inputTokens * 0.075 / 1_000_000.0) + (outputTokens * 0.30 / 1_000_000.0)
        accumulatedOcrCostUsd += costUsd
        val costBrlApprox = costUsd * 5.80

        Log.i(
            "OCR_VIABILIDADE_IA",
            "==========================================================\n" +
            "📊 RELATÓRIO DE CONSUMO - OCR DE REDAÇÃO (GEMINI IA)\n" +
            "----------------------------------------------------------\n" +
            "• Tokens de Entrada (Imagem + Prompt): $inputTokens tokens\n" +
            "• Tokens de Saída (Texto Transcrito):  $outputTokens tokens\n" +
            "• Total desta Transcrição:             $totalTokens tokens\n" +
            "• Custo Desta Extração:                $${String.format("%.6f", costUsd)} USD (~R$ ${String.format("%.5f", costBrlApprox)})\n" +
            "----------------------------------------------------------\n" +
            "• Total de Transcrições OCR Realizadas: $totalOcrCalls\n" +
            "• Total Acumulado de Tokens OCR:        $accumulatedOcrTokens tokens\n" +
            "• Custo Total Acumulado:                $${String.format("%.6f", accumulatedOcrCostUsd)} USD (~R$ ${String.format("%.4f", accumulatedOcrCostUsd * 5.80)})\n" +
            "=========================================================="
        )
    }
}
