package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.MessageEventEntity

@Dao
interface MessageEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: MessageEventEntity)

    @Query("SELECT * FROM message_events WHERE userId = :userId ORDER BY receivedAtEpochMs DESC LIMIT :limit")
    fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<MessageEventEntity>
}
