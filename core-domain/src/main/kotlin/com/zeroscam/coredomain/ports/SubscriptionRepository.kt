package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.Subscription
import com.zeroscam.coredomain.value.UserId

/**
 * Accès aux abonnements utilisateurs.
 */
interface SubscriptionRepository {
    fun findActiveSubscription(userId: UserId): Subscription?

    fun save(subscription: Subscription)
}
