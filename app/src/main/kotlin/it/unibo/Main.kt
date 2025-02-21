package it.unibo

import it.unibo.application.NotificationServiceImpl
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.MongoPriceAlertRepository
import it.unibo.infrastructure.adapter.WebServer
import kotlinx.coroutines.runBlocking

fun main() =
    runBlocking {
        // Initialize your Mongo-backed repository.
        val priceAlertRepository = MongoPriceAlertRepository()

        val eventDispatcher = EventDispatcherAdapter()

        // Create the NotificationService, passing the repository and adapter.
        val notificationService =
            NotificationServiceImpl(
                priceAlertRepository = priceAlertRepository,
                eventDispatcher = eventDispatcher,
            )

        val webServer = WebServer(notificationService)
        webServer.start()
    }
