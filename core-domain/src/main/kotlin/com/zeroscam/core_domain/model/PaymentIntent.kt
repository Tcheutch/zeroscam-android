package com.zeroscam.core_domain.model

import com.zeroscam.core_domain.value.MoneyAmount
import com.zeroscam.core_domain.value.UserId
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
