package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AggregateUserRiskUseCaseTest {
    private val userId = UserId("user-agg-1")
    private val baseInstant = Instant.parse("2024-02-01T12:00:00Z")

    @Test
    fun `single detection is propagated as aggregated risk`() {
        val detection =
            detection(
                id = "det-1",
                channel = DetectionChannel.MESSAGE,
                riskLevel = RiskLevel.MEDIUM,
                confidence = 0.6,
            )

        val useCase = AggregateUserRiskUseCase()
        val aggregated = useCase(listOf(detection))

        assertEquals(userId, aggregated.userId)
        assertEquals(RiskLevel.MEDIUM, aggregated.riskLevel)
        assertEquals(0.6, aggregated.confidenceScore, 0.0001)
        assertEquals(1, aggregated.sources.size)
        assertEquals("det-1", aggregated.sources.first().id)
    }

    @Test
    fun `multiple high risk detections escalate to CRITICAL`() {
        val messageDetection =
            detection(
                id = "det-msg",
                channel = DetectionChannel.MESSAGE,
                riskLevel = RiskLevel.MEDIUM,
                confidence = 0.7,
                offsetSeconds = 0,
            )

        val callDetection =
            detection(
                id = "det-call",
                channel = DetectionChannel.CALL,
                riskLevel = RiskLevel.HIGH,
                confidence = 0.9,
                offsetSeconds = 5,
            )

        val secondCallDetection =
            detection(
                id = "det-call-2",
                // Reuse CALL channel (comment must be on its own line for ktlint)
                channel = DetectionChannel.CALL,
                riskLevel = RiskLevel.HIGH,
                confidence = 0.85,
                offsetSeconds = 10,
            )

        val useCase = AggregateUserRiskUseCase()
        val aggregated =
            useCase(
                listOf(
                    messageDetection,
                    callDetection,
                    secondCallDetection,
                ),
            )

        assertEquals(userId, aggregated.userId)
        assertEquals(RiskLevel.CRITICAL, aggregated.riskLevel)
        assertTrue(aggregated.confidenceScore >= 0.95)
        assertEquals(3, aggregated.sources.size)
    }

    @Test
    fun `all LOW on multiple channels is bumped to MEDIUM with minimum confidence`() {
        val lowMessage =
            detection(
                id = "det-low-msg",
                channel = DetectionChannel.MESSAGE,
                riskLevel = RiskLevel.LOW,
                confidence = 0.4,
            )

        val lowCall =
            detection(
                id = "det-low-call",
                channel = DetectionChannel.CALL,
                riskLevel = RiskLevel.LOW,
                confidence = 0.5,
                offsetSeconds = 5,
            )

        val useCase = AggregateUserRiskUseCase()
        val aggregated = useCase(listOf(lowMessage, lowCall))

        assertEquals(userId, aggregated.userId)
        assertEquals(RiskLevel.MEDIUM, aggregated.riskLevel)
        assertEquals(0.70, aggregated.confidenceScore, 0.0001)
        assertEquals(2, aggregated.sources.size)
    }

    @Test
    fun `confidence is clamped to one when engines overshoot`() {
        val detectionHigh1 =
            detection(
                id = "det-high-1",
                channel = DetectionChannel.MESSAGE,
                riskLevel = RiskLevel.HIGH,
                confidence = 1.2,
            )

        val detectionHigh2 =
            detection(
                id = "det-high-2",
                channel = DetectionChannel.CALL,
                riskLevel = RiskLevel.HIGH,
                confidence = 0.9,
                offsetSeconds = 5,
            )

        val useCase = AggregateUserRiskUseCase()
        val aggregated = useCase(listOf(detectionHigh1, detectionHigh2))

        assertEquals(userId, aggregated.userId)
        assertEquals(RiskLevel.CRITICAL, aggregated.riskLevel)
        assertEquals(1.0, aggregated.confidenceScore, 0.0001)
    }

    private fun detection(
        id: String,
        channel: DetectionChannel,
        riskLevel: RiskLevel,
        confidence: Double,
        offsetSeconds: Long = 0,
    ): DetectionResult {
        return DetectionResult(
            id = id,
            userId = userId,
            createdAt = baseInstant.plusSeconds(offsetSeconds),
            channel = channel,
            riskLevel = riskLevel,
            scamType = null,
            attackVectors = emptyList(),
            confidenceScore = confidence,
            reasons = emptyList(),
            recommendation = "monitor",
        )
    }
}
