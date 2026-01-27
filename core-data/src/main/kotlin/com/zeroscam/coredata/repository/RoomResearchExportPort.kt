package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.JsonCodec
import com.zeroscam.coredata.db.dao.ResearchOutboxDao
import com.zeroscam.coredata.db.entity.ResearchOutboxEntity
import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.UserRiskSnapshot
import java.time.Clock
import java.time.Instant
import java.util.UUID
import org.json.JSONObject

class RoomResearchExportPort(
    private val researchOutboxDao: ResearchOutboxDao,
    private val clock: Clock = Clock.systemUTC(),
) : ResearchExportPort {
    override fun publishUserRiskSnapshot(snapshot: UserRiskSnapshot) {
        val now = Instant.now(clock)

        val payload =
            JSONObject()
                .put("userId", snapshot.userId.value)
                .put("globalRiskLevel", snapshot.globalRiskLevel.name)
                .put("globalConfidence", snapshot.globalConfidence)
                .put(
                    "detections",
                    JsonCodec.toJsonArrayString(snapshot.detections.map { it.id }),
                )
                .toString()

        researchOutboxDao.enqueue(
            ResearchOutboxEntity(
                id = "outbox-${UUID.randomUUID()}",
                userId = snapshot.userId.value,
                payloadJson = payload,
                status = STATUS_PENDING,
                createdAtEpochMs = now.toEpochMilli(),
            ),
        )
    }

    private companion object {
        private const val STATUS_PENDING: String = "PENDING"
    }
}
