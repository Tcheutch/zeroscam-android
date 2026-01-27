package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.UserProfileDao
import com.zeroscam.coredata.mapper.toDomain
import com.zeroscam.coredata.mapper.toEntity
import com.zeroscam.coredomain.model.UserProfile
import com.zeroscam.coredomain.ports.UserProfileRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Clock
import java.time.Instant

class RoomUserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val clock: Clock = Clock.systemUTC(),
) : UserProfileRepository {
    override fun findById(userId: UserId): UserProfile? = userProfileDao.findById(userId.value)?.toDomain()

    override fun save(userProfile: UserProfile) {
        val now = Instant.now(clock)
        userProfileDao.upsert(userProfile.toEntity(now))
    }
}
