package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.SubscriptionPlan
import com.zeroscam.coredomain.enums.SubscriptionStatus
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTest {
    @Test
    fun `isActive returns true when status is ACTIVE and not expired`() {
        val now = Instant.now()
        val subscription =
            Subscription(
                plan = SubscriptionPlan.PREMIUM,
                status = SubscriptionStatus.ACTIVE,
                startedAt = now.minusSeconds(3600),
                expiresAt = now.plusSeconds(3600),
            )

        assertTrue(subscription.isActive(now))
    }

    @Test
    fun `isActive returns false when expired`() {
        val now = Instant.now()
        val subscription =
            Subscription(
                plan = SubscriptionPlan.PREMIUM,
                status = SubscriptionStatus.ACTIVE,
                startedAt = now.minusSeconds(7200),
                expiresAt = now.minusSeconds(3600),
            )

        assertFalse(subscription.isActive(now))
    }
}
