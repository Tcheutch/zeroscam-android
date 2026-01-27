package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.entity.ThreatIntelEntity
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomThreatIntelRepositoryTest {
    @Test
    fun `isKnownScamPhone - returns true when present`() {
        val dao = FakeThreatIntelDao()
        val repo = RoomThreatIntelRepository(dao)

        assertFalse(repo.isKnownScamPhone("+237600000000"))

        dao.upsert(
            ThreatIntelEntity(
                id = "ti1",
                type = "PHONE",
                value = "+237600000000",
                updatedAtEpochMs = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli(),
            ),
        )

        assertTrue(repo.isKnownScamPhone("+237600000000"))
    }
}
