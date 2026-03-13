package com.aristidevs.cursotestingandroid.cart.data.mapper

import com.aristidevs.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain(): CartItem{
    return CartItem(
        productId = productId,
        quantity = quantity
    )
}
fun CartItem.toEntity(): CartItemEntity{
    return CartItemEntity(
        productId = productId,
        quantity = quantity
    )
}