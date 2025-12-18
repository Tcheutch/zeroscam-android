package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.CallScamDetector
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeIncomingCallUseCaseTest {
    @Test
    fun `escalates to CRITICAL when caller is known scam in intel and reasons`() {
        val now = Instant.parse("2024-01-10T10:00:00Z")
        val userId = UserId("user-call-1")

        val baseDetector =
            FakeCallScamDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.8,
                baseReasons =
                    listOf(
                        AnalyzeIncomingCallUseCase.REASON_CALLER_KNOWN_SCAM,
                    ),
            )

        val threatIntel =
            FakeThreatIntelRepository(
                knownScamPhones = setOf("+33123456789"),
            )

        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            AnalyzeIncomingCallUseCase(
                callScamDetector = baseDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val call =
            PhoneCall(
                id = "call-1",
                userId = userId,
                phoneNumber = "+33123456789",
                startedAt = now,
                countryIso = "FR",
                isInContacts = false,
                isFromUnknownNumber = true,
            )

        val result = useCase(call)

        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.97)
        assertTrue(result.reasons.contains(AnalyzeIncomingCallUseCase.REASON_CALLER_KNOWN_SCAM))
        assertTrue(result.reasons.contains("escalation_caller_known_scam"))

        assertEquals(call, logRepository.lastCallLogged)
        assertEquals(result, logRepository.lastCallResult)
    }

    @Test
    fun `escalates for multiple metadata signals`() {
        val now = Instant.parse("2024-01-10T11:00:00Z")
        val userId = UserId("user-call-2")

        val baseDetector =
            FakeCallScamDetector(
                baseRiskLevel = RiskLevel.LOW,
                baseConfidence = 0.4,
                baseReasons =
                    listOf(
                        AnalyzeIncomingCallUseCase.REASON_CALLER_NUMBER_MALFORMED,
                        AnalyzeIncomingCallUseCase.REASON_HIGH_RISK_COUNTRY_PREFIX,
                    ),
            )

        val threatIntel = FakeThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            AnalyzeIncomingCallUseCase(
                callScamDetector = baseDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val call =
            PhoneCall(
                id = "call-2",
                userId = userId,
                phoneNumber = "0099-XXX",
                startedAt = now,
                countryIso = "XX",
                isInContacts = false,
                isFromUnknownNumber = true,
            )

        val result = useCase(call)

        // LOW + 2 signaux -> bumpRisk x2 -> HIGH
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.92)
        assertTrue(result.reasons.contains("escalation_caller_number_malformed"))
        assertTrue(result.reasons.contains("escalation_high_risk_country_prefix"))

        assertEquals(call, logRepository.lastCallLogged)
        assertEquals(result, logRepository.lastCallResult)
    }

    @Test
    fun `keeps original result when no scam indicators`() {
        val now = Instant.parse("2024-01-10T12:00:00Z")
        val userId = UserId("user-call-3")

        val baseDetector =
            FakeCallScamDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.6,
                baseReasons = emptyList(),
            )

        val threatIntel = FakeThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            AnalyzeIncomingCallUseCase(
                callScamDetector = baseDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val call =
            PhoneCall(
                id = "call-3",
                userId = userId,
                phoneNumber = "+237650000000",
                startedAt = now,
                countryIso = "CM",
                isInContacts = true,
                isFromUnknownNumber = false,
            )

        val result = useCase(call)

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(0.6, result.confidenceScore, 0.0001)
        assertTrue(result.reasons.isEmpty())

        assertEquals(call, logRepository.lastCallLogged)
        assertEquals(result, logRepository.lastCallResult)
    }

    // ---------------------------------------------------------------------
    // Fakes
    // ---------------------------------------------------------------------

    private class FakeCallScamDetector(
        private val baseRiskLevel: RiskLevel,
        private val baseConfidence: Double,
        private val baseReasons: List<String>,
    ) : CallScamDetector {
        override fun analyze(call: PhoneCall): DetectionResult =
            DetectionResult(
                id = "det-${call.id}",
                userId = call.userId,
                createdAt = call.startedAt,
                channel = DetectionChannel.CALL,
                riskLevel = baseRiskLevel,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = baseConfidence,
                reasons = baseReasons,
                recommendation = "monitor",
            )
    }

    private class FakeThreatIntelRepository(
        private val knownScamPhones: Set<String> = emptySet(),
    ) : ThreatIntelRepository {
        override fun adjustCallResult(
            call: PhoneCall,
            initialResult: DetectionResult,
        ): DetectionResult {
            return initialResult
        }

        override fun adjustMessageResult(
            message: Message,
            initialResult: DetectionResult,
        ): DetectionResult {
            return initialResult
        }

        override fun adjustPaymentResult(
            paymentIntent: PaymentIntent,
            initialResult: DetectionResult,
        ): DetectionResult {
            return initialResult
        }

        override fun adjustDeviceSecurityResult(
            snapshot: DeviceSecuritySnapshot,
            initialResult: DetectionResult,
        ): DetectionResult {
            return initialResult
        }

        override fun isKnownScamPhone(phoneNumber: String): Boolean = knownScamPhones.contains(phoneNumber)

        override fun isKnownScamSender(sender: String): Boolean = false

        override fun isKnownScamUrl(url: String): Boolean = false

        override fun isKnownScamPaymentDestination(
            iban: String?,
            walletAddress: String?,
        ): Boolean = false
    }

    private class InMemoryDetectionLogRepository : DetectionLogRepository {
        var lastCallLogged: PhoneCall? = null
            private set

        var lastCallResult: DetectionResult? = null
            private set

        override fun logCallDetection(
            call: PhoneCall,
            result: DetectionResult,
        ) {
            lastCallLogged = call
            lastCallResult = result
        }

        override fun logMessageDetection(
            message: Message,
            result: DetectionResult,
        ) {
            // no-op
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
