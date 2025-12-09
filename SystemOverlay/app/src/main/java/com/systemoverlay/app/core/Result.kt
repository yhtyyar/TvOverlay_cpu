package com.systemoverlay.app.core

/**
 * A sealed class representing either success or failure of an operation.
 * Following Railway Oriented Programming pattern.
 */
sealed class Result<out T> {
    
    data class Success<T>(val data: T) : Result<T>()
    
    data class Error(
        val exception: Throwable? = null,
        val message: String = exception?.message ?: "Unknown error"
    ) : Result<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }
    
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }
    
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    fun onError(action: (Error) -> Unit): Result<T> {
        if (this is Error) action(this)
        return this
    }
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable? = null, message: String = ""): Result<Nothing> =
            Error(exception, message.ifEmpty { exception?.message ?: "Unknown error" })
        
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}
