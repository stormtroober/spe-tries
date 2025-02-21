package it.unibo.application

import it.unibo.domain.AlertType
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdateCurrency
import it.unibo.domain.ports.EventDispatcher
import it.unibo.domain.ports.NotificationService
import it.unibo.domain.ports.PriceAlertRepository
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

/**
 * Implementation of the NotificationService interface.
 *
 * @property priceAlertRepository The repository for managing price alerts.
 * @property eventDispatcher The dispatcher for sending notifications.
 */
class NotificationServiceImpl(
    private val priceAlertRepository: PriceAlertRepository,

    private val eventDispatcher: EventDispatcher,
) : NotificationService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun handlePriceUpdate(priceUpdate: PriceUpdateCurrency) {
        logger.info("Handling price update for currency: ${priceUpdate.currency}")

        priceUpdate.priceUpdate.payload.forEach { cryptoPrice ->
            logger.info("Processing price update for crypto: ${cryptoPrice.id} with price: ${cryptoPrice.price}")

            // Get any pending alerts for this crypto and currency.
            val alerts = priceAlertRepository.getAlertsForCrypto(cryptoPrice.id, priceUpdate.currency)
            logger.info(
                "Found ${alerts.size} alerts " +
                    "for crypto: ${cryptoPrice.id} and currency: ${priceUpdate.currency}",
            )

            alerts.forEach { alert ->
                val shouldTrigger =
                    when (alert.alertType) {
                        AlertType.ABOVE -> cryptoPrice.price >= alert.alertPrice
                        AlertType.BELOW -> cryptoPrice.price <= alert.alertPrice
                    }

                if (!alert.triggered && shouldTrigger && alert.active) {
                    logger.info(
                        "Triggering alert for user: ${alert.userId}," +
                            " crypto: ${alert.cryptoId}," +
                            " alert price: ${alert.alertPrice}, current price: ${cryptoPrice.price}",
                    )

                    // Build the JSON payload for the notification.
                    val notificationJson =
                        buildJsonObject {
                            put("userId", alert.userId)
                            put("cryptoId", alert.cryptoId)
                            put("alertPrice", alert.alertPrice)
                            put("currentPrice", cryptoPrice.price)
                            put("message", alert.message.message)
                            put("alertType", alert.alertType.toString())
                        }
                    // Notify the external event dispatcher.
                    eventDispatcher.notifyUser(notificationJson)
                    logger.info("Notification sent for userid: ${alert.userId}")

                    // Mark this alert as triggered.
                    priceAlertRepository.markAsTriggered(alert)
                    logger.info("Alert marked as triggered: ${alert.userId}")
                } else {
                    logger.info(
                        "Alert not triggered for user: ${alert.userId}," +
                            " crypto: ${alert.cryptoId}, " +
                            "alert price: ${alert.alertPrice}," +
                            " current price: ${cryptoPrice.price}",
                    )
                }
            }
        }

        logger.info("Completed handling price update for currency: ${priceUpdate.currency}")
    }

    override suspend fun createAlert(alert: PriceAlert) {
        priceAlertRepository.save(alert)
    }

    override suspend fun getAlerts(userId: String): List<PriceAlert> {
        return priceAlertRepository.getAlertsForUser(userId)
    }

    override suspend fun deleteAlert(alertId: String): Boolean {
        return priceAlertRepository.deleteAlert(alertId)
    }

    override suspend fun setActiveStatus(
        alertId: String,
        status: Boolean,
    ): Boolean {
        return priceAlertRepository.setActiveStatus(alertId, status)
    }
}
