package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.SubscriptionEntity

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isActive = 1 ORDER BY updatedAtEpochMs DESC LIMIT 1")
    fun findActiveByUser(userId: String): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: SubscriptionEntity)
}
