package com.zeroscam.coredata.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_feedback",
    indices = [
        Index(value = ["detectionId"]),
        Index(value = ["createdAtEpochMs"]),
    ],
)
data class UserFeedbackEntity(
    @PrimaryKey val id: String,
    val detectionId: String,
    val isScam: Boolean,
    val comment: String?,
    val createdAtEpochMs: Long,
)
