package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isActive"]),
        Index(value = ["validUntilEpochMs"]),
    ],
)
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val plan: String,
    val isActive: Boolean,
    val validUntilEpochMs: Long?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
