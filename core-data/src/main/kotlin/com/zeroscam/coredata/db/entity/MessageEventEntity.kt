package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["receivedAtEpochMs"]),
        Index(value = ["source"]),
    ],
)
data class MessageEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val content: String,
    val channel: String,
    val source: String?,
    val receivedAtEpochMs: Long,
    val createdAtEpochMs: Long,
)
