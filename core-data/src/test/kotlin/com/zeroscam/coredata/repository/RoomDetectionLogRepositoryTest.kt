package com.zeroscam.coredata.repository

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.value.UserId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomDetectionLogRepositoryTest {
    @Test
    fun `logMessageDetection - writes message + detection`() {
        val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)

        val messageDao = FakeMessageEventDao()
        val callDao = FakeCallEventDao()
        val paymentDao = FakePaymentEventDao()
        val deviceDao = FakeDeviceSecurityEventDao()
        val detectionDao = FakeDetectionEventDao()

        val repo =
            RoomDetectionLogRepository(
                messageEventDao = messageDao,
                callEventDao = callDao,
                paymentEventDao = paymentDao,
                deviceSecurityEventDao = deviceDao,
                detectionEventDao = detectionDao,
                clock = fixedClock,
            )

        val userId = UserId("u1")
        val message =
            Message(
                id = "m1",
                userId = userId,
                content = "hello",
                channel = DetectionChannel.MESSAGE,
                source = "sms",
                receivedAt = Instant.parse("2025-01-01T00:00:00Z"),
            )

        val result =
            DetectionResult(
                id = "d1",
                userId = userId,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
                channel = DetectionChannel.MESSAGE,
                riskLevel = RiskLevel.LOW,
                scamType = null,
                attackVectors = emptyList(),
                confidenceScore = 0.1,
                reasons = listOf("r1"),
                recommendation = "ok",
            )

        repo.logMessageDetection(message, result)

        assertEquals(1, messageDao.upserts.size)
        assertEquals("m1", messageDao.upserts.first().id)

        assertEquals(1, detectionDao.upserts.size)
        assertEquals("d1", detectionDao.upserts.first().id)
    }
}
