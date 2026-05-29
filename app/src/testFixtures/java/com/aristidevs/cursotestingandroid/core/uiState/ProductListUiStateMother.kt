package com.aristidevs.cursotestingandroid.core.uiState

import com.aristidevs.cursotestingandroid.core.builder.product
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.presentation.ProductListUiState

object ProductListUiStateMother {

    val defaultProducts: List<ProductWithPromotion> = listOf(
        ProductWithPromotion(product = product { withId("product-1"); withCategory("fruits") }),
        ProductWithPromotion(product = product { withId("product-2"); withCategory("vegetables") }),
        ProductWithPromotion(product = product { withId("product-3"); withCategory("fruits") })
    )

    fun success(
        products: List<ProductWithPromotion> = defaultProducts,
        categories: List<String> = products.map { it.product.category }.distinct(),
        selectedCategory: String? = null,
        sortOption: SortOption = SortOption.NONE
    ): ProductListUiState.Success {

        return ProductListUiState.Success(
            products = products,
            categories = categories,
            selectedCategory = selectedCategory,
            sortOption = sortOption
        )
    }
}