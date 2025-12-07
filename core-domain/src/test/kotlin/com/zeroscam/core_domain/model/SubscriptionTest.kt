package com.zeroscam.core_domain.model

import com.zeroscam.core_domain.enums.SubscriptionPlan
import com.zeroscam.core_domain.enums.SubscriptionStatus
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import java.time.Instant

class SubscriptionTest {

    @Test
    fun `isActive returns true when status is ACTIVE and not expired`() {
        val now = Instant.now()
        val subscription = Subscription(
            plan = SubscriptionPlan.PREMIUM,
            status = SubscriptionStatus.ACTIVE,
            startedAt = now.minusSeconds(3600),
            expiresAt = now.plusSeconds(3600)
        )

        assertTrue(subscription.isActive(now))
    }

    @Test
    fun `isActive returns false when expired`() {
        val now = Instant.now()
        val subscription = Subscription(
            plan = SubscriptionPlan.PREMIUM,
            status = SubscriptionStatus.ACTIVE,
            startedAt = now.minusSeconds(7200),
            expiresAt = now.minusSeconds(3600)
        )

        assertFalse(subscription.isActive(now))
    }
}
