package com.aristidevs.cursotestingandroid.core.domain.model

sealed class AppError(override val message: String? = null, override val cause: Throwable? = null) :
    Throwable() {

    data class NetworkError(override val cause: Throwable? = null) :
        AppError(message = "Network Error", cause = cause)

    data class NotFoundError(override val cause: Throwable? = null) :
        AppError(message = "Not Found", cause = cause)

    data class DatabaseError(override val cause: Throwable? = null) :
        AppError(message = "Database Error", cause = cause)
//    data class ValidationError(override val message:String): AppError()

    sealed class Validation(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError(message = message, cause = cause) {
        data class QuantityMustBePositive(override val message: String = "Quantity Must Be Positive") :
            Validation(message = message)

        data class InsufficientStock(val available: Int) : Validation(message = "Insufficient Stock. Available: $available")
    }

    data class UnknownError(override val message: String?, override val cause: Throwable? = null) :
        AppError(message = message, cause = cause)

}