@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import it.unibo.domain.AlertType
import it.unibo.domain.Currency
import it.unibo.domain.Message
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdate
import it.unibo.domain.PriceUpdateCurrency
import it.unibo.domain.ports.NotificationService
import kotlinx.serialization.SerializationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebServer(private val notificationService: NotificationService) {
    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val server =
        embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) { json() }
            routing {
                post("/data") {
                    val currencyParam = call.parameters["currency"]
                    if (currencyParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: currency")
                        return@post
                    }
                    val priceUpdate = call.receive<PriceUpdate>()
                    logger.info("Received price update in $currencyParam : $priceUpdate")
                    val priceUpdateCurrency = PriceUpdateCurrency(Currency.fromCode(currencyParam), priceUpdate)

                    notificationService.handlePriceUpdate(priceUpdateCurrency)
                    call.respond(HttpStatusCode.OK, "Data received")
                }

                post("/createAlert") {
                    logger.info("Received alert creation request")
                    val userId = call.authenticate(System.getenv("JWT_SIMMETRIC_KEY")) ?: return@post

                    val cryptoId = call.parameters["cryptoId"]
                    val priceParam = call.parameters["price"]
                    val currencyParam = call.parameters["currency"]
                    val alertTypeParam = call.parameters["alertType"]

                    if (currencyParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: currency")
                        return@post
                    }
                    if (cryptoId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: cryptoId")
                        return@post
                    }
                    if (priceParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: price")
                        return@post
                    }
                    if (alertTypeParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: alertType")
                        return@post
                    }

                    val message: Message =
                        try {
                            call.receive<Message>()
                        } catch (e: SerializationException) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid message format: ${e.message}")
                            return@post
                        } catch (e: NumberFormatException) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid number format: ${e.message}")
                            return@post
                        }

                    val currency = Currency.fromCode(currencyParam)
                    val price = priceParam.toDouble()
                    val alertType =
                        try {
                            AlertType.valueOf(alertTypeParam.uppercase())
                        } catch (e: IllegalArgumentException) {
                            logger.error("Invalid alert type: $alertTypeParam", e)
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Invalid alert type. Allowed values are: ${AlertType.entries.joinToString()}",
                            )
                            return@post
                        }

                    val alert =
                        PriceAlert(
                            userId = userId,
                            cryptoId = cryptoId,
                            alertPrice = price,
                            currency = currency,
                            message = message,
                            alertType = alertType,
                        )
                    notificationService.createAlert(alert)
                    logger.info("Alert created: $alert")
                    call.respond(HttpStatusCode.OK, "Alert created")
                }

                get("/health") {
                    call.respond(mapOf("status" to "healthy"))
                }

                get("/alerts") {
                    val userId = call.authenticate(System.getenv("JWT_SIMMETRIC_KEY")) ?: return@get
                    val alerts = notificationService.getAlerts(userId)
                    call.respond(alerts)
                }

                put("/active") {
                    call.authenticate(System.getenv("JWT_SIMMETRIC_KEY")) ?: return@put
                    val alertId = call.parameters["alertId"]
                    val status = call.parameters["status"]
                    if (alertId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: alertId")
                        return@put
                    }
                    if (status == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: status")
                        return@put
                    }
                    if (status != "true" && status != "false") {
                        call.respond(HttpStatusCode.BadRequest, "Invalid status value: $status")
                        return@put
                    }
                    val res = notificationService.setActiveStatus(alertId, status.toBoolean())
                    if (!res) {
                        call.respond(HttpStatusCode.BadRequest, "Alert not found")
                        return@put
                    } else {
                        call.respond(HttpStatusCode.OK, "Alert status updated")
                    }
                }

                delete("/alerts") {
                    call.authenticate(System.getenv("JWT_SIMMETRIC_KEY")) ?: return@delete
                    val alertId = call.parameters["alertId"]
                    if (alertId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: alertId")
                        return@delete
                    }
                    val res = notificationService.deleteAlert(alertId)
                    if (!res) {
                        call.respond(HttpStatusCode.BadRequest, "Alert not found")
                        return@delete
                    } else {
                        call.respond(HttpStatusCode.OK, "Alert deleted")
                    }
                }
            }
        }

    fun start() {
        server.start(wait = true)
    }

    fun stop() {
        server.stop(GRACE_PERIOD, TIMEOUT)
    }
}
