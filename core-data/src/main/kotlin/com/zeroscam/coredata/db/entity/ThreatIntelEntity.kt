package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "threat_intel",
    indices = [
        Index(value = ["type"]),
        Index(value = ["value"]),
        Index(value = ["updatedAtEpochMs"]),
    ],
)
data class ThreatIntelEntity(
    @PrimaryKey val id: String,
    val type: String,
    val value: String,
    val updatedAtEpochMs: Long,
)
