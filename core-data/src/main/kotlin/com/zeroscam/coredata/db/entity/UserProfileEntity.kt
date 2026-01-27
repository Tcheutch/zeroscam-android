package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String?,
    val phoneNumber: String?,
    val countryIso: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
