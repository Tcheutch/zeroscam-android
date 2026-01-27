package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.DetectionEventEntity

@Dao
interface DetectionEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: DetectionEventEntity)

    @Query("SELECT * FROM detection_events WHERE userId = :userId ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<DetectionEventEntity>
}
