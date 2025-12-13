package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.value.UserId
import java.time.Instant

data class UserProfile(
    val id: UserId,
    val phoneNumber: String?,
    val email: String?,
    val subscription: Subscription?,
) {
    fun hasPremiumAccess(now: Instant = Instant.now()): Boolean {
        return subscription?.isActive(now) == true &&
            subscription.plan == com.zeroscam.coredomain.enums.SubscriptionPlan.PREMIUM
    }
}
