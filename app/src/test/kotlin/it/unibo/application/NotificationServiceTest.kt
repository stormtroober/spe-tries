package it.unibo.application

import it.unibo.domain.AlertType
import it.unibo.domain.CryptoPrice
import it.unibo.domain.Currency
import it.unibo.domain.Message
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdate
import it.unibo.domain.PriceUpdateCurrency
import it.unibo.domain.ports.EventDispatcher
import it.unibo.domain.ports.PriceAlertRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// A simple in-memory fake implementation of PriceAlertRepository.
class FakePriceAlertRepository : PriceAlertRepository {
    val alerts = mutableListOf<PriceAlert>()

    override suspend fun save(alert: PriceAlert) {
        alerts.add(alert)
    }

    override suspend fun getAlertsForCrypto(
        cryptoId: String,
        currency: Currency,
    ): List<PriceAlert> {
        return alerts.filter { it.cryptoId == cryptoId && it.currency == currency && !it.triggered }
    }

    override suspend fun markAsTriggered(alert: PriceAlert) {
        val index = alerts.indexOfFirst { it == alert }
        if (index >= 0) {
            alerts[index] = alerts[index].copy(triggered = true)
        }
    }

    // Stub implementations for additional methods.
    override suspend fun getAlertsForUser(userId: String): List<PriceAlert> {
        return alerts.filter { it.userId == userId }
    }

    override suspend fun deleteAlert(alertId: String): Boolean {
        return alerts.removeIf { it.id == alertId }
    }

    override suspend fun setActiveStatus(
        alertId: String,
        status: Boolean,
    ): Boolean {
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index >= 0) {
            alerts[index] = alerts[index].copy(active = status)
            return true
        }
        return false
    }
}

// A fake event dispatcher that collects notifications in a list.
class FakeEventDispatcherAdapter : EventDispatcher {
    val notifications = mutableListOf<JsonElement>()

    // Override notifyUser to simply record the notification.
    override fun notifyUser(data: JsonElement) {
        notifications.add(data)
    }
}

class NotificationServiceTest {
    @Test
    fun `test createAlert and handlePriceUpdate triggers UP_THRESHOLD notification`() =
        runBlocking {
            // Arrange: create fake repository and dispatcher.
            val fakeRepository = FakePriceAlertRepository()
            val fakeDispatcher = FakeEventDispatcherAdapter()

            // Initialize the notification service with our fakes.
            val notificationService = NotificationServiceImpl(fakeRepository, fakeDispatcher)

            // 1. Create a new alert and save it.
            val alert =
                PriceAlert(
                    id = "alert1",
                    userId = "user1",
                    cryptoId = "bitcoin",
                    alertPrice = 50000.0,
                    currency = Currency.USD,
                    message = Message("Alert triggered for crypto: bitcoin"),
                    alertType = AlertType.ABOVE,
                    active = true,
                    triggered = false,
                )
            notificationService.createAlert(alert)

            // Verify that the alert was saved (and not triggered yet).
            assertEquals(1, fakeRepository.alerts.size)
            assertTrue(fakeRepository.alerts[0].triggered.not())

            // 2. Create a dummy price update: a price for "bitcoin" that is above the alert threshold.
            val cryptoPrice =
                CryptoPrice(
                    id = "bitcoin",
                    price = 60000.0,
                    symbol = "BTC",
                )
            val priceUpdate =
                PriceUpdate(
                    payload = listOf(cryptoPrice),
                    timestamp = "2021-09-01T12:00:00Z",
                )
            val priceUpdateCurrency = PriceUpdateCurrency(currency = Currency.USD, priceUpdate = priceUpdate)

            // Act: Process the price update.
            notificationService.handlePriceUpdate(priceUpdateCurrency)

            // 3. Verify that the alert is now triggered.
            val pendingAlerts = fakeRepository.getAlertsForCrypto("bitcoin", Currency.USD)
            assertTrue(pendingAlerts.isEmpty(), "Alert should be triggered and no longer returned as pending.")

            // 4. Verify that a notification was sent.
            assertEquals(1, fakeDispatcher.notifications.size)
            val notification = fakeDispatcher.notifications[0].jsonObject

            // Check that the notification contains the expected data.
            assertEquals("user1", notification["userId"]?.jsonPrimitive?.content)
            assertEquals("bitcoin", notification["cryptoId"]?.jsonPrimitive?.content)
            // Note: Numeric values may be represented as strings, so we compare string representations.
            assertEquals("50000.0", notification["alertPrice"]?.jsonPrimitive?.content)
            assertEquals("60000.0", notification["currentPrice"]?.jsonPrimitive?.content)
            assertEquals("Alert triggered for crypto: bitcoin", notification["message"]?.jsonPrimitive?.content)
        }

    @Test
    fun `test createAlert and handlePriceUpdate triggers DOWN_THRESHOLD notification`() =
        runBlocking {
            // Arrange: create fake repository and dispatcher.
            val fakeRepository = FakePriceAlertRepository()
            val fakeDispatcher = FakeEventDispatcherAdapter()

            // Initialize the notification service with our fakes.
            val notificationService = NotificationServiceImpl(fakeRepository, fakeDispatcher)

            // 1. Create a new alert and save it.
            val alert =
                PriceAlert(
                    id = "alert2",
                    userId = "user1",
                    cryptoId = "bitcoin",
                    alertPrice = 50000.0,
                    currency = Currency.USD,
                    message = Message("Alert triggered for crypto: bitcoin"),
                    alertType = AlertType.BELOW,
                    active = true,
                    triggered = false,
                )
            notificationService.createAlert(alert)

            // Verify that the alert was saved (and not triggered yet).
            assertEquals(1, fakeRepository.alerts.size)
            assertTrue(fakeRepository.alerts[0].triggered.not())

            // 2. Create a dummy price update: a price for "bitcoin" that is below the alert threshold.
            val cryptoPrice =
                CryptoPrice(
                    id = "bitcoin",
                    price = 40000.0,
                    symbol = "BTC",
                )
            val priceUpdate =
                PriceUpdate(
                    payload = listOf(cryptoPrice),
                    timestamp = "2021-09-01T12:00:00Z",
                )
            val priceUpdateCurrency = PriceUpdateCurrency(currency = Currency.USD, priceUpdate = priceUpdate)

            // Act: Process the price update.
            notificationService.handlePriceUpdate(priceUpdateCurrency)

            // 3. Verify that the alert is now triggered.
            val pendingAlerts = fakeRepository.getAlertsForCrypto("bitcoin", Currency.USD)
            assertTrue(pendingAlerts.isEmpty(), "Alert should be triggered and no longer returned as pending.")

            // 4. Verify that a notification was sent.
            assertEquals(1, fakeDispatcher.notifications.size)
            val notification = fakeDispatcher.notifications[0].jsonObject

            // Check that the notification contains the expected data.
            assertEquals("user1", notification["userId"]?.jsonPrimitive?.content)
            assertEquals("bitcoin", notification["cryptoId"]?.jsonPrimitive?.content)
            assertEquals("50000.0", notification["alertPrice"]?.jsonPrimitive?.content)
            assertEquals("40000.0", notification["currentPrice"]?.jsonPrimitive?.content)
            assertEquals("Alert triggered for crypto: bitcoin", notification["message"]?.jsonPrimitive?.content)
        }
}
