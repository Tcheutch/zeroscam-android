package com.zeroscam.coredata.mapper

import com.zeroscam.coredata.db.JsonCodec
import com.zeroscam.coredata.db.entity.CallEventEntity
import com.zeroscam.coredata.db.entity.DetectionEventEntity
import com.zeroscam.coredata.db.entity.DeviceSecurityEventEntity
import com.zeroscam.coredata.db.entity.MessageEventEntity
import com.zeroscam.coredata.db.entity.PaymentEventEntity
import com.zeroscam.coredata.db.entity.SubscriptionEntity
import com.zeroscam.coredata.db.entity.UserFeedbackEntity
import com.zeroscam.coredata.db.entity.UserProfileEntity
import com.zeroscam.coredomain.enums.SubscriptionPlan
import com.zeroscam.coredomain.enums.SubscriptionStatus
import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.model.Subscription
import com.zeroscam.coredomain.model.UserProfile
import com.zeroscam.coredomain.value.MoneyAmount
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import java.util.UUID

internal fun Instant.toEpochMs(): Long = this.toEpochMilli()

internal fun epochMsToInstant(epochMs: Long): Instant = Instant.ofEpochMilli(epochMs)

internal fun Message.toEntity(now: Instant): MessageEventEntity {
    return MessageEventEntity(
        id = id,
        userId = userId.value,
        content = content,
        channel = channel.name,
        source = source,
        receivedAtEpochMs = receivedAt.toEpochMs(),
        createdAtEpochMs = now.toEpochMs(),
    )
}

internal fun PhoneCall.toEntity(now: Instant): CallEventEntity {
    return CallEventEntity(
        id = id,
        userId = userId.value,
        phoneNumber = phoneNumber,
        countryIso = countryIso,
        isInContacts = isInContacts,
        isFromUnknownNumber = isFromUnknownNumber,
        startedAtEpochMs = startedAt.toEpochMs(),
        createdAtEpochMs = now.toEpochMs(),
    )
}

internal fun PaymentIntent.toEntity(): PaymentEventEntity {
    return PaymentEventEntity(
        id = id,
        userId = userId.value,
        amountMinor = amount.value,
        currency = currency,
        recipientAccount = recipientAccount,
        channel = channel,
        createdAtEpochMs = createdAt.toEpochMs(),
    )
}

internal fun DeviceSecuritySnapshot.toEntity(now: Instant): DeviceSecurityEventEntity {
    return DeviceSecurityEventEntity(
        id = id,
        userId = userId.value,
        capturedAtEpochMs = capturedAt.toEpochMs(),
        isRootedOrJailbroken = isRootedOrJailbroken,
        isEmulator = isEmulator,
        hasDebuggableBuild = hasDebuggableBuild,
        hasSuspiciousApps = hasSuspiciousApps,
        integrityCheckPassed = integrityCheckPassed,
        createdAtEpochMs = now.toEpochMs(),
    )
}

internal fun DetectionResult.toEntity(): DetectionEventEntity {
    return DetectionEventEntity(
        id = id,
        userId = userId.value,
        channel = channel.name,
        riskLevel = riskLevel.name,
        confidenceScore = confidenceScore,
        recommendation = recommendation,
        reasonsJson = JsonCodec.toJsonArrayString(reasons),
        createdAtEpochMs = createdAt.toEpochMs(),
    )
}

internal fun DetectionFeedback.toEntity(now: Instant): UserFeedbackEntity {
    return UserFeedbackEntity(
        id = UUID.randomUUID().toString(),
        detectionId = detectionId,
        isScam = isScam ?: false,
        comment = comment,
        createdAtEpochMs = now.toEpochMs(),
    )
}

internal fun UserProfile.toEntity(now: Instant): UserProfileEntity {
    return UserProfileEntity(
        userId = id.value,
        displayName = "",
        phoneNumber = phoneNumber ?: "",
        countryIso = "",
        createdAtEpochMs = now.toEpochMs(),
        updatedAtEpochMs = now.toEpochMs(),
    )
}

internal fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = UserId(userId),
        phoneNumber = phoneNumber,
        email = null,
        subscription = null,
    )
}

internal fun Subscription.toEntity(
    id: String,
    userId: String,
    now: Instant,
): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        userId = userId,
        plan = plan.name,
        isActive = isActive(now),
        validUntilEpochMs = expiresAt?.toEpochMs(),
        createdAtEpochMs = startedAt.toEpochMs(),
        updatedAtEpochMs = now.toEpochMs(),
    )
}

internal fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        plan = SubscriptionPlan.valueOf(plan),
        status = if (isActive) SubscriptionStatus.ACTIVE else SubscriptionStatus.EXPIRED,
        startedAt = epochMsToInstant(createdAtEpochMs),
        expiresAt = validUntilEpochMs?.let(::epochMsToInstant),
    )
}

internal fun PaymentEventEntity.toDomain(): PaymentIntent {
    return PaymentIntent(
        id = id,
        userId = UserId(userId),
        amount = MoneyAmount(amountMinor),
        currency = currency,
        recipientAccount = recipientAccount,
        channel = channel,
        createdAt = epochMsToInstant(createdAtEpochMs),
        metadata = emptyMap(),
    )
}
