package com.zeroscam.app

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.value.MoneyAmount
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import kotlin.math.roundToLong

/**
 * Fixtures androidTest "béton armé" — 100% conformes aux modèles core-domain.
 *
 * - Une seule source de vérité (évite redeclaration).
 * - MoneyAmount stocké en plus petite unité (centimes) => Long.
 */
internal object DummyFactory {
    private val fixedNow: Instant = Instant.parse("2025-01-01T00:00:00Z")

    fun userId(value: String = "u_test"): UserId = UserId(value)

    /** Convertit un montant "humain" (ex: 100.50) en centimes (Long). */
    fun money(amount: Double): MoneyAmount = MoneyAmount((amount * 100.0).roundToLong())

    fun result(
        userId: UserId = userId(),
        channel: DetectionChannel = DetectionChannel.entries.first(),
        riskLevel: RiskLevel = RiskLevel.LOW,
        confidenceScore: Double = 0.10,
        reasons: List<String> = emptyList(),
        recommendation: String = "test",
        now: Instant = fixedNow,
    ): DetectionResult =
        DetectionResult(
            id = "det_test",
            userId = userId,
            createdAt = now,
            channel = channel,
            riskLevel = riskLevel,
            scamType = null,
            attackVectors = emptyList(),
            confidenceScore = confidenceScore,
            reasons = reasons,
            recommendation = recommendation,
        )

    fun message(
        id: String = "msg_test",
        userId: UserId = userId(),
        content: String = "hello",
        channel: DetectionChannel = DetectionChannel.entries.first(),
        source: String? = "sms",
        receivedAt: Instant = fixedNow,
    ): Message =
        Message(
            id = id,
            userId = userId,
            content = content,
            channel = channel,
            source = source,
            receivedAt = receivedAt,
        )

    fun call(
        id: String = "call_test",
        userId: UserId = userId(),
        phoneNumber: String = "+237600000000",
        startedAt: Instant = fixedNow,
        countryIso: String? = "CM",
        isInContacts: Boolean = false,
        isFromUnknownNumber: Boolean = true,
    ): PhoneCall =
        PhoneCall(
            id = id,
            userId = userId,
            phoneNumber = phoneNumber,
            startedAt = startedAt,
            countryIso = countryIso,
            isInContacts = isInContacts,
            isFromUnknownNumber = isFromUnknownNumber,
        )

    fun payment(
        id: String = "pay_test",
        userId: UserId = userId(),
        amount: MoneyAmount = money(100.0),
        currency: String = "XAF",
        recipientAccount: String = "momo:237699000000",
        channel: String = "momo",
        createdAt: Instant = fixedNow,
        metadata: Map<String, String> = emptyMap(),
    ): PaymentIntent =
        PaymentIntent(
            id = id,
            userId = userId,
            amount = amount,
            currency = currency,
            recipientAccount = recipientAccount,
            channel = channel,
            createdAt = createdAt,
            metadata = metadata,
        )

    fun device(
        id: String = "dev_test",
        userId: UserId = userId(),
        capturedAt: Instant = fixedNow,
        isRootedOrJailbroken: Boolean = false,
        isEmulator: Boolean = true,
        hasDebuggableBuild: Boolean = true,
        hasSuspiciousApps: Boolean = false,
        integrityCheckPassed: Boolean = true,
    ): DeviceSecuritySnapshot =
        DeviceSecuritySnapshot(
            id = id,
            userId = userId,
            capturedAt = capturedAt,
            isRootedOrJailbroken = isRootedOrJailbroken,
            isEmulator = isEmulator,
            hasDebuggableBuild = hasDebuggableBuild,
            hasSuspiciousApps = hasSuspiciousApps,
            integrityCheckPassed = integrityCheckPassed,
        )
}
