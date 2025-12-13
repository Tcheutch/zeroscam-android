package com.zeroscam.coredomain.value

/**
 * VO pour les numéros – permet de centraliser la validation
 * et les normalisations plus tard (E.164, etc.).
 */
@JvmInline
value class PhoneNumber(val raw: String) {
    init {
        require(raw.isNotBlank()) { "PhoneNumber must not be blank" }
    }

    override fun toString(): String = raw
}
