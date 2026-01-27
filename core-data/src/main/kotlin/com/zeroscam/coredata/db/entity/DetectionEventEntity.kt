package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detection_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["riskLevel"]),
        Index(value = ["channel"]),
        Index(value = ["createdAtEpochMs"]),
    ],
)
data class DetectionEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val channel: String,
    val riskLevel: String,
    val confidenceScore: Double,
    val recommendation: String,
    val reasonsJson: String,
    val createdAtEpochMs: Long,
)
