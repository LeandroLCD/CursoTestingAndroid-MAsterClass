package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCartItemCountUseCase @Inject constructor(
    private val cartItemRepository: CartItemRepository
) {
    operator fun invoke(): Flow<Int> =
        cartItemRepository.getCartItems().map { items -> items.sumOf { it.quantity } }
}
