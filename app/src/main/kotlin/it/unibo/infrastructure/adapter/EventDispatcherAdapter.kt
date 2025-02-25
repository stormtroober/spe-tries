@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.UnresolvedAddressException
import it.unibo.domain.EventPayload
import it.unibo.domain.ports.EventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class EventDispatcherAdapter(
    private val httpServerHost: String = "event-dispatcher",
    private val httpServerPort: Int = 3000,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : EventDispatcher {
    private val logger = LoggerFactory.getLogger(EventDispatcherAdapter::class.java)
    private val client = HttpClient(CIO)
    private val mutex = Mutex()

    override fun publish(data: EventPayload) {
        scope.launch {
            mutex.withLock {
                try {
                    val jsonData = Json.encodeToString(data)
                    logger.info("Publishing data: $jsonData")
                    val response: HttpResponse =
                        client.post {
                            url {
                                protocol = URLProtocol.HTTP
                                host = httpServerHost
                                port = httpServerPort
                                encodedPath = "/realtime/events/cryptomarketdata"
                            }
                            contentType(ContentType.Application.Json)
                            setBody(jsonData)
                        }
                    logger.info("Response: ${response.status}")
                } catch (e: IOException) {
                    logger.error("Failed to publish data due to network error", e)
                } catch (e: SerializationException) {
                    logger.error("Failed to publish data due to serialization error", e)
                } catch (uae: UnresolvedAddressException) {
                    logger.error("Failed to publish data due to address", uae)
                }
            }
        }
    }

    override fun close() {
        client.close()
        logger.info("HTTP client closed")
    }
}
