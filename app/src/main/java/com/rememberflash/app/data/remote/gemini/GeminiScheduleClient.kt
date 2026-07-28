package com.rememberflash.app.data.remote.gemini

interface GeminiScheduleClient {
    suspend fun generateStudySchedule(prompt: String): String
}
