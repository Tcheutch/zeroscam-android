package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.DeviceThreatDetector
import com.zeroscam.coredomain.ports.ThreatIntelRepository

/**
 * Use case : évaluation de l’état de sécurité du device – V2 agressive.
 *
 * Pipeline :
 * 1. Analyse brute via [DeviceThreatDetector] (root/jailbreak, emulator, apps douteuses…).
 * 2. Ajustement via [ThreatIntelRepository.adjustDeviceSecurityResult].
 * 3. Escalades déterministes basées sur les flags du snapshot :
 *    - integrityCheckPassed == false  → CRITICAL quasi systématique
 *    - isRootedOrJailbroken == true   → au moins HIGH, voire CRITICAL
 *    - combinaison de hasSuspiciousApps / isEmulator / hasDebuggableBuild → bump supplémentaire
 * 4. Journalisation via [DetectionLogRepository.logDeviceSecurityDetection].
 */
class EvaluateDeviceSecurityStateUseCase(
    private val deviceThreatDetector: DeviceThreatDetector,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(snapshot: DeviceSecuritySnapshot): DetectionResult {
        // 1) Score brut device-security (moteur local / ML / règles)
        val baseResult = deviceThreatDetector.analyze(snapshot)

        // 2) Ajustement threat-intel (IOC device, profils compromis, etc.)
        val intelAdjusted =
            threatIntelRepository.adjustDeviceSecurityResult(
                snapshot = snapshot,
                initialResult = baseResult,
            )

        // 3) Escalades déterministes basées sur les flags du snapshot
        val escalated =
            escalateForDeviceFlags(
                snapshot = snapshot,
                currentResult = intelAdjusted,
            )

        // 4) Journalisation structurée
        detectionLogRepository.logDeviceSecurityDetection(
            snapshot = snapshot,
            result = escalated,
        )

        return escalated
    }

    /**
     * Politique d’escalade V2 :
     *
     *  - integrityCheckPassed == false :
     *      → CRITICAL, confidence ≥ 0.97
     *
     *  - isRootedOrJailbroken == true :
     *      → au moins HIGH
     *      → si combiné avec d’autres signaux, peut finir en CRITICAL
     *
     *  - hasSuspiciousApps / isEmulator / hasDebuggableBuild :
     *      → bumps successifs jusqu’à CRITICAL au maximum
     */
    private fun escalateForDeviceFlags(
        snapshot: DeviceSecuritySnapshot,
        currentResult: DetectionResult,
    ): DetectionResult {
        var risk = currentResult.riskLevel
        var confidence = currentResult.confidenceScore
        val extraReasons = mutableListOf<String>()

        // 1) Intégrité échouée → CRITICAL quasi systématique
        if (!snapshot.integrityCheckPassed) {
            risk = RiskLevel.CRITICAL
            confidence = maxOf(confidence, 0.97)
            extraReasons += REASON_DEVICE_INTEGRITY_CHECK_FAILED
        }

        // 2) Root / jailbreak → au moins HIGH
        if (snapshot.isRootedOrJailbroken) {
            if (risk < RiskLevel.HIGH) {
                risk = RiskLevel.HIGH
            }
            confidence = maxOf(confidence, 0.93)
            extraReasons += REASON_DEVICE_ROOT_OR_JAILBREAK_DETECTED
        }

        // 3) Signaux secondaires (apps douteuses, émulateur, build debuggable)
        var secondarySignalsCount = 0

        if (snapshot.hasSuspiciousApps) {
            secondarySignalsCount++
            extraReasons += REASON_DEVICE_SUSPICIOUS_APPS
        }

        if (snapshot.isEmulator) {
            secondarySignalsCount++
            extraReasons += REASON_DEVICE_EMULATOR_SUSPICIOUS
        }

        if (snapshot.hasDebuggableBuild) {
            secondarySignalsCount++
            extraReasons += REASON_DEVICE_DEBUGGABLE_BUILD
        }

        // Bump additionnel en fonction du nombre de signaux secondaires.
        if (secondarySignalsCount > 0 && risk < RiskLevel.CRITICAL) {
            repeat(secondarySignalsCount.coerceAtMost(2)) {
                risk = bumpRisk(risk)
            }
            confidence = maxOf(confidence, 0.90)
        }

        val hasAnyFlag =
            !snapshot.integrityCheckPassed ||
                snapshot.isRootedOrJailbroken ||
                snapshot.hasSuspiciousApps ||
                snapshot.isEmulator ||
                snapshot.hasDebuggableBuild

        if (!hasAnyFlag || extraReasons.isEmpty()) {
            return currentResult
        }

        return currentResult.copy(
            riskLevel = risk,
            confidenceScore = confidence,
            reasons = currentResult.reasons + extraReasons,
        )
    }

    private fun bumpRisk(riskLevel: RiskLevel): RiskLevel =
        when (riskLevel) {
            RiskLevel.LOW -> RiskLevel.MEDIUM
            RiskLevel.MEDIUM -> RiskLevel.HIGH
            RiskLevel.HIGH -> RiskLevel.CRITICAL
            RiskLevel.CRITICAL -> RiskLevel.CRITICAL
        }

    companion object {
        const val REASON_DEVICE_ROOT_OR_JAILBREAK_DETECTED =
            "device_root_or_jailbreak_detected"
        const val REASON_DEVICE_INTEGRITY_CHECK_FAILED =
            "device_integrity_check_failed"
        const val REASON_DEVICE_EMULATOR_SUSPICIOUS =
            "device_emulator_suspicious"
        const val REASON_DEVICE_SUSPICIOUS_APPS =
            "device_suspicious_apps_installed"
        const val REASON_DEVICE_DEBUGGABLE_BUILD =
            "device_debuggable_build_detected"
    }
}
