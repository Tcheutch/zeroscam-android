package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.CallEventEntity

@Dao
interface CallEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: CallEventEntity)

    @Query("SELECT * FROM call_events WHERE userId = :userId ORDER BY startedAtEpochMs DESC LIMIT :limit")
    fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<CallEventEntity>
}
