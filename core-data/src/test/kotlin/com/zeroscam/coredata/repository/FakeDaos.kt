package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.CallEventDao
import com.zeroscam.coredata.db.dao.DetectionEventDao
import com.zeroscam.coredata.db.dao.DeviceSecurityEventDao
import com.zeroscam.coredata.db.dao.MessageEventDao
import com.zeroscam.coredata.db.dao.PaymentEventDao
import com.zeroscam.coredata.db.dao.ThreatIntelDao
import com.zeroscam.coredata.db.entity.CallEventEntity
import com.zeroscam.coredata.db.entity.DetectionEventEntity
import com.zeroscam.coredata.db.entity.DeviceSecurityEventEntity
import com.zeroscam.coredata.db.entity.MessageEventEntity
import com.zeroscam.coredata.db.entity.PaymentEventEntity
import com.zeroscam.coredata.db.entity.ThreatIntelEntity

internal class FakeMessageEventDao : MessageEventDao {
    val upserts = mutableListOf<MessageEventEntity>()

    override fun upsert(entity: MessageEventEntity) {
        upserts.add(entity)
    }

    override fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<MessageEventEntity> = emptyList()
}

internal class FakeCallEventDao : CallEventDao {
    val upserts = mutableListOf<CallEventEntity>()

    override fun upsert(entity: CallEventEntity) {
        upserts.add(entity)
    }

    override fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<CallEventEntity> = emptyList()
}

internal class FakePaymentEventDao : PaymentEventDao {
    val upserts = mutableListOf<PaymentEventEntity>()

    override fun upsert(entity: PaymentEventEntity) {
        upserts.add(entity)
    }

    override fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<PaymentEventEntity> = emptyList()
}

internal class FakeDeviceSecurityEventDao : DeviceSecurityEventDao {
    val upserts = mutableListOf<DeviceSecurityEventEntity>()

    override fun upsert(entity: DeviceSecurityEventEntity) {
        upserts.add(entity)
    }

    override fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<DeviceSecurityEventEntity> = emptyList()
}

internal class FakeDetectionEventDao : DetectionEventDao {
    val upserts = mutableListOf<DetectionEventEntity>()

    override fun upsert(entity: DetectionEventEntity) {
        upserts.add(entity)
    }

    override fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<DetectionEventEntity> = emptyList()
}

internal class FakeThreatIntelDao : ThreatIntelDao {
    private val store = mutableSetOf<Pair<String, String>>()
    val upserts = mutableListOf<ThreatIntelEntity>()

    override fun exists(
        type: String,
        value: String,
    ): Int = if (store.contains(type to value)) 1 else 0

    override fun upsert(entity: ThreatIntelEntity) {
        upserts.add(entity)
        store.add(entity.type to entity.value)
    }
}
