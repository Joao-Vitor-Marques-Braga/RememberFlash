package com.rememberflash.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val createdAt: Long = System.currentTimeMillis()
)
