package com.zeroscam.coredata.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zeroscam.coredata.db.dao.CallEventDao
import com.zeroscam.coredata.db.dao.DetectionEventDao
import com.zeroscam.coredata.db.dao.DeviceSecurityEventDao
import com.zeroscam.coredata.db.dao.MessageEventDao
import com.zeroscam.coredata.db.dao.PaymentEventDao
import com.zeroscam.coredata.db.dao.ResearchOutboxDao
import com.zeroscam.coredata.db.dao.SubscriptionDao
import com.zeroscam.coredata.db.dao.ThreatIntelDao
import com.zeroscam.coredata.db.dao.UserFeedbackDao
import com.zeroscam.coredata.db.dao.UserProfileDao
import com.zeroscam.coredata.db.entity.CallEventEntity
import com.zeroscam.coredata.db.entity.DetectionEventEntity
import com.zeroscam.coredata.db.entity.DeviceSecurityEventEntity
import com.zeroscam.coredata.db.entity.MessageEventEntity
import com.zeroscam.coredata.db.entity.PaymentEventEntity
import com.zeroscam.coredata.db.entity.ResearchOutboxEntity
import com.zeroscam.coredata.db.entity.SubscriptionEntity
import com.zeroscam.coredata.db.entity.ThreatIntelEntity
import com.zeroscam.coredata.db.entity.UserFeedbackEntity
import com.zeroscam.coredata.db.entity.UserProfileEntity

@Database(
    entities = [
        MessageEventEntity::class,
        CallEventEntity::class,
        PaymentEventEntity::class,
        DeviceSecurityEventEntity::class,
        DetectionEventEntity::class,
        UserProfileEntity::class,
        SubscriptionEntity::class,
        UserFeedbackEntity::class,
        ThreatIntelEntity::class,
        ResearchOutboxEntity::class,
    ],
    version = DbConstants.DB_VERSION,
    exportSchema = true,
)
abstract class ZeroScamDatabase : RoomDatabase() {
    abstract fun messageEventDao(): MessageEventDao

    abstract fun callEventDao(): CallEventDao

    abstract fun paymentEventDao(): PaymentEventDao

    abstract fun deviceSecurityEventDao(): DeviceSecurityEventDao

    abstract fun detectionEventDao(): DetectionEventDao

    abstract fun userProfileDao(): UserProfileDao

    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun userFeedbackDao(): UserFeedbackDao

    abstract fun threatIntelDao(): ThreatIntelDao

    abstract fun researchOutboxDao(): ResearchOutboxDao
}
