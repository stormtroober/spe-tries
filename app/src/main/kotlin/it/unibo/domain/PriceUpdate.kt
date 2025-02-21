package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
data class CryptoPrice(
    val id: String,
    val symbol: String,
    val price: Double,
)

@Serializable
data class PriceUpdate(
    val timestamp: String,
    val payload: List<CryptoPrice>,
)

data class PriceUpdateCurrency(
    val currency: Currency,
    val priceUpdate: PriceUpdate,
)
