package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeroscam.coredata.db.entity.PaymentEventEntity

@Dao
interface PaymentEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: PaymentEventEntity)

    @Query("SELECT * FROM payment_events WHERE userId = :userId ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun findRecentByUser(
        userId: String,
        limit: Int,
    ): List<PaymentEventEntity>
}
