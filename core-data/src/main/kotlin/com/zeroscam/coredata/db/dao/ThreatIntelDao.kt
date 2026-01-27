package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.ThreatIntelEntity

@Dao
interface ThreatIntelDao {
    @Query("SELECT COUNT(1) FROM threat_intel WHERE type = :type AND value = :value LIMIT 1")
    fun exists(
        type: String,
        value: String,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: ThreatIntelEntity)
}
