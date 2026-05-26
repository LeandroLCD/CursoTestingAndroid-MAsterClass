package com.aristidevs.cursotestingandroid.productlist.data.repository.builder

import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity

class ProductEntityBuilder {
    private var id: String = "product-1"
    private var name: String = "Test Product"
    private var description: String? = "A test description"
    private var price: Double = 10.0
    private var category: String? = "food"
    private var stock: Int? = 10
    private var imageUrl: String? = null

    fun withId(id: String) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withDescription(description: String?) = apply { this.description = description }
    fun withPrice(price: Double) = apply { this.price = price }
    fun withCategory(category: String?) = apply { this.category = category }
    fun withStock(stock: Int?) = apply { this.stock = stock }
    fun withImageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }

    fun build(): ProductEntity = ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        category = category,
        stock = stock,
        imageUrl = imageUrl
    )
}

fun productEntity(block: ProductEntityBuilder.() -> Unit = {}): ProductEntity =
    ProductEntityBuilder().apply(block).build()

