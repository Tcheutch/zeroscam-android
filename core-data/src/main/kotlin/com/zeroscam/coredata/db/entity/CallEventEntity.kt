package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["phoneNumber"]),
        Index(value = ["startedAtEpochMs"]),
    ],
)
data class CallEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val phoneNumber: String,
    val countryIso: String?,
    val isInContacts: Boolean,
    val isFromUnknownNumber: Boolean,
    val startedAtEpochMs: Long,
    val createdAtEpochMs: Long,
)
