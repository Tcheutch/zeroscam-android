package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.DeviceThreatDetector
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateDeviceSecurityStateUseCaseTest {
    @Test
    fun `escalates to CRITICAL when integrity check fails`() {
        val detector =
            FakeDeviceThreatDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.8,
            )
        val threatIntel = NoOpThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            EvaluateDeviceSecurityStateUseCase(
                deviceThreatDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val snapshot =
            DeviceSecuritySnapshot(
                id = "snap-integrity-fail",
                userId = UserId("user-1"),
                capturedAt = Instant.parse("2024-01-10T10:00:00Z"),
                isRootedOrJailbroken = false,
                isEmulator = false,
                hasDebuggableBuild = false,
                hasSuspiciousApps = false,
                integrityCheckPassed = false,
            )

        val result = useCase(snapshot)

        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.97)
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_INTEGRITY_CHECK_FAILED,
            ),
        )

        assertEquals(snapshot, logRepository.lastDeviceSnapshot)
        assertEquals(result, logRepository.lastDeviceResult)
    }

    @Test
    fun `escalates rooted device at least to HIGH`() {
        val detector =
            FakeDeviceThreatDetector(
                baseRiskLevel = RiskLevel.LOW,
                baseConfidence = 0.4,
            )
        val threatIntel = NoOpThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            EvaluateDeviceSecurityStateUseCase(
                deviceThreatDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val snapshot =
            DeviceSecuritySnapshot(
                id = "snap-rooted",
                userId = UserId("user-2"),
                capturedAt = Instant.parse("2024-01-11T09:30:00Z"),
                isRootedOrJailbroken = true,
                isEmulator = false,
                hasDebuggableBuild = false,
                hasSuspiciousApps = false,
                integrityCheckPassed = true,
            )

        val result = useCase(snapshot)

        assertTrue(result.riskLevel >= RiskLevel.HIGH)
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_ROOT_OR_JAILBREAK_DETECTED,
            ),
        )

        assertEquals(snapshot, logRepository.lastDeviceSnapshot)
        assertEquals(result, logRepository.lastDeviceResult)
    }

    @Test
    fun `escalates to CRITICAL when multiple suspicious signals are present`() {
        val detector =
            FakeDeviceThreatDetector(
                baseRiskLevel = RiskLevel.MEDIUM,
                baseConfidence = 0.7,
            )
        val threatIntel = NoOpThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            EvaluateDeviceSecurityStateUseCase(
                deviceThreatDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val snapshot =
            DeviceSecuritySnapshot(
                id = "snap-multi-suspicious",
                userId = UserId("user-3"),
                capturedAt = Instant.parse("2024-01-12T08:15:00Z"),
                isRootedOrJailbroken = true,
                isEmulator = true,
                hasDebuggableBuild = true,
                hasSuspiciousApps = true,
                integrityCheckPassed = true,
            )

        val result = useCase(snapshot)

        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertTrue(result.confidenceScore >= 0.90)
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_ROOT_OR_JAILBREAK_DETECTED,
            ),
        )
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_EMULATOR_SUSPICIOUS,
            ),
        )
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_SUSPICIOUS_APPS,
            ),
        )
        assertTrue(
            result.reasons.contains(
                EvaluateDeviceSecurityStateUseCase.REASON_DEVICE_DEBUGGABLE_BUILD,
            ),
        )

        assertEquals(snapshot, logRepository.lastDeviceSnapshot)
        assertEquals(result, logRepository.lastDeviceResult)
    }

    @Test
    fun `leaves result untouched when device looks clean`() {
        val detector =
            FakeDeviceThreatDetector(
                baseRiskLevel = RiskLevel.LOW,
                baseConfidence = 0.3,
            )
        val threatIntel = NoOpThreatIntelRepository()
        val logRepository = InMemoryDetectionLogRepository()

        val useCase =
            EvaluateDeviceSecurityStateUseCase(
                deviceThreatDetector = detector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val snapshot =
            DeviceSecuritySnapshot(
                id = "snap-clean",
                userId = UserId("user-4"),
                capturedAt = Instant.parse("2024-01-13T14:00:00Z"),
                isRootedOrJailbroken = false,
                isEmulator = false,
                hasDebuggableBuild = false,
                hasSuspiciousApps = false,
                integrityCheckPassed = true,
            )

        val result = useCase(snapshot)

        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(0.3, result.confidenceScore, 0.0001)
        assertTrue(result.reasons.isEmpty())

        assertEquals(snapshot, logRepository.lastDeviceSnapshot)
        assertEquals(result, logRepository.lastDeviceResult)
    }

    // ---------------------------------------------------------------------
    // Fakes
    // ---------------------------------------------------------------------

    private class FakeDeviceThreatDetector(
        private val baseRiskLevel: RiskLevel,
        private val baseConfidence: Double,
    ) : DeviceThreatDetector {
        override fun analyze(snapshot: DeviceSecuritySnapshot): DetectionResult =
            DetectionResult(
                id = "det-${'$'}{snapshot.id}",
                userId = snapshot.userId,
                createdAt = snapshot.capturedAt,
                channel = DetectionChannel.values().first(),
                riskLevel = baseRiskLevel,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = baseConfidence,
                reasons = emptyList(),
                recommendation = "monitor",
            )
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

    private class InMemoryDetectionLogRepository : DetectionLogRepository {
        var lastCall: PhoneCall? = null
            private set
        var lastCallResult: DetectionResult? = null
            private set

        var lastMessage: Message? = null
            private set
        var lastMessageResult: DetectionResult? = null
            private set

        var lastPaymentIntent: PaymentIntent? = null
            private set
        var lastPaymentResult: DetectionResult? = null
            private set

        var lastDeviceSnapshot: DeviceSecuritySnapshot? = null
            private set
        var lastDeviceResult: DetectionResult? = null
            private set

        override fun logCallDetection(
            call: PhoneCall,
            result: DetectionResult,
        ) {
            lastCall = call
            lastCallResult = result
        }

        override fun logMessageDetection(
            message: Message,
            result: DetectionResult,
        ) {
            lastMessage = message
            lastMessageResult = result
        }

        override fun logPaymentDetection(
            paymentIntent: PaymentIntent,
            result: DetectionResult,
        ) {
            lastPaymentIntent = paymentIntent
            lastPaymentResult = result
        }

        override fun logDeviceSecurityDetection(
            snapshot: DeviceSecuritySnapshot,
            result: DetectionResult,
        ) {
            lastDeviceSnapshot = snapshot
            lastDeviceResult = result
        }
    }
}
