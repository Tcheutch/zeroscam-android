package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "research_outbox",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAtEpochMs"]),
        Index(value = ["status"]),
    ],
)
data class ResearchOutboxEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val payloadJson: String,
    val status: String,
    val createdAtEpochMs: Long,
)
