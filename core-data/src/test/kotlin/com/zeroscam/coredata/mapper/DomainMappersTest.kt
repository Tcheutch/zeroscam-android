package com.zeroscam.coredata.mapper

import com.zeroscam.coredata.db.entity.PaymentEventEntity
import com.zeroscam.coredomain.value.MoneyAmount
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainMappersTest {
    @Test
    fun `PaymentEventEntity to domain - maps amount minor`() {
        val entity =
            PaymentEventEntity(
                id = "p1",
                userId = "u1",
                amountMinor = 1234L,
                currency = "XAF",
                recipientAccount = "momo:2376",
                channel = "momo",
                createdAtEpochMs = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli(),
            )

        val domain = entity.toDomain()
        assertEquals("p1", domain.id)
        assertEquals(UserId("u1"), domain.userId)
        assertEquals(MoneyAmount(1234L), domain.amount)
        assertEquals("XAF", domain.currency)
    }
}
