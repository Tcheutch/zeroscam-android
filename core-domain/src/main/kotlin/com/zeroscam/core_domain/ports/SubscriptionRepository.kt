package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.Subscription
import com.zeroscam.core_domain.value.UserId

/**
 * Accès aux abonnements utilisateurs.
 */
interface SubscriptionRepository {
    fun findActiveSubscription(userId: UserId): Subscription?

    fun save(subscription: Subscription)
}
