package com.zeroscam.core_domain.model

import com.zeroscam.core_domain.enums.SubscriptionPlan
import com.zeroscam.core_domain.enums.SubscriptionStatus
import java.time.Instant

data class Subscription(
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val startedAt: Instant,
    val expiresAt: Instant?
) {
    fun isActive(now: Instant = Instant.now()): Boolean {
        return status == SubscriptionStatus.ACTIVE &&
            (expiresAt == null || expiresAt.isAfter(now))
    }
}
