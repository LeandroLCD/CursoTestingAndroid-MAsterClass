package com.aristidevs.cursotestingandroid.productlist.data.remote.builder

import com.aristidevs.cursotestingandroid.productlist.data.remote.response.PromotionResponse

class PromotionResponseBuilder {
    private var id: String = "promo-1"
    private var productId: String = "product-1"
    private var type: String = "PERCENT"
    private var percent: Int? = 10
    private var buyX: Int? = null
    private var payY: Int? = null
    private var startAtEpoch: Long? = null
    private var endAtEpoch: Long? = null

    fun withId(id: String) = apply { this.id = id }
    fun withProductId(productId: String) = apply { this.productId = productId }
    fun withType(type: String) = apply { this.type = type }
    fun withPercent(percent: Int?) = apply { this.percent = percent }
    fun withBuyX(buyX: Int?) = apply { this.buyX = buyX }
    fun withPayY(payY: Int?) = apply { this.payY = payY }
    fun withStartAtEpoch(startAtEpoch: Long?) = apply { this.startAtEpoch = startAtEpoch }
    fun withEndAtEpoch(endAtEpoch: Long?) = apply { this.endAtEpoch = endAtEpoch }

    fun build(): PromotionResponse = PromotionResponse(
        id = id,
        productId = productId,
        type = type,
        percent = percent,
        buyX = buyX,
        payY = payY,
        startAtEpoch = startAtEpoch,
        endAtEpoch = endAtEpoch
    )
}

fun promotionResponse(block: PromotionResponseBuilder.() -> Unit = {}): PromotionResponse =
    PromotionResponseBuilder().apply(block).build()

