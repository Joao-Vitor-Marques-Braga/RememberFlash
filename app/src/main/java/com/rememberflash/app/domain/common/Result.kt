package com.rememberflash.app.domain.common

/**
 * Tipo selado genérico para representar o resultado de operações de forma limpa,
 * substituindo retornos nulos e exceções não controladas no fluxo principal.
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(
        val exception: Throwable? = null,
        val message: String = exception?.localizedMessage ?: "Erro desconhecido"
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Resultado ainda em carregamento")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(message: String, exception: Throwable? = null): Result<Nothing> =
            Error(exception, message)
        fun loading(): Result<Nothing> = Loading
    }
}
