package com.rememberflash.app.domain.exception

/**
 * Exceção lançada para violações de regras de autenticação e validação cadastral (ex: CPF inválido, CPF duplicado).
 */
class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
