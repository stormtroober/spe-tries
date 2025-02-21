package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AlertType {
    ABOVE,
    BELOW,
}

/**
 * Represents a price alert for a cryptocurrency.
 *
 * @property id The unique identifier of the alert.
 * @property userId The unique identifier of the user who created the alert.
 * @property cryptoId The unique identifier of the cryptocurrency.
 * @property alertPrice The price at which the alert should be triggered.
 * @property currency The currency in which the alert price is specified.
 * @property message The message to be sent when the alert is triggered.
 * @property alertType The type of alert (ABOVE or BELOW).
 * @property triggered Indicates whether the alert has been triggered.
 * @property active Indicates whether the alert is currently active.
 */
@Serializable
data class PriceAlert(
    val id: String? = null,
    val userId: String,
    val cryptoId: String,
    val alertPrice: Double,
    val currency: Currency,
    val message: Message,
    val alertType: AlertType,
    val triggered: Boolean = false,
    val active: Boolean = true,
)
