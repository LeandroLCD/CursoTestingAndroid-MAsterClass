package com.aristidevs.cursotestingandroid.productlist.presentation

import com.aristidevs.cursotestingandroid.productlist.domain.model.Product
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FailingProductRepositoryStub(private val exception: Throwable): ProductRepository {

    override fun getProducts(): Flow<List<Product>> = flow { throw exception}

    override fun getProductById(id: String): Flow<Product?> = flow { throw exception}

    override fun getProductsByIds(ids: Set<String>): Flow<List<Product>> = flow { throw exception}

    override suspend fun refreshProduct() {}
}