package com.aristidevs.cursotestingandroid.productlist.data.remote.builder

import com.aristidevs.cursotestingandroid.productlist.data.remote.response.ProductResponse

class ProductResponseBuilder {
    private var id: String = "product-1"
    private var name: String = "Test Product"
    private var description: String? = "A test product"
    private var priceCents: Int? = 1000
    private var category: String? = "food"
    private var stock: Int? = 10
    private var imageUrl: String? = null

    fun withId(id: String) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withDescription(description: String?) = apply { this.description = description }
    fun withPriceCents(priceCents: Int?) = apply { this.priceCents = priceCents }
    fun withCategory(category: String?) = apply { this.category = category }
    fun withStock(stock: Int?) = apply { this.stock = stock }
    fun withImageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }

    fun build(): ProductResponse = ProductResponse(
        id = id,
        name = name,
        description = description,
        priceCents = priceCents,
        category = category,
        stock = stock,
        imageUrl = imageUrl
    )
}

fun productResponse(block: ProductResponseBuilder.() -> Unit = {}): ProductResponse =
    ProductResponseBuilder().apply(block).build()

