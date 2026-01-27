package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["recipientAccount"]),
        Index(value = ["createdAtEpochMs"]),
    ],
)
data class PaymentEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountMinor: Long,
    val currency: String,
    val recipientAccount: String,
    val channel: String,
    val createdAtEpochMs: Long,
)
