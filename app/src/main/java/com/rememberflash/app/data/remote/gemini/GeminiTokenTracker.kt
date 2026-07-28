package com.rememberflash.app.data.remote.gemini

object GeminiTokenTracker {
    @Volatile
    var lastInputTokens: Int = 0
    
    @Volatile
    var lastOutputTokens: Int = 0

    val lastTotalTokens: Int
        get() = lastInputTokens + lastOutputTokens

    fun reset() {
        lastInputTokens = 0
        lastOutputTokens = 0
    }
}
