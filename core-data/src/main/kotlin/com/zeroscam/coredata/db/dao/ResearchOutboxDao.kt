package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.zeroscam.coredata.db.entity.ResearchOutboxEntity

@Dao
interface ResearchOutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun enqueue(entity: ResearchOutboxEntity)
}
