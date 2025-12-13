package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.SubscriptionPlan
import com.zeroscam.coredomain.enums.SubscriptionStatus
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {
    @Test
    fun `hasPremiumAccess returns true when subscription is active and premium`() {
        val now = Instant.now()
        val subscription =
            Subscription(
                plan = SubscriptionPlan.PREMIUM,
                status = SubscriptionStatus.ACTIVE,
                startedAt = now.minusSeconds(3600),
                expiresAt = now.plusSeconds(3600),
            )

        val profile =
            UserProfile(
                id = UserId("user-123"),
                phoneNumber = "+237600000000",
                email = "user@example.com",
                subscription = subscription,
            )

        assertTrue(profile.hasPremiumAccess(now))
    }

    @Test
    fun `hasPremiumAccess returns false when no subscription`() {
        val profile =
            UserProfile(
                id = UserId("user-123"),
                phoneNumber = null,
                email = null,
                subscription = null,
            )

        assertFalse(profile.hasPremiumAccess())
    }
}
