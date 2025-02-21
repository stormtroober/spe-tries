package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val message: String,
)
