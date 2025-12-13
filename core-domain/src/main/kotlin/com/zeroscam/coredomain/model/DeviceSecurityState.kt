package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.value.DeviceId
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Snapshot de l’état de sécurité du device pour détecter
 * jailbreaking / rooting / hook / environnement non fiable.
 */
data class DeviceSecurityState(
    val deviceId: DeviceId,
    val userId: UserId,
    val capturedAt: Instant,
    val isRootedOrJailbroken: Boolean,
    val hasUsbDebuggingEnabled: Boolean,
    val allowsUnknownSources: Boolean,
    val hasTamperedSystemApps: Boolean,
    /**
     * Noms de paquets d’apps suspectes (hook, overlay, etc.).
     * Le détail viendra de core-ml / core-data.
     */
    val suspiciousPackages: List<String> = emptyList(),
)
