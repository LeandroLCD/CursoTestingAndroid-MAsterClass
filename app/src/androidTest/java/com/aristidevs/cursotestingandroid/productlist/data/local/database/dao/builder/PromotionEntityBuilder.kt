package com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.builder

import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity

class PromotionEntityBuilder {
    private var id: String = "promo-1"
    private var productIds: String = "product-1,product-2"
    private var type: String = "PERCENT"
    private var percent: Int? = 10
    private var buyX: Int? = null
    private var payY: Int? = null
    private var startAtEpoch: Long = 1000L
    private var endAtEpoch: Long = 2000L

    fun withId(id: String) = apply { this.id = id }
    fun withProductIds(productIds: String) = apply { this.productIds = productIds }
    fun withType(type: String) = apply { this.type = type }
    fun withPercent(percent: Int?) = apply { this.percent = percent }
    fun withBuyX(buyX: Int?) = apply { this.buyX = buyX }
    fun withPayY(payY: Int?) = apply { this.payY = payY }
    fun withStartAtEpoch(startAtEpoch: Long) = apply { this.startAtEpoch = startAtEpoch }
    fun withEndAtEpoch(endAtEpoch: Long) = apply { this.endAtEpoch = endAtEpoch }

    fun build() = PromotionEntity(
        id = id,
        productIds = productIds,
        type = type,
        percent = percent,
        buyX = buyX,
        payY = payY,
        startAtEpoch = startAtEpoch,
        endAtEpoch = endAtEpoch
    )
}

fun promotionEntity(block: PromotionEntityBuilder.() -> Unit = {}): PromotionEntity =
    PromotionEntityBuilder().apply(block).build()
