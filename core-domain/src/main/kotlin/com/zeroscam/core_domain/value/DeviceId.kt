package com.zeroscam.core_domain.value

/**
 * Identifiant logique du device (Android ID, installation ID, etc.).
 */
@JvmInline
value class DeviceId(val value: String) {
    init {
        require(value.isNotBlank()) { "DeviceId must not be blank" }
    }

    override fun toString(): String = value
}
