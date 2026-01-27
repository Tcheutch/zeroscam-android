package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.UserFeedbackDao
import com.zeroscam.coredata.mapper.toEntity
import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.ports.UserFeedbackRepository
import java.time.Clock
import java.time.Instant

class RoomUserFeedbackRepository(
    private val userFeedbackDao: UserFeedbackDao,
    private val clock: Clock = Clock.systemUTC(),
) : UserFeedbackRepository {
    override fun saveFeedback(feedback: DetectionFeedback) {
        val now = Instant.now(clock)
        userFeedbackDao.upsert(feedback.toEntity(now))
    }
}
