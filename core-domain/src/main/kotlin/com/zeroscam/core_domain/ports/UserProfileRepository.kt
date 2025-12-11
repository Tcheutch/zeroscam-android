package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.UserProfile
import com.zeroscam.core_domain.value.UserId

/**
 * Accès aux profils utilisateurs.
 */
interface UserProfileRepository {
    fun findById(userId: UserId): UserProfile?

    fun save(userProfile: UserProfile)
}
