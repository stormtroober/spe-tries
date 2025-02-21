package it.unibo.domain.ports

import it.unibo.domain.Currency
import it.unibo.domain.PriceAlert

interface PriceAlertRepository {
    /**
     * Saves a new price alert to the repository.
     *
     * @param alert The price alert to be saved.
     */
    suspend fun save(alert: PriceAlert)

    /**
     * Retrieves a list of price alerts for a specific cryptocurrency and currency.
     *
     * @param cryptoId The ID of the cryptocurrency.
     * @param currency The currency for which the alerts are set.
     * @return A list of price alerts.
     */
    suspend fun getAlertsForCrypto(
        cryptoId: String,
        currency: Currency,
    ): List<PriceAlert>

    /**
     * Marks a price alert as triggered.
     *
     * @param alert The price alert to be marked as triggered.
     */
    suspend fun markAsTriggered(alert: PriceAlert)

    /**
     * Retrieves a list of price alerts for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of price alerts.
     */
    suspend fun getAlertsForUser(userId: String): List<PriceAlert>

    /**
     * Deletes a price alert from the repository.
     *
     * @param alertId The ID of the price alert to be deleted.
     * @return True if the alert was successfully deleted, false otherwise.
     */
    suspend fun deleteAlert(alertId: String): Boolean

    /**
     * Sets the active status of a price alert.
     *
     * @param alertId The ID of the price alert.
     * @param status The new active status of the alert.
     * @return True if the status was successfully updated, false otherwise.
     */
    suspend fun setActiveStatus(
        alertId: String,
        status: Boolean,
    ): Boolean
}
