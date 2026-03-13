package com.aristidevs.cursotestingandroid.productlist.data.remote

import com.aristidevs.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import com.aristidevs.cursotestingandroid.productlist.data.remote.response.PromotionsResponse
import retrofit2.http.GET

interface MiniMarketApiService {

    @GET("data/products.json")
    suspend fun getProducts(): ProductsResponse

    @GET("data/promotions.json")
    suspend fun getPromotions(): PromotionsResponse

}