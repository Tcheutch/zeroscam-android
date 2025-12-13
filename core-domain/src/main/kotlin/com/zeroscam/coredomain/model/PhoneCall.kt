package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Représente un appel téléphonique observé par ZeroScam.
 */
data class PhoneCall(
    val id: String,
    val userId: UserId,
    val phoneNumber: String,
    val startedAt: Instant,
    val countryIso: String?,
    val isInContacts: Boolean,
    val isFromUnknownNumber: Boolean,
)
