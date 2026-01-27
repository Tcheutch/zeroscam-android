package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.SubscriptionDao
import com.zeroscam.coredata.mapper.toDomain
import com.zeroscam.coredata.mapper.toEntity
import com.zeroscam.coredomain.model.Subscription
import com.zeroscam.coredomain.ports.SubscriptionRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Clock
import java.time.Instant
import java.util.UUID

class RoomSubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val clock: Clock = Clock.systemUTC(),
) : SubscriptionRepository {
    override fun findActiveSubscription(userId: UserId): Subscription? =
        subscriptionDao.findActiveByUser(userId.value)?.toDomain()

    override fun save(subscription: Subscription) {
        val now = Instant.now(clock)
        // FIXME: Domain model Subscription lacks ID and UserId.
        // Generating random ID and using placeholder UserId to satisfy compilation.
        // This requires a Domain API update to pass UserId properly.
        // Critical: Missing context for userId
        subscriptionDao.upsert(
            subscription.toEntity(
                id = UUID.randomUUID().toString(),
                userId = "unknown_user",
                now = now,
            ),
        )
    }
}
