package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.MessageScamDetector
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeIncomingMessageUseCaseTest {
    @Test
    fun escalatesToCriticalWhenSenderAndUrlKnownScam() {
        val scamUrl = "https://scam.example.com"

        val detector =
            FakeMessageScamDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.8,
                baseReasons =
                    listOf(
                        AnalyzeIncomingMessageUseCase.REASON_SENDER_KNOWN_SCAM,
                    ),
            )

        val threatIntel =
            FakeThreatIntelRepository(
                knownScamUrls = setOf(scamUrl),
            )

        val logRepository = FakeDetectionLogRepository()

        val useCase =
            AnalyzeIncomingMessageUseCase(
                messageScamDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val message =
            Message(
                id = "msg-1",
                userId = UserId("user-1"),
                channel = DetectionChannel.values().first(),
                content = "Hello, click here to win a prize: $scamUrl",
                receivedAt = Instant.parse("2024-01-01T10:00:00Z"),
            )

        val result = useCase(message)

        // Risk & score
        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.97)

        // Reasons primaires + escalades
        assertTrue(result.reasons.contains(AnalyzeIncomingMessageUseCase.REASON_SENDER_KNOWN_SCAM))
        assertTrue(result.reasons.contains(AnalyzeIncomingMessageUseCase.REASON_URL_KNOWN_SCAM))
        assertTrue(result.reasons.contains("escalation_sender_known_scam"))
        assertTrue(result.reasons.contains("escalation_url_known_scam"))

        // Logging
        assertEquals(message, logRepository.lastMessageLogged)
        assertEquals(result, logRepository.lastMessageResult)
    }

    @Test
    fun escalatesQrPaymentScenarioToHigh() {
        val detector =
            FakeMessageScamDetector(
                baseRiskLevel = RiskLevel.LOW,
                baseConfidence = 0.5,
                baseReasons =
                    listOf(
                        AnalyzeIncomingMessageUseCase.REASON_QR_CODE_PAYMENT,
                    ),
            )

        val threatIntel = FakeThreatIntelRepository()
        val logRepository = FakeDetectionLogRepository()

        val useCase =
            AnalyzeIncomingMessageUseCase(
                messageScamDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val message =
            Message(
                id = "msg-qr-pay",
                userId = UserId("user-qr"),
                channel = DetectionChannel.values().first(),
                content = "Scan to pay your order now via MoMo.",
                receivedAt = Instant.parse("2024-01-02T12:00:00Z"),
            )

        val result = useCase(message)

        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.95)
        assertTrue(result.reasons.contains("escalation_qr_code_payment"))

        assertEquals(message, logRepository.lastMessageLogged)
        assertEquals(result, logRepository.lastMessageResult)
    }

    @Test
    fun escalatesModeratelyForGenericQrCodeWithoutPaymentHint() {
        val detector =
            FakeMessageScamDetector(
                baseRiskLevel = RiskLevel.LOW,
                baseConfidence = 0.4,
                baseReasons =
                    listOf(
                        AnalyzeIncomingMessageUseCase.REASON_QR_CODE_PRESENT,
                    ),
            )

        val threatIntel = FakeThreatIntelRepository()
        val logRepository = FakeDetectionLogRepository()

        val useCase =
            AnalyzeIncomingMessageUseCase(
                messageScamDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val message =
            Message(
                id = "msg-qr-generic",
                userId = UserId("user-generic"),
                channel = DetectionChannel.values().first(),
                content = "Scan this QR code to view the event details.",
                receivedAt = Instant.parse("2024-01-03T09:30:00Z"),
            )

        val result = useCase(message)

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.88)
        assertTrue(result.reasons.contains("escalation_qr_code_generic"))

        assertEquals(message, logRepository.lastMessageLogged)
        assertEquals(result, logRepository.lastMessageResult)
    }

    @Test
    fun doesNotEscalateWhenNoSignals() {
        val detector =
            FakeMessageScamDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.4,
                baseReasons = emptyList(),
            )

        val threatIntel = FakeThreatIntelRepository()
        val logRepository = FakeDetectionLogRepository()

        val useCase =
            AnalyzeIncomingMessageUseCase(
                messageScamDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val message =
            Message(
                id = "msg-clean",
                userId = UserId("user-clean"),
                channel = DetectionChannel.values().first(),
                content = "Hello, this is a normal informational message.",
                receivedAt = Instant.parse("2024-01-04T15:45:00Z"),
            )

        val result = useCase(message)

        // Pas d’escalade
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(0.4, result.confidenceScore, 0.0001)
        assertTrue(result.reasons.isEmpty())

        assertEquals(message, logRepository.lastMessageLogged)
        assertEquals(result, logRepository.lastMessageResult)
    }

    // ---------------------------------------------------------------------
    // Fakes
    // ---------------------------------------------------------------------

    private class FakeMessageScamDetector(
        private val baseRiskLevel: RiskLevel,
        private val baseConfidence: Double,
        private val baseReasons: List<String>,
    ) : MessageScamDetector {
        override fun analyze(message: Message): DetectionResult =
            DetectionResult(
                id = "det-${message.id}",
                userId = message.userId,
                createdAt = message.receivedAt,
                channel = message.channel,
                riskLevel = baseRiskLevel,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = baseConfidence,
                reasons = baseReasons,
                recommendation = "monitor",
            )
    }

    private class FakeThreatIntelRepository(
        private val knownScamUrls: Set<String> = emptySet(),
    ) : ThreatIntelRepository {
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

        override fun isKnownScamUrl(url: String): Boolean = knownScamUrls.contains(url)

        override fun isKnownScamPaymentDestination(
            iban: String?,
            walletAddress: String?,
        ): Boolean = false
    }

    private class FakeDetectionLogRepository : DetectionLogRepository {
        var lastMessageLogged: Message? = null
            private set

        var lastMessageResult: DetectionResult? = null
            private set

        override fun logCallDetection(
            call: PhoneCall,
            result: DetectionResult,
        ) {
            // no-op
        }

        override fun logMessageDetection(
            message: Message,
            result: DetectionResult,
        ) {
            lastMessageLogged = message
            lastMessageResult = result
        }

        override fun logPaymentDetection(
            paymentIntent: PaymentIntent,
            result: DetectionResult,
        ) {
            // no-op
        }

        override fun logDeviceSecurityDetection(
            snapshot: DeviceSecuritySnapshot,
            result: DetectionResult,
        ) {
            // no-op
        }
    }
}
