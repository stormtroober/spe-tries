package it.unibo.domain.ports

import kotlinx.serialization.json.JsonElement

interface EventDispatcher {
    /**
     * Notifies the user by sending a JSON payload to the external HTTP server.
     *
     * @param data The JSON payload to be sent.
     */
    fun notifyUser(data: JsonElement)
}
