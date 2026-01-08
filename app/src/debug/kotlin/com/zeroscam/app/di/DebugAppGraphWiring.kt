package com.zeroscam.app.di

import android.content.Context
import android.util.Log
import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.CallScamDetector
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.DeviceThreatDetector
import com.zeroscam.coredomain.ports.MessageScamDetector
import com.zeroscam.coredomain.ports.PaymentRiskEngine
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import com.zeroscam.coredomain.usecase.AnalyzeIncomingCallUseCase
import com.zeroscam.coredomain.usecase.AnalyzeIncomingMessageUseCase
import com.zeroscam.coredomain.usecase.EvaluateDeviceSecurityStateUseCase
import com.zeroscam.coredomain.usecase.EvaluatePaymentIntentUseCase
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import java.util.UUID

/**
 * DEBUG fallback "béton armé".
 * Vit dans src/debug -> jamais packagé en release.
 *
 * AppGraph.initIfNeeded() (main) appelle cette classe via reflection.
 */
object DebugAppGraphWiring {
    @JvmStatic
    fun initIfNeeded(
        @Suppress("UNUSED_PARAMETER") context: Context,
    ) {
        // Déjà init ? (pas d'accès au flag internal => on tente un getter)
        val alreadyInit =
            runCatching { AppGraph.analyzeIncomingMessageUseCase }
                .isSuccess

        if (alreadyInit) return

        val threatIntelRepository: ThreatIntelRepository = NoOpThreatIntelRepository()
        val detectionLogRepository: DetectionLogRepository = NoOpDetectionLogRepository()

        val messageScamDetector: MessageScamDetector = NoOpMessageScamDetector()
        val callScamDetector: CallScamDetector = NoOpCallScamDetector()
        val paymentRiskEngine: PaymentRiskEngine = NoOpPaymentRiskEngine()
        val deviceThreatDetector: DeviceThreatDetector = NoOpDeviceThreatDetector()

        AppGraph.init(
            analyzeIncomingMessageUseCase =
                AnalyzeIncomingMessageUseCase(
                    messageScamDetector = messageScamDetector,
                    threatIntelRepository = threatIntelRepository,
                    detectionLogRepository = detectionLogRepository,
                ),
            analyzeIncomingCallUseCase =
                AnalyzeIncomingCallUseCase(
                    callScamDetector = callScamDetector,
                    threatIntelRepository = threatIntelRepository,
                    detectionLogRepository = detectionLogRepository,
                ),
            evaluatePaymentIntentUseCase =
                EvaluatePaymentIntentUseCase(
                    paymentRiskEngine = paymentRiskEngine,
                    threatIntelRepository = threatIntelRepository,
                    detectionLogRepository = detectionLogRepository,
                ),
            evaluateDeviceSecurityStateUseCase =
                EvaluateDeviceSecurityStateUseCase(
                    deviceThreatDetector = deviceThreatDetector,
                    threatIntelRepository = threatIntelRepository,
                    detectionLogRepository = detectionLogRepository,
                ),
        )

        Log.w(TAG, "DEBUG fallback wired (NoOp ports). Safe for androidTest/debug boot.")
    }

    private const val TAG = "AppGraph"
}

private fun safeChannel(vararg preferred: String): DetectionChannel {
    val fallback = DetectionChannel.entries.first()
    for (name in preferred) {
        val candidate = runCatching { DetectionChannel.valueOf(name) }.getOrNull()
        if (candidate != null) return candidate
    }
    return fallback
}

private fun noOpResult(
    idPrefix: String,
    userId: UserId,
    channel: DetectionChannel,
    risk: RiskLevel = RiskLevel.LOW,
    confidence: Double = 0.10,
    recommendation: String = "NoOp: no strong signal.",
): DetectionResult =
    DetectionResult(
        id = "$idPrefix-${UUID.randomUUID()}",
        userId = userId,
        createdAt = Instant.now(),
        channel = channel,
        riskLevel = risk,
        scamType = null,
        attackVectors = emptyList(),
        confidenceScore = confidence,
        reasons = emptyList(),
        recommendation = recommendation,
    )

private class NoOpMessageScamDetector : MessageScamDetector {
    override fun analyze(message: Message): DetectionResult =
        noOpResult(
            idPrefix = "det-msg",
            userId = message.userId,
            channel = safeChannel("MESSAGE", "SMS", "WHATSAPP", "EMAIL"),
        )
}

private class NoOpCallScamDetector : CallScamDetector {
    override fun analyze(call: PhoneCall): DetectionResult =
        noOpResult(
            idPrefix = "det-call",
            userId = call.userId,
            channel = safeChannel("CALL", "PHONE_CALL"),
        )
}

private class NoOpPaymentRiskEngine : PaymentRiskEngine {
    override fun analyze(paymentIntent: PaymentIntent): DetectionResult =
        noOpResult(
            idPrefix = "det-pay",
            userId = paymentIntent.userId,
            channel = safeChannel("PAYMENT", "TRANSFER", "MOMO", "WALLET"),
        )
}

private class NoOpDeviceThreatDetector : DeviceThreatDetector {
    override fun analyze(snapshot: DeviceSecuritySnapshot): DetectionResult {
        val risk =
            if (!snapshot.integrityCheckPassed || snapshot.isRootedOrJailbroken) {
                RiskLevel.HIGH
            } else if (
                snapshot.isEmulator ||
                snapshot.hasDebuggableBuild ||
                snapshot.hasSuspiciousApps
            ) {
                RiskLevel.MEDIUM
            } else {
                RiskLevel.LOW
            }

        val confidence = if (risk >= RiskLevel.HIGH) 0.85 else 0.50

        return noOpResult(
            idPrefix = "det-dev",
            userId = snapshot.userId,
            channel = safeChannel("DEVICE_SECURITY", "DEVICE", "SECURITY"),
            risk = risk,
            confidence = confidence,
            recommendation = "NoOp device detector: basic snapshot evaluation.",
        )
    }
}

private class NoOpThreatIntelRepository : ThreatIntelRepository {
    override fun adjustCallResult(
        call: PhoneCall,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustMessageResult(
        message: Message,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustPaymentResult(
        paymentIntent: PaymentIntent,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustDeviceSecurityResult(
        snapshot: DeviceSecuritySnapshot,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun isKnownScamPhone(phoneNumber: String): Boolean = false

    override fun isKnownScamSender(sender: String): Boolean = false

    override fun isKnownScamUrl(url: String): Boolean = false

    override fun isKnownScamPaymentDestination(
        iban: String?,
        walletAddress: String?,
    ): Boolean = false
}

private class NoOpDetectionLogRepository : DetectionLogRepository {
    override fun logMessageDetection(
        message: Message,
        result: DetectionResult,
    ) = Unit

    override fun logCallDetection(
        call: PhoneCall,
        result: DetectionResult,
    ) = Unit

    override fun logPaymentDetection(
        paymentIntent: PaymentIntent,
        result: DetectionResult,
    ) = Unit

    override fun logDeviceSecurityDetection(
        snapshot: DeviceSecuritySnapshot,
        result: DetectionResult,
    ) = Unit
}
