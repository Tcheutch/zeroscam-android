package com.zeroscam.coredomain.value

/**
 * Montant stocké en plus petite unité (ex : centimes).
 */
@JvmInline
value class MoneyAmount(val value: Long) {
    fun toDouble(): Double = value / 100.0
}
