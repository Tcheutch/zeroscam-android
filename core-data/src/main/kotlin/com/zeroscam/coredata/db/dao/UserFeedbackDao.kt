package com.zeroscam.coredata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.zeroscam.coredata.db.entity.UserFeedbackEntity

@Dao
interface UserFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: UserFeedbackEntity)
}
