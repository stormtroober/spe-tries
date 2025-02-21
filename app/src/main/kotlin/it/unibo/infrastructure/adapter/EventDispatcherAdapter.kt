@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.UnresolvedAddressException
import it.unibo.domain.ports.EventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.slf4j.LoggerFactory

/**
 * Adapter for dispatching events to an external HTTP server.
 *
 * @property httpServerHost The host of the HTTP server.
 * @property httpServerPort The port of the HTTP server.
 * @property scope The coroutine scope for launching asynchronous tasks.
 */
class EventDispatcherAdapter(
    private val httpServerHost: String = "event-dispatcher",
    private val httpServerPort: Int = 3000,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : EventDispatcher {
    private val logger = LoggerFactory.getLogger(EventDispatcherAdapter::class.java)
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }
    private val mutex = Mutex()

    override fun notifyUser(data: JsonElement) {
        scope.launch {
            mutex.withLock {
                try {
                    val response: HttpResponse =
                        client.post {
                            url {
                                protocol = URLProtocol.HTTP
                                host = httpServerHost
                                port = httpServerPort
                                encodedPath = "/realtime/events/notifyUser"
                            }
                            contentType(ContentType.Application.Json)
                            setBody(data)
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

    fun close() {
        client.close()
        logger.info("HTTP client closed")
    }
}
