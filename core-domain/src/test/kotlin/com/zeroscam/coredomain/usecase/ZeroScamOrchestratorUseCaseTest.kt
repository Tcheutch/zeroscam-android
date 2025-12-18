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
import com.zeroscam.coredomain.ports.DeviceThreatDetector
import com.zeroscam.coredomain.ports.MessageScamDetector
import com.zeroscam.coredomain.ports.PaymentRiskEngine
import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import com.zeroscam.coredomain.ports.UserRiskSnapshot
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ZeroScamOrchestratorUseCaseTest {
    @Test
    fun `aggregates multi channel detections and exports snapshot`() {
        val userId = UserId("user-orchestrator")
        val baseInstant = Instant.parse("2024-02-01T10:00:00Z")

        val messageDetector = OrchestratorFakeMessageScamDetector()
        val callDetector = OrchestratorFakeCallScamDetector()
        val deviceDetector = OrchestratorFakeDeviceThreatDetector()
        val paymentEngine = NoopPaymentRiskEngine()
        val threatIntel = NoopThreatIntelRepository()
        val logRepository = NoopDetectionLogRepository()
        val researchExport = InMemoryResearchExportPort()

        val messageUseCase =
            AnalyzeIncomingMessageUseCase(
                messageScamDetector = messageDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val callUseCase =
            AnalyzeIncomingCallUseCase(
                callScamDetector = callDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val paymentUseCase =
            EvaluatePaymentIntentUseCase(
                paymentRiskEngine = paymentEngine,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val deviceUseCase =
            EvaluateDeviceSecurityStateUseCase(
                deviceThreatDetector = deviceDetector,
                threatIntelRepository = threatIntel,
                detectionLogRepository = logRepository,
            )

        val aggregateUseCase = AggregateUserRiskUseCase()

        val orchestrator =
            ZeroScamOrchestratorUseCase(
                analyzeIncomingMessageUseCase = messageUseCase,
                analyzeIncomingCallUseCase = callUseCase,
                evaluatePaymentIntentUseCase = paymentUseCase,
                evaluateDeviceSecurityStateUseCase = deviceUseCase,
                aggregateUserRiskUseCase = aggregateUseCase,
                researchExportPort = researchExport,
            )

        val message =
            Message(
                id = "msg-1",
                userId = userId,
                channel = DetectionChannel.MESSAGE,
                content = "Hello world",
                receivedAt = baseInstant,
            )

        val call =
            PhoneCall(
                id = "call-1",
                userId = userId,
                phoneNumber = "+237650000000",
                startedAt = baseInstant.plusSeconds(5),
                countryIso = "CM",
                isInContacts = true,
                isFromUnknownNumber = false,
            )

        val snapshot =
            DeviceSecuritySnapshot(
                id = "dev-1",
                userId = userId,
                capturedAt = baseInstant.plusSeconds(10),
                isRootedOrJailbroken = false,
                isEmulator = false,
                hasDebuggableBuild = false,
                hasSuspiciousApps = false,
                integrityCheckPassed = true,
            )

        val request =
            ZeroScamOrchestrationRequest(
                message = message,
                call = call,
                paymentIntent = null,
                deviceSnapshot = snapshot,
            )

        val result = orchestrator(request)

        val aggregated = result.aggregatedUserRisk
        assertNotNull(aggregated)
        aggregated!!

        // Avec 2 HIGH (call + device) et 1 MEDIUM (message) :
        // AggregateUserRiskUseCase -> CRITICAL + confiance renforcée.
        assertEquals(RiskLevel.CRITICAL, aggregated.riskLevel)
        assert(aggregated.confidenceScore >= 0.95)
        assertEquals(3, aggregated.sources.size)

        // Orchestrator renvoie bien les résultats unitaires
        assertNotNull(result.messageDetection)
        assertNotNull(result.callDetection)
        assertNotNull(result.deviceSecurityDetection)
        assertEquals(null, result.paymentDetection)

        // Export vers Vigilis-Research / ZeroScam Research backend
        val exportedSnapshot = researchExport.lastSnapshot
        assertNotNull(exportedSnapshot)
        exportedSnapshot!!

        assertEquals(aggregated.userId, exportedSnapshot.userId)
        assertEquals(aggregated.riskLevel, exportedSnapshot.globalRiskLevel)
        assertEquals(aggregated.confidenceScore, exportedSnapshot.globalConfidence, 0.0001)
        assertEquals(3, exportedSnapshot.detections.size)
    }

    private class OrchestratorFakeMessageScamDetector : MessageScamDetector {
        override fun analyze(message: Message): DetectionResult =
            DetectionResult(
                id = "det-message-${'$'}{message.id}",
                userId = message.userId,
                createdAt = message.receivedAt,
                channel = message.channel,
                riskLevel = RiskLevel.MEDIUM,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = 0.7,
                reasons = emptyList(),
                recommendation = "monitor",
            )
    }

    private class OrchestratorFakeCallScamDetector : CallScamDetector {
        override fun analyze(call: PhoneCall): DetectionResult =
            DetectionResult(
                id = "det-call-${'$'}{call.id}",
                userId = call.userId,
                createdAt = call.startedAt,
                channel = DetectionChannel.CALL,
                riskLevel = RiskLevel.HIGH,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = 0.9,
                reasons = emptyList(),
                recommendation = "monitor",
            )
    }

    private class OrchestratorFakeDeviceThreatDetector : DeviceThreatDetector {
        override fun analyze(snapshot: DeviceSecuritySnapshot): DetectionResult =
            DetectionResult(
                id = "det-device-${'$'}{snapshot.id}",
                userId = snapshot.userId,
                createdAt = snapshot.capturedAt,
                // IMPORTANT : on ne dépend pas d'une constante inexistante.
                // On utilise une valeur valide de l'enum pour rester robuste.
                channel = DetectionChannel.values().first(),
                riskLevel = RiskLevel.HIGH,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = 0.85,
                reasons = emptyList(),
                recommendation = "monitor",
            )
    }

    private class NoopPaymentRiskEngine : PaymentRiskEngine {
        override fun analyze(paymentIntent: PaymentIntent): DetectionResult {
            error("Payment path should not be invoked in this test")
        }
    }

    private class NoopThreatIntelRepository : ThreatIntelRepository {
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

    private class NoopDetectionLogRepository : DetectionLogRepository {
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

    private class InMemoryResearchExportPort : ResearchExportPort {
        var lastSnapshot: UserRiskSnapshot? = null
            private set

        override fun publishUserRiskSnapshot(snapshot: UserRiskSnapshot) {
            lastSnapshot = snapshot
        }
    }
}
