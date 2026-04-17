package com.aristidevs.cursotestingandroid.ui.widget

import android.content.Context
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.domain.model.AppError

fun Context.stringThrowable(throwable: Throwable): String {
    return when (throwable) {
        is AppError.DatabaseError -> getString(R.string.database_error)
        is AppError.NetworkError -> getString(R.string.detail_network_error)
        is AppError.NotFoundError -> getString(R.string.products_not_found)
        is AppError.UnknownError -> getString(R.string.detail_unknown_error)
        is AppError.Validation.InsufficientStock -> getString(R.string.detail_insufficient_stock_error)
        is AppError.Validation.QuantityMustBePositive -> getString(R.string.quantity_must_be_positive_error)
        else -> getString(R.string.detail_unknown_error)
    }
}