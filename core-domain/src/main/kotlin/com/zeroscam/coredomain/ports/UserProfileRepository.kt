package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.UserProfile
import com.zeroscam.coredomain.value.UserId

/**
 * Accès aux profils utilisateurs.
 */
interface UserProfileRepository {
    fun findById(userId: UserId): UserProfile?

    fun save(userProfile: UserProfile)
}
