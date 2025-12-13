package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.value.MoneyAmount
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Intention de paiement évaluée par PaymentGuardian.
 */
data class PaymentIntent(
    val id: String,
    val userId: UserId,
    val amount: MoneyAmount,
    val currency: String,
    val recipientAccount: String,
    val channel: String,
    val createdAt: Instant,
    val metadata: Map<String, String> = emptyMap(),
)
