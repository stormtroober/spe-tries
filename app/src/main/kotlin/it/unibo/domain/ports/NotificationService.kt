package it.unibo.domain.ports

import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdateCurrency

interface NotificationService {
    /**
     * Handles a price update and triggers any present alerts.
     *
     * @param priceUpdate The price update to handle.
     */
    suspend fun handlePriceUpdate(priceUpdate: PriceUpdateCurrency)

    /**
     * Creates a new price alert.
     *
     * @param alert The alert to create.
     */
    suspend fun createAlert(alert: PriceAlert)

    /**
     * Retrieves all alerts for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return A list of price alerts for the user.
     */
    suspend fun getAlerts(userId: String): List<PriceAlert>

    /**
     * Deletes a specific alert.
     *
     * @param alertId The unique identifier of the alert to delete.
     * @return True if the alert was successfully deleted, false otherwise.
     */
    suspend fun deleteAlert(alertId: String): Boolean

    /**
     * Sets the active status of a specific alert.
     *
     * @param alertId The unique identifier of the alert.
     * @param status The new active status of the alert.
     * @return True if the status was successfully updated, false otherwise.
     */
    suspend fun setActiveStatus(
        alertId: String,
        status: Boolean,
    ): Boolean
}
