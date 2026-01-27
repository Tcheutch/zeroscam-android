package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_security_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["capturedAtEpochMs"]),
    ],
)
data class DeviceSecurityEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val capturedAtEpochMs: Long,
    val isRootedOrJailbroken: Boolean,
    val isEmulator: Boolean,
    val hasDebuggableBuild: Boolean,
    val hasSuspiciousApps: Boolean,
    val integrityCheckPassed: Boolean,
    val createdAtEpochMs: Long,
)
