package com.zeroscam.core_domain.value

/**
 * Email simplifié – on pourra durcir la validation plus tard si besoin.
 */
@JvmInline
value class EmailAddress(val raw: String) {
    init {
        require(raw.isNotBlank()) { "EmailAddress must not be blank" }
    }

    override fun toString(): String = raw
}
