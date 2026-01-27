package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.CallEventDao
import com.zeroscam.coredata.db.dao.DetectionEventDao
import com.zeroscam.coredata.db.dao.DeviceSecurityEventDao
import com.zeroscam.coredata.db.dao.MessageEventDao
import com.zeroscam.coredata.db.dao.PaymentEventDao
import com.zeroscam.coredata.mapper.toEntity
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.DetectionLogRepository
import java.time.Clock
import java.time.Instant

class RoomDetectionLogRepository(
    private val messageEventDao: MessageEventDao,
    private val callEventDao: CallEventDao,
    private val paymentEventDao: PaymentEventDao,
    private val deviceSecurityEventDao: DeviceSecurityEventDao,
    private val detectionEventDao: DetectionEventDao,
    private val clock: Clock = Clock.systemUTC(),
) : DetectionLogRepository {
    override fun logCallDetection(
        call: PhoneCall,
        result: DetectionResult,
    ) {
        val now = Instant.now(clock)
        callEventDao.upsert(call.toEntity(now))
        detectionEventDao.upsert(result.toEntity())
    }

    override fun logMessageDetection(
        message: Message,
        result: DetectionResult,
    ) {
        val now = Instant.now(clock)
        messageEventDao.upsert(message.toEntity(now))
        detectionEventDao.upsert(result.toEntity())
    }

    override fun logPaymentDetection(
        paymentIntent: PaymentIntent,
        result: DetectionResult,
    ) {
        paymentEventDao.upsert(paymentIntent.toEntity())
        detectionEventDao.upsert(result.toEntity())
    }

    override fun logDeviceSecurityDetection(
        snapshot: DeviceSecuritySnapshot,
        result: DetectionResult,
    ) {
        val now = Instant.now(clock)
        deviceSecurityEventDao.upsert(snapshot.toEntity(now))
        detectionEventDao.upsert(result.toEntity())
    }
}
