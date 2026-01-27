package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.DeviceSecurityEventEntity

@Dao
interface DeviceSecurityEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: DeviceSecurityEventEntity)

    @Query("SELECT * FROM device_security_events WHERE userId = :userId ORDER BY capturedAtEpochMs DESC LIMIT :limit")
    fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<DeviceSecurityEventEntity>
}
