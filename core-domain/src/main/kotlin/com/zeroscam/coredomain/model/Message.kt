package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Représente un message analysable par ZeroScam (SMS, mail, OTT, etc.).
 */
data class Message(
    val id: String,
    val userId: UserId,
    val content: String,
    val channel: DetectionChannel,
    val source: String? = null,
    val receivedAt: Instant,
)
