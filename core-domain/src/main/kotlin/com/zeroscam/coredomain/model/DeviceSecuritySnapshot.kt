package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Instantané de l'état de sécurité du device au moment de l'analyse.
 * Alimente le moteur de détection (root/jailbreak, hash jacking, etc.).
 */
data class DeviceSecuritySnapshot(
    val id: String,
    val userId: UserId,
    val capturedAt: Instant,
    val isRootedOrJailbroken: Boolean,
    val isEmulator: Boolean,
    val hasDebuggableBuild: Boolean,
    val hasSuspiciousApps: Boolean,
    val integrityCheckPassed: Boolean,
)
