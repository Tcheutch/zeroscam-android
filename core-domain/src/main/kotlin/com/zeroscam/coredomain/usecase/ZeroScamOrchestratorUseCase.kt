package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.UserRiskSnapshot

/**
 * Requête d'orchestration ZeroScam : regroupe les signaux multi-canaux
 * possibles pour un même utilisateur.
 */
data class ZeroScamOrchestrationRequest(
    val message: Message? = null,
    val call: PhoneCall? = null,
    val paymentIntent: PaymentIntent? = null,
    val deviceSnapshot: DeviceSecuritySnapshot? = null,
)

/**
 * Résultat d'orchestration ZeroScam.
 */
data class ZeroScamOrchestrationResult(
    val aggregatedUserRisk: AggregatedUserRisk?,
    val messageDetection: DetectionResult?,
    val callDetection: DetectionResult?,
    val paymentDetection: DetectionResult?,
    val deviceSecurityDetection: DetectionResult?,
)

/**
 * Use case d'orchestration ZeroScam :
 *
 *  1. Exécute les use cases unitaires (message, call, payment, device).
 *  2. Agrège le risque utilisateur via [AggregateUserRiskUseCase].
 *  3. Exporte un snapshot de risque vers Vigilis-Research via [ResearchExportPort].
 */
class ZeroScamOrchestratorUseCase(
    private val analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase,
    private val analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase,
    private val evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase,
    private val evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase,
    private val aggregateUserRiskUseCase: AggregateUserRiskUseCase,
    private val researchExportPort: ResearchExportPort,
) {
    operator fun invoke(request: ZeroScamOrchestrationRequest): ZeroScamOrchestrationResult {
        var messageDetection: DetectionResult? = null
        var callDetection: DetectionResult? = null
        var paymentDetection: DetectionResult? = null
        var deviceDetection: DetectionResult? = null

        val allDetections = mutableListOf<DetectionResult>()

        request.message?.let { message ->
            val result = analyzeIncomingMessageUseCase(message)
            messageDetection = result
            allDetections += result
        }

        request.call?.let { call ->
            val result = analyzeIncomingCallUseCase(call)
            callDetection = result
            allDetections += result
        }

        request.paymentIntent?.let { payment ->
            val result = evaluatePaymentIntentUseCase(payment)
            paymentDetection = result
            allDetections += result
        }

        request.deviceSnapshot?.let { snapshot ->
            val result = evaluateDeviceSecurityStateUseCase(snapshot)
            deviceDetection = result
            allDetections += result
        }

        val aggregated =
            if (allDetections.isNotEmpty()) {
                aggregateUserRiskUseCase(allDetections)
            } else {
                null
            }

        if (aggregated != null) {
            val snapshot =
                UserRiskSnapshot(
                    userId = aggregated.userId,
                    globalRiskLevel = aggregated.riskLevel,
                    globalConfidence = aggregated.confidenceScore,
                    detections = allDetections,
                )
            researchExportPort.publishUserRiskSnapshot(snapshot)
        }

        return ZeroScamOrchestrationResult(
            aggregatedUserRisk = aggregated,
            messageDetection = messageDetection,
            callDetection = callDetection,
            paymentDetection = paymentDetection,
            deviceSecurityDetection = deviceDetection,
        )
    }
}
